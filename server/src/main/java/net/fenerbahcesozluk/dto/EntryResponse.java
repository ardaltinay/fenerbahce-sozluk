package net.fenerbahcesozluk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.fenerbahcesozluk.enums.VoteType;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntryResponse {

    private UUID id;
    private String content;
    private UUID topicId;
    private String topicTitle;
    private String authorUsername;
    private UUID authorId;
    private Integer likeCount;
    private Integer dislikeCount;
    private Integer favoriteCount;
    private boolean isEdited;
    private VoteType currentUserVote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
