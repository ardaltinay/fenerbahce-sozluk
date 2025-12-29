package net.fenerbahcesozluk.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import net.fenerbahcesozluk.entity.Topic;
import net.fenerbahcesozluk.repository.TopicRepository;
import net.fenerbahcesozluk.service.TransfermarktService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/transfermarkt")
@RequiredArgsConstructor
public class TransfermarktController {

    private final TransfermarktService transfermarktService;
    private final TopicRepository topicRepository;

    @GetMapping("/search/players/{name}")
    public ResponseEntity<?> searchPlayers(@PathVariable String name) {
        JsonNode result = transfermarktService.searchPlayers(name);
        if (result == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Search failed"));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search/clubs/{name}")
    public ResponseEntity<?> searchClubs(@PathVariable String name) {
        JsonNode result = transfermarktService.searchClubs(name);
        if (result == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Search failed"));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/player/{id}")
    public ResponseEntity<?> getPlayerProfile(@PathVariable String id) {
        JsonNode result = transfermarktService.getPlayerProfile(id);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/club/{id}")
    public ResponseEntity<?> getClubProfile(@PathVariable String id) {
        JsonNode result = transfermarktService.getClubProfile(id);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/topics/{topicId}/link")
    public ResponseEntity<?> linkTransfermarkt(@PathVariable UUID topicId, @RequestBody Map<String, String> request) {

        String type = request.get("type");
        String transfermarktId = request.get("transfermarktId");

        if (type == null || transfermarktId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "type and transfermarktId required"));
        }

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new net.fenerbahcesozluk.exception.BusinessException("Topic bulunamadı",
                        org.springframework.http.HttpStatus.NOT_FOUND));

        topic.setTopicType(type);
        topic.setTransfermarktId(transfermarktId);
        topicRepository.save(topic);

        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/topics/{topicId}/unlink")
    public ResponseEntity<?> unlinkTransfermarkt(@PathVariable UUID topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new net.fenerbahcesozluk.exception.BusinessException("Topic bulunamadı",
                        org.springframework.http.HttpStatus.NOT_FOUND));

        topic.setTopicType(null);
        topic.setTransfermarktId(null);
        topicRepository.save(topic);

        return ResponseEntity.ok(Map.of("success", true));
    }
}
