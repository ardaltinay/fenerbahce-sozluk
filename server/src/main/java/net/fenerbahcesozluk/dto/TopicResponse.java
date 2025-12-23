package net.fenerbahcesozluk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicResponse {

  private UUID id;
  private String title;
  private String categoryName;
  private UUID categoryId;
  private String authorUsername;
  private UUID authorId;
  private Integer entryCount;
  private Long viewCount;
  private boolean isLocked;
  private boolean isPinned;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
