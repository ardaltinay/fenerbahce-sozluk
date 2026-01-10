package net.fenerbahcesozluk.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.fenerbahcesozluk.repository.MessageRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageCleanupService {

    private final MessageRepository messageRepository;

    /**
     * Her gün gece 3'te 1 aydan eski mesajları siler. Cron: saniye dakika saat gün
     * ay gün-of-week
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupOldMessages() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusMonths(1);
        int deletedCount = messageRepository.deleteMessagesOlderThan(cutoffDate);
        log.info("Cleanup: {} adet eski mesaj silindi (önceki tarih: {})", deletedCount, cutoffDate);
    }
}
