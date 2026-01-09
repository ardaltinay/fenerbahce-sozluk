package net.fenerbahcesozluk.service;

import lombok.RequiredArgsConstructor;
import net.fenerbahcesozluk.dto.ConversationResponse;
import net.fenerbahcesozluk.dto.MessageRequest;
import net.fenerbahcesozluk.dto.MessageResponse;
import net.fenerbahcesozluk.entity.Message;
import net.fenerbahcesozluk.entity.User;
import net.fenerbahcesozluk.repository.MessageRepository;
import net.fenerbahcesozluk.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private final SimpMessagingTemplate messagingTemplate;

  /**
   * Yeni mesaj gönderir ve WebSocket ile alıcıya bildirir
   */
  @Transactional
  public MessageResponse sendMessage(User sender, MessageRequest request) {
    User receiver = userRepository.findByUsername(request.getReceiverUsername())
        .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + request.getReceiverUsername()));

    if (sender.getId().equals(receiver.getId())) {
      throw new RuntimeException("Kendinize mesaj gönderemezsiniz");
    }

    Message message = Message.builder()
        .sender(sender)
        .receiver(receiver)
        .content(request.getContent())
        .build();

    message = messageRepository.save(message);

    MessageResponse response = toResponse(message, sender);

    // WebSocket ile alıcıya bildir
    messagingTemplate.convertAndSendToUser(
        receiver.getUsername(),
        "/queue/messages",
        response);

    // Alıcının okunmamış sayısını güncelle
    int unreadCount = messageRepository.countTotalUnread(receiver);
    messagingTemplate.convertAndSendToUser(
        receiver.getUsername(),
        "/queue/unread-count",
        unreadCount);

    return response;
  }

  /**
   * Kullanıcının tüm konuşmalarını getirir
   */
  @Transactional(readOnly = true)
  public List<ConversationResponse> getConversations(User currentUser) {
    // İki ayrı sorgu ile partnerleri al ve birleştir
    List<User> receivers = messageRepository.findMessageReceivers(currentUser);
    List<User> senders = messageRepository.findMessageSenders(currentUser);

    // Unique partnerleri birleştir
    java.util.Set<User> partnerSet = new java.util.HashSet<>(receivers);
    partnerSet.addAll(senders);
    List<User> partners = new java.util.ArrayList<>(partnerSet);

    return partners.stream()
        .map(partner -> {
          List<Message> lastMessages = messageRepository.findLastMessages(currentUser, partner, PageRequest.of(0, 1));
          Message lastMessage = lastMessages.isEmpty() ? null : lastMessages.get(0);
          int unreadCount = messageRepository.countUnreadInConversation(partner, currentUser);

          return ConversationResponse.builder()
              .username(partner.getUsername())
              .lastMessage(lastMessage != null ? truncate(lastMessage.getContent(), 50) : "")
              .lastMessageAt(lastMessage != null ? lastMessage.getCreatedAt() : null)
              .unreadCount(unreadCount)
              .build();
        })
        .sorted(Comparator.comparing(ConversationResponse::getLastMessageAt,
            Comparator.nullsLast(Comparator.reverseOrder())))
        .collect(Collectors.toList());
  }

  /**
   * Belirli bir kullanıcıyla olan mesajları getirir
   */
  @Transactional(readOnly = true)
  public Page<MessageResponse> getMessages(User currentUser, String partnerUsername, int page, int size) {
    User partner = userRepository.findByUsername(partnerUsername)
        .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + partnerUsername));

    Pageable pageable = PageRequest.of(page, size);
    Page<Message> messages = messageRepository.findConversation(currentUser, partner, pageable);

    return messages.map(m -> toResponse(m, currentUser));
  }

  /**
   * Konuşmadaki tüm okunmamış mesajları okundu olarak işaretler
   */
  @Transactional
  public void markConversationAsRead(User currentUser, String senderUsername) {
    User sender = userRepository.findByUsername(senderUsername)
        .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + senderUsername));

    List<Message> unreadMessages = messageRepository.findUnreadMessages(sender, currentUser);

    LocalDateTime now = LocalDateTime.now();
    unreadMessages.forEach(m -> m.setReadAt(now));
    messageRepository.saveAll(unreadMessages);

    // Göndericiye mesajların okunduğunu bildir
    messagingTemplate.convertAndSendToUser(
        sender.getUsername(),
        "/queue/messages-read",
        currentUser.getUsername());
  }

  /**
   * Tek bir mesajı okundu olarak işaretler
   */
  @Transactional
  public void markAsRead(User currentUser, UUID messageId) {
    Message message = messageRepository.findById(messageId)
        .orElseThrow(() -> new RuntimeException("Mesaj bulunamadı"));

    if (!message.getReceiver().getId().equals(currentUser.getId())) {
      throw new RuntimeException("Bu mesajı okuma yetkiniz yok");
    }

    if (message.getReadAt() == null) {
      message.setReadAt(LocalDateTime.now());
      messageRepository.save(message);
    }
  }

  /**
   * Kullanıcının toplam okunmamış mesaj sayısını getirir
   */
  @Transactional(readOnly = true)
  public int getUnreadCount(User currentUser) {
    return messageRepository.countTotalUnread(currentUser);
  }

  /**
   * Mesajı siler (soft delete)
   */
  @Transactional
  public void deleteMessage(User currentUser, UUID messageId) {
    Message message = messageRepository.findById(messageId)
        .orElseThrow(() -> new RuntimeException("Mesaj bulunamadı"));

    if (message.getSender().getId().equals(currentUser.getId())) {
      message.setDeletedBySender(true);
    } else if (message.getReceiver().getId().equals(currentUser.getId())) {
      message.setDeletedByReceiver(true);
    } else {
      throw new RuntimeException("Bu mesajı silme yetkiniz yok");
    }

    // Her iki taraf da sildiyse tamamen sil
    if (message.isDeletedBySender() && message.isDeletedByReceiver()) {
      message.setDeleted(true);
    }

    messageRepository.save(message);
  }

  /**
   * Belirli bir kullanıcıyla olan konuşmayı siler (soft delete - sadece kullanıcı
   * tarafı)
   */
  @Transactional
  public void deleteConversation(User currentUser, String partnerUsername) {
    User partner = userRepository.findByUsername(partnerUsername)
        .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + partnerUsername));

    // Tüm mesajları al
    List<Message> sentMessages = messageRepository.findBySenderAndReceiverAndDeletedFalse(currentUser, partner);
    List<Message> receivedMessages = messageRepository.findBySenderAndReceiverAndDeletedFalse(partner, currentUser);

    // Gönderilen mesajları sil
    for (Message message : sentMessages) {
      message.setDeletedBySender(true);
      if (message.isDeletedByReceiver()) {
        message.setDeleted(true);
      }
    }

    // Alınan mesajları sil
    for (Message message : receivedMessages) {
      message.setDeletedByReceiver(true);
      if (message.isDeletedBySender()) {
        message.setDeleted(true);
      }
    }

    messageRepository.saveAll(sentMessages);
    messageRepository.saveAll(receivedMessages);
  }

  private MessageResponse toResponse(Message message, User currentUser) {
    return MessageResponse.builder()
        .id(message.getId())
        .senderUsername(message.getSender().getUsername())
        .receiverUsername(message.getReceiver().getUsername())
        .content(message.getContent())
        .createdAt(message.getCreatedAt())
        .readAt(message.getReadAt())
        .isOwn(message.getSender().getId().equals(currentUser.getId()))
        .isRead(message.getReadAt() != null)
        .build();
  }

  private String truncate(String text, int maxLength) {
    if (text == null)
      return "";
    return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
  }
}
