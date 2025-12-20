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
public class TopicRequest {

  @NotBlank(message = "Başlık boş olamaz")
  @Size(min = 3, max = 255, message = "Başlık 3-255 karakter arasında olmalıdır")
  private String title;

  @NotNull(message = "Kategori seçimi zorunludur")
  private UUID categoryId;
}
