package net.fenerbahcesozluk.service;

import lombok.RequiredArgsConstructor;
import net.fenerbahcesozluk.dto.StatsResponse;
import net.fenerbahcesozluk.repository.EntryRepository;
import net.fenerbahcesozluk.repository.TopicRepository;
import net.fenerbahcesozluk.repository.UserRepository;
import net.fenerbahcesozluk.repository.VoteRepository;
import net.fenerbahcesozluk.enums.VoteType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

  private final EntryRepository entryRepository;
  private final TopicRepository topicRepository;
  private final UserRepository userRepository;
  private final VoteRepository voteRepository;

  public StatsResponse getStats() {
    long totalEntries = entryRepository.count();
    long totalTopics = topicRepository.count();
    long totalAuthors = userRepository.count();

    long totalLikes = voteRepository.countByVoteType(VoteType.LIKE);
    long totalDislikes = voteRepository.countByVoteType(VoteType.DISLIKE);
    long totalFavorites = voteRepository.countByVoteType(VoteType.FAVORITE);

    // Top 5 authors by entry count
    List<Object[]> topAuthorsRaw = entryRepository.findTopAuthors(5);
    List<StatsResponse.TopAuthor> topAuthors = topAuthorsRaw.stream()
        .map(row -> StatsResponse.TopAuthor.builder()
            .username((String) row[0])
            .entryCount(((Number) row[1]).longValue())
            .build())
        .collect(Collectors.toList());

    // Top 5 topics by entry count
    List<Object[]> topTopicsRaw = topicRepository.findTopTopics(5);
    List<StatsResponse.TopTopic> topTopics = topTopicsRaw.stream()
        .map(row -> StatsResponse.TopTopic.builder()
            .id(row[0].toString())
            .title((String) row[1])
            .entryCount(((Number) row[2]).longValue())
            .build())
        .collect(Collectors.toList());

    return StatsResponse.builder()
        .totalEntries(totalEntries)
        .totalAuthors(totalAuthors)
        .totalTopics(totalTopics)
        .totalLikes(totalLikes)
        .totalDislikes(totalDislikes)
        .totalFavorites(totalFavorites)
        .topAuthors(topAuthors)
        .topTopics(topTopics)
        .build();
  }
}
