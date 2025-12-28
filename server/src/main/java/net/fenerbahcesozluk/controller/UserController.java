package net.fenerbahcesozluk.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.fenerbahcesozluk.dto.ChangePasswordRequest;
import net.fenerbahcesozluk.dto.DeleteAccountRequest;
import net.fenerbahcesozluk.entity.User;
import net.fenerbahcesozluk.enums.VoteType;
import net.fenerbahcesozluk.repository.EntryRepository;
import net.fenerbahcesozluk.repository.VoteRepository;
import net.fenerbahcesozluk.service.EntryService;
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
  private final VoteRepository voteRepository;
  private final EntryService entryService;

  @GetMapping("/search")
  public ResponseEntity<java.util.List<Map<String, Object>>> searchUsers(@RequestParam String q) {
    java.util.List<Map<String, Object>> results = userService.searchUsers(q);
    return ResponseEntity.ok(results);
  }

  @GetMapping("/{username}")
  public ResponseEntity<Map<String, Object>> getUserByUsername(@PathVariable String username) {
    User user = userService.getUserByUsername(username);

    Map<String, Object> response = new HashMap<>();
    response.put("id", user.getId());
    response.put("username", user.getUsername());
    response.put("role", user.getRole().name());
    response.put("isActive", user.isActive());
    response.put("bannedUntil", user.getBannedUntil());
    response.put("banReason", user.getBanReason());
    response.put("createdAt", user.getCreatedAt());
    response.put("entryCount", entryRepository.countByAuthorId(user.getId()));
    response.put("likeCount", voteRepository.countByEntryAuthorIdAndVoteType(user.getId(), VoteType.LIKE));
    response.put("dislikeCount", voteRepository.countByEntryAuthorIdAndVoteType(user.getId(), VoteType.DISLIKE));
    response.put("favoriteCount", voteRepository.countByEntryAuthorIdAndVoteType(user.getId(), VoteType.FAVORITE));

    return ResponseEntity.ok(response);
  }

  @GetMapping("/{username}/favorites")
  public ResponseEntity<?> getUserFavorites(
      @PathVariable String username,
      @AuthenticationPrincipal User currentUser) {
    User user = userService.getUserByUsername(username);
    return ResponseEntity.ok(entryService.getFavoriteEntriesByUserId(user.getId(), currentUser));
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

  @PostMapping("/me/suspend-account")
  public ResponseEntity<Map<String, String>> suspendOwnAccount(
      @Valid @RequestBody DeleteAccountRequest request,
      @AuthenticationPrincipal User currentUser) {
    // Reusing DeleteAccountRequest for password validation
    userService.suspendOwnAccount(currentUser, request.getPassword());

    return ResponseEntity.ok(Map.of(
        "message", "Hesabınız askıya alındı"));
  }

  @PostMapping("/{id}/suspend")
  public ResponseEntity<Map<String, String>> suspendUser(
      @PathVariable UUID id,
      @AuthenticationPrincipal User currentUser) {
    userService.suspendUser(id, currentUser);

    Map<String, String> response = new HashMap<>();
    response.put("message", "Kullanıcı hesabı askıya alındı");
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

  @PostMapping("/{id}/promote")
  public ResponseEntity<Map<String, String>> promoteToModerator(
      @PathVariable UUID id,
      @AuthenticationPrincipal User currentUser) {
    userService.promoteToModerator(id, currentUser);

    Map<String, String> response = new HashMap<>();
    response.put("message", "Kullanıcı moderatör yapıldı");
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{id}/demote")
  public ResponseEntity<Map<String, String>> demoteToUser(
      @PathVariable UUID id,
      @AuthenticationPrincipal User currentUser) {
    userService.demoteToUser(id, currentUser);

    Map<String, String> response = new HashMap<>();
    response.put("message", "Kullanıcı normal kullanıcı yapıldı");
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{id}/unban")
  public ResponseEntity<Map<String, String>> unbanUser(
      @PathVariable UUID id,
      @AuthenticationPrincipal User currentUser) {
    userService.unbanUser(id, currentUser);

    Map<String, String> response = new HashMap<>();
    response.put("message", "Kullanıcı yasağı kaldırıldı");
    return ResponseEntity.ok(response);
  }
}
