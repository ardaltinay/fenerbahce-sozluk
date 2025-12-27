package net.fenerbahcesozluk.service;

import lombok.RequiredArgsConstructor;
import net.fenerbahcesozluk.dto.EntryRequest;
import net.fenerbahcesozluk.dto.EntryResponse;
import net.fenerbahcesozluk.entity.Entry;
import net.fenerbahcesozluk.entity.Topic;
import net.fenerbahcesozluk.entity.User;
import net.fenerbahcesozluk.entity.Vote;
import net.fenerbahcesozluk.enums.VoteType;
import net.fenerbahcesozluk.exception.BusinessException;
import net.fenerbahcesozluk.repository.EntryRepository;
import net.fenerbahcesozluk.repository.TopicRepository;
import net.fenerbahcesozluk.repository.VoteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EntryService {

  private final EntryRepository entryRepository;
  private final TopicRepository topicRepository;
  private final VoteRepository voteRepository;
  private final StatsService statsService;
  private final TopicService topicService;

  public Page<EntryResponse> getEntriesByTopic(UUID topicId, User currentUser, Pageable pageable) {
    return entryRepository.findByTopicIdAndIsActiveTrueOrderByCreatedAtAsc(topicId, pageable)
        .map(entry -> toResponse(entry, currentUser));
  }

  public Page<EntryResponse> getEntriesByAuthor(UUID authorId, User currentUser, Pageable pageable) {
    return entryRepository.findByAuthorIdAndIsActiveTrueOrderByCreatedAtDesc(authorId, pageable)
        .map(entry -> toResponse(entry, currentUser));
  }

  public Page<EntryResponse> getPopularEntries(User currentUser, Pageable pageable) {
    return entryRepository.findPopularEntries(pageable)
        .map(entry -> toResponse(entry, currentUser));
  }

  public Page<EntryResponse> getHistoryEntries(User currentUser, Pageable pageable) {
    LocalDateTime now = LocalDateTime.now();
    return entryRepository.findHistoryEntries(now.getDayOfMonth(), now.getMonthValue(), now.getYear(), pageable)
        .map(entry -> toResponse(entry, currentUser));
  }

  public Page<EntryResponse> getLatestEntries(User currentUser, Pageable pageable) {
    return entryRepository.findLatestEntries(pageable)
        .map(entry -> toResponse(entry, currentUser));
  }

  public Page<EntryResponse> getRandomEntries(User currentUser, Pageable pageable) {
    return entryRepository.findRandomEntries(pageable)
        .map(entry -> toResponse(entry, currentUser));
  }

  public List<EntryResponse> getFavoriteEntriesByUserId(UUID userId, User currentUser) {
    return voteRepository.findByUserIdAndVoteType(userId, VoteType.FAVORITE)
        .stream()
        .map(Vote::getEntry)
        .filter(entry -> entry.isActive())
        .map(entry -> toResponse(entry, currentUser))
        .collect(java.util.stream.Collectors.toList());
  }

  public EntryResponse getEntryById(UUID id, User currentUser) {
    Entry entry = entryRepository.findById(id)
        .orElseThrow(() -> new BusinessException("Entry bulunamadı", HttpStatus.NOT_FOUND));
    return toResponse(entry, currentUser);
  }

  @Transactional
  public EntryResponse createEntry(EntryRequest request, User author) {
    Topic topic = topicRepository.findById(request.getTopicId())
        .orElseThrow(() -> new BusinessException("Başlık bulunamadı", HttpStatus.NOT_FOUND));

    if (topic.isLocked()) {
      throw new BusinessException("Bu başlık kilitli, entry yazılamaz", HttpStatus.FORBIDDEN);
    }

    Entry entry = Entry.builder()
        .content(request.getContent())
        .topic(topic)
        .author(author)
        .build();

    Entry saved = entryRepository.save(entry);
    topicRepository.incrementEntryCount(topic.getId());
    statsService.evictStatsCache();
    topicService.evictTopicCaches();

    return toResponse(saved, author);
  }

  @Transactional
  public EntryResponse updateEntry(UUID entryId, String newContent, User currentUser) {
    Entry entry = entryRepository.findById(entryId)
        .orElseThrow(() -> new BusinessException("Entry bulunamadı", HttpStatus.NOT_FOUND));

    // Allow edit if: owner, MODERATOR, or ADMIN
    boolean isOwner = entry.getAuthor().getId().equals(currentUser.getId());
    boolean isModerator = currentUser.getRole().name().equals("MODERATOR");
    boolean isAdmin = currentUser.getRole().name().equals("ADMIN");

    if (!isOwner && !isModerator && !isAdmin) {
      throw new BusinessException("Bu entry'yi düzenleme yetkiniz yok", HttpStatus.FORBIDDEN);
    }

    entry.setContent(newContent);
    entry.setEdited(true);
    Entry saved = entryRepository.save(entry);

    return toResponse(saved, currentUser);
  }

  @Transactional
  public void deleteEntry(UUID entryId, String reason, User currentUser) {
    Entry entry = entryRepository.findById(entryId)
        .orElseThrow(() -> new BusinessException("Entry bulunamadı", HttpStatus.NOT_FOUND));

    // Allow delete if: owner, MODERATOR, or ADMIN
    boolean isOwner = entry.getAuthor().getId().equals(currentUser.getId());
    boolean isModerator = currentUser.getRole().name().equals("MODERATOR");
    boolean isAdmin = currentUser.getRole().name().equals("ADMIN");

    if (!isOwner && !isModerator && !isAdmin) {
      throw new BusinessException("Bu işlem için yetkiniz yok", HttpStatus.FORBIDDEN);
    }

    entry.setActive(false);
    entry.setDeleteReason(reason);
    entryRepository.save(entry);
    topicRepository.decrementEntryCount(entry.getTopic().getId());
    statsService.evictStatsCache();
    topicService.evictTopicCaches();
  }

  private EntryResponse toResponse(Entry entry, User currentUser) {
    VoteType currentUserVote = null;

    if (currentUser != null) {
      Vote vote = voteRepository.findByEntryIdAndUserId(entry.getId(), currentUser.getId())
          .orElse(null);
      if (vote != null) {
        currentUserVote = vote.getVoteType();
      }
    }

    return EntryResponse.builder()
        .id(entry.getId())
        .content(entry.getContent())
        .topicId(entry.getTopic().getId())
        .topicTitle(entry.getTopic().getTitle())
        .authorId(entry.getAuthor().getId())
        .authorUsername(entry.getAuthor().getUsername())
        .likeCount(entry.getLikeCount())
        .dislikeCount(entry.getDislikeCount())
        .favoriteCount(entry.getFavoriteCount())
        .isEdited(entry.isEdited())
        .currentUserVote(currentUserVote)
        .createdAt(entry.getCreatedAt())
        .updatedAt(entry.getUpdatedAt())
        .build();
  }
}
