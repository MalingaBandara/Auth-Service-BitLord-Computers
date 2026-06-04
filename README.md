<div align="center">
  <h1>🔒 Bitlord's Computer Parts - Auth Service</h1>
  <p>Handles user authentication, authorization, and secure JWT generation.</p>
</div>

## 📖 Overview
The **Auth Service** is responsible for validating user credentials and securely issuing JSON Web Tokens (JWTs). These tokens are then utilized by the API Gateway to authorize access to protected resources across the microservices ecosystem.

[⬅️ Back to Main Repository](https://github.com/MalingaBandara/Bitlord-Computer-Parts)

## 🛠️ Tech Stack
- **Language**: Java 17
- **Framework**: Spring Boot 3.2 / Spring Security
- **Data Access**: Spring Data JPA
- **Database**: MySQL 8.0
- **Authentication**: JWT (JSON Web Tokens)
- **Service Discovery**: Netflix Eureka Client
- **Observability**: Prometheus, Micrometer, Zipkin

## 🔌 API Endpoints
Base path routing via API Gateway: `/api/auth`
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/login` | Authenticate user and return a JWT |
| `POST` | `/register` | Register a new user account |
| `GET` | `/validate` | Internal endpoint to validate JWT signatures |

## 🗄️ Database
- **Engine**: MySQL
- **Database Name**: `bitlord_auth`
- **Port**: `3307` (when running via Docker Compose)

## 🚀 How to Run Locally

### Prerequisites
- JDK 17
- Maven
- Infrastructure dependencies running (MySQL, Eureka Server) via the main repository's `docker-compose.yml`.

### Steps
1. Navigate to the `auth-service` directory.
2. Build the project:
   ```bash
   mvn clean install -DskipTests
   ```
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```
4. The service will register itself with the Eureka Server on a randomized port (or its predefined configuration).
