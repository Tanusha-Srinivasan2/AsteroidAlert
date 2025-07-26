package com.star.notificationservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime; // <-- ADD THIS IMPORT for more precise timestamps


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- NEW FIELD TO LINK TO USER ---
    @ManyToOne(fetch = FetchType.LAZY) // Many notifications can belong to one user
    @JoinColumn(name = "user_id", nullable = true) // Foreign key column in notification table
    private User user; // Link to the User entity

    private String asteroidName;
    private LocalDate closeAppraochDate;
    private BigDecimal missDistanceKilometers;
    private double estimatedDiameterAvgMeters;
    private boolean emailSent;

    // --- NEW FIELD FOR DETAILED ASTEROID INFO LINK ---
    // This could be a unique ID from NASA's API or your AsteroidAlert service
    private String nasaAsteroidId; // e.g., NASA's unique ID for the asteroid

    // --- OPTIONAL: Timestamp for when the notification was created/received ---
    private LocalDateTime receivedAt; // To track when the alert was processed/saved
}