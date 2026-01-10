package net.fenerbahcesozluk.controller;

import lombok.RequiredArgsConstructor;
import net.fenerbahcesozluk.entity.Topic;
import net.fenerbahcesozluk.exception.BusinessException;
import net.fenerbahcesozluk.repository.TopicRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/kunye")
@RequiredArgsConstructor
public class KunyeController {

    private final TopicRepository topicRepository;

    @PostMapping("/topics/{topicId}")
    public ResponseEntity<?> saveKunye(@PathVariable UUID topicId, @RequestBody Map<String, String> request) {

        String imageUrl = request.get("imageUrl");
        String kunyeData = request.get("kunyeData");

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new BusinessException("Topic bulunamadı", HttpStatus.NOT_FOUND));

        topic.setKunyeImageUrl(imageUrl);
        topic.setKunyeData(kunyeData);
        topicRepository.save(topic);

        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/topics/{topicId}")
    public ResponseEntity<?> deleteKunye(@PathVariable UUID topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new BusinessException("Topic bulunamadı", HttpStatus.NOT_FOUND));

        topic.setKunyeImageUrl(null);
        topic.setKunyeData(null);
        topicRepository.save(topic);

        return ResponseEntity.ok(Map.of("success", true));
    }
}
