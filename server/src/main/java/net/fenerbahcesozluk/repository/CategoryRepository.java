package net.fenerbahcesozluk.repository;

import net.fenerbahcesozluk.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

  Optional<Category> findBySlug(String slug);

  Optional<Category> findByName(String name);

  boolean existsBySlug(String slug);

  boolean existsByName(String name);

  List<Category> findByIsActiveTrueOrderByDisplayOrderAsc();
}
