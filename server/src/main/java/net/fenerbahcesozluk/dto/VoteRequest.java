package net.fenerbahcesozluk.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.fenerbahcesozluk.enums.VoteType;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteRequest {

    @NotNull(message = "Entry ID'si boş olamaz")
    private UUID entryId;

    @NotNull(message = "Vote tipi boş olamaz")
    private VoteType voteType;
}
