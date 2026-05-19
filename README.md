# assignP

assignP is a Spring Boot task management SaaS application built with Java 21, Spring Boot 3.2.4, Spring Data JPA, Spring Security, and JWT authentication.

## Project Overview

- **Purpose:** Manage users, projects, tasks, saved task filters, search, and notifications.
- **Local database:** MySQL (configured in `application-local.properties`).
- **Heroku deployment:** PostgreSQL via `DATABASE_URL` and `Procfile`.
- **Auto-profile selection:** `DataSourceConfiguration` chooses the datasource based on environment variables.

## Key files and directories

### Root files

- `pom.xml` - Maven project configuration and dependencies.
- `Procfile` - Heroku deployment command, activates the `postgres` profile.
- `system.properties` - Java runtime version for Heroku.
- `HELP.md` - generic Spring Boot reference links.

### Main application

- `src/main/java/com/PracticalAssignment/assignP/AssignPApplication.java`
  - Application entry point.
  - Activates the `local` profile for local development via `application-local.properties`.

### Configuration

- `src/main/java/com/PracticalAssignment/assignP/config/DataSourceConfiguration.java`
  - Custom datasource configuration.
  - Reads `DATABASE_URL` for Heroku Postgres.
  - Uses `spring.datasource.*` properties from local config when `DATABASE_URL` is absent.
  - Builds a `DataSource` for either MySQL or PostgreSQL depending on environment.

### Properties files

- `src/main/resources/application.properties`
  - Common Spring Boot settings.
  - `server.port` defaults to `8080` but uses `PORT` when deployed to Heroku.
  - JPA/Hibernate settings, JWT settings, and database initialization flags.

- `src/main/resources/application-local.properties`
  - Local MySQL datasource configuration.
  - Uses defaults for `jdbc:mysql://localhost:3306/assignpdb` and root user.
  - Includes MySQL driver class and Hibernate dialect override.

- `src/main/resources/application-postgres.properties`
  - Heroku PostgreSQL profile settings (if present).

### Package structure

- `controller/` - REST controllers that expose the application endpoints.
  - `AuthController.java` - user authentication and login.
  - `UserController.java` - CRUD operations for users.
  - `ProjectController.java` - project management endpoints.
  - `TaskController.java` - task management endpoints.
  - `SearchController.java` - search across tasks/projects.
  - `SavedTaskFilterController.java` - saved task filters.
  - `NotificationController.java` - notification-related endpoints.

- `service/` - business logic and service layer.
  - `AuthService.java` - authentication and JWT generation.
  - `ProjectService.java` - handles project flows.
  - `TaskService.java` - handles tasks and task workflows.
  - `GlobalSearchService.java` - search implementation.
  - `SavedTaskFilterService.java` - saved filter CRUD flows.
  - `NotificationService.java` - notification logic.

- `repository/` - Spring Data JPA repositories for persistence.
- `model/` - JPA entity classes.
- `dto/` - data transfer objects used by controllers and services.
- `security/` - Spring Security and JWT implementation.

## How the application decides which database to use

1. **Local run:** `AssignPApplication` activates `local` profile by default.
2. **DataSource loading:** `DataSourceConfiguration` checks `DATABASE_URL`.
   - If `DATABASE_URL` exists, it configures PostgreSQL for Heroku.
   - Otherwise, it loads MySQL settings from `application-local.properties`.
3. **Heroku deployment:** `Procfile` launches the jar with `-Dspring.profiles.active=postgres`.

## Running locally

1. Ensure local MySQL is running.
2. From the project root, run:

```powershell
./mvnw.cmd spring-boot:run -DskipTests
```

3. Open `http://localhost:8080`.

## Building for Heroku

1. Build the application:

```powershell
./mvnw.cmd clean package -DskipTests
```

2. Deploy the generated JAR to Heroku, which will use the `Procfile` command:

```text
web: java -Dspring.profiles.active=postgres -jar target/assignP-0.0.1-SNAPSHOT.jar
```

3. Heroku supplies `PORT` and `DATABASE_URL` automatically.

## Important notes

- `application.properties` should not hardcode `spring.profiles.active`; runtime or environment should control profiles.
- Local MySQL settings can be overridden with environment variables such as `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`.
- The application uses JWT via `jwt.secret` and `jwt.expiration` in `application.properties`.

## Troubleshooting

- **Port 8080 in use:** Stop the existing process or set a different port using `server.port` or `PORT`.
- **MySQL connection refused:** Start local MySQL and verify `localhost:3306` is reachable.
- **Heroku Postgres errors:** Ensure `DATABASE_URL` is set and the app uses the `postgres` profile.

## API Endpoints

The application exposes RESTful endpoints for authentication, user management, projects, tasks, notifications, search, and saved filters.

**See [API_ENDPOINTS.md](API_ENDPOINTS.md) for:**
- Complete list of all endpoints (authentication, projects, tasks, notifications, filters, search)
- Request/response examples for each endpoint
- curl commands for testing all endpoints
- Postman setup instructions
- Quick test workflow guide

### Summary of endpoint categories

| Resource | Endpoints |
|----------|-----------|
| **Authentication** | `/api/auth/register`, `/api/auth/login`, `/api/auth/refresh`, `/api/auth/logout` |
| **Users** | `GET /api/users` |
| **Projects** | `GET/POST /api/projects`, `GET/DELETE /api/projects/{id}`, `POST/DELETE /api/projects/{id}/members/{userId}` |
| **Tasks** | `GET/POST /api/tasks`, `PUT/DELETE /api/tasks/{id}`, `GET /api/tasks/{id}/details`, `POST /api/tasks/{id}/comments` |
| **Notifications** | `GET /api/notifications`, `POST /api/notifications/{id}/read`, `POST /api/notifications/read-all` |
| **Task Filters** | `GET/POST /api/task-filters`, `DELETE /api/task-filters/{id}` |
| **Search** | `GET /api/search/global` |

### Quick test example

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"TestPass123"}'

# Login (save token from response)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"TestPass123"}'

# Get all users (use token from login)
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer YOUR_TOKEN"
```

All protected endpoints (except `/api/auth/**`) require the `Authorization: Bearer {token}` header.

## Recommended future documentation additions

- Add a package-level overview for `model`, `repository`, `dto`, and `security`.
- Document each controller endpoint and request/response shape.
- Add a `Deployment.md` with Heroku and local environment examples.
