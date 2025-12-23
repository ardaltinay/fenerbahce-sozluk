package net.fenerbahcesozluk.service;

import lombok.RequiredArgsConstructor;
import net.fenerbahcesozluk.dto.EntryRequest;
import net.fenerbahcesozluk.dto.EntryResponse;
import net.fenerbahcesozluk.entity.Entry;
import net.fenerbahcesozluk.entity.Topic;
import net.fenerbahcesozluk.entity.User;
import net.fenerbahcesozluk.entity.Vote;
import net.fenerbahcesozluk.enums.VoteType;
import net.fenerbahcesozluk.repository.EntryRepository;
import net.fenerbahcesozluk.repository.TopicRepository;
import net.fenerbahcesozluk.repository.VoteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EntryService {

  private final EntryRepository entryRepository;
  private final TopicRepository topicRepository;
  private final VoteRepository voteRepository;

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

  public EntryResponse getEntryById(UUID id, User currentUser) {
    Entry entry = entryRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Entry bulunamadı"));
    return toResponse(entry, currentUser);
  }

  @Transactional
  public EntryResponse createEntry(EntryRequest request, User author) {
    Topic topic = topicRepository.findById(request.getTopicId())
        .orElseThrow(() -> new RuntimeException("Başlık bulunamadı"));

    if (topic.isLocked()) {
      throw new RuntimeException("Bu başlık kilitli, entry yazılamaz");
    }

    Entry entry = Entry.builder()
        .content(request.getContent())
        .topic(topic)
        .author(author)
        .build();

    Entry saved = entryRepository.save(entry);
    topicRepository.incrementEntryCount(topic.getId());

    return toResponse(saved, author);
  }

  @Transactional
  public EntryResponse updateEntry(UUID entryId, String newContent, User currentUser) {
    Entry entry = entryRepository.findById(entryId)
        .orElseThrow(() -> new RuntimeException("Entry bulunamadı"));

    // Allow edit if: owner, MODERATOR, or ADMIN
    boolean isOwner = entry.getAuthor().getId().equals(currentUser.getId());
    boolean isModerator = currentUser.getRole().name().equals("MODERATOR");
    boolean isAdmin = currentUser.getRole().name().equals("ADMIN");

    if (!isOwner && !isModerator && !isAdmin) {
      throw new RuntimeException("Bu entry'yi düzenleme yetkiniz yok");
    }

    entry.setContent(newContent);
    entry.setEdited(true);
    Entry saved = entryRepository.save(entry);

    return toResponse(saved, currentUser);
  }

  @Transactional
  public void deleteEntry(UUID entryId, String reason, User currentUser) {
    Entry entry = entryRepository.findById(entryId)
        .orElseThrow(() -> new RuntimeException("Entry bulunamadı"));

    // Allow delete if: owner, MODERATOR, or ADMIN
    boolean isOwner = entry.getAuthor().getId().equals(currentUser.getId());
    boolean isModerator = currentUser.getRole().name().equals("MODERATOR");
    boolean isAdmin = currentUser.getRole().name().equals("ADMIN");

    if (!isOwner && !isModerator && !isAdmin) {
      throw new RuntimeException("Bu işlem için yetkiniz yok");
    }

    entry.setActive(false);
    entry.setDeleteReason(reason);
    entryRepository.save(entry);
    topicRepository.decrementEntryCount(entry.getTopic().getId());
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
