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
public class MessageRequest {

  @NotBlank(message = "Alıcı kullanıcı adı boş olamaz")
  private String receiverUsername;

  @NotBlank(message = "Mesaj içeriği boş olamaz")
  @Size(max = 2000, message = "Mesaj en fazla 2000 karakter olabilir")
  private String content;
}
