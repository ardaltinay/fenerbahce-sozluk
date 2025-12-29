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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public List<Map<String, Object>> searchUsers(String query) {
        List<User> users = userRepository.findByUsernameContainingIgnoreCaseAndIsActiveTrue(query);
        List<Map<String, Object>> results = new ArrayList<>();
        for (User user : users) {
            Map<String, Object> userMap = new HashMap<>();
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
    public void suspendOwnAccount(User currentUser, String password) {
        // Verify password
        if (!passwordEncoder.matches(password, currentUser.getPassword())) {
            throw new BusinessException("Şifre yanlış", HttpStatus.BAD_REQUEST);
        }

        // Admins cannot suspend their own account this way
        if (currentUser.getRole().equals(Role.ADMIN)) {
            throw new BusinessException("Admin hesabı bu şekilde askıya alınamaz", HttpStatus.FORBIDDEN);
        }

        // Soft delete - deactivate account
        currentUser.setActive(false);
        userRepository.save(currentUser);
    }

    @Transactional
    public void suspendUser(UUID userId, User currentUser) {
        // Only ADMIN can suspend users
        if (!currentUser.getRole().equals(Role.ADMIN)) {
            throw new BusinessException("Bu işlem için yetkiniz yok", HttpStatus.FORBIDDEN);
        }

        // Cannot suspend yourself
        if (userId.equals(currentUser.getId())) {
            throw new BusinessException("Kendi hesabınızı askıya alamazsınız", HttpStatus.BAD_REQUEST);
        }

        User userToSuspend = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Kullanıcı bulunamadı", HttpStatus.NOT_FOUND));

        // Cannot suspend other admins
        if (userToSuspend.getRole().equals(Role.ADMIN)) {
            throw new BusinessException("Admin kullanıcılar askıya alınamaz", HttpStatus.FORBIDDEN);
        }

        // Soft delete - set inactive
        userToSuspend.setActive(false);
        userRepository.save(userToSuspend);
    }

    @Transactional
    public void banUser(UUID userId, long durationSeconds, String reason, User currentUser) {
        // ADMIN or MODERATOR can ban users
        if (!currentUser.getRole().equals(Role.ADMIN) && !currentUser.getRole().equals(Role.MODERATOR)) {
            throw new BusinessException("Bu işlem için yetkiniz yok", HttpStatus.FORBIDDEN);
        }

        User userToBan = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Kullanıcı bulunamadı", HttpStatus.NOT_FOUND));

        // Cannot ban Admins
        if (userToBan.getRole().equals(Role.ADMIN)) {
            throw new BusinessException("Admin kullanıcılar yasaklanamaz", HttpStatus.FORBIDDEN);
        }

        // Moderators cannot ban other Moderators
        if (currentUser.getRole().equals(Role.MODERATOR) && userToBan.getRole().equals(Role.MODERATOR)) {
            throw new BusinessException("Moderatörler diğer moderatörleri yasaklayamaz", HttpStatus.FORBIDDEN);
        }

        userToBan.setBannedUntil(java.time.LocalDateTime.now().plusSeconds(durationSeconds));
        userToBan.setBanReason(reason);
        userRepository.save(userToBan);
    }

    @Transactional
    public void unbanUser(UUID userId, User currentUser) {
        // ADMIN or MODERATOR can unban users
        if (!currentUser.getRole().equals(Role.ADMIN) && !currentUser.getRole().equals(Role.MODERATOR)) {
            throw new BusinessException("Bu işlem için yetkiniz yok", HttpStatus.FORBIDDEN);
        }

        User userToUnban = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Kullanıcı bulunamadı", HttpStatus.NOT_FOUND));

        if (currentUser.getRole().equals(Role.MODERATOR)
                && (userToUnban.getRole().equals(Role.ADMIN) || userToUnban.getRole().equals(Role.MODERATOR))) {
            throw new BusinessException("Bu işlem için yetkiniz yok", HttpStatus.FORBIDDEN);
        }

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
