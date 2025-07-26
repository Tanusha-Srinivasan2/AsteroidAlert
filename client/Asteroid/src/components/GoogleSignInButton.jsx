import React from "react";
import styles from "./googleSignInButton.module.css";

const GoogleSignInButton = ({ googleButtonRef }) => {
  return (
    <div ref={googleButtonRef} className={styles.googleButtonContainer}>
      {/* Google's button will appear here */}
    </div>
  );
};

export default GoogleSignInButton;
