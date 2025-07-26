import React, { useEffect } from "react"; // Import useEffect
import GoogleSignInButton from "../components/GoogleSignInButton";
import styles from "./LoginPage.module.css";

const LoginPage = ({ googleButtonRef }) => {
  // Effect to ensure the Google Sign-In button is rendered when the component mounts
  // or when the ref becomes available (e.g., after a logout and re-render).
  useEffect(() => {
    if (
      googleButtonRef.current &&
      window.google &&
      window.google.accounts &&
      window.google.accounts.id
    ) {
      // Re-render the Google Sign-In button into the ref's current element
      window.google.accounts.id.renderButton(googleButtonRef.current, {
        theme: "outline",
        size: "large",
        text: "signin_with",
        shape: "rounded",
        logo_alignment: "left",
      });
      // You might also want to call window.google.accounts.id.prompt(); here
      // if you want the sign-in dialog to appear automatically on the login page.
    }
  }, [googleButtonRef]); // Dependency array: re-run if googleButtonRef changes (its .current property)

  return (
    <div className={styles.loginPageContainer}>
      <div className={styles.loginCard}>
        <h2 className={styles.loginTitle}>Sign In to Asteroid App</h2>
        <p className={styles.loginSubtitle}>
          Access personalized asteroid alerts and notification settings.
        </p>
        <GoogleSignInButton googleButtonRef={googleButtonRef} />
        <p className={styles.loginInfo}>
          Your data is secure with Google's authentication.
        </p>
      </div>
    </div>
  );
};

export default LoginPage;
