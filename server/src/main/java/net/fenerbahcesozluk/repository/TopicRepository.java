package net.fenerbahcesozluk.repository;

import net.fenerbahcesozluk.entity.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TopicRepository extends JpaRepository<Topic, UUID> {

    Optional<Topic> findByTitleIgnoreCaseAndIsActiveTrue(String title);

    Page<Topic> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT t FROM Topic t WHERE t.isActive = true AND t.entryCount > 0 ORDER BY t.entryCount DESC")
    Page<Topic> findPopularTopics(Pageable pageable);

    @Query("SELECT t FROM Topic t WHERE t.isActive = true AND LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Topic> searchByTitle(@Param("keyword") String keyword, Pageable pageable);

    @Modifying
    @Query("UPDATE Topic t SET t.viewCount = t.viewCount + 1 WHERE t.id = :topicId")
    void incrementViewCount(@Param("topicId") UUID topicId);

    @Modifying
    @Query("UPDATE Topic t SET t.entryCount = t.entryCount + 1 WHERE t.id = :topicId")
    void incrementEntryCount(@Param("topicId") UUID topicId);

    @Query(value = "SELECT t FROM Topic t JOIN Entry e ON t.id = e.topic.id WHERE e.createdAt >= :startDate AND e.isActive = true AND t.isActive = true GROUP BY t.id ORDER BY COUNT(e) DESC, t.title ASC", countQuery = "SELECT count(DISTINCT t.id) FROM Topic t JOIN Entry e ON t.id = e.topic.id WHERE e.createdAt >= :startDate AND e.isActive = true AND t.isActive = true")
    Page<Topic> findTrends(@Param("startDate") java.time.LocalDateTime startDate, Pageable pageable);

    // Topics with entries in a specific date range, ordered by entry count in that
    // range
    @Query(value = "SELECT t FROM Topic t WHERE t.isActive = true AND EXISTS (SELECT 1 FROM Entry e WHERE e.topic.id = t.id AND e.isActive = true AND e.createdAt >= :start AND e.createdAt < :end) ORDER BY (SELECT COUNT(e) FROM Entry e WHERE e.topic.id = t.id AND e.isActive = true AND e.createdAt >= :start AND e.createdAt < :end) DESC, t.title ASC", countQuery = "SELECT COUNT(DISTINCT t.id) FROM Topic t WHERE t.isActive = true AND EXISTS (SELECT 1 FROM Entry e WHERE e.topic.id = t.id AND e.isActive = true AND e.createdAt >= :start AND e.createdAt < :end)")
    Page<Topic> findTopicsWithEntriesInDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
            Pageable pageable);

    // Topics with entries before a certain date (for "older" section)
    @Query(value = "SELECT t FROM Topic t WHERE t.isActive = true AND EXISTS (SELECT 1 FROM Entry e WHERE e.topic.id = t.id AND e.isActive = true AND e.createdAt < :before) ORDER BY (SELECT COUNT(e) FROM Entry e WHERE e.topic.id = t.id AND e.isActive = true AND e.createdAt < :before) DESC, t.title ASC", countQuery = "SELECT COUNT(DISTINCT t.id) FROM Topic t WHERE t.isActive = true AND EXISTS (SELECT 1 FROM Entry e WHERE e.topic.id = t.id AND e.isActive = true AND e.createdAt < :before)")
    Page<Topic> findTopicsWithEntriesBefore(@Param("before") LocalDateTime before, Pageable pageable);

    @Modifying
    @Query("UPDATE Topic t SET t.entryCount = t.entryCount - 1 WHERE t.id = :topicId AND t.entryCount > 0")
    void decrementEntryCount(@Param("topicId") UUID topicId);

    @Query("SELECT t.id, t.title, t.entryCount FROM Topic t WHERE t.isActive = true AND t.entryCount > 0 ORDER BY t.entryCount DESC")
    List<Object[]> findTopTopics(int limit);
}
