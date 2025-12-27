package net.fenerbahcesozluk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicRequest {

  @NotBlank(message = "Başlık boş olamaz")
  @Size(min = 3, max = 50, message = "Başlık 3-50 karakter arasında olmalıdır")
  private String title;

  // Optional Transfermarkt integration
  private String topicType;
  private String transfermarktId;
}
