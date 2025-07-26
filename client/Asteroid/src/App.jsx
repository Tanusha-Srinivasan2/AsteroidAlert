import React, {
  useState,
  useEffect,
  useRef,
  createContext,
  useContext,
} from "react";
import Nav from "./components/Nav";
import "./App.css"; // Your existing global CSS
import LoginPage from "./pages/LoginPage";
import DashboardPage from "./pages/DashBoardPage";
import HistoryPage from "./pages/HistoryPage";
import NotificationDetailPage from "./pages/NotificationDetailPage";
import SettingsPage from "./pages/SettingsPage";

// Create a context for authentication to easily share state
const AuthContext = createContext(null);
export const useAuth = () => useContext(AuthContext);

// Main App component
const App = () => {
  // State for authenticated user data (decoded JWT payload)
  const [user, setUser] = useState(null);
  // State for the raw JWT (ID Token) received from Google
  const [idToken, setIdToken] = useState(null);
  // State for current page navigation (simple routing)
  const [currentPage, setCurrentPage] = useState("dashboard"); // Default page
  // State to hold selected notification ID for detail view
  const [selectedNotificationId, setSelectedNotificationId] = useState(null);

  // Ref for the Google Sign-In button container (used by LoginPage)
  const googleButtonRef = useRef(null);

  // Backend API base URL
  const NOTIFICATION_SERVICE_BASE_URL = "http://localhost:8081/api";

  // Function to initialize Google Identity Services
  const initializeGoogleSignIn = () => {
    if (window.google && window.google.accounts && window.google.accounts.id) {
      window.google.accounts.id.initialize({
        client_id:
          "267018054140-epftc4fdf5f7u544vcpkf6ev0co6debl.apps.googleusercontent.com", // <<< IMPORTANT: Replace with your actual Client ID
        callback: handleCredentialResponse,
        auto_select: false,
        cancel_on_tap_outside: true,
      });

      // Render the Google Sign-In button into the ref's current element
      // This button will be displayed on the LoginPage
      window.google.accounts.id.renderButton(googleButtonRef.current, {
        theme: "outline",
        size: "large",
        text: "signin_with",
        shape: "rounded",
        logo_alignment: "left",
      });
    } else {
      console.error("Google Identity Services script not loaded.");
    }
  };

  // Callback function for handling the credential response from Google
  const handleCredentialResponse = async (response) => {
    if (response.credential) {
      const token = response.credential;
      setIdToken(token); // Store the raw JWT

      try {
        const payload = JSON.parse(atob(token.split(".")[1]));
        setUser(payload); // Store the decoded user payload

        // --- Backend Login Sync ---
        // Call your backend to sync user data (find or create user)
        await loginSync(token);

        // Redirect to dashboard after successful login and sync
        setCurrentPage("dashboard");
      } catch (error) {
        console.error("Error decoding JWT or syncing with backend:", error);
        // Handle error, maybe show a message to the user
        setUser(null);
        setIdToken(null);
      }
    }
  };

  // Function to call backend for user login sync
  const loginSync = async (token) => {
    try {
      const response = await fetch(
        `${NOTIFICATION_SERVICE_BASE_URL}/auth/login-sync`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`, // Send the JWT
          },
          body: JSON.stringify({}), // Empty body as per backend endpoint
        }
      );

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      console.log("Backend login sync successful:", data);
      // You might want to store the full backend user object in state if needed
    } catch (error) {
      console.error("Error during backend login sync:", error);
      // Handle error, e.g., show a message to the user
      alert("Failed to sync user with backend. Please try again."); // Using alert for simplicity, use a custom modal in production
      setUser(null); // Clear user state if sync fails
      setIdToken(null);
    }
  };

  // Function to handle user logout
  const handleLogout = () => {
    setUser(null);
    setIdToken(null);
    setCurrentPage("login"); // Go back to login page
    // You might also want to revoke the Google session if necessary for security
    if (window.google && window.google.accounts && window.google.accounts.id) {
      window.google.accounts.id.disableAutoSelect(); // Prevents auto-login on next visit
    }
    console.log("User logged out.");
  };

  // Effect hook to load the Google Identity Services script and initialize sign-in
  useEffect(() => {
    const script = document.createElement("script");
    script.src = "https://accounts.google.com/gsi/client";
    script.async = true;
    script.defer = true;
    script.onload = initializeGoogleSignIn;
    document.body.appendChild(script);

    return () => {
      document.body.removeChild(script);
    };
  }, []); // Runs once on mount

  // Function to render the current page based on state
  const renderPage = () => {
    if (!user) {
      return <LoginPage googleButtonRef={googleButtonRef} />;
    }

    switch (currentPage) {
      case "dashboard":
        return <DashboardPage />;
      case "history":
        return (
          <HistoryPage
            onSelectNotification={setSelectedNotificationId}
            onNavigate={setCurrentPage}
          />
        );
      case "notificationDetail":
        return (
          <NotificationDetailPage
            notificationId={selectedNotificationId}
            onNavigate={setCurrentPage}
          />
        );
      case "settings":
        return <SettingsPage />;
      case "login": // Fallback if explicitly navigated to login while logged in (shouldn't happen much)
        return <LoginPage googleButtonRef={googleButtonRef} />;
      default:
        return <DashboardPage />;
    }
  };

  return (
    <AuthContext.Provider
      value={{ user, idToken, handleLogout, NOTIFICATION_SERVICE_BASE_URL }}
    >
      <div className="video-background-container">
        <video autoPlay loop muted playsInline className="video-background">
          <source src="/stars.mov" type="video/quicktime" />
          <source src="stars3.mp4" type="video/mp4" />
        </video>
        <div className="content-overlay">
          <Nav
            onNavigate={setCurrentPage}
            user={user}
            onLogout={handleLogout}
          />
          <div className="main-content-area">{renderPage()}</div>
        </div>
      </div>
    </AuthContext.Provider>
  );
};

export default App;
