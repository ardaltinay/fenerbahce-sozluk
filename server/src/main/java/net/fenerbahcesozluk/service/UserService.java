package net.fenerbahcesozluk.service;

import lombok.RequiredArgsConstructor;
import net.fenerbahcesozluk.dto.ChangePasswordRequest;
import net.fenerbahcesozluk.entity.User;
import net.fenerbahcesozluk.enums.Role;
import net.fenerbahcesozluk.exception.BusinessException;
import net.fenerbahcesozluk.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public User getUserByUsername(String username) {
    return userRepository.findByUsername(username)
        .orElseThrow(() -> new BusinessException("Kullanıcı bulunamadı: " + username, HttpStatus.NOT_FOUND));
  }

  public java.util.List<java.util.Map<String, Object>> searchUsers(String query) {
    java.util.List<User> users = userRepository.findByUsernameContainingIgnoreCaseAndIsActiveTrue(query);
    java.util.List<java.util.Map<String, Object>> results = new java.util.ArrayList<>();
    for (User user : users) {
      java.util.Map<String, Object> userMap = new java.util.HashMap<>();
      userMap.put("username", user.getUsername());
      userMap.put("role", user.getRole().name());
      userMap.put("createdAt", user.getCreatedAt());
      results.add(userMap);
    }
    return results;
  }

  public User getUserById(UUID id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new BusinessException("Kullanıcı bulunamadı", HttpStatus.NOT_FOUND));
  }

  @Transactional
  public void changePassword(User currentUser, ChangePasswordRequest request) {
    // Verify current password
    if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
      throw new BusinessException("Mevcut şifre yanlış", HttpStatus.BAD_REQUEST);
    }

    // Check new password is different
    if (passwordEncoder.matches(request.getNewPassword(), currentUser.getPassword())) {
      throw new BusinessException("Yeni şifre mevcut şifre ile aynı olamaz", HttpStatus.BAD_REQUEST);
    }

    // Update password
    currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(currentUser);
  }

  @Transactional
  public void deleteOwnAccount(User currentUser, String password) {
    // Verify password
    if (!passwordEncoder.matches(password, currentUser.getPassword())) {
      throw new BusinessException("Şifre yanlış", HttpStatus.BAD_REQUEST);
    }

    // Admins cannot delete their own account this way
    if (currentUser.getRole().equals(Role.ADMIN)) {
      throw new BusinessException("Admin hesabı bu şekilde silinemez", HttpStatus.FORBIDDEN);
    }

    // Soft delete - deactivate account
    currentUser.setActive(false);
    userRepository.save(currentUser);
  }

  @Transactional
  public void deleteUser(UUID userId, User currentUser) {
    // Only ADMIN can delete users
    if (!currentUser.getRole().equals(Role.ADMIN)) {
      throw new BusinessException("Bu işlem için yetkiniz yok", HttpStatus.FORBIDDEN);
    }

    // Cannot delete yourself
    if (userId.equals(currentUser.getId())) {
      throw new BusinessException("Kendi hesabınızı silemezsiniz", HttpStatus.BAD_REQUEST);
    }

    User userToDelete = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException("Kullanıcı bulunamadı", HttpStatus.NOT_FOUND));

    // Cannot delete other admins
    if (userToDelete.getRole().equals(Role.ADMIN)) {
      throw new BusinessException("Admin kullanıcılar silinemez", HttpStatus.FORBIDDEN);
    }

    // Soft delete - set inactive
    userToDelete.setActive(false);
    userRepository.save(userToDelete);
  }

  @Transactional
  public void banUser(UUID userId, long durationSeconds, String reason, User currentUser) {
    // Only ADMIN can ban users
    if (!currentUser.getRole().equals(Role.ADMIN)) {
      throw new BusinessException("Bu işlem için yetkiniz yok", HttpStatus.FORBIDDEN);
    }

    User userToBan = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException("Kullanıcı bulunamadı", HttpStatus.NOT_FOUND));

    if (userToBan.getRole().equals(Role.ADMIN)) {
      throw new BusinessException("Admin kullanıcılar yasaklanamaz", HttpStatus.FORBIDDEN);
    }

    userToBan.setBannedUntil(java.time.LocalDateTime.now().plusSeconds(durationSeconds));
    userToBan.setBanReason(reason);
    userRepository.save(userToBan);
  }

  @Transactional
  public void unbanUser(UUID userId, User currentUser) {
    // Only ADMIN can unban users
    if (!currentUser.getRole().equals(Role.ADMIN)) {
      throw new BusinessException("Bu işlem için yetkiniz yok", HttpStatus.FORBIDDEN);
    }

    User userToUnban = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException("Kullanıcı bulunamadı", HttpStatus.NOT_FOUND));

    userToUnban.setBannedUntil(null);
    userToUnban.setBanReason(null);
    userRepository.save(userToUnban);
  }

  @Transactional
  public void promoteToModerator(UUID userId, User currentUser) {
    // Only ADMIN can promote users
    if (!currentUser.getRole().equals(Role.ADMIN)) {
      throw new BusinessException("Bu işlem için yetkiniz yok", HttpStatus.FORBIDDEN);
    }

    User userToPromote = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException("Kullanıcı bulunamadı", HttpStatus.NOT_FOUND));

    // Cannot promote yourself
    if (userId.equals(currentUser.getId())) {
      throw new BusinessException("Kendi rolünüzü değiştiremezsiniz", HttpStatus.BAD_REQUEST);
    }

    // Cannot promote admins
    if (userToPromote.getRole().equals(Role.ADMIN)) {
      throw new BusinessException("Admin kullanıcının rolü değiştirilemez", HttpStatus.FORBIDDEN);
    }

    // Already moderator
    if (userToPromote.getRole().equals(Role.MODERATOR)) {
      throw new BusinessException("Kullanıcı zaten moderatör", HttpStatus.BAD_REQUEST);
    }

    userToPromote.setRole(Role.MODERATOR);
    userRepository.save(userToPromote);
  }

  @Transactional
  public void demoteToUser(UUID userId, User currentUser) {
    // Only ADMIN can demote users
    if (!currentUser.getRole().equals(Role.ADMIN)) {
      throw new BusinessException("Bu işlem için yetkiniz yok", HttpStatus.FORBIDDEN);
    }

    User userToDemote = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException("Kullanıcı bulunamadı", HttpStatus.NOT_FOUND));

    // Cannot demote yourself
    if (userId.equals(currentUser.getId())) {
      throw new BusinessException("Kendi rolünüzü değiştiremezsiniz", HttpStatus.BAD_REQUEST);
    }

    // Cannot demote admins
    if (userToDemote.getRole().equals(Role.ADMIN)) {
      throw new BusinessException("Admin kullanıcının rolü değiştirilemez", HttpStatus.FORBIDDEN);
    }

    // Already user
    if (userToDemote.getRole().equals(Role.USER)) {
      throw new BusinessException("Kullanıcı zaten normal kullanıcı", HttpStatus.BAD_REQUEST);
    }

    userToDemote.setRole(Role.USER);
    userRepository.save(userToDemote);
  }
}
