import React, { useState, useEffect } from "react";
import { useAuth } from "../App";
import styles from "./HistoryPage.module.css"; // Import the new CSS module

const HistoryPage = ({ onSelectNotification, onNavigate }) => {
  const { idToken, NOTIFICATION_SERVICE_BASE_URL } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchHistory = async () => {
      if (!idToken) {
        setError("User not authenticated.");
        setLoading(false);
        return;
      }
      setLoading(true);
      setError(null);
      try {
        const response = await fetch(
          `${NOTIFICATION_SERVICE_BASE_URL}/notifications/history`,
          {
            method: "GET",
            headers: {
              Authorization: `Bearer ${idToken}`,
              "Content-Type": "application/json",
            },
          }
        );

        if (response.status === 204) {
          setNotifications([]);
          setLoading(false);
          return;
        }

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        setNotifications(data);
        console.log("Fetched notifications:", data);
      } catch (err) {
        console.error("Error fetching notification history:", err);
        setError("Failed to load history. " + err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchHistory();
  }, [idToken, NOTIFICATION_SERVICE_BASE_URL]);

  const handleViewDetails = (notificationId) => {
    onSelectNotification(notificationId);
    onNavigate("notificationDetail");
  };

  if (loading) {
    return (
      <div className={styles.loadingMessage}>Loading asteroid history...</div>
    );
  }

  if (error) {
    return <div className={styles.errorMessage}>Error: {error}</div>;
  }

  return (
    <div className={styles.historyPageContainer}>
      <div className={styles.historyCard}>
        <h2 className={styles.historyTitle}>Asteroid Alert History</h2>

        {notifications.length === 0 ? (
          <p className={styles.noHistoryMessage}>
            No asteroid alerts found in your history yet.
          </p>
        ) : (
          <div className={styles.tableWrapper}>
            <table className={styles.historyTable}>
              <thead className={styles.tableHeader}>
                <tr>
                  <th className={styles.tableTh}>Asteroid Name</th>
                  <th className={styles.tableTh}>Close Approach Date</th>
                  <th className={styles.tableTh}>Miss Distance (km)</th>
                  <th className={styles.tableTh}>Avg Diameter (m)</th>
                  <th className={styles.tableTh}>Received At</th>
                  <th className={styles.tableTh}>Actions</th>
                </tr>
              </thead>
              <tbody className={styles.tableBody}>
                {notifications.map((notification) => (
                  <tr key={notification.id} className={styles.tableRow}>
                    <td className={styles.tableTd}>
                      {notification.asteroidName}
                    </td>
                    <td className={styles.tableTd}>
                      {notification.closeAppraochDate || "N/A"}
                    </td>
                    <td className={styles.tableTd}>
                      {parseFloat(
                        notification.missDistanceKilometers
                      ).toLocaleString()}
                    </td>
                    <td className={styles.tableTd}>
                      {notification.estimatedDiameterAvgMeters.toFixed(2)}
                    </td>
                    <td className={styles.tableTd}>
                      {new Date(notification.receivedAt).toLocaleString()}
                    </td>
                    <td className={styles.tableTd}>
                      <button
                        onClick={() => handleViewDetails(notification.id)}
                        className={styles.detailsButton}
                      >
                        View Details
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default HistoryPage;
