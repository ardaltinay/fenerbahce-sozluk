package net.fenerbahcesozluk.repository;

import net.fenerbahcesozluk.entity.Entry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EntryRepository extends JpaRepository<Entry, UUID> {

  Page<Entry> findByTopicIdAndIsActiveTrueOrderByCreatedAtAsc(UUID topicId, Pageable pageable);

  Page<Entry> findByAuthorIdAndIsActiveTrueOrderByCreatedAtDesc(UUID authorId, Pageable pageable);

  @Query("SELECT e FROM Entry e WHERE e.isActive = true ORDER BY e.likeCount DESC, e.createdAt DESC")
  Page<Entry> findPopularEntries(Pageable pageable);

  @Query("SELECT e FROM Entry e WHERE e.isActive = true ORDER BY e.createdAt DESC")
  Page<Entry> findLatestEntries(Pageable pageable);

  @Query("SELECT COUNT(e) FROM Entry e WHERE e.topic.id = :topicId AND e.isActive = true")
  Long countByTopicId(@Param("topicId") UUID topicId);

  @Query("SELECT COUNT(e) FROM Entry e WHERE e.author.id = :authorId AND e.isActive = true")
  Long countByAuthorId(@Param("authorId") UUID authorId);

  @Query("SELECT COALESCE(SUM(e.likeCount), 0) FROM Entry e WHERE e.author.id = :authorId AND e.isActive = true")
  Long sumLikeCountByAuthorId(@Param("authorId") UUID authorId);

  @Query("SELECT COALESCE(SUM(e.favoriteCount), 0) FROM Entry e WHERE e.author.id = :authorId AND e.isActive = true")
  Long sumFavoriteCountByAuthorId(@Param("authorId") UUID authorId);

  @Query("SELECT e FROM Entry e WHERE EXTRACT(DAY FROM e.createdAt) = :day AND EXTRACT(MONTH FROM e.createdAt) = :month AND EXTRACT(YEAR FROM e.createdAt) < :year AND e.isActive = true ORDER BY e.createdAt DESC")
  Page<Entry> findHistoryEntries(@Param("day") int day, @Param("month") int month, @Param("year") int year,
      Pageable pageable);

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
}
