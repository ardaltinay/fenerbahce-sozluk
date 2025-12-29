package net.fenerbahcesozluk.service;

import lombok.RequiredArgsConstructor;
import net.fenerbahcesozluk.dto.EntryResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Broadcast new entry to all users viewing the topic
     */
    public void broadcastNewEntry(UUID topicId, EntryResponse entry) {
        messagingTemplate.convertAndSend("/topic/entries/" + topicId, entry);
    }

    /**
     * Broadcast sidebar update to all connected users
     */
    public void broadcastSidebarUpdate() {
        messagingTemplate.convertAndSend("/topic/sidebar", "refresh");
    }
}
