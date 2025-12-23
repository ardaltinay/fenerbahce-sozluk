package net.fenerbahcesozluk.service;

import lombok.RequiredArgsConstructor;
import net.fenerbahcesozluk.dto.TopicRequest;
import net.fenerbahcesozluk.dto.TopicResponse;
import net.fenerbahcesozluk.entity.Category;
import net.fenerbahcesozluk.entity.Topic;
import net.fenerbahcesozluk.entity.User;
import net.fenerbahcesozluk.exception.BusinessException;
import net.fenerbahcesozluk.repository.CategoryRepository;
import net.fenerbahcesozluk.repository.TopicRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TopicService {

  private final TopicRepository topicRepository;
  private final CategoryRepository categoryRepository;

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

  public TopicResponse getTopicById(UUID id) {
    Topic topic = topicRepository.findById(id)
        .orElseThrow(() -> new BusinessException("Başlık bulunamadı", HttpStatus.NOT_FOUND));
    return toResponse(topic);
  }

  @Transactional
  public TopicResponse createTopic(TopicRequest request, User author) {
    if (request.getTitle() != null && request.getTitle().length() > 50) {
      throw new BusinessException("Başlık 50 karakterden uzun olamaz", HttpStatus.BAD_REQUEST);
    }

    Category category = categoryRepository.findById(request.getCategoryId())
        .orElseThrow(() -> new BusinessException("Kategori bulunamadı", HttpStatus.NOT_FOUND));

    Topic topic = Topic.builder()
        .title(request.getTitle())
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
        .orElseThrow(() -> new BusinessException("Başlık bulunamadı", HttpStatus.NOT_FOUND));

    boolean isOwner = topic.getAuthor().getId().equals(currentUser.getId());
    boolean isModerator = currentUser.getRole().name().equals("MODERATOR");
    boolean isAdmin = currentUser.getRole().name().equals("ADMIN");

    if (!isOwner && !isModerator && !isAdmin) {
      throw new BusinessException("Bu işlem için yetkiniz yok", HttpStatus.FORBIDDEN);
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

}
