package net.fenerbahcesozluk.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ContactRequest {

  @NotBlank(message = "İsim boş olamaz")
  @Size(min = 2, max = 100, message = "İsim 2-100 karakter arasında olmalıdır")
  private String name;

  @NotBlank(message = "Email boş olamaz")
  @Email(message = "Geçerli bir email adresi giriniz")
  private String email;

  @NotBlank(message = "Konu boş olamaz")
  @Size(min = 3, max = 200, message = "Konu 3-200 karakter arasında olmalıdır")
  private String subject;

  @NotBlank(message = "Mesaj boş olamaz")
  @Size(min = 10, max = 5000, message = "Mesaj 10-5000 karakter arasında olmalıdır")
  private String message;
}
