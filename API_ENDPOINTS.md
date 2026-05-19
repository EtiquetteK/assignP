# API Endpoints Guide

## Base URL
- **Local:** `http://localhost:8080`
- **Heroku:** `https://assignp-task-management-app-9bf075cabf2c.herokuapp.com`

---

## 🔐 Authentication Endpoints

### 1. Register
**Endpoint:** `POST /api/auth/register`

Register a new user account.

**Request:**
```json
{
  "username": "john_doe",
  "password": "SecurePassword123"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Registration successful",
  "data": {
    "id": 1,
    "username": "john_doe",
    "role": "MEMBER"
  }
}
```

**Testing with curl:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john_doe","password":"SecurePassword123"}'
```

---

### 2. Login
**Endpoint:** `POST /api/auth/login`

Authenticate and receive a JWT token.

**Request:**
```json
{
  "username": "john_doe",
  "password": "SecurePassword123"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "role": "MEMBER"
  }
}
```

**Testing with curl:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john_doe","password":"SecurePassword123"}'
```

---

### 3. Refresh Token
**Endpoint:** `POST /api/auth/refresh`

Get a new JWT token using a refresh token.

**Request:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Token refreshed",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "role": "MEMBER"
  }
}
```

**Testing with curl:**
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"YOUR_REFRESH_TOKEN"}'
```

---

### 4. Logout
**Endpoint:** `POST /api/auth/logout`

Invalidate the current JWT token (blacklist).

**Request:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Logout successful"
}
```

**Testing with curl:**
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"token":"YOUR_TOKEN"}'
```

---

## 👥 User Endpoints

### 1. Get All Users
**Endpoint:** `GET /api/users`

Retrieve all users in the system.

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "username": "john_doe",
    "role": "MEMBER"
  },
  {
    "id": 2,
    "username": "admin_user",
    "role": "ADMIN"
  }
]
```

**Testing with curl:**
```bash
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 📂 Project Endpoints

### 1. Get All Projects
**Endpoint:** `GET /api/projects`

Retrieve all projects accessible to the current user.

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Web App Redesign",
    "description": "Redesign the company website",
    "owner": "john_doe",
    "members": ["john_doe", "jane_smith"]
  }
]
```

**Testing with curl:**
```bash
curl -X GET http://localhost:8080/api/projects \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 2. Get Project by ID
**Endpoint:** `GET /api/projects/{id}`

Retrieve a specific project.

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "Web App Redesign",
  "description": "Redesign the company website",
  "owner": "john_doe",
  "members": ["john_doe", "jane_smith"]
}
```

**Testing with curl:**
```bash
curl -X GET http://localhost:8080/api/projects/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 3. Create Project
**Endpoint:** `POST /api/projects`

Create a new project.

**Request:**
```json
{
  "name": "Mobile App Development",
  "description": "Build a cross-platform mobile app"
}
```

**Response (200 OK):**
```json
{
  "id": 2,
  "name": "Mobile App Development",
  "description": "Build a cross-platform mobile app",
  "owner": "john_doe",
  "members": ["john_doe"]
}
```

**Testing with curl:**
```bash
curl -X POST http://localhost:8080/api/projects \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "Mobile App Development",
    "description": "Build a cross-platform mobile app"
  }'
```

---

### 4. Delete Project
**Endpoint:** `DELETE /api/projects/{id}`

Delete a project (must be owner).

**Response (200 OK):**
```
(empty)
```

**Testing with curl:**
```bash
curl -X DELETE http://localhost:8080/api/projects/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 5. Add Member to Project
**Endpoint:** `POST /api/projects/{projectId}/members/{userId}`

Add a user to a project (must be owner/admin).

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "Web App Redesign",
  "description": "Redesign the company website",
  "owner": "john_doe",
  "members": ["john_doe", "jane_smith", "new_user"]
}
```

**Testing with curl:**
```bash
curl -X POST http://localhost:8080/api/projects/1/members/3 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 6. Remove Member from Project
**Endpoint:** `DELETE /api/projects/{projectId}/members/{userId}`

Remove a user from a project (must be owner/admin).

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "Web App Redesign",
  "description": "Redesign the company website",
  "owner": "john_doe",
  "members": ["john_doe"]
}
```

