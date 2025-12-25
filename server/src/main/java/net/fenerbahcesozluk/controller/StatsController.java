package net.fenerbahcesozluk.controller;

import lombok.RequiredArgsConstructor;
import net.fenerbahcesozluk.dto.StatsResponse;
import net.fenerbahcesozluk.service.StatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

  private final StatsService statsService;

  @GetMapping
  public ResponseEntity<StatsResponse> getStats() {
    return ResponseEntity.ok(statsService.getStats());
  }
}
