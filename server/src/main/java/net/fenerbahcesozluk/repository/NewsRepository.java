package net.fenerbahcesozluk.repository;

import net.fenerbahcesozluk.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public interface NewsRepository extends JpaRepository<News, UUID> {
    Page<News> findAllByOrderByPubDateDesc(Pageable pageable);

    boolean existsByGuid(String guid);

    void deleteByPubDateBefore(LocalDateTime date); // For cleanup
}
