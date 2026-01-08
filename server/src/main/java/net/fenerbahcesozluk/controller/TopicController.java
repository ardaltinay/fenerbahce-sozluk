package net.fenerbahcesozluk.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.fenerbahcesozluk.dto.TopicMergeRequest;
import net.fenerbahcesozluk.dto.TopicRequest;
import net.fenerbahcesozluk.dto.TopicResponse;
import net.fenerbahcesozluk.entity.User;
import net.fenerbahcesozluk.exception.RateLimitExceededException;
import net.fenerbahcesozluk.service.RateLimitService;
import net.fenerbahcesozluk.service.TopicService;
import net.fenerbahcesozluk.util.HttpUtils;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;
    private final RateLimitService rateLimitService;

    @GetMapping
    public ResponseEntity<Page<TopicResponse>> getAllTopics(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(topicService.getAllTopics(pageable));
    }

    @GetMapping("/popular")
    public ResponseEntity<Page<TopicResponse>> getPopularTopics(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(topicService.getPopularTopics(pageable));
    }

    @GetMapping("/trends")
    public ResponseEntity<Page<TopicResponse>> getTrendingTopics(@PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(topicService.getTrendingTopics(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<TopicResponse>> searchTopics(@RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(topicService.searchTopics(keyword, pageable));
    }

    @GetMapping("/by-date/{period}")
    public ResponseEntity<Page<TopicResponse>> getTopicsByDate(@PathVariable String period,
            @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(topicService.getTopicsByDatePeriod(period, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TopicResponse> getTopicById(@PathVariable UUID id) {
        topicService.incrementViewCount(id);
        return ResponseEntity.ok(topicService.getTopicById(id));
    }

    @PostMapping
    public ResponseEntity<TopicResponse> createTopic(@Valid @RequestBody TopicRequest request,
            @AuthenticationPrincipal User currentUser, HttpServletRequest httpRequest) {
        String clientIp = HttpUtils.getClientIp(httpRequest);

        if (!rateLimitService.isAllowed("topic-create", clientIp)) {
            long retryAfter = rateLimitService.getSecondsUntilReset("topic-create", clientIp);
            throw new RateLimitExceededException(
                    "Çok fazla başlık açtınız. Lütfen " + retryAfter + " saniye sonra tekrar deneyin.", retryAfter);
        }

        return ResponseEntity.ok(topicService.createTopic(request, currentUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTopic(@PathVariable UUID id, @RequestParam(required = false) String reason,
            @AuthenticationPrincipal User currentUser) {
        topicService.deleteTopic(id, reason, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{sourceId}/merge")
    public ResponseEntity<Map<String, Object>> mergeTopic(
            @PathVariable UUID sourceId,
            @Valid @RequestBody TopicMergeRequest request,
            @AuthenticationPrincipal User currentUser) {
        topicService.mergeTopic(sourceId, request.getTargetTopicId(), currentUser);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Başlık başarıyla birleştirildi",
                "targetTopicId", request.getTargetTopicId()));
    }

}
