package net.fenerbahcesozluk.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.fenerbahcesozluk.dto.ConversationResponse;
import net.fenerbahcesozluk.dto.MessageRequest;
import net.fenerbahcesozluk.dto.MessageResponse;
import net.fenerbahcesozluk.entity.User;
import net.fenerbahcesozluk.service.MessageService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

  private final MessageService messageService;

  /**
   * Yeni mesaj gönder
   */
  @PostMapping
  public ResponseEntity<MessageResponse> sendMessage(
      @AuthenticationPrincipal User currentUser,
      @Valid @RequestBody MessageRequest request) {
    MessageResponse response = messageService.sendMessage(currentUser, request);
    return ResponseEntity.ok(response);
  }

  /**
   * Konuşma listesini getir
   */
  @GetMapping("/conversations")
  public ResponseEntity<List<ConversationResponse>> getConversations(
      @AuthenticationPrincipal User currentUser) {
    List<ConversationResponse> conversations = messageService.getConversations(currentUser);
    return ResponseEntity.ok(conversations);
  }

  /**
   * Belirli bir kullanıcıyla olan mesajları getir
   */
  @GetMapping("/conversation/{username}")
  public ResponseEntity<Page<MessageResponse>> getMessages(
      @AuthenticationPrincipal User currentUser,
      @PathVariable String username,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "50") int size) {
    Page<MessageResponse> messages = messageService.getMessages(currentUser, username, page, size);
    return ResponseEntity.ok(messages);
  }

  /**
   * Konuşmadaki tüm mesajları okundu olarak işaretle
   */
  @PutMapping("/conversation/{username}/read")
  public ResponseEntity<Void> markConversationAsRead(
      @AuthenticationPrincipal User currentUser,
      @PathVariable String username) {
    messageService.markConversationAsRead(currentUser, username);
    return ResponseEntity.ok().build();
  }

  /**
   * Tek bir mesajı okundu olarak işaretle
   */
  @PutMapping("/{id}/read")
  public ResponseEntity<Void> markAsRead(
      @AuthenticationPrincipal User currentUser,
      @PathVariable UUID id) {
    messageService.markAsRead(currentUser, id);
    return ResponseEntity.ok().build();
  }

  /**
   * Toplam okunmamış mesaj sayısını getir
   */
  @GetMapping("/unread-count")
  public ResponseEntity<Map<String, Integer>> getUnreadCount(
      @AuthenticationPrincipal User currentUser) {
    int count = messageService.getUnreadCount(currentUser);
    return ResponseEntity.ok(Map.of("count", count));
  }

  /**
   * Mesajı sil
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteMessage(
      @AuthenticationPrincipal User currentUser,
      @PathVariable UUID id) {
    messageService.deleteMessage(currentUser, id);
    return ResponseEntity.ok().build();
  }
}
