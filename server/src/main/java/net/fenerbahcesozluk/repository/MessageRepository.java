package net.fenerbahcesozluk.repository;

import net.fenerbahcesozluk.entity.Message;
import net.fenerbahcesozluk.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

  /**
   * İki kullanıcı arasındaki mesajları getirir (pagination ile)
   */
  @Query("SELECT m FROM Message m " +
      "WHERE ((m.sender = :user1 AND m.receiver = :user2 AND m.deletedBySender = false) " +
      "    OR (m.sender = :user2 AND m.receiver = :user1 AND m.deletedByReceiver = false)) " +
      "AND m.isDeleted = false " +
      "ORDER BY m.createdAt DESC")
  Page<Message> findConversation(@Param("user1") User user1, @Param("user2") User user2, Pageable pageable);

  /**
   * Kullanıcının mesaj gönderdiği kişiler
   */
  @Query("SELECT DISTINCT m.receiver FROM Message m WHERE m.sender = :user AND m.isDeleted = false")
  List<User> findMessageReceivers(@Param("user") User user);

  /**
   * Kullanıcıya mesaj gönderen kişiler
   */
  @Query("SELECT DISTINCT m.sender FROM Message m WHERE m.receiver = :user AND m.isDeleted = false")
  List<User> findMessageSenders(@Param("user") User user);

  /**
   * Belirli bir konuşmadaki son mesajı getirir
   */
  @Query("SELECT m FROM Message m " +
      "WHERE ((m.sender = :user1 AND m.receiver = :user2) " +
      "    OR (m.sender = :user2 AND m.receiver = :user1)) " +
      "AND m.isDeleted = false " +
      "ORDER BY m.createdAt DESC")
  List<Message> findLastMessages(@Param("user1") User user1, @Param("user2") User user2, Pageable pageable);

  /**
   * Belirli bir konuşmadaki okunmamış mesaj sayısı
   */
  @Query("SELECT COUNT(m) FROM Message m " +
      "WHERE m.sender = :sender AND m.receiver = :receiver " +
      "AND m.readAt IS NULL " +
      "AND m.isDeleted = false")
  int countUnreadInConversation(@Param("sender") User sender, @Param("receiver") User receiver);

  /**
   * Kullanıcının toplam okunmamış mesaj sayısı
   */
  @Query("SELECT COUNT(m) FROM Message m " +
      "WHERE m.receiver = :user " +
      "AND m.readAt IS NULL " +
      "AND m.isDeleted = false")
  int countTotalUnread(@Param("user") User user);

  /**
   * Belirli bir konuşmadaki okunmamış mesajları getirir (okundu işaretlemek için)
   */
  @Query("SELECT m FROM Message m " +
      "WHERE m.sender = :sender AND m.receiver = :receiver " +
      "AND m.readAt IS NULL " +
      "AND m.isDeleted = false")
  List<Message> findUnreadMessages(@Param("sender") User sender, @Param("receiver") User receiver);

  /**
   * 1 aydan eski mesajları siler
   */
  @Modifying
  @Query("DELETE FROM Message m WHERE m.createdAt < :cutoffDate")
  int deleteMessagesOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
}
