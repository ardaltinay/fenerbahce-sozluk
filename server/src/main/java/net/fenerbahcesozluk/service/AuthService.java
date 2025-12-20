package net.fenerbahcesozluk.service;

import lombok.RequiredArgsConstructor;
import net.fenerbahcesozluk.dto.AuthResponse;
import net.fenerbahcesozluk.dto.LoginRequest;
import net.fenerbahcesozluk.dto.RegisterRequest;
import net.fenerbahcesozluk.entity.User;
import net.fenerbahcesozluk.enums.Role;
import net.fenerbahcesozluk.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  public AuthResponse register(RegisterRequest request) {
    // Check if username already exists
    if (userRepository.existsByUsername(request.getUsername())) {
      throw new RuntimeException("Bu kullanıcı adı zaten kullanılıyor");
    }

    // Check if email already exists
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new RuntimeException("Bu email adresi zaten kullanılıyor");
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
        .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

    var token = jwtService.generateToken(user);

    return AuthResponse.builder()
        .token(token)
        .username(user.getUsername())
        .email(user.getEmail())
        .role(user.getRole().name())
        .message("Giriş başarılı")
        .build();
  }
}
