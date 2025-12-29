package net.fenerbahcesozluk.repository;

import net.fenerbahcesozluk.entity.PasswordResetToken;
import net.fenerbahcesozluk.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

  Optional<PasswordResetToken> findByTokenAndUsedFalse(String token);

  @Modifying
  @Transactional
  void deleteByUser(User user);

  @Modifying
  @Transactional
  @Query("DELETE FROM PasswordResetToken t WHERE t.expiryDate < :now")
  void deleteExpiredTokens(LocalDateTime now);
}
