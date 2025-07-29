# AsteroidAlert

AsteroidAlert is a modern, full-stack application designed to provide real-time alerts about potentially hazardous asteroids. It features a scalable Spring Boot microservices backend and a dynamic React.js frontend. The system fetches data directly from NASA's NeoWs (Near Earth Object Web Service) API, processes it to identify threats, and notifies users via email. The entire application is containerized using Docker for easy setup and deployment.

## User Interface Screenshots

<div align="center">
  <img src="https://i.imgur.com/your-login-image.png" width="400" alt="Login Screen" />
</div>
<p align="center"><em>Secure sign-in using Google Authentication.</em></p>

<div align="center">
  <img src="https://i.imgur.com/your-dashboard-image.png" width="400" alt="Dashboard" />
</div>
<p align="center"><em>A personalized dashboard welcoming the user.</em></p>

<div align="center">
  <img src="https://i.imgur.com/your-history-image.png" width="600" alt="History Page" />
</div>
<p align="center"><em>A detailed history of all past asteroid alert notifications.</em></p>

<div align="center">
  <img src="https://i.imgur.com/your-details-image.png" width="400" alt="Notification Details" />
  <img src="https://i.imgur.com/your-settings-image.png" width="400" alt="Settings Page" />
</div>
<p align="center"><em>Users can view detailed information about each alert and manage their notification settings.</em></p>

<div align="center">
  <img src="https://i.imgur.com/your-architecture-image.png" width="800" alt="System Architecture" />
</div>
<p align="center"><em>System Architecture Diagram</em></p>

## Features

-   **Secure User Authentication**: Seamless and secure user login using Google Sign-In (OAuth 2.0).
-   **Real-time NASA Data**: Fetches near-earth object data directly from the official NASA NeoWs API.
-   **Hazardous Asteroid Identification**: Intelligently filters and identifies asteroids that are classified as potentially hazardous.
-   **Asynchronous Email Alerts**: Leverages Apache Kafka to send timely and non-blocking email notifications to users about potential threats.
-   **Notification History**: Provides users with a comprehensive history of all past asteroid alerts they have received.
-   **User-Managed Settings**: Allows users to enable or disable their email notification preferences at any time.
-   **Scalable Microservices Architecture**: Built with two core microservices for handling data retrieval and user notifications independently.
-   **Containerized Deployment**: Fully containerized with Docker and Docker Compose for a streamlined, one-command setup.

## Architecture

### Microservices

-   **AsteroidAlert Service**: This service is responsible for communicating with the NASA NeoWs API. It fetches the asteroid data for a given date range, filters for potentially hazardous objects, and publishes the findings as events to a Kafka topic.
-   **Notification Service**: This service acts as the user-facing backend. It manages user authentication, stores user data and notification history in a MySQL database, consumes events from the Kafka topic, and sends out email alerts to subscribed users.

### Event-Driven Messaging

-   The **AsteroidAlert Service** acts as a Kafka producer, publishing `AsteroidCollisionEvent` messages whenever a hazardous asteroid is detected.
-   The **Notification Service** acts as a Kafka consumer, listening to the `asteroid-alert` topic. Upon receiving an event, it saves the alert to the database and triggers the `EmailService` to notify the relevant users. This decoupled approach ensures that the data fetching process is not blocked by the notification sending process, enhancing scalability and resilience.

### Data Management

-   The **Notification Service** uses its own **MySQL** database instance to store all user-related data, including user profiles, settings, and a complete history of notifications. This ensures a clear separation of concerns between the services.

## Tech Stack

-   **Backend**: Java 21, Spring Boot, Spring Security (OAuth2/JWT), Spring Kafka, Spring Data JPA
-   **Frontend**: React.js, Vite
-   **Database**: MySQL
-   **Messaging**: Apache Kafka, Zookeeper
-   **Authentication**: Google Identity Services (OAuth 2.0)
-   **Containerization**: Docker, Docker Compose
-   **Mail Service**: Mailtrap (for development)

## Prerequisites

-   Docker ≥ 20.10
-   Docker Compose ≥ 1.29
-   Git

## Getting Started

1.  **Clone the repository**

    ```bash
    git clone https://github.com/your-username/AsteroidAlert.git
    cd AsteroidAlert
    ```

2.  **Configure Environment Variables**
    You will need to set up your own API keys and credentials in the `application.properties` files for both the `AsteroidAlert` and `notificationservice` modules.

    -   NASA API Key (`nasa.api.key`)
    -   Google Client ID (`google.client-id`)
    -   Mailtrap credentials (`spring.mail.*`)
    -   Database credentials (`spring.datasource.*`)

3.  **Build and start all services**

    ```bash
    docker-compose up --build
    ```

4.  **Open the React frontend** at
    ```
    http://localhost:5173
    ```

## Project Structure

```
AsteroidAlert-main/
├── AsteroidAlert/ # Spring Boot AsteroidAlert Service
│ ├── src/main/java/
│ └── pom.xml
├── notificationservice/ # Spring Boot Notification Service
│ ├── src/main/java/
│ └── pom.xml
├── client/ # React.js SPA
│ └── Asteroid/
│ ├── src/
│ └── package.json
├── docker-compose.yml # Orchestrates all containers
└── README.md # Project documentation
```

## Authentication Flow

1.  The React app redirects users to Google for sign-in.
2.  Upon successful authentication, Google returns a JWT ID Token to the client.
3.  The React frontend stores this token and includes it in the `Authorization: Bearer` header for all subsequent API calls to the backend.
4.  The Spring Boot **Notification Service** is configured as an OAuth 2.0 Resource Server. It validates the incoming JWT against Google's public keys to authenticate the user and secure its endpoints.

## Recent Changes

Based on the project's commit history, the development process included:

-   Initial backend commit and setup of the Asteroid alerting service.
-   Completion of the Notification Service, including email and user management logic.
-   Implementation of Google authentication, user synchronization, and notification history features.
-   Started the basic React frontend.
-   Integrated the frontend with the backend and refined the UI layout.
-   Resolved CORS and JWT authentication errors to finalize the integration.
-   Fixed all outstanding CORS issues.
