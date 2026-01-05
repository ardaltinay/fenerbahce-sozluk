package net.fenerbahcesozluk.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.fenerbahcesozluk.dto.ContactRequest;
import net.fenerbahcesozluk.exception.RateLimitExceededException;
import net.fenerbahcesozluk.service.EmailService;
import net.fenerbahcesozluk.service.RateLimitService;
import net.fenerbahcesozluk.util.HttpUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final EmailService emailService;
    private final RateLimitService rateLimitService;

    @PostMapping
    public ResponseEntity<Map<String, String>> sendContactMessage(@Valid @RequestBody ContactRequest request,
            HttpServletRequest httpRequest) {

        String clientIp = HttpUtils.getClientIp(httpRequest);

        // Rate limit: max 3 contact messages per hour
        if (!rateLimitService.isAllowed("contact", clientIp)) {
            long retryAfter = rateLimitService.getSecondsUntilReset("contact", clientIp);
            throw new RateLimitExceededException(
                    "Çok fazla mesaj gönderdiniz. Lütfen " + retryAfter + " saniye sonra tekrar deneyin.", retryAfter);
        }

        emailService.sendContactEmail(request);

        return ResponseEntity
                .ok(Map.of("message", "Mesajınız başarıyla gönderildi. En kısa sürede size dönüş yapacağız."));
    }

}
