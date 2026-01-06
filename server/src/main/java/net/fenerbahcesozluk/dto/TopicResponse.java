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
    private String authorUsername;
    private UUID authorId;
    private Integer entryCount;
    private Integer todayEntryCount;
    private Integer yesterdayEntryCount;
    private Integer olderEntryCount;
    private Long viewCount;
    private boolean isLocked;
    private boolean isPinned;
    private String kunyeImageUrl;
    private String kunyeData;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastActivityAt;
}
