package net.fenerbahcesozluk.controller;

import lombok.RequiredArgsConstructor;
import net.fenerbahcesozluk.dto.CategoryResponse;
import net.fenerbahcesozluk.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

  private final CategoryService categoryService;

  @GetMapping
  public ResponseEntity<List<CategoryResponse>> getAllCategories() {
    return ResponseEntity.ok(categoryService.getAllCategories());
  }

  @GetMapping("/{id}")
  public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable UUID id) {
    return ResponseEntity.ok(categoryService.getCategoryById(id));
  }

  @GetMapping("/slug/{slug}")
  public ResponseEntity<CategoryResponse> getCategoryBySlug(@PathVariable String slug) {
    return ResponseEntity.ok(categoryService.getCategoryBySlug(slug));
  }
}
