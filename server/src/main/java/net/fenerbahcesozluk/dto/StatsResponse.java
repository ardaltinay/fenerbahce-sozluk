package net.fenerbahcesozluk.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class StatsResponse {
  private long totalEntries;
  private long totalAuthors;
  private long totalTopics;
  private long totalLikes;
  private long totalDislikes;
  private long totalFavorites;
  private List<TopAuthor> topAuthors;
  private List<TopTopic> topTopics;

  @Data
  @Builder
  public static class TopAuthor {
    private String username;
    private long entryCount;
  }

  @Data
  @Builder
  public static class TopTopic {
    private String id;
    private String title;
    private long entryCount;
  }
}
