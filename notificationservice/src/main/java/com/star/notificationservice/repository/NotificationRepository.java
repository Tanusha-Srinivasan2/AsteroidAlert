package com.star.notificationservice.repository;

import com.star.notificationservice.entity.Notification;
import com.star.notificationservice.entity.User; // <-- ADD THIS IMPORT
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByEmailSent(boolean b);

    // --- NEW METHOD FOR NOTIFICATION HISTORY ---
    // Find all notifications associated with a specific user, ordered by receivedAt descending
    List<Notification> findByUserOrderByReceivedAtDesc(User user);

    // If you prefer to fetch by user ID directly, you can also add:
    // List<Notification> findByUserIdOrderByReceivedAtDesc(Long userId);
}