**Testing with curl:**
```bash
curl -X DELETE http://localhost:8080/api/projects/1/members/3 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## ✅ Task Endpoints

### 1. Get All Tasks
**Endpoint:** `GET /api/tasks`

Retrieve tasks with optional filtering.

**Query Parameters:**
- `projectId` - Filter by project
- `userId` - Filter by assignee
- `status` - Filter by status (TODO, IN_PROGRESS, DONE)
- `projectIds` - CSV of project IDs
- `userIds` - CSV of assignee IDs
- `statuses` - CSV of statuses
- `query` - Text search

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "title": "Design homepage",
    "description": "Create mockups for homepage",
    "projectId": 1,
    "assignedTo": "jane_smith",
    "status": "IN_PROGRESS",
    "priority": "HIGH",
    "dueDate": "2026-06-01"
  }
]
```

**Testing with curl:**
```bash
# Get all tasks
curl -X GET http://localhost:8080/api/tasks \
  -H "Authorization: Bearer YOUR_TOKEN"

# Filter by project
curl -X GET "http://localhost:8080/api/tasks?projectId=1" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Filter by status
curl -X GET "http://localhost:8080/api/tasks?status=IN_PROGRESS" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Multiple filters (CSV)
curl -X GET "http://localhost:8080/api/tasks?projectIds=1,2&statuses=TODO,IN_PROGRESS" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Text search
curl -X GET "http://localhost:8080/api/tasks?query=design" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 2. Get Task Details
**Endpoint:** `GET /api/tasks/{id}/details`

Get detailed information about a task including comments.

**Response (200 OK):**
```json
{
  "id": 1,
  "title": "Design homepage",
  "description": "Create mockups for homepage",
  "projectId": 1,
  "assignedTo": "jane_smith",
  "status": "IN_PROGRESS",
  "priority": "HIGH",
  "dueDate": "2026-06-01",
  "comments": [
    {
      "id": 1,
      "author": "john_doe",
      "content": "Great start on the mockups",
      "createdAt": "2026-05-18T10:30:00"
    }
  ]
}
```

**Testing with curl:**
```bash
curl -X GET http://localhost:8080/api/tasks/1/details \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 3. Create Task
**Endpoint:** `POST /api/tasks`

Create a new task.

**Request:**
```json
{
  "title": "Design homepage",
  "description": "Create mockups for homepage",
  "projectId": 1,
  "assignedTo": "jane_smith",
  "status": "TODO",
  "priority": "HIGH",
  "dueDate": "2026-06-01"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "title": "Design homepage",
  "description": "Create mockups for homepage",
  "projectId": 1,
  "assignedTo": "jane_smith",
  "status": "TODO",
  "priority": "HIGH",
  "dueDate": "2026-06-01"
}
```

**Testing with curl:**
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "title": "Design homepage",
    "description": "Create mockups for homepage",
    "projectId": 1,
    "assignedTo": "jane_smith",
    "status": "TODO",
    "priority": "HIGH",
    "dueDate": "2026-06-01"
  }'
```

---

### 4. Update Task
**Endpoint:** `PUT /api/tasks/{id}`

Update a task.

**Request:**
```json
{
  "title": "Design homepage (Updated)",
  "status": "IN_PROGRESS"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "title": "Design homepage (Updated)",
  "description": "Create mockups for homepage",
  "projectId": 1,
  "assignedTo": "jane_smith",
  "status": "IN_PROGRESS",
  "priority": "HIGH",
  "dueDate": "2026-06-01"
}
```

**Testing with curl:**
```bash
curl -X PUT http://localhost:8080/api/tasks/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "title": "Design homepage (Updated)",
    "status": "IN_PROGRESS"
  }'
```

---

### 5. Delete Task
**Endpoint:** `DELETE /api/tasks/{id}`

Delete a task.

**Response (200 OK):**
```
(empty)
```

**Testing with curl:**
```bash
curl -X DELETE http://localhost:8080/api/tasks/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 6. Add Comment to Task
**Endpoint:** `POST /api/tasks/{id}/comments`

Add a comment to a task.

**Request:**
```json
{
  "content": "Great start on the mockups"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "author": "john_doe",
  "content": "Great start on the mockups",
  "createdAt": "2026-05-18T10:30:00"
}
```

**Testing with curl:**
```bash
curl -X POST http://localhost:8080/api/tasks/1/comments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"content": "Great start on the mockups"}'
```

---

## 🔔 Notification Endpoints

### 1. Get My Notifications
**Endpoint:** `GET /api/notifications`

Retrieve your notifications.

**Query Parameters:**
- `unreadOnly` - Filter to unread only (default: false)

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "message": "You were added to project Web App Redesign",
    "type": "PROJECT_MEMBER_ADDED",
    "read": false,
    "createdAt": "2026-05-18T10:00:00"
  }
]
```

**Testing with curl:**
```bash
# All notifications
curl -X GET http://localhost:8080/api/notifications \
  -H "Authorization: Bearer YOUR_TOKEN"

