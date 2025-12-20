package net.fenerbahcesozluk.service;

import lombok.RequiredArgsConstructor;
import net.fenerbahcesozluk.entity.User;
import net.fenerbahcesozluk.enums.Role;
import net.fenerbahcesozluk.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  public User getUserByUsername(String username) {
    return userRepository.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + username));
  }

  public User getUserById(UUID id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
  }

  @Transactional
  public void deleteUser(UUID userId, User currentUser) {
    // Only ADMIN can delete users
    if (!currentUser.getRole().equals(Role.ADMIN)) {
      throw new RuntimeException("Bu işlem için yetkiniz yok");
    }

    // Cannot delete yourself
    if (userId.equals(currentUser.getId())) {
      throw new RuntimeException("Kendi hesabınızı silemezsiniz");
    }

    User userToDelete = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

    // Cannot delete other admins
    if (userToDelete.getRole().equals(Role.ADMIN)) {
      throw new RuntimeException("Admin kullanıcılar silinemez");
    }

    // Soft delete - set inactive
    userToDelete.setActive(false);
    userRepository.save(userToDelete);
  }

  @Transactional
  public void banUser(UUID userId, User currentUser) {
    // Only ADMIN can ban users
    if (!currentUser.getRole().equals(Role.ADMIN)) {
      throw new RuntimeException("Bu işlem için yetkiniz yok");
    }

    User userToBan = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

    if (userToBan.getRole().equals(Role.ADMIN)) {
      throw new RuntimeException("Admin kullanıcılar yasaklanamaz");
    }

    // Release username for future use
    String deletedUsername = "_deleted_" + System.currentTimeMillis();
    userToBan.setUsername(deletedUsername);
    userToBan.setActive(false);
    userRepository.save(userToBan);
  }
}
