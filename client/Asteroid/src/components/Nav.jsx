import React from "react";
import styles from "./nav.module.css";
import buttonStyles from "./googleSignInButton.module.css";

import { useAuth } from "../App";

export default function Nav({ onNavigate }) {
  const { user, handleLogout } = useAuth();

  return (
    <nav className={styles.navbar}>
      <div className={styles.logo} onClick={() => onNavigate("dashboard")}>
        Star
      </div>
      <ul className={styles["nav-links"]}>
        <li onClick={() => onNavigate("dashboard")}>Home</li>
        {user && (
          <>
            <li onClick={() => onNavigate("history")}>History</li>
            <li onClick={() => onNavigate("settings")}>Settings</li>
          </>
        )}
      </ul>
      <div className={styles.rightSection}>
        {user ? (
          <button onClick={handleLogout} className={buttonStyles.logoutButton}>
            Logout ({user.name || user.email})
          </button>
        ) : (
          <button
            onClick={() => onNavigate("login")}
            className={buttonStyles.loginButton}
          >
            Login
          </button>
        )}
      </div>
    </nav>
  );
}
