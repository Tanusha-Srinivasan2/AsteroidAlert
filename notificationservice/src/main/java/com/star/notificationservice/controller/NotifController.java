package com.star.notificationservice.controller;

import com.star.notificationservice.entity.Notification;
import com.star.notificationservice.entity.User;
import com.star.notificationservice.service.NotificationService;
import com.star.notificationservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@Slf4j
public class NotifController {
    private final UserService userService;
    private final NotificationService notificationService;

    @Autowired
    public NotifController(UserService userService, NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @PostMapping("/auth/login-sync")
    @Transactional
    public ResponseEntity<User> loginSync(@AuthenticationPrincipal Jwt jwt) {
        String googleId = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        String fullName = jwt.getClaimAsString("name");

        log.info("Login sync initiated for Google ID: {}", googleId);

        User syncedUser = userService.findOrCreateUserByGoogleId(googleId, email, fullName);

        return new ResponseEntity<>(syncedUser, HttpStatus.OK);
    }

    /**
     * Retrieves the notification settings for the authenticated user.
     *
     * @param jwt The authenticated user's JWT.
     * @return The User entity containing their settings.
     */
    @GetMapping("/users/settings") // NEW: GET endpoint for user settings
    public ResponseEntity<User> getUserSettings(@AuthenticationPrincipal Jwt jwt) {
        String googleId = jwt.getSubject();
        log.info("Fetching settings for Google ID: {}", googleId);

        Optional<User> userOptional = userService.getUserByGoogleId(googleId);

        if (userOptional.isPresent()) {
            return new ResponseEntity<>(userOptional.get(), HttpStatus.OK);
        } else {
            log.warn("Attempted to fetch settings for non-existent user with Google ID: {}", googleId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/users/settings")
    public ResponseEntity<User> updateUserSettings(@AuthenticationPrincipal Jwt jwt, @RequestBody Map<String, Object> updates) {
        String googleId = jwt.getSubject();
        log.info("Updating settings for Google ID: {}", googleId);

        User updatedUser = userService.updateNotificationSettings(googleId, updates);

        if (updatedUser != null) {
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } else {
            log.warn("Attempted to update settings for non-existent user with Google ID: {}", googleId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/notifications/history")
    public ResponseEntity<List<Notification>> getNotificationHistory(@AuthenticationPrincipal Jwt jwt) {
        String googleId = jwt.getSubject();
        log.info("Fetching notification history for Google ID: {}", googleId);

        List<Notification> history = notificationService.getNotificationsForUser(googleId);

        if (history.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(history, HttpStatus.OK);
        }
    }

    @GetMapping("/notifications/{notificationId}")
    public ResponseEntity<Notification> getNotificationDetails(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long notificationId) {
        String googleId = jwt.getSubject();
        log.info("Fetching details for notification ID: {} for user: {}", notificationId, googleId);

        Optional<Notification> notificationOptional = notificationService.getNotificationById(notificationId);

        if (notificationOptional.isPresent()) {
            Notification notification = notificationOptional.get();
            return new ResponseEntity<>(notification, HttpStatus.OK);
        } else {
            log.warn("Notification with ID {} not found.", notificationId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
