package net.fenerbahcesozluk.service;

import lombok.RequiredArgsConstructor;
import net.fenerbahcesozluk.dto.TopicRequest;
import net.fenerbahcesozluk.dto.TopicResponse;
import net.fenerbahcesozluk.entity.Category;
import net.fenerbahcesozluk.entity.Topic;
import net.fenerbahcesozluk.entity.User;
import net.fenerbahcesozluk.repository.CategoryRepository;
import net.fenerbahcesozluk.repository.TopicRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class TopicService {

  private final TopicRepository topicRepository;
  private final CategoryRepository categoryRepository;

  private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
  private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

  public Page<TopicResponse> getAllTopics(Pageable pageable) {
    return topicRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable)
        .map(this::toResponse);
  }

  public Page<TopicResponse> getPopularTopics(Pageable pageable) {
    return topicRepository.findPopularTopics(pageable)
        .map(this::toResponse);
  }

  public Page<TopicResponse> getTrendingTopics(Pageable pageable) {
    LocalDateTime thirtyDaysAgo = LocalDate.now().minusDays(30).atStartOfDay();
    return topicRepository.findTrends(thirtyDaysAgo, pageable)
        .map(this::toResponse);
  }

  public Page<TopicResponse> getTopicsByCategory(UUID categoryId, Pageable pageable) {
    return topicRepository.findByCategoryIdAndIsActiveTrueOrderByCreatedAtDesc(categoryId, pageable)
        .map(this::toResponse);
  }

  public Page<TopicResponse> searchTopics(String keyword, Pageable pageable) {
    return topicRepository.searchByTitle(keyword, pageable)
        .map(this::toResponse);
  }

  public TopicResponse getTopicBySlug(String slug) {
    Topic topic = topicRepository.findBySlug(slug)
        .orElseThrow(() -> new RuntimeException("Başlık bulunamadı: " + slug));
    return toResponse(topic);
  }

  public TopicResponse getTopicById(UUID id) {
    Topic topic = topicRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Başlık bulunamadı"));
    return toResponse(topic);
  }

  @Transactional
  public TopicResponse createTopic(TopicRequest request, User author) {
    String slug = toSlug(request.getTitle());

    // Make slug unique if exists
    if (topicRepository.existsBySlug(slug)) {
      throw new RuntimeException("duplicate_topic:" + slug);
    }

    Category category = categoryRepository.findById(request.getCategoryId())
        .orElseThrow(() -> new RuntimeException("Kategori bulunamadı"));

    Topic topic = Topic.builder()
        .title(request.getTitle())
        .slug(slug)
        .category(category)
        .author(author)
        .build();

    Topic saved = topicRepository.save(topic);
    return toResponse(saved);
  }

  @Transactional
  public void incrementViewCount(UUID topicId) {
    topicRepository.incrementViewCount(topicId);
  }

  @Transactional
  public void deleteTopic(UUID topicId, String reason, User currentUser) {
    Topic topic = topicRepository.findById(topicId)
        .orElseThrow(() -> new RuntimeException("Başlık bulunamadı"));

    boolean isOwner = topic.getAuthor().getId().equals(currentUser.getId());
    boolean isModerator = currentUser.getRole().name().equals("MODERATOR");
    boolean isAdmin = currentUser.getRole().name().equals("ADMIN");

    if (!isOwner && !isModerator && !isAdmin) {
      throw new RuntimeException("Bu işlem için yetkiniz yok");
    }

    // Soft delete
    topic.setActive(false);
    topic.setDeleteReason(reason);
    topicRepository.save(topic);
  }

  private TopicResponse toResponse(Topic topic) {
    return TopicResponse.builder()
        .id(topic.getId())
        .title(topic.getTitle())
        .slug(topic.getSlug())
        .categoryId(topic.getCategory() != null ? topic.getCategory().getId() : null)
        .categoryName(topic.getCategory() != null ? topic.getCategory().getName() : null)
        .authorId(topic.getAuthor().getId())
        .authorUsername(topic.getAuthor().getUsername())
        .entryCount(topic.getEntryCount())
        .viewCount(topic.getViewCount())
        .isLocked(topic.isLocked())
        .isPinned(topic.isPinned())
        .createdAt(topic.getCreatedAt())
        .updatedAt(topic.getUpdatedAt())
        .build();
  }

  private String toSlug(String input) {
    String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
    String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
    String slug = NONLATIN.matcher(normalized).replaceAll("");
    return slug.toLowerCase(Locale.forLanguageTag("tr"));
  }
}
