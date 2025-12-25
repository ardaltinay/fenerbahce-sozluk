package net.fenerbahcesozluk.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeleteAccountRequest {

  @NotBlank(message = "Şifre boş olamaz")
  private String password;
}
