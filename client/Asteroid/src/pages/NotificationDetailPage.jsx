import React, { useState, useEffect } from "react";
import { useAuth } from "../App";
import styles from "./NotificationDetailPage.module.css"; // Import the new CSS module

const NotificationDetailPage = ({ notificationId, onNavigate }) => {
  const { idToken, NOTIFICATION_SERVICE_BASE_URL } = useAuth();
  const [notification, setNotification] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchNotificationDetails = async () => {
      if (!notificationId) {
        setError("No notification selected.");
        setLoading(false);
        return;
      }
      if (!idToken) {
        setError("User not authenticated.");
        setLoading(false);
        return;
      }

      setLoading(true);
      setError(null);
      try {
        const response = await fetch(
          `${NOTIFICATION_SERVICE_BASE_URL}/notifications/${notificationId}`,
          {
            method: "GET",
            headers: {
              Authorization: `Bearer ${idToken}`,
              "Content-Type": "application/json",
            },
          }
        );

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        setNotification(data);
        console.log("Fetched notification details:", data);
      } catch (err) {
        console.error("Error fetching notification details:", err);
        setError("Failed to load details. " + err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchNotificationDetails();
  }, [notificationId, idToken, NOTIFICATION_SERVICE_BASE_URL]);

  if (loading) {
    return (
      <div className={styles.loadingMessage}>Loading asteroid details...</div>
    );
  }

  if (error) {
    return <div className={styles.errorMessage}>Error: {error}</div>;
  }

  if (!notification) {
    return (
      <div className={styles.noNotificationMessage}>
        Notification not found.
      </div>
    );
  }

  return (
    <div className={styles.detailPageContainer}>
      <div className={styles.detailCard}>
        <h2 className={styles.detailTitle}>Asteroid Details</h2>

        <div className={styles.detailContent}>
          <p>
            <span className={styles.detailLabel}>Asteroid Name:</span>{" "}
            {notification.asteroidName}
          </p>
          <p>
            <span className={styles.detailLabel}>NASA ID:</span>{" "}
            {notification.nasaAsteroidId || "N/A"}
          </p>
          <p>
            <span className={styles.detailLabel}>Close Approach Date:</span>{" "}
            {notification.closeAppraochDate || "N/A"}
          </p>
          <p>
            <span className={styles.detailLabel}>Miss Distance:</span>{" "}
            {parseFloat(notification.missDistanceKilometers).toLocaleString()}{" "}
            km
          </p>
          <p>
            <span className={styles.detailLabel}>Estimated Avg Diameter:</span>{" "}
            {notification.estimatedDiameterAvgMeters.toFixed(2)} meters
          </p>
          <p>
            <span className={styles.detailLabel}>Alert Received At:</span>{" "}
            {new Date(notification.receivedAt).toLocaleString()}
          </p>
          <p>
            <span className={styles.detailLabel}>Email Sent:</span>{" "}
            {notification.emailSent ? "Yes" : "No"}
          </p>
        </div>

        <div className={styles.backButtonContainer}>
          <button
            onClick={() => onNavigate("history")}
            className={styles.backButton}
          >
            Back to History
          </button>
        </div>
      </div>
    </div>
  );
};

export default NotificationDetailPage;
