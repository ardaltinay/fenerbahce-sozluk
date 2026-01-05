package net.fenerbahcesozluk.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.fenerbahcesozluk.dto.VoteRequest;
import net.fenerbahcesozluk.entity.User;
import net.fenerbahcesozluk.exception.RateLimitExceededException;
import net.fenerbahcesozluk.service.RateLimitService;
import net.fenerbahcesozluk.service.VoteService;
import net.fenerbahcesozluk.util.HttpUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/votes")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;
    private final RateLimitService rateLimitService;

    @PostMapping
    public ResponseEntity<Void> vote(@Valid @RequestBody VoteRequest request,
            @AuthenticationPrincipal User currentUser, HttpServletRequest httpRequest) {
        String clientIp = HttpUtils.getClientIp(httpRequest);

        if (!rateLimitService.isAllowed("vote", clientIp)) {
            long retryAfter = rateLimitService.getSecondsUntilReset("vote", clientIp);
            throw new RateLimitExceededException(
                    "Çok fazla oy kullandınız. Lütfen " + retryAfter + " saniye sonra tekrar deneyin.", retryAfter);
        }

        voteService.vote(request, currentUser);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{entryId}")
    public ResponseEntity<Void> removeVote(@PathVariable UUID entryId, @AuthenticationPrincipal User currentUser) {
        voteService.removeVote(entryId, currentUser);
        return ResponseEntity.noContent().build();
    }

}
