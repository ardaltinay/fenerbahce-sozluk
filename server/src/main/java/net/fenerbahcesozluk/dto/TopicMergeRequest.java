package net.fenerbahcesozluk.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class TopicMergeRequest {
    @NotNull(message = "Hedef başlık ID'si gereklidir")
    private UUID targetTopicId;
}
