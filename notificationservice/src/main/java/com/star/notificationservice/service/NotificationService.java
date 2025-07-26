package com.star.notificationservice.service;

import com.star.asteroidalerting.event.AsteroidCollisionEvent;
import com.star.notificationservice.entity.Notification;
import com.star.notificationservice.entity.User; // <-- ADD THIS IMPORT
import com.star.notificationservice.repository.NotificationRepository;
import com.star.notificationservice.repository.UserRepository; // <-- ADD THIS IMPORT
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <-- ADD THIS IMPORT

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime; // <-- ADD THIS IMPORT for receivedAt
import java.time.LocalTime;
import java.util.List;
import java.util.Optional; // <-- ADD THIS IMPORT

@Service
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final UserRepository userRepository; // <-- ADD THIS FIELD

    @Autowired
    public NotificationService(NotificationRepository notificationRepository, EmailService emailService, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
        this.userRepository = userRepository; // <-- INJECT UserRepository
    }

    @KafkaListener(topics = "asteroid-alert", groupId = "notification-service")
    @Transactional // Ensure saving notification is part of a transaction
    public void alertEvent(AsteroidCollisionEvent notificationEvent) {
        log.info("Received asteroid alert event:{}", notificationEvent);

        LocalDate closeApproachDate = null;
        try {
            if (!"UNKNOWN".equalsIgnoreCase(notificationEvent.getCloseApproachDate())) {
                closeApproachDate = LocalDate.parse(notificationEvent.getCloseApproachDate());
            }
        } catch (java.time.format.DateTimeParseException e) {
            log.error("Failed to parse date from Kafka message: {}", notificationEvent.getCloseApproachDate(), e);
        }

        // --- CHALLENGE: Associating Kafka events with specific users ---
        // Your current Kafka event (AsteroidCollisionEvent) does not contain user-specific information.
        // This means that when an alert comes in, you can't directly link it to a specific user
        // at this stage.
        //
        // Common approaches:
        // 1. Notifications are general: Save the notification, and then when a user requests history,
        //    you show them relevant notifications (e.g., all recent ones, or based on their preferences).
        //    This is what we'll proceed with for now.
        // 2. Kafka message contains user IDs: If the AsteroidAlert service knew which users
        //    to send alerts to (e.g., based on subscriptions), it would include user IDs in the Kafka message.
        //    Then, you'd iterate through those users here and create a Notification entry for each.
        // 3. Post-processing: Save general notification, then later, a separate job or service
        //    distributes/links them to users based on preferences.
        //
        // For simplicity, we'll save the notification as a general alert for now.
        // When fetching history, we'll show all alerts, or you'd need to add logic to filter
        // based on user preferences (e.g., if a user only wants alerts for PHAs, you'd filter
        // the list of all notifications based on the asteroid type).

        // Create the entity for the notification received
        final Notification notification = Notification.builder()
                .asteroidName(notificationEvent.getAsteroidName())
                .closeAppraochDate(closeApproachDate)
                .estimatedDiameterAvgMeters(notificationEvent.getEstimatedDiameterAvgMeters())
                .missDistanceKilometers(new BigDecimal(notificationEvent.getMissDistanceKilometers()))
                .emailSent(false)
                .nasaAsteroidId("N/A") // Placeholder, ideally from AsteroidAlert service
                .receivedAt(LocalDateTime.now()) // Timestamp when received
                // .user(null) // Cannot link to a specific user here without user info in Kafka message
                .build();

        final Notification savedNotification = notificationRepository.saveAndFlush(notification);
        log.info("Notification saved:{}", savedNotification);
    }

    @Scheduled(fixedRate = 10000)
    public void sendAlertingEmail() {
        log.info("Triggering scheduled job to send email alerts at {}", LocalTime.now());
        emailService.sendAsteroidAlertEmail();
    }

    /**
     * Retrieves a list of asteroid notifications for a given user.
     * This is used for the "view history" functionality.
     *
     * @param googleId The Google ID of the authenticated user.
     * @return A list of Notification entities for that user.
     */
    public List<Notification> getNotificationsForUser(String googleId) {
        // First, find the user based on their Google ID
        Optional<User> userOptional = userRepository.findByGoogleId(googleId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Now, fetch notifications associated with this user
            // IMPORTANT: If notifications are not directly linked to a user in `alertEvent`,
            // you'll need to adjust this logic. For now, assuming notifications are saved
            // and then filtered/displayed based on some criteria (e.g., all recent notifications).
            //
            // If `Notification` entity has a `user` field and it's populated, use:
            // return notificationRepository.findByUserOrderByReceivedAtDesc(user);
            //
            // If notifications are general and you want to show ALL recent ones to any logged-in user:
            return notificationRepository.findAll(); // Or add a method like findTopNByOrderByReceivedAtDesc()
        } else {
            log.warn("Attempted to get notifications for non-existent user with Google ID: {}", googleId);
            return List.of(); // Return empty list if user not found
        }
    }

    /**
     * Retrieves a single detailed asteroid notification by its ID.
     * This is used for the "detailed description" functionality.
     *
     * @param notificationId The ID of the notification.
     * @return An Optional containing the Notification if found.
     */
    public Optional<Notification> getNotificationById(Long notificationId) {
        return notificationRepository.findById(notificationId);
    }
}