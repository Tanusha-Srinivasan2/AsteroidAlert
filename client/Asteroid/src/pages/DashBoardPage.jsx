import React from "react";
import { useAuth } from "../App";
import styles from "./DashboardPage.module.css";

const DashboardPage = () => {
  const { user } = useAuth();

  return (
    <div className={styles.dashboardPageContainer}>
      <div className={styles.dashboardCard}>
        <h2 className={styles.welcomeTitle}>
          Welcome, {user ? user.name || user.email : "Guest"}!
        </h2>
        <p className={styles.welcomeSubtitle}>
          Your personalized hub for asteroid alerts and space news.
        </p>

        <div className={styles.gridContainer}>
          <div className={styles.infoCard}>
            <h3 className={styles.cardTitleBlue}>Latest Alerts</h3>
            <p className={styles.cardText}>
              Stay informed about the most recent potentially hazardous asteroid
              close approaches. Check the "History" tab for a full list.
            </p>
          </div>

          <div className={styles.infoCard}>
            <h3 className={styles.cardTitleGreen}>Your Settings</h3>
            <p className={styles.cardText}>
              Manage your notification preferences to receive alerts via email.
              Visit the "Settings" tab to update.
            </p>
            <p className={styles.cardText}>
              Current Status:{" "}
              <span className={styles.statusEnabled}>
                Notifications Enabled
              </span>{" "}
              (Conceptual)
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;
