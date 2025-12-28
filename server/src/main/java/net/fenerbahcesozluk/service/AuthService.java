package net.fenerbahcesozluk.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.fenerbahcesozluk.dto.AuthResponse;
import net.fenerbahcesozluk.dto.ForgotPasswordRequest;
import net.fenerbahcesozluk.dto.LoginRequest;
import net.fenerbahcesozluk.dto.RegisterRequest;
import net.fenerbahcesozluk.dto.ResetPasswordRequest;
import net.fenerbahcesozluk.entity.PasswordResetToken;
import net.fenerbahcesozluk.entity.User;
import net.fenerbahcesozluk.enums.Role;
import net.fenerbahcesozluk.exception.BusinessException;
import net.fenerbahcesozluk.repository.PasswordResetTokenRepository;
import net.fenerbahcesozluk.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  private final EmailService emailService;

  public AuthResponse register(RegisterRequest request) {
    // Check if username already exists
    if (userRepository.existsByUsername(request.getUsername())) {
      throw new BusinessException("Bu kullanıcı adı zaten kullanılıyor", HttpStatus.CONFLICT);
    }

    // Check if email already exists
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new BusinessException("Bu email adresi zaten kullanılıyor", HttpStatus.CONFLICT);
    }

    // Create new user
    var user = User.builder()
        .username(request.getUsername())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .role(Role.USER)
        .build();

    userRepository.save(user);

    // Generate JWT token
    var token = jwtService.generateToken(user);

    return AuthResponse.builder()
        .token(token)
        .username(user.getUsername())
        .email(user.getEmail())
        .role(user.getRole().name())
        .message("Kayıt başarılı")
        .build();
  }

  public AuthResponse login(LoginRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.getUsername(),
            request.getPassword()));

    var user = userRepository.findByUsername(request.getUsername())
        .or(() -> userRepository.findByEmail(request.getUsername()))
        .orElseThrow(() -> new BusinessException("Kullanıcı bulunamadı", HttpStatus.NOT_FOUND));

    if (user.getBannedUntil() != null && user.getBannedUntil().isAfter(java.time.LocalDateTime.now())) {
      String reason = user.getBanReason() != null ? user.getBanReason() : "Belirtilmedi";

      java.time.LocalDateTime bannedUntil = user.getBannedUntil();
      String dateStr;

      // Check if banned for more than 50 years (indefinite)
      if (bannedUntil.getYear() > java.time.LocalDateTime.now().getYear() + 50) {
        dateStr = "Süresiz";
      } else {
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        dateStr = bannedUntil.format(formatter);
      }

      throw new BusinessException(
          String.format("Hesabınız yasaklanmıştır.\nSebep: %s\nYasak Bitiş: %s", reason, dateStr),
          HttpStatus.FORBIDDEN);
    }

    // Generate token with remember me support
    var token = jwtService.generateToken(user, request.isRememberMe());
    long expiresIn = jwtService.getExpirationDuration(request.isRememberMe());

    return AuthResponse.builder()
        .token(token)
        .username(user.getUsername())
        .email(user.getEmail())
        .role(user.getRole().name())
        .expiresIn(expiresIn)
        .message("Giriş başarılı")
        .build();
  }

  @Transactional
  public void forgotPassword(ForgotPasswordRequest request) {
    var userOptional = userRepository.findByEmail(request.getEmail());

    // Always return success to prevent email enumeration attacks
    if (userOptional.isEmpty()) {
      log.warn("Password reset requested for non-existent email: {}", request.getEmail());
      return;
    }

    var user = userOptional.get();

    // Delete any existing tokens for this user
    passwordResetTokenRepository.deleteByUser(user);

    // Generate new token
    String token = UUID.randomUUID().toString();
    var resetToken = PasswordResetToken.builder()
        .token(token)
        .user(user)
        .expiryDate(LocalDateTime.now().plusHours(1))
        .used(false)
        .build();

    passwordResetTokenRepository.save(resetToken);

    // Send email
    emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), token);

    log.info("Password reset token created for user: {}", user.getUsername());
  }

  @Transactional
  public void resetPassword(ResetPasswordRequest request) {
    var tokenEntity = passwordResetTokenRepository.findByTokenAndUsedFalse(request.getToken())
        .orElseThrow(() -> new BusinessException("Geçersiz veya kullanılmış token", HttpStatus.BAD_REQUEST));

    if (tokenEntity.isExpired()) {
      throw new BusinessException("Token süresi dolmuş. Lütfen yeni bir şifre sıfırlama talebi oluşturun.",
          HttpStatus.BAD_REQUEST);
    }

    var user = tokenEntity.getUser();

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);

    // Mark token as used
    tokenEntity.setUsed(true);
    passwordResetTokenRepository.save(tokenEntity);

    log.info("Password reset successful for user: {}", user.getUsername());
  }

  public boolean validateResetToken(String token) {
    var tokenEntity = passwordResetTokenRepository.findByTokenAndUsedFalse(token);
    return tokenEntity.isPresent() && tokenEntity.get().isValid();
  }
}
