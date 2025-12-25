package net.fenerbahcesozluk.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.fenerbahcesozluk.dto.ChangePasswordRequest;
import net.fenerbahcesozluk.dto.DeleteAccountRequest;
import net.fenerbahcesozluk.entity.User;
import net.fenerbahcesozluk.repository.EntryRepository;
import net.fenerbahcesozluk.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final EntryRepository entryRepository;

  @GetMapping("/{username}")
  public ResponseEntity<Map<String, Object>> getUserByUsername(@PathVariable String username) {
    User user = userService.getUserByUsername(username);

    Map<String, Object> response = new HashMap<>();
    response.put("id", user.getId());
    response.put("username", user.getUsername());
    response.put("role", user.getRole().name());
    response.put("isActive", user.isActive());
    response.put("createdAt", user.getCreatedAt());
    response.put("entryCount", entryRepository.countByAuthorId(user.getId()));
    response.put("likeCount", entryRepository.sumLikeCountByAuthorId(user.getId()));
    response.put("favoriteCount", entryRepository.sumFavoriteCountByAuthorId(user.getId()));

    return ResponseEntity.ok(response);
  }

  @GetMapping("/me")
  public ResponseEntity<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal User currentUser) {
    Map<String, Object> response = new HashMap<>();
    response.put("id", currentUser.getId());
    response.put("username", currentUser.getUsername());
    response.put("email", currentUser.getEmail());
    response.put("role", currentUser.getRole().name());
    response.put("createdAt", currentUser.getCreatedAt());

    return ResponseEntity.ok(response);
  }

  @PostMapping("/me/change-password")
  public ResponseEntity<Map<String, String>> changePassword(
      @Valid @RequestBody ChangePasswordRequest request,
      @AuthenticationPrincipal User currentUser) {
    userService.changePassword(currentUser, request);

    return ResponseEntity.ok(Map.of(
        "message", "Şifreniz başarıyla güncellendi"));
  }

  @PostMapping("/me/delete-account")
  public ResponseEntity<Map<String, String>> deleteOwnAccount(
      @Valid @RequestBody DeleteAccountRequest request,
      @AuthenticationPrincipal User currentUser) {
    userService.deleteOwnAccount(currentUser, request.getPassword());

    return ResponseEntity.ok(Map.of(
        "message", "Hesabınız başarıyla silindi"));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Map<String, String>> deleteUser(
      @PathVariable UUID id,
      @AuthenticationPrincipal User currentUser) {
    userService.deleteUser(id, currentUser);

    Map<String, String> response = new HashMap<>();
    response.put("message", "Kullanıcı başarıyla kaldırıldı");
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{id}/ban")
  public ResponseEntity<Map<String, String>> banUser(
      @PathVariable UUID id,
      @RequestBody Map<String, Object> body,
      @AuthenticationPrincipal User currentUser) {
    long durationSeconds = Long.parseLong(body.get("duration").toString());
    String reason = (String) body.get("reason");

    userService.banUser(id, durationSeconds, reason, currentUser);

    Map<String, String> response = new HashMap<>();
    response.put("message", "Kullanıcı yasaklandı");
    return ResponseEntity.ok(response);
  }
}
