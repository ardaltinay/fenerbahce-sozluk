package net.fenerbahcesozluk.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.fenerbahcesozluk.dto.EntryRequest;
import net.fenerbahcesozluk.dto.EntryResponse;
import net.fenerbahcesozluk.entity.User;
import net.fenerbahcesozluk.service.EntryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/entries")
@RequiredArgsConstructor
public class EntryController {

  private final EntryService entryService;

  @GetMapping("/topic/{topicId}")
  public ResponseEntity<Page<EntryResponse>> getEntriesByTopic(
      @PathVariable UUID topicId,
      @AuthenticationPrincipal User currentUser,
      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
    return ResponseEntity.ok(entryService.getEntriesByTopic(topicId, currentUser, pageable));
  }

  @GetMapping("/author/{authorId}")
  public ResponseEntity<Page<EntryResponse>> getEntriesByAuthor(
      @PathVariable UUID authorId,
      @AuthenticationPrincipal User currentUser,
      @PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(entryService.getEntriesByAuthor(authorId, currentUser, pageable));
  }

  @GetMapping("/popular")
  public ResponseEntity<Page<EntryResponse>> getPopularEntries(
      @AuthenticationPrincipal User currentUser,
      @PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(entryService.getPopularEntries(currentUser, pageable));
  }

  @GetMapping("/history")
  public ResponseEntity<Page<EntryResponse>> getHistoryEntries(
      @AuthenticationPrincipal User currentUser,
      @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    return ResponseEntity.ok(entryService.getHistoryEntries(currentUser, pageable));
  }

  @GetMapping("/latest")
  public ResponseEntity<Page<EntryResponse>> getLatestEntries(
      @AuthenticationPrincipal User currentUser,
      @PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(entryService.getLatestEntries(currentUser, pageable));
  }

  @GetMapping("/{id}")
  public ResponseEntity<EntryResponse> getEntryById(
      @PathVariable UUID id,
      @AuthenticationPrincipal User currentUser) {
    return ResponseEntity.ok(entryService.getEntryById(id, currentUser));
  }

  @PostMapping
  public ResponseEntity<EntryResponse> createEntry(
      @Valid @RequestBody EntryRequest request,
      @AuthenticationPrincipal User currentUser) {
    return ResponseEntity.ok(entryService.createEntry(request, currentUser));
  }

  @PutMapping("/{id}")
  public ResponseEntity<EntryResponse> updateEntry(
      @PathVariable UUID id,
      @RequestBody Map<String, String> body,
      @AuthenticationPrincipal User currentUser) {
    String content = body.get("content");
    return ResponseEntity.ok(entryService.updateEntry(id, content, currentUser));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteEntry(
      @PathVariable UUID id,
      @AuthenticationPrincipal User currentUser) {
    entryService.deleteEntry(id, currentUser);
    return ResponseEntity.noContent().build();
  }
}
