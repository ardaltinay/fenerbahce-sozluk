package net.fenerbahcesozluk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntryRequest {

  @NotNull(message = "Başlık ID'si boş olamaz")
  private UUID topicId;

  @NotBlank(message = "Entry içeriği boş olamaz")
  @Size(min = 10, max = 50000, message = "Entry içeriği 10-50000 karakter arasında olmalıdır")
  private String content;
}
