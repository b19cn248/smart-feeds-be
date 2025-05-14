package com.olh.feeds.service.impl;

import com.olh.feeds.dao.entity.User;
import com.olh.feeds.dao.repository.UserRepository;
import com.olh.feeds.dto.request.user.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public User createUser(UserDTO userDTO) {
        // Kiểm tra user đã tồn tại bằng keycloakId
        if (userDTO.getKeycloakId() != null && !userDTO.getKeycloakId().isEmpty()) {
            Optional<User> existingUser = userRepository.findByKeycloakId(userDTO.getKeycloakId());
            if (existingUser.isPresent()) {
                return existingUser.get();
            }
        }

        // Kiểm tra user đã tồn tại bằng email
        Optional<User> userByEmail = userRepository.findByEmail(userDTO.getEmail());
        if (userByEmail.isPresent()) {
            return userByEmail.get();
        }

        // Tạo user mới
        User user = new User();
        user.setEmail(userDTO.getEmail());
        user.setUsername(userDTO.getUsername());
        user.setName(userDTO.getName());
        user.setLocation(userDTO.getLocation());
        user.setPreferences(userDTO.getPreferences());
        user.setPoints(userDTO.getPoints() != null ? userDTO.getPoints() : 0);
        user.setKeycloakId(userDTO.getKeycloakId());
        user.setSettingsId(1L);
        user.setSubscriptionId(1L);
        user.setReadLaterListId(1L);

        // Thiết lập thông tin audit
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setCreatedBy(userDTO.getCreatedBy() != null ? userDTO.getCreatedBy() : "SYSTEM");
        user.setUpdatedBy(userDTO.getUpdatedBy() != null ? userDTO.getUpdatedBy() : "SYSTEM");
        user.setIsDeleted(false);

        return userRepository.save(user);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByKeycloakId(String keycloakId) {
        return userRepository.existsByKeycloakId(keycloakId);
    }
}
