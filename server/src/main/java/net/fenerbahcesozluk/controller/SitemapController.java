package net.fenerbahcesozluk.controller;

import lombok.RequiredArgsConstructor;
import net.fenerbahcesozluk.entity.Topic;
import net.fenerbahcesozluk.repository.TopicRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * SEO için sitemap.xml endpoint'i Google ve diğer arama motorları için dinamik
 * sitemap oluşturur
 */
@RestController
@RequiredArgsConstructor
public class SitemapController {

    private final TopicRepository topicRepository;
    private static final String BASE_URL = "https://fenerbahcesozluk.net";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String generateSitemap() {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        // Ana sayfa
        xml.append(createUrl(BASE_URL, LocalDateTime.now(), "daily", "1.0"));

        // Statik sayfalar
        xml.append(createUrl(BASE_URL + "/iletisim", null, "monthly", "0.5"));
        xml.append(createUrl(BASE_URL + "/istatistikler", null, "daily", "0.6"));
        xml.append(createUrl(BASE_URL + "/haberler", null, "hourly", "0.8"));

        // Tüm başlıklar (topics)
        List<Topic> topics = topicRepository.findAll();
        for (Topic topic : topics) {
            String topicUrl = BASE_URL + "/baslik/" + topic.getId() + "/" + encodeUrl(topic.getTitle());
            LocalDateTime lastModDate = topic.getUpdatedAt() != null ? topic.getUpdatedAt() : topic.getCreatedAt();
            xml.append(createUrl(topicUrl, lastModDate, "daily", "0.8"));
        }

        xml.append("</urlset>");
        return xml.toString();
    }

    private String createUrl(String loc, LocalDateTime lastmod, String changefreq, String priority) {
        StringBuilder url = new StringBuilder();
        url.append("  <url>\n");
        url.append("    <loc>").append(escapeXml(loc)).append("</loc>\n");
        if (lastmod != null) {
            url.append("    <lastmod>").append(lastmod.format(DATE_FORMAT)).append("</lastmod>\n");
        }
        url.append("    <changefreq>").append(changefreq).append("</changefreq>\n");
        url.append("    <priority>").append(priority).append("</priority>\n");
        url.append("  </url>\n");
        return url.toString();
    }

    private String encodeUrl(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
        } catch (Exception e) {
            return value;
        }
    }

    private String escapeXml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
