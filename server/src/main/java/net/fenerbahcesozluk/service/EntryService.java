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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EntryService {

    private final EntryRepository entryRepository;
    private final TopicRepository topicRepository;
    private final VoteRepository voteRepository;
    private final StatsService statsService;
    private final TopicService topicService;
    private final WebSocketService webSocketService;

    public Page<EntryResponse> getEntriesByTopic(UUID topicId, User currentUser, Pageable pageable) {
        Page<Entry> entries = entryRepository.findByTopicIdAndIsActiveTrueOrderByCreatedAtAsc(topicId, pageable);
        return toResponsePage(entries, currentUser);
    }

    /**
     * Get entries by topic with optional date filter
     * 
     * @param dateFilter:
     *            "today", "yesterday", "older", or null for all
     */
    public Page<EntryResponse> getEntriesByTopicWithDateFilter(UUID topicId, String dateFilter, User currentUser,
            Pageable pageable) {
        if (dateFilter == null || dateFilter.isEmpty()) {
            return getEntriesByTopic(topicId, currentUser, pageable);
        }

        LocalDate today = LocalDate.now();
        Page<Entry> entries;

        switch (dateFilter.toLowerCase()) {
            case "today" -> {
                LocalDateTime todayStart = today.atStartOfDay();
                LocalDateTime todayEnd = today.plusDays(1).atStartOfDay();
                entries = entryRepository.findByTopicIdAndDateRange(topicId, todayStart, todayEnd, pageable);
            }
            case "yesterday" -> {
                LocalDateTime yesterdayStart = today.minusDays(1).atStartOfDay();
                LocalDateTime yesterdayEnd = today.atStartOfDay();
                entries = entryRepository.findByTopicIdAndDateRange(topicId, yesterdayStart, yesterdayEnd, pageable);
            }
            case "older" -> {
                LocalDateTime yesterdayStart = today.minusDays(1).atStartOfDay();
                entries = entryRepository.findByTopicIdBefore(topicId, yesterdayStart, pageable);
            }
            default -> {
                entries = entryRepository.findByTopicIdAndIsActiveTrueOrderByCreatedAtAsc(topicId, pageable);
            }
        }

        return toResponsePage(entries, currentUser);
    }

    public Page<EntryResponse> getEntriesByAuthor(UUID authorId, User currentUser, Pageable pageable) {
        Page<Entry> entries = entryRepository.findByAuthorIdAndIsActiveTrueOrderByCreatedAtDesc(authorId, pageable);
        return toResponsePage(entries, currentUser);
    }

    public Page<EntryResponse> getPopularEntries(User currentUser, Pageable pageable) {
        Page<Entry> entries = entryRepository.findPopularEntries(pageable);
        return toResponsePage(entries, currentUser);
    }

    public Page<EntryResponse> getHistoryEntries(User currentUser, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        Page<Entry> entries = entryRepository.findHistoryEntries(now.getDayOfMonth(), now.getMonthValue(),
                now.getYear(), pageable);
        return toResponsePage(entries, currentUser);
    }

    public Page<EntryResponse> getLatestEntries(User currentUser, Pageable pageable) {
        Page<Entry> entries = entryRepository.findLatestEntries(pageable);
        return toResponsePage(entries, currentUser);
    }

    public Page<EntryResponse> getRandomEntries(User currentUser, Pageable pageable) {
        Page<Entry> entries = entryRepository.findRandomEntries(pageable);
        return toResponsePage(entries, currentUser);
    }

    public EntryResponse getRandomPopularEntry(User currentUser) {
        // Fetch top 20 popular entries from high-entry topics, then pick one randomly
        List<Entry> entries = entryRepository.findPopularEntriesFromTopTopics(PageRequest.of(0, 20));
        if (entries.isEmpty()) {
            return null;
        }
        // Pick a random entry from the list - single entry so toResponse is fine
        int randomIndex = (int) (Math.random() * entries.size());
        return toResponse(entries.get(randomIndex), currentUser);
    }

    public List<EntryResponse> getFavoriteEntriesByUserId(UUID userId, User currentUser) {
        List<Entry> favoriteEntries = voteRepository.findByUserIdAndVoteType(userId, VoteType.FAVORITE).stream()
                .map(Vote::getEntry).filter(Entry::isActive).collect(Collectors.toList());
        return toResponseList(favoriteEntries, currentUser);
    }

    public List<EntryResponse> getTopLikedByAuthor(UUID authorId, User currentUser, int limit) {
        List<Entry> entries = entryRepository.findTopLikedByAuthor(authorId, PageRequest.of(0, limit));
        return toResponseList(entries, currentUser);
    }

    public List<EntryResponse> getTopFavoritedByAuthor(UUID authorId, User currentUser, int limit) {
        List<Entry> entries = entryRepository.findTopFavoritedByAuthor(authorId, PageRequest.of(0, limit));
        return toResponseList(entries, currentUser);
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

        Entry entry = Entry.builder().content(request.getContent()).topic(topic).author(author).build();

        Entry saved = entryRepository.save(entry);
        topicRepository.incrementEntryCount(topic.getId());
        statsService.evictStatsCache();
        topicService.evictTopicCaches();

        EntryResponse response = toResponse(saved, author);

        // Broadcast to WebSocket subscribers
        webSocketService.broadcastNewEntry(topic.getId(), response);
        webSocketService.broadcastSidebarUpdate();

        return response;
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

    // Batch-optimized page conversion - loads all votes in one query
    private Page<EntryResponse> toResponsePage(Page<Entry> entries, User currentUser) {
        if (entries.isEmpty()) {
            return entries.map(e -> toResponseWithVote(e, null));
        }

        // Batch load votes for all entries
        Map<UUID, VoteType> userVotes = Collections.emptyMap();
        if (currentUser != null) {
            List<UUID> entryIds = entries.stream().map(Entry::getId).collect(Collectors.toList());
            userVotes = voteRepository.findByEntryIdsAndUserId(entryIds, currentUser.getId()).stream()
                    .collect(Collectors.toMap(v -> v.getEntry().getId(), Vote::getVoteType));
        }

        final Map<UUID, VoteType> votes = userVotes;
        return entries.map(entry -> toResponseWithVote(entry, votes.get(entry.getId())));
    }

    // Batch-optimized list conversion
    private List<EntryResponse> toResponseList(List<Entry> entries, User currentUser) {
        if (entries.isEmpty()) {
            return Collections.emptyList();
        }

        Map<UUID, VoteType> userVotes = Collections.emptyMap();
        if (currentUser != null) {
            List<UUID> entryIds = entries.stream().map(Entry::getId).collect(Collectors.toList());
            userVotes = voteRepository.findByEntryIdsAndUserId(entryIds, currentUser.getId()).stream()
                    .collect(Collectors.toMap(v -> v.getEntry().getId(), Vote::getVoteType));
        }

        final Map<UUID, VoteType> votes = userVotes;
        return entries.stream().map(entry -> toResponseWithVote(entry, votes.get(entry.getId())))
                .collect(Collectors.toList());
    }

    // Single entry response with pre-loaded vote
    private EntryResponse toResponseWithVote(Entry entry, VoteType currentUserVote) {
        return EntryResponse.builder().id(entry.getId()).content(entry.getContent()).topicId(entry.getTopic().getId())
                .topicTitle(entry.getTopic().getTitle()).authorId(entry.getAuthor().getId())
                .authorUsername(entry.getAuthor().getUsername()).likeCount(entry.getLikeCount())
                .dislikeCount(entry.getDislikeCount()).favoriteCount(entry.getFavoriteCount())
                .isEdited(entry.isEdited()).currentUserVote(currentUserVote).createdAt(entry.getCreatedAt())
                .updatedAt(entry.getUpdatedAt()).build();
    }

    // Keep original for single entry lookups
    private EntryResponse toResponse(Entry entry, User currentUser) {
        VoteType currentUserVote = null;

        if (currentUser != null) {
            Vote vote = voteRepository.findByEntryIdAndUserId(entry.getId(), currentUser.getId()).orElse(null);
            if (vote != null) {
                currentUserVote = vote.getVoteType();
            }
        }

        return toResponseWithVote(entry, currentUserVote);
    }
}
