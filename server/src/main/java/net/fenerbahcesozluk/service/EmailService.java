package net.fenerbahcesozluk.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.fenerbahcesozluk.dto.ContactRequest;
import net.fenerbahcesozluk.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Async
    public void sendPasswordResetEmail(String toEmail, String username, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject("Şifre Sıfırlama - Fenerbahçe Sözlük");

            String resetLink = frontendUrl + "/sifre-sifirla/" + token;
            String htmlContent = buildPasswordResetEmail(username, resetLink);

            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new BusinessException("Email gönderilemedi", HttpStatus.SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            log.error("Unexpected error sending email to: {}", toEmail, e);
            throw new BusinessException("Email gönderilemedi", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @Async
    public void sendContactEmail(ContactRequest request) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(fromEmail); // Send to admin
            helper.setReplyTo(request.getEmail()); // Reply goes to user
            helper.setSubject("[İletişim] " + request.getSubject());

            String htmlContent = buildContactEmail(request);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Contact email sent from: {}", request.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send contact email from: {}", request.getEmail(), e);
            throw new BusinessException("Mesaj gönderilemedi", HttpStatus.SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            log.error("Unexpected error sending contact email from: {}", request.getEmail(), e);
            throw new BusinessException("Mesaj gönderilemedi", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private String buildContactEmail(ContactRequest request) {
        return """
                <!DOCTYPE html>
                <html lang="tr">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #0d0d1a;">
                    <table cellpadding="0" cellspacing="0" border="0" width="100%%" style="background-color: #0d0d1a; padding: 40px 20px;">
                        <tr>
                            <td align="center">
                                <table cellpadding="0" cellspacing="0" border="0" width="600" style="background: linear-gradient(135deg, #1a1a2e 0%%, #0f0f23 100%%); border-radius: 16px; border: 1px solid rgba(212, 200, 74, 0.2); box-shadow: 0 10px 40px rgba(0,0,0,0.6);">
                                    <!-- Header -->
                                    <tr>
                                        <td style="padding: 40px 40px 20px; text-align: center; border-bottom: 1px solid rgba(212, 200, 74, 0.1);">
                                            <h1 style="margin: 0; font-size: 28px; font-weight: 700; color: #d4c84a;">📬 Yeni İletişim Mesajı</h1>
                                        </td>
                                    </tr>

                                    <!-- Content -->
                                    <tr>
                                        <td style="padding: 40px;">
                                            <table cellpadding="0" cellspacing="0" border="0" width="100%%" style="margin-bottom: 25px;">
                                                <tr>
                                                    <td style="padding: 12px 0; border-bottom: 1px solid rgba(255,255,255,0.1);">
                                                        <span style="color: #888; font-size: 14px;">Gönderen:</span><br>
                                                        <span style="color: #e0e0e0; font-size: 16px; font-weight: 600;">%s</span>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td style="padding: 12px 0; border-bottom: 1px solid rgba(255,255,255,0.1);">
                                                        <span style="color: #888; font-size: 14px;">Email:</span><br>
                                                        <a href="mailto:%s" style="color: #d4c84a; font-size: 16px; text-decoration: none;">%s</a>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td style="padding: 12px 0; border-bottom: 1px solid rgba(255,255,255,0.1);">
                                                        <span style="color: #888; font-size: 14px;">Konu:</span><br>
                                                        <span style="color: #e0e0e0; font-size: 16px; font-weight: 600;">%s</span>
                                                    </td>
                                                </tr>
                                            </table>

                                            <div style="background: rgba(0,0,0,0.3); border-radius: 8px; padding: 20px; border-left: 4px solid #d4c84a;">
                                                <span style="color: #888; font-size: 14px; display: block; margin-bottom: 10px;">Mesaj:</span>
                                                <p style="margin: 0; font-size: 15px; line-height: 1.7; color: #e0e0e0; white-space: pre-wrap;">%s</p>
                                            </div>
                                        </td>
                                    </tr>

                                    <!-- Footer -->
                                    <tr>
                                        <td style="padding: 30px 40px; text-align: center; border-top: 1px solid rgba(212, 200, 74, 0.1); background: rgba(0,0,0,0.2); border-radius: 0 0 16px 16px;">
                                            <p style="margin: 0; font-size: 12px; color: #666;">
                                                Bu mesajı yanıtlamak için doğrudan "Yanıtla" tuşuna basabilirsiniz.
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """
                .formatted(request.getName(), request.getEmail(), request.getEmail(), request.getSubject(),
                        request.getMessage());
    }

    private String buildPasswordResetEmail(String username, String resetLink) {
        return """
                <!DOCTYPE html>
                <html lang="tr">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #0d0d1a;">
                    <table cellpadding="0" cellspacing="0" border="0" width="100%%" style="background-color: #0d0d1a; padding: 40px 20px;">
                        <tr>
                            <td align="center">
                                <table cellpadding="0" cellspacing="0" border="0" width="600" style="background: linear-gradient(135deg, #1a1a2e 0%%, #0f0f23 100%%); border-radius: 16px; border: 1px solid rgba(212, 200, 74, 0.2); box-shadow: 0 10px 40px rgba(0,0,0,0.6);">
                                    <!-- Header -->
                                    <tr>
                                        <td style="padding: 40px 40px 20px; text-align: center; border-bottom: 1px solid rgba(212, 200, 74, 0.1);">
                                            <h1 style="margin: 0; font-size: 28px; font-weight: 700; color: #d4c84a;">⚽ Fenerbahçe Sözlük</h1>
                                        </td>
                                    </tr>

                                    <!-- Content -->
                                    <tr>
                                        <td style="padding: 40px;">
                                            <h2 style="margin: 0 0 20px; font-size: 22px; color: #e0e0e0;">Şifre Sıfırlama Talebi</h2>

                                            <p style="margin: 0 0 20px; font-size: 16px; line-height: 1.6; color: #b0b0b0;">
                                                Merhaba <strong style="color: #e0e0e0;">%s</strong>,
                                            </p>

                                            <p style="margin: 0 0 30px; font-size: 16px; line-height: 1.6; color: #b0b0b0;">
                                                Fenerbahçe Sözlük hesabınız için şifre sıfırlama talebinde bulundunuz.
                                                Şifrenizi sıfırlamak için aşağıdaki butona tıklayın:
                                            </p>

                                            <!-- Button -->
                                            <table cellpadding="0" cellspacing="0" border="0" width="100%%">
                                                <tr>
                                                    <td align="center" style="padding: 10px 0 30px;">
                                                        <a href="%s" style="display: inline-block; padding: 16px 40px; background: linear-gradient(135deg, #d4c84a 0%%, #b8ae3d 100%%); color: #0f0f1a; font-size: 16px; font-weight: 600; text-decoration: none; border-radius: 8px; box-shadow: 0 4px 15px rgba(212, 200, 74, 0.3);">
                                                            Şifremi Sıfırla
                                                        </a>
                                                    </td>
                                                </tr>
                                            </table>

                                            <p style="margin: 0 0 15px; font-size: 14px; line-height: 1.6; color: #888;">
                                                Bu link <strong style="color: #d4c84a;">1 saat</strong> içinde geçerliliğini yitirecektir.
                                            </p>

                                            <p style="margin: 0; font-size: 14px; line-height: 1.6; color: #888;">
                                                Eğer bu talebi siz yapmadıysanız, bu emaili görmezden gelebilirsiniz.
                                                Hesabınız güvende kalacaktır.
                                            </p>
                                        </td>
                                    </tr>

                                    <!-- Footer -->
                                    <tr>
                                        <td style="padding: 30px 40px; text-align: center; border-top: 1px solid rgba(212, 200, 74, 0.1); background: rgba(0,0,0,0.2); border-radius: 0 0 16px 16px;">
                                            <p style="margin: 0 0 10px; font-size: 12px; color: #666;">
                                                Bu email otomatik olarak gönderilmiştir, lütfen yanıtlamayınız.
                                            </p>
                                            <p style="margin: 0; font-size: 12px; color: #555;">
                                                © 2025 Fenerbahçe Sözlük. Tüm hakları saklıdır.
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """
                .formatted(username, resetLink);
    }
}
