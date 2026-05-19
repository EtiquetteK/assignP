# C4 Architecture for assignP

## System Context

assignP is a cloud-enabled task management SaaS application. It consists of:
- A Spring Boot backend running Java 21.
- A frontend served from static assets and single-page HTML pages.
- A relational database backend: local MySQL for development and Heroku PostgreSQL for production.

## Containers

- **assignP backend** (`Spring Boot`) handles REST API requests, authentication, user/project/task management, and SSE status streaming.
- **Browser frontend** (`HTML/JavaScript`) consumes the backend API at `/api/*`.
- **Database**: MySQL locally or PostgreSQL on Heroku.

## Components

- `AssignPApplication` - application entry point.
- `DataSourceConfiguration` - datasource selector for local MySQL or Heroku PostgreSQL.
- `SecurityConfig` + `JwtFilter` + `JwtUtil` - handle JWT auth, headers, and security policies.
- `AuthController` - login, register, refresh, and logout.
- `ProjectController`, `TaskController`, `UserController`, `NotificationController`, `SearchController` - domain APIs.
- `StatusController` - real-time server status event stream.

## Runtime

- Local: `application-local.properties` configures MySQL.
- Heroku: `DATABASE_URL` and `Procfile` enable PostgreSQL and `postgres` profile.
- `main.yaml` CI/CD now runs tests before deploy.
