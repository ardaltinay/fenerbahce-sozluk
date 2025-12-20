package net.fenerbahcesozluk.service;

import lombok.RequiredArgsConstructor;
import net.fenerbahcesozluk.dto.CategoryResponse;
import net.fenerbahcesozluk.entity.Category;
import net.fenerbahcesozluk.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryRepository categoryRepository;

  public List<CategoryResponse> getAllCategories() {
    return categoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
        .stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  public CategoryResponse getCategoryById(UUID id) {
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Kategori bulunamadı"));
    return toResponse(category);
  }

  public CategoryResponse getCategoryBySlug(String slug) {
    Category category = categoryRepository.findBySlug(slug)
        .orElseThrow(() -> new RuntimeException("Kategori bulunamadı: " + slug));
    return toResponse(category);
  }

  private CategoryResponse toResponse(Category category) {
    return CategoryResponse.builder()
        .id(category.getId())
        .name(category.getName())
        .description(category.getDescription())
        .slug(category.getSlug())
        .icon(category.getIcon())
        .displayOrder(category.getDisplayOrder())
        .topicCount(category.getTopics() != null ? category.getTopics().size() : 0)
        .build();
  }
}
