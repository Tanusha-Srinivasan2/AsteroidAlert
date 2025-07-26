import React, { useState, useEffect } from "react";
import { useAuth } from "../App";
import styles from "./SettingsPage.module.css"; // Import the new CSS module

const SettingsPage = () => {
  const { idToken, user, NOTIFICATION_SERVICE_BASE_URL } = useAuth();
  const [notificationEnabled, setNotificationEnabled] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [message, setMessage] = useState("");

  // Fetch current settings on component mount
  useEffect(() => {
    const fetchSettings = async () => {
      if (!idToken || !user) {
        setError("User not authenticated.");
        setLoading(false);
        return;
      }
      setLoading(true);
      setError(null);
      setMessage("");
      try {
        const response = await fetch(
          `${NOTIFICATION_SERVICE_BASE_URL}/users/settings`,
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
        setNotificationEnabled(data.notificationEnabled);
        console.log("Fetched settings:", data);
      } catch (err) {
        console.error("Error fetching settings:", err);
        setError("Failed to load settings. " + err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchSettings();
  }, [idToken, user, NOTIFICATION_SERVICE_BASE_URL]);

  // Handle saving settings
  const handleSaveSettings = async () => {
    if (!idToken) {
      setError("User not authenticated. Please log in again.");
      return;
    }
    setLoading(true);
    setError(null);
    setMessage("");
    try {
      const response = await fetch(
        `${NOTIFICATION_SERVICE_BASE_URL}/users/settings`,
        {
          method: "PUT",
          headers: {
            Authorization: `Bearer ${idToken}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ notificationEnabled: notificationEnabled }),
        }
      );

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      setNotificationEnabled(data.notificationEnabled);
      setMessage("Settings saved successfully!");
      console.log("Settings updated:", data);
    } catch (err) {
      console.error("Error saving settings:", err);
      setError("Failed to save settings. " + err.message);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div className={styles.loadingMessage}>Loading settings...</div>;
  }

  if (error) {
    return <div className={styles.errorMessage}>Error: {error}</div>;
  }

  return (
    <div className={styles.settingsPageContainer}>
      <div className={styles.settingsCard}>
        <h2 className={styles.settingsTitle}>Notification Settings</h2>

        <div className={styles.settingItem}>
          <label htmlFor="notificationToggle" className={styles.settingLabel}>
            Enable Email Notifications
          </label>
          <input
            type="checkbox"
            id="notificationToggle"
            checked={notificationEnabled}
            onChange={(e) => setNotificationEnabled(e.target.checked)}
            className={styles.checkboxInput}
          />
        </div>

        {message && <p className={styles.successMessage}>{message}</p>}
        {error && <p className={styles.errorMessageText}>{error}</p>}

        <div className={styles.saveButtonContainer}>
          <button onClick={handleSaveSettings} className={styles.saveButton}>
            Save Settings
          </button>
        </div>
      </div>
    </div>
  );
};

export default SettingsPage;
