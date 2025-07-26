package com.star.notificationservice.service;

import com.star.notificationservice.entity.User;
import com.star.notificationservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User findOrCreateUserByGoogleId(String googleId, String email, String fullName) {
        Optional<User> existingUser = userRepository.findByGoogleId(googleId);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setLastLoginAt(LocalDate.now());
            user.setEmail(email);
            user.setFullName(fullName);
            return userRepository.save(user);
        } else {
            User newUser = User.builder()
                    .googleId(googleId)
                    .email(email)
                    .fullName(fullName)
                    .notificationEnabled(true) // Default: enable notifications for new users
                    // --- REMOVED: alertFrequency and preferredAsteroidTypes initialization ---
                    .createdAt(LocalDate.now())
                    .lastLoginAt(LocalDate.now())
                    .build();
            return userRepository.save(newUser);
        }
    }

    @Transactional
    public User updateNotificationSettings(String googleId, Map<String, Object> updates) {
        Optional<User> userOptional = userRepository.findByGoogleId(googleId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            updates.forEach((key, value) -> {
                switch (key) {
                    case "notificationEnabled":
                        if (value instanceof Boolean) {
                            user.setNotificationEnabled((Boolean) value);
                        } else {
                            System.err.println("Invalid type for notificationEnabled: " + value.getClass().getName());
                        }
                        break;
                    // --- REMOVED: Cases for alertFrequency and preferredAsteroidTypes ---
                    default:
                        System.err.println("Unknown setting key: " + key);
                        break;
                }
            });
            user.setLastLoginAt(LocalDate.now());
            return userRepository.save(user);
        }
        return null;
    }

    public Optional<User> getUserByGoogleId(String googleId) {
        return userRepository.findByGoogleId(googleId);
    }
}