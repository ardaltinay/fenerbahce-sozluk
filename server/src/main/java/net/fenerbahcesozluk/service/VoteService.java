package net.fenerbahcesozluk.service;

import lombok.RequiredArgsConstructor;
import net.fenerbahcesozluk.dto.VoteRequest;
import net.fenerbahcesozluk.entity.Entry;
import net.fenerbahcesozluk.entity.User;
import net.fenerbahcesozluk.entity.Vote;
import net.fenerbahcesozluk.enums.VoteType;
import net.fenerbahcesozluk.repository.EntryRepository;
import net.fenerbahcesozluk.repository.VoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VoteService {

  private final VoteRepository voteRepository;
  private final EntryRepository entryRepository;

  @Transactional
  public void vote(VoteRequest request, User user) {
    Entry entry = entryRepository.findById(request.getEntryId())
        .orElseThrow(() -> new RuntimeException("Entry bulunamadı"));

    // Check if user already voted
    Optional<Vote> existingVote = voteRepository.findByEntryIdAndUserId(
        request.getEntryId(), user.getId());

    if (existingVote.isPresent()) {
      Vote vote = existingVote.get();
      VoteType oldVoteType = vote.getVoteType();

      // If same vote type, remove the vote (toggle off)
      if (oldVoteType == request.getVoteType()) {
        removeVoteCount(entry.getId(), oldVoteType);
        voteRepository.delete(vote);
        return;
      }

      // Different vote type, update the vote
      removeVoteCount(entry.getId(), oldVoteType);
      addVoteCount(entry.getId(), request.getVoteType());
      vote.setVoteType(request.getVoteType());
      voteRepository.save(vote);
    } else {
      // New vote
      Vote vote = Vote.builder()
          .entry(entry)
          .user(user)
          .voteType(request.getVoteType())
          .build();

      voteRepository.save(vote);
      addVoteCount(entry.getId(), request.getVoteType());
    }
  }

  @Transactional
  public void removeVote(UUID entryId, User user) {
    Optional<Vote> existingVote = voteRepository.findByEntryIdAndUserId(entryId, user.getId());

    if (existingVote.isPresent()) {
      Vote vote = existingVote.get();
      removeVoteCount(entryId, vote.getVoteType());
      voteRepository.delete(vote);
    }
  }

  private void addVoteCount(UUID entryId, VoteType voteType) {
    switch (voteType) {
      case LIKE -> entryRepository.incrementLikeCount(entryId);
      case DISLIKE -> entryRepository.incrementDislikeCount(entryId);
      case FAVORITE -> entryRepository.incrementFavoriteCount(entryId);
    }
  }

  private void removeVoteCount(UUID entryId, VoteType voteType) {
    switch (voteType) {
      case LIKE -> entryRepository.decrementLikeCount(entryId);
      case DISLIKE -> entryRepository.decrementDislikeCount(entryId);
      case FAVORITE -> entryRepository.decrementFavoriteCount(entryId);
    }
  }
}
