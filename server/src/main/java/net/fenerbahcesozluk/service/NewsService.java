package net.fenerbahcesozluk.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.fenerbahcesozluk.dto.RestPage;
import net.fenerbahcesozluk.entity.News;

import net.fenerbahcesozluk.repository.NewsRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsService {

    private final NewsRepository newsRepository;

    private static final List<String> RSS_FEEDS = List.of("https://www.ntvspor.net/rss/kategori/futbol",
            "https://kontraspor.com/rss", "https://ajansspor.com/rss", "https://www.fotomac.com.tr/rss/Fenerbahce.xml");

    private static final List<String> KEYWORDS = List.of("Fenerbahçe", "Fenerbahce", "Sarı Kanarya", "Sarı Lacivert",
            "FB", "Kadıköy");

    @Cacheable(value = "news_v2", key = "'page_' + #pageable.pageNumber + '_size_' + #pageable.pageSize")
    public Page<News> getNews(Pageable pageable) {
        Page<News> page = newsRepository.findAllByOrderByPubDateDesc(pageable);
        return new RestPage<>(new ArrayList<>(page.getContent()), pageable,
                page.getTotalElements());
    }

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(fixedRate = 3600000) // Every 1 hour
    @Transactional
    @CacheEvict(value = "news_v2", allEntries = true)
    public void fetchNewsTask() {
        log.info("Starting scheduled news fetch task...");
        for (String feedUrl : RSS_FEEDS) {
            try {
                fetchAndProcessFeed(feedUrl);
            } catch (Exception e) {
                log.error("Error fetching feed: {}", feedUrl, e);
            }
        }
        log.info("News fetch task completed. Cache evicted.");

        // Cleanup old news (older than 30 days)
        cleanupOldNews();
    }

    private void cleanupOldNews() {
        try {
            LocalDateTime threshold = LocalDateTime.now().minusDays(10);
            newsRepository.deleteByPubDateBefore(threshold);
        } catch (Exception e) {
            log.error("Error cleaning up old news", e);
        }
    }

    private void fetchAndProcessFeed(String url) throws Exception {
        String sourceName = getSourceName(url);
        log.debug("Fetching from source: {}", sourceName);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new URL(url).openStream());
        doc.getDocumentElement().normalize();

        NodeList items = doc.getElementsByTagName("item");

        // Batch load existing data for duplicate checking (2 queries instead of N*2)
        Set<String> existingGuids = newsRepository.findAllGuids();
        Set<String> recentTitles = newsRepository.findRecentTitlesLowerCase(LocalDateTime.now().minusHours(6));

        List<News> newsToSave = new ArrayList<>();

        for (int i = 0; i < items.getLength(); i++) {
            Node node = items.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                News news = processItem(element, sourceName, existingGuids, recentTitles);
                if (news != null) {
                    newsToSave.add(news);
                    // Add to sets to prevent duplicates within same feed
                    existingGuids.add(news.getGuid());
                    recentTitles.add(news.getTitle().toLowerCase(Locale.forLanguageTag("tr")));
                }
            }
        }

        // Batch save all news at once
        if (!newsToSave.isEmpty()) {
            newsRepository.saveAll(newsToSave);
            log.info("Saved {} news items from {}", newsToSave.size(), sourceName);
        }
    }

    private News processItem(Element element, String sourceName, Set<String> existingGuids, Set<String> recentTitles) {
        try {
            String title = getElementValue(element, "title");
            String link = getElementValue(element, "link");
            String description = getElementValue(element, "description");
            String pubDateStr = getElementValue(element, "pubDate");
            String guid = getElementValue(element, "guid");

            if (guid == null || guid.isEmpty()) {
                guid = link; // Fallback to link as guid
            }

            // 1. Check if relevant to Fenerbahçe (unless source is specific like
            // fanatik/fenerbahce)
            if (!isRelevant(title, description, sourceName)) {
                return null;
            }

            // 2. Check duplicates by GUID (memory check instead of DB)
            if (existingGuids.contains(guid)) {
                return null;
            }

            // 3. Check if same title exists from another source (memory check instead of
            // DB)
            String titleLower = title.toLowerCase(Locale.forLanguageTag("tr"));
            if (recentTitles.contains(titleLower)) {
                log.debug("Duplicate news title found, skipping: {}", title);
                return null;
            }

            // 4. Extract Image
            String imageUrl = extractImage(element, description);

            // 5. Parse Date
            LocalDateTime pubDate;
            try {
                if (pubDateStr != null && !pubDateStr.isEmpty()) {
                    pubDate = LocalDateTime.parse(pubDateStr, DateTimeFormatter.RFC_1123_DATE_TIME);
                } else {
                    pubDate = LocalDateTime.now();
                }
            } catch (Exception e) {
                log.warn("Date parsing failed for: {} - error: {}", pubDateStr, e.getMessage());
                pubDate = LocalDateTime.now();
            }

            return News.builder()
                    .title(title)
                    .link(link)
                    .description(cleanHtml(description))
                    .imageUrl(imageUrl)
                    .source(sourceName)
                    .pubDate(pubDate)
                    .guid(guid)
                    .build();

        } catch (Exception e) {
            log.error("Error processing item", e);
            return null;
        }
    }

    private boolean isRelevant(String title, String description, String sourceName) {
        // specific feeds are always relevant
        if (sourceName.contains("Fotomaç"))
            return true;

        String content = (title + " " + description).toLowerCase(Locale.forLanguageTag("tr"));
        for (String keyword : KEYWORDS) {
            if (content.contains(keyword.toLowerCase(Locale.forLanguageTag("tr")))) {
                return true;
            }
        }
        return false;
    }

    private String getSourceName(String url) {
        if (url.contains("kontraspor"))
            return "Kontra Spor";
        if (url.contains("ntvspor"))
            return "NTV Spor";
        if (url.contains("fotomac"))
            return "Fotomaç";
        if (url.contains("ajansspor"))
            return "Ajans Spor";
        if (url.contains("aspor"))
            return "A Spor";
        if (url.contains("fanatik"))
            return "Fanatik";
        return "Haber Kaynağı";
    }

    private String getElementValue(Element parent, String tagName) {
        NodeList list = parent.getElementsByTagName(tagName);
        if (list != null && list.getLength() > 0) {
            return list.item(0).getTextContent();
        }
        return "";
    }

    private String extractImage(Element element, String description) {
        // 1. Try 'enclosure' tag
        NodeList enclosures = element.getElementsByTagName("enclosure");
        if (enclosures != null && enclosures.getLength() > 0) {
            Element enclosure = (Element) enclosures.item(0);
            if (enclosure.hasAttribute("type") && enclosure.getAttribute("type").startsWith("image")) {
                return enclosure.getAttribute("url");
            }
        }

        // 2. Try 'media:content' or 'media:thumbnail' (requires namespace awareness
        // usually, but getElementsByTagName might work lightly)
        // Browsers handles this well, but standard DOM might need namespace.
        // Simplified check:
        try {
            NodeList media = element.getElementsByTagName("media:content"); // Might fail without NS
            if (media.getLength() > 0)
                return ((Element) media.item(0)).getAttribute("url");
        } catch (Exception e) {
        }

        // 3. Try regex on description for <img src="...">
        if (description != null && description.contains("<img")) {
            int srcStart = description.indexOf("src=\"");
            if (srcStart != -1) {
                int urlStart = srcStart + 5;
                int urlEnd = description.indexOf("\"", urlStart);
                if (urlEnd != -1) {
                    return description.substring(urlStart, urlEnd);
                }
            }
        }

        // Default placeholder or null
        return null;
    }

    private String cleanHtml(String html) {
        if (html == null)
            return "";
        // Remove HTML tags for clean info
        return html.replaceAll("\\<.*?\\>", "").trim();
    }
}
