package net.fenerbahcesozluk.repository;

import net.fenerbahcesozluk.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public interface NewsRepository extends JpaRepository<News, UUID> {
    Page<News> findAllByOrderByPubDateDesc(Pageable pageable);

    boolean existsByGuid(String guid);

    // Check if same title exists within a time window (prevents cross-source
    // duplicates)
    boolean existsByTitleIgnoreCaseAndPubDateAfter(String title, LocalDateTime after);

    // Batch query: Get all existing GUIDs for duplicate checking
    @Query("SELECT n.guid FROM News n")
    Set<String> findAllGuids();

    // Batch query: Get all titles from recent news (within time window)
    @Query("SELECT LOWER(n.title) FROM News n WHERE n.pubDate > :after")
    Set<String> findRecentTitlesLowerCase(@Param("after") LocalDateTime after);

    @Modifying
    void deleteByPubDateBefore(LocalDateTime date); // For cleanup
}
