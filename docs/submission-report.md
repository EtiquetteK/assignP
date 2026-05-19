# Submission Report

## Live Deployment

- Heroku URL: https://assignp-task-management-app-9bf075cabf2c.herokuapp.com

## CI/CD and Quality Gates

- GitHub Actions now runs `mvn -B test jacoco:report` before deployment.
- The build-and-deploy job depends on test success and will not push Docker images if tests fail.

## Deliverables Covered

- Deliverable 1: Architecture diagrams, ERD, and OpenAPI spec files are added under `docs/`.
- Deliverable 2: Authentication logout, rate limiting, structured JSON responses, request validation, RBAC, and a live status SSE endpoint are implemented.
- Deliverable 3: Unit and integration test coverage is added and configured in `pom.xml`.
- Deliverable 4: GitHub Actions pipeline now includes a test stage before deployment.
- Deliverable 5: Security hardening measures have been added, including HTTPS enforcement, strict headers, input sanitization, and auth rate limiting.

## Bonus and Out-of-Scope

The application includes bonus features such as saved filters, notifications, and global search. These enhancements are documented as optional improvements for future delivery.