# Unread only
curl -X GET "http://localhost:8080/api/notifications?unreadOnly=true" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 2. Mark Notification as Read
**Endpoint:** `POST /api/notifications/{id}/read`

Mark a single notification as read.

**Response (200 OK):**
```
(empty)
```

**Testing with curl:**
```bash
curl -X POST http://localhost:8080/api/notifications/1/read \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 3. Mark All Notifications as Read
**Endpoint:** `POST /api/notifications/read-all`

Mark all your notifications as read.

**Response (200 OK):**
```
(empty)
```

**Testing with curl:**
```bash
curl -X POST http://localhost:8080/api/notifications/read-all \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 💾 Saved Task Filter Endpoints

### 1. Get My Filters
**Endpoint:** `GET /api/task-filters`

Retrieve your saved task filters.

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "My High Priority Tasks",
    "projectIds": [1, 2],
    "statuses": ["IN_PROGRESS", "TODO"],
    "priority": "HIGH"
  }
]
```

**Testing with curl:**
```bash
curl -X GET http://localhost:8080/api/task-filters \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 2. Save Filter
**Endpoint:** `POST /api/task-filters`

Create and save a new task filter.

**Request:**
```json
{
  "name": "My High Priority Tasks",
  "projectIds": [1, 2],
  "statuses": ["IN_PROGRESS", "TODO"],
  "priority": "HIGH"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "My High Priority Tasks",
  "projectIds": [1, 2],
  "statuses": ["IN_PROGRESS", "TODO"],
  "priority": "HIGH"
}
```

**Testing with curl:**
```bash
curl -X POST http://localhost:8080/api/task-filters \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "My High Priority Tasks",
    "projectIds": [1, 2],
    "statuses": ["IN_PROGRESS", "TODO"],
    "priority": "HIGH"
  }'
```

---

### 3. Delete Filter
**Endpoint:** `DELETE /api/task-filters/{id}`

Delete a saved filter.

**Response (200 OK):**
```
(empty)
```

**Testing with curl:**
```bash
curl -X DELETE http://localhost:8080/api/task-filters/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 🔍 Search Endpoint

### Global Search
**Endpoint:** `GET /api/search/global`

Search for projects, tasks, and users globally.

**Query Parameters:**
- `q` - Search query (required)

**Response (200 OK):**
```json
{
  "projects": [
    {
      "id": 1,
      "name": "Web App Redesign"
    }
  ],
  "tasks": [
    {
      "id": 1,
      "title": "Design homepage"
    }
  ],
  "users": [
    {
      "id": 2,
      "username": "jane_smith"
    }
  ]
}
```

**Testing with curl:**
```bash
curl -X GET "http://localhost:8080/api/search/global?q=design" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 📋 Quick Test Workflow

### 1. Register and Login
```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"TestPass123"}'

# Login (save the token from response)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"TestPass123"}'
```

### 2. Create a Project
```bash
curl -X POST http://localhost:8080/api/projects \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"name":"Test Project","description":"For testing"}'
```

### 3. Create a Task
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "title":"Test Task",
    "description":"This is a test",
    "projectId":1,
    "status":"TODO",
    "priority":"MEDIUM"
  }'
```

### 4. Get Tasks
```bash
curl -X GET http://localhost:8080/api/tasks \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 🔐 Authentication Header

All protected endpoints (except `/api/auth/**`) require the JWT token:

```bash
-H "Authorization: Bearer YOUR_JWT_TOKEN"
```

Example:
```bash
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsInJvbGUiOiJNRU1CRVIiLCJpYXQiOjE2NjE0NzU2MDAsImV4cCI6MTY2MTQ3OTIwMH0.gFI9rNw..."
```

---

## 📌 API Response Format

All endpoints return responses in this format:

**Success:**
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "id": 1,
    "name": "Example"
  }
}
```

**Error:**
```json
{
  "success": false,
  "message": "Error description"
}
```

---

## 🧪 Using Postman

1. Import the base collection
2. Create authentication requests:
   - POST `{{base_url}}/api/auth/register`
   - POST `{{base_url}}/api/auth/login`
3. Copy the `token` from login response
4. In Postman, set a global variable:
   - Key: `token`
   - Value: (paste token)
5. Use `{{token}}` in Authorization headers for all protected endpoints

---

## 🛠️ Testing Tools

- **Curl:** Command-line HTTP client
- **Postman:** GUI-based API client
- **Insomnia:** REST client with built-in auth support
- **HTTPie:** User-friendly curl alternative

All examples above use curl, but work with any HTTP client.
