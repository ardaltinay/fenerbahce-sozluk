package net.fenerbahcesozluk.service;

import lombok.RequiredArgsConstructor;
import net.fenerbahcesozluk.dto.RestPage;
import net.fenerbahcesozluk.dto.TopicRequest;
import net.fenerbahcesozluk.dto.TopicResponse;
import net.fenerbahcesozluk.entity.Topic;
import net.fenerbahcesozluk.entity.User;
import net.fenerbahcesozluk.exception.BusinessException;
import net.fenerbahcesozluk.repository.EntryRepository;
import net.fenerbahcesozluk.repository.TopicRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;
    private final EntryRepository entryRepository;
    private final StatsService statsService;
    private final WebSocketService webSocketService;

    public Page<TopicResponse> getAllTopics(Pageable pageable) {
        return topicRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable).map(this::toResponse);
    }

    public Page<TopicResponse> getPopularTopics(Pageable pageable) {
        return getPopularTopicsCached(pageable.getPageNumber(), pageable.getPageSize());
    }

    @Cacheable(value = "popularTopics_v2", key = "#pageNumber")
    public Page<TopicResponse> getPopularTopicsCached(int pageNumber, int pageSize) {
        Page<TopicResponse> page = topicRepository.findPopularTopics(PageRequest.of(pageNumber, pageSize))
                .map(this::toResponse);
        return new RestPage<>(new ArrayList<>(page.getContent()), page.getPageable(), page.getTotalElements());
    }

    public Page<TopicResponse> getTrendingTopics(Pageable pageable) {
        return getTrendingTopicsCached(pageable.getPageNumber(), pageable.getPageSize());
    }

    @Cacheable(value = "trendingTopics_v2", key = "#pageNumber")
    public Page<TopicResponse> getTrendingTopicsCached(int pageNumber, int pageSize) {
        LocalDateTime thirtyDaysAgo = LocalDate.now().minusDays(30).atStartOfDay();
        Page<TopicResponse> page = topicRepository.findTrends(thirtyDaysAgo, PageRequest.of(pageNumber, pageSize))
                .map(this::toResponse);
        return new RestPage<>(new ArrayList<>(page.getContent()), page.getPageable(), page.getTotalElements());
    }

    /**
     * Get topics by date period: today, yesterday, or older
     */
    public Page<TopicResponse> getTopicsByDatePeriod(String period, Pageable pageable) {
        LocalDate today = LocalDate.now();

        return switch (period.toLowerCase()) {
            case "today" -> {
                LocalDateTime todayStart = today.atStartOfDay();
                LocalDateTime todayEnd = today.plusDays(1).atStartOfDay();
                yield topicRepository.findTopicsWithEntriesInDateRange(todayStart, todayEnd, pageable)
                        .map(topic -> toResponseWithDateCounts(topic, todayStart, todayEnd));
            }
            case "yesterday" -> {
                LocalDateTime yesterdayStart = today.minusDays(1).atStartOfDay();
                LocalDateTime yesterdayEnd = today.atStartOfDay();
                yield topicRepository.findTopicsWithEntriesInDateRange(yesterdayStart, yesterdayEnd, pageable)
                        .map(topic -> toResponseWithDateCounts(topic, yesterdayStart, yesterdayEnd));
            }
            case "older" -> {
                LocalDateTime yesterdayStart = today.minusDays(1).atStartOfDay();
                yield topicRepository.findTopicsWithEntriesBefore(yesterdayStart, pageable)
                        .map(topic -> toResponseWithOlderCounts(topic, yesterdayStart));
            }
            default -> topicRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable).map(this::toResponse);
        };
    }

    public Page<TopicResponse> searchTopics(String keyword, Pageable pageable) {
        return topicRepository.searchByTitle(keyword, pageable).map(this::toResponse);
    }

    public TopicResponse getTopicById(UUID id) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Başlık bulunamadı", HttpStatus.NOT_FOUND));

        // Check if topic is active (not deleted)
        if (!topic.isActive()) {
            throw new BusinessException("Başlık bulunamadı", HttpStatus.NOT_FOUND);
        }

        return toResponseWithAllDateCounts(topic);
    }

    @Transactional
    public TopicResponse createTopic(TopicRequest request, User author) {
        if (request.getTitle() != null && request.getTitle().length() > 50) {
            throw new BusinessException("Başlık 50 karakterden uzun olamaz", HttpStatus.BAD_REQUEST);
        }

        topicRepository.findByTitleIgnoreCaseAndIsActiveTrue(request.getTitle()).ifPresent(t -> {
            throw new BusinessException("duplicate_topic:" + t.getId(), HttpStatus.CONFLICT);
        });

        Topic topic = Topic.builder().title(request.getTitle()).author(author).build();

        Topic saved = topicRepository.save(topic);
        statsService.evictStatsCache();
        return toResponse(saved);
    }

    @Transactional
    public void incrementViewCount(UUID topicId) {
        topicRepository.incrementViewCount(topicId);
    }

    @Transactional
    public void deleteTopic(UUID topicId, String reason, User currentUser) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new BusinessException("Başlık bulunamadı", HttpStatus.NOT_FOUND));

        boolean isOwner = topic.getAuthor().getId().equals(currentUser.getId());
        boolean isModerator = currentUser.getRole().name().equals("MODERATOR");
        boolean isAdmin = currentUser.getRole().name().equals("ADMIN");

        if (!isOwner && !isModerator && !isAdmin) {
            throw new BusinessException("Bu işlem için yetkiniz yok", HttpStatus.FORBIDDEN);
        }

        // Soft delete
        topic.setActive(false);
        topic.setDeleteReason(reason);
        topicRepository.save(topic);
        statsService.evictStatsCache();
    }

    private TopicResponse toResponse(Topic topic) {
        return TopicResponse.builder()
                .id(topic.getId())
                .title(topic.getTitle())
                .authorId(topic.getAuthor().getId())
                .authorUsername(topic.getAuthor().getUsername())
                .entryCount(topic.getEntryCount())
                .viewCount(topic.getViewCount())
                .isLocked(topic.isLocked())
                .isPinned(topic.isPinned())

                .kunyeImageUrl(topic.getKunyeImageUrl())
                .kunyeData(topic.getKunyeData())
                .createdAt(topic.getCreatedAt())
                .updatedAt(topic.getUpdatedAt())
                .lastActivityAt(topic.getUpdatedAt())
                .build();
    }

    private TopicResponse toResponseWithDateCounts(Topic topic, LocalDateTime periodStart, LocalDateTime periodEnd) {
        Integer periodCount = entryRepository.countByTopicIdAndDateRange(topic.getId(), periodStart, periodEnd);

        return TopicResponse.builder()
                .id(topic.getId())
                .title(topic.getTitle())
                .authorId(topic.getAuthor().getId())
                .authorUsername(topic.getAuthor().getUsername())
                .entryCount(topic.getEntryCount())
                .todayEntryCount(periodCount) // For sidebar, we show the period count in todayEntryCount
                .viewCount(topic.getViewCount())
                .isLocked(topic.isLocked())
                .isPinned(topic.isPinned())

                .kunyeImageUrl(topic.getKunyeImageUrl())
                .kunyeData(topic.getKunyeData())
                .createdAt(topic.getCreatedAt())
                .updatedAt(topic.getUpdatedAt())
                .lastActivityAt(topic.getUpdatedAt())
                .build();
    }

    private TopicResponse toResponseWithOlderCounts(Topic topic, LocalDateTime before) {
        Integer olderCount = entryRepository.countByTopicIdBefore(topic.getId(), before);

        return TopicResponse.builder()
                .id(topic.getId())
                .title(topic.getTitle())
                .authorId(topic.getAuthor().getId())
                .authorUsername(topic.getAuthor().getUsername())
                .entryCount(topic.getEntryCount())
                .olderEntryCount(olderCount)
                .viewCount(topic.getViewCount())
                .isLocked(topic.isLocked())
                .isPinned(topic.isPinned())

                .kunyeImageUrl(topic.getKunyeImageUrl())
                .kunyeData(topic.getKunyeData())
                .createdAt(topic.getCreatedAt())
                .updatedAt(topic.getUpdatedAt())
                .lastActivityAt(topic.getUpdatedAt())
                .build();
    }

    private TopicResponse toResponseWithAllDateCounts(Topic topic) {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.plusDays(1).atStartOfDay();
        LocalDateTime yesterdayStart = today.minusDays(1).atStartOfDay();

        Integer todayCount = entryRepository.countByTopicIdAndDateRange(topic.getId(), todayStart, todayEnd);
        Integer yesterdayCount = entryRepository.countByTopicIdAndDateRange(topic.getId(), yesterdayStart, todayStart);
        Integer olderCount = entryRepository.countByTopicIdBefore(topic.getId(), yesterdayStart);

        return TopicResponse.builder()
                .id(topic.getId())
                .title(topic.getTitle())
                .authorId(topic.getAuthor().getId())
                .authorUsername(topic.getAuthor().getUsername())
                .entryCount(topic.getEntryCount())
                .todayEntryCount(todayCount)
                .yesterdayEntryCount(yesterdayCount)
                .olderEntryCount(olderCount)
                .viewCount(topic.getViewCount())
                .isLocked(topic.isLocked())
                .isPinned(topic.isPinned())

                .kunyeImageUrl(topic.getKunyeImageUrl())
                .kunyeData(topic.getKunyeData())
                .createdAt(topic.getCreatedAt())
                .updatedAt(topic.getUpdatedAt())
                .lastActivityAt(topic.getUpdatedAt())
                .build();
    }

    /**
     * Merge source topic into target topic.
     * Moves all entries from source to target and deletes source topic.
     */
    @Transactional
    public void mergeTopic(UUID sourceId, UUID targetId, User currentUser) {
        if (sourceId.equals(targetId)) {
            throw new BusinessException("Bir başlık kendisiyle birleştirilemez", HttpStatus.BAD_REQUEST);
        }

        Topic sourceTopic = topicRepository.findById(sourceId)
                .orElseThrow(() -> new BusinessException("Kaynak başlık bulunamadı", HttpStatus.NOT_FOUND));

        Topic targetTopic = topicRepository.findById(targetId)
                .orElseThrow(() -> new BusinessException("Hedef başlık bulunamadı", HttpStatus.NOT_FOUND));

        // Yetki kontrolü
        boolean isModerator = currentUser.getRole().name().equals("MODERATOR");
        boolean isAdmin = currentUser.getRole().name().equals("ADMIN");

        if (!isModerator && !isAdmin) {
            throw new BusinessException("Bu işlem için yetkiniz yok", HttpStatus.FORBIDDEN);
        }

        // Entry'leri taşı
        int movedEntries = entryRepository.moveEntriesToTopic(sourceId, targetId);

        // Hedef başlığın entry sayısını güncelle
        targetTopic.setEntryCount(targetTopic.getEntryCount() + movedEntries);
        topicRepository.save(targetTopic);

        // Kaynak başlığı soft delete
        sourceTopic.setActive(false);
        sourceTopic.setDeleteReason("Birleştirildi: " + targetTopic.getTitle());
        topicRepository.save(sourceTopic);

        // Cache'leri temizle
        evictTopicCaches();
        statsService.evictStatsCache();

        // WebSocket ile sidebar güncellemesi
        webSocketService.broadcastSidebarUpdate();
    }

    @Caching(evict = { @CacheEvict(value = "trendingTopics_v2", allEntries = true),
            @CacheEvict(value = "popularTopics_v2", allEntries = true) })
    public void evictTopicCaches() {
        // Evict topic list caches
    }

}
