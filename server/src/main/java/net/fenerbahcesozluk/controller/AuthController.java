package net.fenerbahcesozluk.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.fenerbahcesozluk.dto.AuthResponse;
import net.fenerbahcesozluk.dto.ForgotPasswordRequest;
import net.fenerbahcesozluk.dto.LoginRequest;
import net.fenerbahcesozluk.dto.RegisterRequest;
import net.fenerbahcesozluk.dto.ResetPasswordRequest;
import net.fenerbahcesozluk.exception.RateLimitExceededException;
import net.fenerbahcesozluk.service.AuthService;
import net.fenerbahcesozluk.service.RateLimitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final RateLimitService rateLimitService;

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(
      @Valid @RequestBody RegisterRequest request,
      HttpServletRequest httpRequest) {
    String clientIp = getClientIp(httpRequest);

    if (!rateLimitService.isAllowed("register", clientIp)) {
      long retryAfter = rateLimitService.getSecondsUntilReset("register", clientIp);
      throw new RateLimitExceededException(
          "Çok fazla kayıt denemesi. Lütfen " + retryAfter + " saniye sonra tekrar deneyin.",
          retryAfter);
    }

    return ResponseEntity.ok(authService.register(request));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(
      @Valid @RequestBody LoginRequest request,
      HttpServletRequest httpRequest) {
    String clientIp = getClientIp(httpRequest);

    if (!rateLimitService.isAllowed("login", clientIp)) {
      long retryAfter = rateLimitService.getSecondsUntilReset("login", clientIp);
      throw new RateLimitExceededException(
          "Çok fazla giriş denemesi. Lütfen " + retryAfter + " saniye sonra tekrar deneyin.",
          retryAfter);
    }

    return ResponseEntity.ok(authService.login(request));
  }

  @PostMapping("/forgot-password")
  public ResponseEntity<Map<String, String>> forgotPassword(
      @Valid @RequestBody ForgotPasswordRequest request,
      HttpServletRequest httpRequest) {
    String clientIp = getClientIp(httpRequest);

    if (!rateLimitService.isAllowed("forgot-password", clientIp)) {
      long retryAfter = rateLimitService.getSecondsUntilReset("forgot-password", clientIp);
      throw new RateLimitExceededException(
          "Çok fazla şifre sıfırlama talebi. Lütfen " + retryAfter + " saniye sonra tekrar deneyin.",
          retryAfter);
    }

    authService.forgotPassword(request);

    // Always return success message to prevent email enumeration
    return ResponseEntity.ok(Map.of(
        "message", "Eğer bu email adresi kayıtlıysa, şifre sıfırlama bağlantısı gönderildi."));
  }

  @PostMapping("/reset-password")
  public ResponseEntity<Map<String, String>> resetPassword(
      @Valid @RequestBody ResetPasswordRequest request) {
    authService.resetPassword(request);
    return ResponseEntity.ok(Map.of(
        "message", "Şifreniz başarıyla güncellendi. Yeni şifrenizle giriş yapabilirsiniz."));
  }

  @GetMapping("/validate-reset-token/{token}")
  public ResponseEntity<Map<String, Boolean>> validateResetToken(@PathVariable String token) {
    boolean isValid = authService.validateResetToken(token);
    return ResponseEntity.ok(Map.of("valid", isValid));
  }

  @GetMapping("/health")
  public ResponseEntity<String> health() {
    return ResponseEntity.ok("Auth service is running");
  }

  /**
   * Extract client IP, considering X-Forwarded-For header for proxy/CDN setups.
   */
  private String getClientIp(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      // X-Forwarded-For can contain multiple IPs, take the first one (original
      // client)
      return xForwardedFor.split(",")[0].trim();
    }
    String xRealIp = request.getHeader("X-Real-IP");
    if (xRealIp != null && !xRealIp.isEmpty()) {
      return xRealIp;
    }
    return request.getRemoteAddr();
  }
}
