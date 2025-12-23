package net.fenerbahcesozluk.repository;

import net.fenerbahcesozluk.entity.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TopicRepository extends JpaRepository<Topic, UUID> {

  java.util.Optional<Topic> findByTitleIgnoreCase(String title);

  Page<Topic> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);

  Page<Topic> findByCategoryIdAndIsActiveTrueOrderByCreatedAtDesc(UUID categoryId, Pageable pageable);

  Page<Topic> findByAuthorIdAndIsActiveTrueOrderByCreatedAtDesc(UUID authorId, Pageable pageable);

  @Query("SELECT t FROM Topic t WHERE t.isActive = true AND t.entryCount > 0 ORDER BY t.entryCount DESC")
  Page<Topic> findPopularTopics(Pageable pageable);

  @Query("SELECT t FROM Topic t WHERE t.isActive = true AND t.isPinned = true ORDER BY t.createdAt DESC")
  Page<Topic> findPinnedTopics(Pageable pageable);

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

  @Modifying
  @Query("UPDATE Topic t SET t.entryCount = t.entryCount - 1 WHERE t.id = :topicId AND t.entryCount > 0")
  void decrementEntryCount(@Param("topicId") UUID topicId);
}
