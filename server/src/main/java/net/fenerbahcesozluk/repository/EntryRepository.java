package net.fenerbahcesozluk.repository;

import net.fenerbahcesozluk.entity.Entry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EntryRepository extends JpaRepository<Entry, UUID> {

        Page<Entry> findByTopicIdAndIsActiveTrueOrderByCreatedAtAsc(UUID topicId, Pageable pageable);

        Page<Entry> findByAuthorIdAndIsActiveTrueOrderByCreatedAtDesc(UUID authorId, Pageable pageable);

        // Date-filtered entries for a topic
        @Query("SELECT e FROM Entry e LEFT JOIN FETCH e.author WHERE e.topic.id = :topicId AND e.isActive = true AND e.createdAt >= :start AND e.createdAt < :end ORDER BY e.createdAt ASC")
        Page<Entry> findByTopicIdAndDateRange(@Param("topicId") UUID topicId, @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end, Pageable pageable);

        // Count entries by topic and date range
        @Query("SELECT COUNT(e) FROM Entry e WHERE e.topic.id = :topicId AND e.isActive = true AND e.createdAt >= :start AND e.createdAt < :end")
        Integer countByTopicIdAndDateRange(@Param("topicId") UUID topicId, @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        // Count entries before a date (for "older" count)
        @Query("SELECT COUNT(e) FROM Entry e WHERE e.topic.id = :topicId AND e.isActive = true AND e.createdAt < :before")
        Integer countByTopicIdBefore(@Param("topicId") UUID topicId, @Param("before") LocalDateTime before);

        // Find entries before a date (for "older" section)
        @Query("SELECT e FROM Entry e LEFT JOIN FETCH e.author WHERE e.topic.id = :topicId AND e.isActive = true AND e.createdAt < :before ORDER BY e.createdAt ASC")
        Page<Entry> findByTopicIdBefore(@Param("topicId") UUID topicId, @Param("before") LocalDateTime before,
                        Pageable pageable);

        @Query("SELECT e FROM Entry e WHERE e.isActive = true ORDER BY e.likeCount DESC, e.createdAt DESC")
        Page<Entry> findPopularEntries(Pageable pageable);

        @Query("SELECT e FROM Entry e WHERE e.isActive = true ORDER BY e.createdAt DESC")
        Page<Entry> findLatestEntries(Pageable pageable);

        @Query("SELECT COUNT(e) FROM Entry e WHERE e.author.id = :authorId AND e.isActive = true")
        Long countByAuthorId(@Param("authorId") UUID authorId);

        @Query("SELECT e FROM Entry e WHERE EXTRACT(DAY FROM e.createdAt) = :day AND EXTRACT(MONTH FROM e.createdAt) = :month AND EXTRACT(YEAR FROM e.createdAt) < :year AND e.isActive = true ORDER BY e.createdAt DESC")
        Page<Entry> findHistoryEntries(@Param("day") int day, @Param("month") int month, @Param("year") int year,
                        Pageable pageable);

        @Query(value = "SELECT * FROM entries WHERE is_active = true ORDER BY RANDOM()", nativeQuery = true)
        Page<Entry> findRandomEntries(Pageable pageable);

        @Modifying
        @Query("UPDATE Entry e SET e.likeCount = e.likeCount + 1 WHERE e.id = :entryId")
        void incrementLikeCount(@Param("entryId") UUID entryId);

        @Modifying
        @Query("UPDATE Entry e SET e.likeCount = e.likeCount - 1 WHERE e.id = :entryId AND e.likeCount > 0")
        void decrementLikeCount(@Param("entryId") UUID entryId);

        @Modifying
        @Query("UPDATE Entry e SET e.dislikeCount = e.dislikeCount + 1 WHERE e.id = :entryId")
        void incrementDislikeCount(@Param("entryId") UUID entryId);

        @Modifying
        @Query("UPDATE Entry e SET e.dislikeCount = e.dislikeCount - 1 WHERE e.id = :entryId AND e.dislikeCount > 0")
        void decrementDislikeCount(@Param("entryId") UUID entryId);

        @Modifying
        @Query("UPDATE Entry e SET e.favoriteCount = e.favoriteCount + 1 WHERE e.id = :entryId")
        void incrementFavoriteCount(@Param("entryId") UUID entryId);

        @Modifying
        @Query("UPDATE Entry e SET e.favoriteCount = e.favoriteCount - 1 WHERE e.id = :entryId AND e.favoriteCount > 0")
        void decrementFavoriteCount(@Param("entryId") UUID entryId);

        // Top entries by author - only entries with counts > 0
        @Query("SELECT e FROM Entry e WHERE e.author.id = :authorId AND e.isActive = true AND e.likeCount > 0 ORDER BY e.likeCount DESC")
        List<Entry> findTopLikedByAuthor(@Param("authorId") UUID authorId, Pageable pageable);

        @Query("SELECT e FROM Entry e WHERE e.author.id = :authorId AND e.isActive = true AND e.favoriteCount > 0 ORDER BY e.favoriteCount DESC")
        List<Entry> findTopFavoritedByAuthor(@Param("authorId") UUID authorId, Pageable pageable);

        @Query("SELECT e.author.username, COUNT(e) FROM Entry e WHERE e.isActive = true GROUP BY e.author.username ORDER BY COUNT(e) DESC")
        List<Object[]> findTopAuthors(int limit);

        // Popular entries from high-entry-count topics ordered by likes
        @Query("SELECT e FROM Entry e WHERE e.isActive = true AND e.likeCount > 0 ORDER BY e.topic.entryCount DESC, e.likeCount DESC")
        List<Entry> findPopularEntriesFromTopTopics(Pageable pageable);
}
