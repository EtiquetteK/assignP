const API_BASE = window.location.port === "8080" ? "/api" : "http://localhost:8080/api";
let token = localStorage.getItem("token") || "";
let currentRole = "MEMBER";
let projects = [];
let users = [];
let tasks = [];
let savedFilters = [];
let notifications = [];
let refreshTimer = null;
let searchDebounceTimer = null;
let activeView = "kanban";
let lastFocusedElement = null;
const STATUS_FLOW = ["TODO", "IN_PROGRESS", "DONE"];

function parseJwt(tokenValue) {
  if (!tokenValue) return null;
  try {
    const payload = tokenValue.split(".")[1];
    if (!payload) return null;
    const normalized = payload.replace(/-/g, "+").replace(/_/g, "/");
    const padded = normalized + "=".repeat((4 - (normalized.length % 4)) % 4);
    const json = atob(padded)
      .split("")
      .map((character) => `%${character.charCodeAt(0).toString(16).padStart(2, "0")}`)
      .join("");
    return JSON.parse(decodeURIComponent(json));
  } catch {
    return null;
  }
}

function refreshCurrentRole() {
  const claims = parseJwt(token);
  const role = claims && claims.role ? String(claims.role).trim().toUpperCase() : "";
  currentRole = role === "ADMIN" || role === "ROLE_ADMIN" ? "ADMIN" : "MEMBER";
}

function isAdmin() {
  return currentRole === "ADMIN";
}

function authHeaders(includeJson = false) {
  const headers = {};
  if (includeJson) headers["Content-Type"] = "application/json";
  if (token) headers.Authorization = `Bearer ${token}`;
  return headers;
}

function setStatus(message, isError = false) {
  const el = document.getElementById("statusMessage");
  el.textContent = message;
  el.className = `status ${isError ? "error" : "success"}`;
}

async function readResponseBody(res) {
  const text = await res.text();
  if (!text) return "";
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

async function apiRequest(path, options = {}) {
  const res = await fetch(`${API_BASE}${path}`, options);
  const body = await readResponseBody(res);
  if (!res.ok) {
    throw new Error(typeof body === "string" ? body : JSON.stringify(body));
  }
  return body;
}

function ensureLoggedIn() {
  if (!token) {
    setStatus("No token found. Please login first.", true);
    window.location.href = "login.html";
  }
}

function escapeHtml(value = "") {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function formatDateTime(value) {
  if (!value) return "";
  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) return String(value);
  return `${parsed.toLocaleDateString()} ${parsed.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}`;
}

function normalizeStatusClass(status) {
  return String(status || "").toLowerCase().replaceAll("_", "-");
}

function selectedValues(selectId) {
  const select = document.getElementById(selectId);
  return Array.from(select.selectedOptions).map((option) => option.value).filter(Boolean);
}

function setSelectedValues(selectId, values) {
  const selectedSet = new Set((values || []).map(String));
  const select = document.getElementById(selectId);
  Array.from(select.options).forEach((option) => {
    option.selected = selectedSet.has(option.value);
  });
}

function renderLandingCards() {
  const cards = document.getElementById("landingCards");
  if (isAdmin()) {
    cards.innerHTML = `
      <article class="role-card admin-card">
        <h3>Admin Console</h3>
        <p>Own all projects, curate members, and guide delivery flow.</p>
      </article>
      <article class="role-card">
        <h3>Quick Action</h3>
        <p>Create projects and assign members from the management panel.</p>
      </article>
      <article class="role-card">
        <h3>Task Oversight</h3>
        <p>Drag tasks across lanes and audit project activity live.</p>
      </article>
    `;
  } else {
    cards.innerHTML = `
      <article class="role-card member-card">
        <h3>Member Workspace</h3>
        <p>Focus on assigned projects and keep your task flow healthy.</p>
      </article>
      <article class="role-card">
        <h3>Quick Action</h3>
        <p>Use Kanban drag-and-drop for instant task status updates.</p>
      </article>
      <article class="role-card">
        <h3>Task Details</h3>
        <p>Open a task drawer for comments, due dates, and activity.</p>
      </article>
    `;
  }
}

function applyRoleUi() {
  const roleBadge = document.getElementById("roleBadge");
  const projectPanelTitle = document.getElementById("projectPanelTitle");
  const projectForm = document.getElementById("projectForm");
  const projectSectionNote = document.getElementById("projectSectionNote");
  const projectListTitle = document.getElementById("projectListTitle");
  const taskBoardNote = document.getElementById("taskBoardNote");
  const heroSubtitle = document.getElementById("heroSubtitle");

  roleBadge.textContent = isAdmin() ? "Admin" : "Member";
  projectForm.classList.toggle("hidden", !isAdmin());
  projectPanelTitle.textContent = isAdmin() ? "Project Management" : "Your Projects";
  projectSectionNote.textContent = isAdmin()
    ? "Manage full project membership and lifecycle from this panel."
    : "You can view project members for projects assigned to you.";
  projectListTitle.textContent = isAdmin() ? "All Projects" : "Assigned Projects";
  taskBoardNote.textContent = isAdmin()
    ? "Drag cards across lanes for immediate status updates and open drawers for full details."
    : "Drag cards for your tasks and collaborate through task comments.";
  heroSubtitle.textContent = isAdmin()
    ? "Admin dashboard for projects, members, and delivery health."
    : "Member dashboard focused on assigned projects and task progress.";

  renderLandingCards();
}

function populateSelectOptions() {
  const taskProjectSelect = document.getElementById("taskProjectId");
  taskProjectSelect.innerHTML = projects
    .map((project) => `<option value="${project.id}">${escapeHtml(project.name)}</option>`)
    .join("");

  const taskUserSelect = document.getElementById("taskUserId");
  const drawerAssignee = document.getElementById("drawerAssignee");
  const userOptions = users.map((user) => `<option value="${user.id}">${escapeHtml(user.username)}</option>`).join("");
  taskUserSelect.innerHTML = `<option value="">Unassigned</option>${userOptions}`;
  drawerAssignee.innerHTML = `<option value="">Unassigned</option>${userOptions}`;

  const filterProjectIds = document.getElementById("filterProjectIds");
  filterProjectIds.innerHTML = projects
    .map((project) => `<option value="${project.id}">${escapeHtml(project.name)}</option>`)
    .join("");

  const filterUserIds = document.getElementById("filterUserIds");
  filterUserIds.innerHTML = users
    .map((user) => `<option value="${user.id}">${escapeHtml(user.username)}</option>`)
    .join("");
}

function renderFilterBadges() {
  const badges = [];
  const projectIds = selectedValues("filterProjectIds");
  const userIds = selectedValues("filterUserIds");
  const statuses = selectedValues("filterStatuses");
  const query = document.getElementById("filterQuery").value.trim();

  projectIds.forEach((id) => {
    const project = projects.find((item) => String(item.id) === id);
    badges.push(`Project: ${project ? project.name : id}`);
  });

  userIds.forEach((id) => {
    const user = users.find((item) => String(item.id) === id);
    badges.push(`Assignee: ${user ? user.username : id}`);
  });

  statuses.forEach((status) => badges.push(`Status: ${status}`));
  if (query) badges.push(`Search: ${query}`);

  const badgeContainer = document.getElementById("filterBadges");
  if (!badges.length) {
    badgeContainer.innerHTML = "<span class='hint'>No active filters</span>";
    return;
  }

  badgeContainer.innerHTML = badges.map((label) => `<span class="filter-badge">${escapeHtml(label)}</span>`).join("");
}

function renderSavedFilters() {
  const container = document.getElementById("savedFilters");
  if (!savedFilters.length) {
    container.innerHTML = "<span class='hint'>No saved filters</span>";
    return;
  }

  container.innerHTML = savedFilters
    .map(
      (filter) => `
        <div class="saved-filter-item">
          <button type="button" data-action="apply-saved-filter" data-id="${filter.id}">${escapeHtml(filter.name)}</button>
          <button type="button" class="danger-btn" data-action="delete-saved-filter" data-id="${filter.id}">x</button>
        </div>
      `
    )
    .join("");
}

function renderProjects() {
  const container = document.getElementById("projectList");
  if (!projects.length) {
    container.innerHTML = `
      <article class="onboarding-box">
        <h4>No projects yet</h4>
        <p>Create your first project, add members, and then create tasks to kick off delivery.</p>
        <p class="hint">Tip: admins can create projects from this panel, members can ask an admin for access.</p>
      </article>
    `;
    return;
  }

  container.innerHTML = projects
    .map((project) => {
      const members = (project.members || [])
        .map(
          (member) => `
            <span class="member-chip ${member.owner ? "owner" : ""}">
              ${escapeHtml(member.username)}
              <small>${escapeHtml(member.role || "MEMBER")}</small>
              ${isAdmin() && !member.owner ? `<button type="button" data-action="remove-member" data-project-id="${project.id}" data-user-id="${member.id}">x</button>` : ""}
            </span>
          `
        )
        .join("");

      const addMemberControl = isAdmin()
        ? `
            <div class="project-actions">
              <select id="project-member-${project.id}">
                <option value="">Select user</option>
                ${users.map((user) => `<option value="${user.id}">${escapeHtml(user.username)}</option>`).join("")}
              </select>
              <button type="button" data-action="add-member" data-project-id="${project.id}">Add Member</button>
              <button type="button" class="danger-btn" data-action="delete-project" data-project-id="${project.id}">Delete Project</button>
            </div>
          `
        : "";

      return `
        <article class="project-item project-admin-card">
          <div>
            <h4 class="project-title">${escapeHtml(project.name)}</h4>
            <p class="project-meta">Owner: ${escapeHtml(project.ownerUsername || "Unknown")}</p>
            <div class="members-wrap">${members || "<span class='hint'>No members yet</span>"}</div>
          </div>
          ${addMemberControl}
        </article>
      `;
    })
    .join("");
}

function renderGlobalSearchResults(result) {
  const container = document.getElementById("globalSearchResults");
  const noResults =
    (!result.projects || !result.projects.length) &&
    (!result.tasks || !result.tasks.length) &&
    (!result.users || !result.users.length);

  if (noResults) {
    container.innerHTML = "<p class='hint'>No matches found.</p>";
    return;
  }

  const projectsHtml = (result.projects || [])
    .map((project) => `<li><button type="button" data-search-type="project" data-id="${project.id}">${escapeHtml(project.name)}</button></li>`)
    .join("");
  const tasksHtml = (result.tasks || [])
    .map((task) => `<li><button type="button" data-search-type="task" data-id="${task.id}">#${task.id} ${escapeHtml(task.description || "Untitled")}</button></li>`)
    .join("");
  const usersHtml = (result.users || [])
    .map((user) => `<li><button type="button" data-search-type="user" data-id="${user.id}">${escapeHtml(user.username)} <small>${escapeHtml(user.role || "")}</small></button></li>`)
    .join("");

  container.innerHTML = `
    <div class="search-result-grid">
      <article>
        <h4>Projects</h4>
        <ul>${projectsHtml || "<li class='hint'>No project matches</li>"}</ul>
      </article>
      <article>
        <h4>Tasks</h4>
        <ul>${tasksHtml || "<li class='hint'>No task matches</li>"}</ul>
      </article>
      <article>
        <h4>Users</h4>
        <ul>${usersHtml || "<li class='hint'>No user matches</li>"}</ul>
      </article>
    </div>
  `;
}

function renderNotifications() {
  const container = document.getElementById("notificationList");
  if (!notifications.length) {
    container.innerHTML = "<p class='hint'>No alerts right now.</p>";
    return;
  }

  container.innerHTML = notifications
    .map((notification) => {
      const type = escapeHtml(notification.type || "INFO");
      const canMarkRead = notification.id ? `<button type="button" data-action="read-notification" data-id="${notification.id}">Mark read</button>` : "";
      return `
        <article class="notification-item ${notification.read ? "read" : "unread"}">
          <div>
            <p class="notification-type">${type}</p>
            <p>${escapeHtml(notification.message || "")}</p>
            <small>${escapeHtml(formatDateTime(notification.createdAt))}</small>
          </div>
          ${canMarkRead}
        </article>
      `;
    })
    .join("");
}

function renderMetrics() {
  document.getElementById("metricTotal").textContent = String(tasks.length);
  document.getElementById("metricTodo").textContent = String(tasks.filter((task) => task.status === "TODO").length);
  document.getElementById("metricProgress").textContent = String(tasks.filter((task) => task.status === "IN_PROGRESS").length);
  document.getElementById("metricDone").textContent = String(tasks.filter((task) => task.status === "DONE").length);
}

function taskCard(task) {
  const due = task.dueDate ? `Due ${task.dueDate}` : "No due date";
  const status = task.status || "TODO";
  return `
    <article class="kanban-card" draggable="true" tabindex="0" data-task-id="${task.id}" data-status="${escapeHtml(status)}" aria-label="Task ${task.id}. ${escapeHtml(task.description || "Untitled")}. Status ${escapeHtml(status)}. Use left and right arrow keys to move status, or Enter to open details.">
      <h4>#${task.id} ${escapeHtml(task.description || "Untitled")}</h4>
      <p>${escapeHtml(task.projectName || "Unknown project")}</p>
      <div class="task-meta">${escapeHtml(task.assigneeUsername || "Unassigned")} | ${escapeHtml(due)}</div>
      <button type="button" data-action="open-task" data-task-id="${task.id}">Details</button>
    </article>
  `;
}

function renderKanban() {
  const todoTasks = tasks.filter((task) => task.status === "TODO");
  const progressTasks = tasks.filter((task) => task.status === "IN_PROGRESS");
  const doneTasks = tasks.filter((task) => task.status === "DONE");

  document.getElementById("todoLane").innerHTML = todoTasks.map(taskCard).join("") || "<p class='hint'>No tasks</p>";
  document.getElementById("progressLane").innerHTML = progressTasks.map(taskCard).join("") || "<p class='hint'>No tasks</p>";
  document.getElementById("doneLane").innerHTML = doneTasks.map(taskCard).join("") || "<p class='hint'>No tasks</p>";

  document.getElementById("colTodoCount").textContent = String(todoTasks.length);
  document.getElementById("colProgressCount").textContent = String(progressTasks.length);
  document.getElementById("colDoneCount").textContent = String(doneTasks.length);

  const onboarding = document.getElementById("taskOnboarding");
  if (!tasks.length) {
    onboarding.classList.remove("hidden");
    onboarding.innerHTML = `
      <h4>No tasks available</h4>
      <p>Start by creating a task and assigning an owner with a due date.</p>
      <p class="hint">Use the planner and timeline views once you have dated tasks.</p>
    `;
  } else {
    onboarding.classList.add("hidden");
    onboarding.innerHTML = "";
  }

  renderMetrics();
}

function renderCalendarView() {
  const container = document.getElementById("calendarView");
  const today = new Date();
  const days = Array.from({ length: 7 }, (_, index) => {
    const date = new Date(today);
    date.setDate(today.getDate() + index);
    const key = date.toISOString().slice(0, 10);
    const dayTasks = tasks.filter((task) => task.dueDate === key);
    return { date, key, dayTasks };
  });

  container.innerHTML = `
    <div class="planner-grid">
      ${days
        .map(
          (day) => `
            <article class="planner-day">
              <h4>${escapeHtml(day.date.toLocaleDateString(undefined, { weekday: "short", month: "short", day: "numeric" }))}</h4>
              <p class="hint">${day.dayTasks.length} task(s)</p>
              <div class="planner-items">
                ${day.dayTasks.length
                  ? day.dayTasks
                      .map(
                        (task) => `<button type="button" data-action="open-task" data-task-id="${task.id}" class="planner-task ${normalizeStatusClass(task.status)}">#${task.id} ${escapeHtml(task.description || "Untitled")}</button>`
                      )
                      .join("")
                  : "<p class='hint'>No tasks due</p>"}
              </div>
            </article>
          `
        )
        .join("")}
    </div>
  `;
}

function renderTimelineView() {
  const container = document.getElementById("timelineView");
  const timelineTasks = tasks
    .filter((task) => task.dueDate)
    .slice()
    .sort((a, b) => String(a.dueDate).localeCompare(String(b.dueDate)));

  if (!timelineTasks.length) {
    container.innerHTML = "<p class='hint'>No milestones yet. Add due dates to see the timeline.</p>";
    return;
  }

  container.innerHTML = `
    <div class="timeline-list">
      ${timelineTasks
        .map(
          (task) => `
            <article class="timeline-item ${normalizeStatusClass(task.status)}">
              <div class="timeline-date">${escapeHtml(task.dueDate)}</div>
              <div>
                <h4>#${task.id} ${escapeHtml(task.description || "Untitled")}</h4>
                <p>${escapeHtml(task.projectName || "Unknown project")} • ${escapeHtml(task.assigneeUsername || "Unassigned")}</p>
              </div>
              <button type="button" data-action="open-task" data-task-id="${task.id}">Open</button>
            </article>
          `
        )
        .join("")}
    </div>
  `;
}

function applyActiveView() {
  const kanban = document.getElementById("kanbanBoard");
  const calendar = document.getElementById("calendarView");
  const timeline = document.getElementById("timelineView");

  kanban.classList.toggle("hidden", activeView !== "kanban");
  calendar.classList.toggle("hidden", activeView !== "calendar");
  timeline.classList.toggle("hidden", activeView !== "timeline");

  document.getElementById("viewKanbanBtn").classList.toggle("is-active", activeView === "kanban");
  document.getElementById("viewCalendarBtn").classList.toggle("is-active", activeView === "calendar");
  document.getElementById("viewTimelineBtn").classList.toggle("is-active", activeView === "timeline");
}

async function runGlobalSearch() {
  const query = document.getElementById("globalSearchInput").value.trim();
  if (!query) {
    document.getElementById("globalSearchResults").innerHTML = "<p class='hint'>Type to search across tasks, projects, and users.</p>";
    return;
  }

  try {
    const result = await apiRequest(`/search/global?q=${encodeURIComponent(query)}`, { headers: authHeaders() });
    renderGlobalSearchResults(result);
  } catch (err) {
    setStatus(`Global search failed: ${err.message}`, true);
  }
}

async function loadNotifications() {
  notifications = await apiRequest("/notifications", { headers: authHeaders() });
  renderNotifications();
}

async function markNotificationRead(notificationId) {
  await apiRequest(`/notifications/${notificationId}/read`, {
    method: "POST",
    headers: authHeaders(),
  });
}

async function markAllNotificationsRead() {
  await apiRequest("/notifications/read-all", {
    method: "POST",
    headers: authHeaders(),
  });
}

function openDrawer() {
  lastFocusedElement = document.activeElement instanceof HTMLElement ? document.activeElement : null;
  const drawer = document.getElementById("taskDrawer");
  drawer.classList.add("open");
  drawer.setAttribute("aria-hidden", "false");
  const closeBtn = document.getElementById("closeDrawerBtn");
  closeBtn.focus();
}

function closeDrawer() {
  const drawer = document.getElementById("taskDrawer");
  drawer.classList.remove("open");
  drawer.setAttribute("aria-hidden", "true");
  if (lastFocusedElement) {
    lastFocusedElement.focus();
  }
}

function shiftStatus(currentStatus, direction) {
  const index = STATUS_FLOW.indexOf(currentStatus);
  if (index < 0) return currentStatus;
  const nextIndex = index + direction;
  if (nextIndex < 0 || nextIndex >= STATUS_FLOW.length) {
    return currentStatus;
  }
  return STATUS_FLOW[nextIndex];
}

async function openTaskDetails(taskId) {
  try {
    const details = await apiRequest(`/tasks/${taskId}/details`, { headers: authHeaders() });
    const task = details.task;
    document.getElementById("drawerTitle").textContent = `Task #${task.id}`;
    document.getElementById("drawerTaskId").value = String(task.id);
    document.getElementById("drawerDescription").value = task.description || "";
    document.getElementById("drawerStatus").value = task.status || "TODO";
    document.getElementById("drawerAssignee").value = task.userId ? String(task.userId) : "";
    document.getElementById("drawerDueDate").value = task.dueDate || "";

    const comments = details.comments || [];
    document.getElementById("drawerComments").innerHTML = comments.length
      ? comments
          .map(
            (comment) => `
              <article class="drawer-item">
                <strong>${escapeHtml(comment.authorUsername || "Unknown")}</strong>
                <small>${escapeHtml(comment.createdAt || "")}</small>
                <p>${escapeHtml(comment.content || "")}</p>
              </article>
            `
          )
          .join("")
      : "<p class='hint'>No comments yet</p>";

    const activity = details.activity || [];
    document.getElementById("drawerActivity").innerHTML = activity.length
      ? activity
          .map(
            (entry) => `
              <article class="drawer-item">
                <strong>${escapeHtml(entry.type || "")}</strong>
                <small>${escapeHtml(entry.createdAt || "")}</small>
                <p>${escapeHtml(entry.actorUsername || "System")}: ${escapeHtml(entry.message || "")}</p>
              </article>
            `
          )
          .join("")
      : "<p class='hint'>No activity yet</p>";

    openDrawer();
  } catch (err) {
    setStatus(`Failed to load task details: ${err.message}`, true);
  }
}

async function updateTask(taskId, payload, successMessage) {
  try {
    await apiRequest(`/tasks/${taskId}`, {
      method: "PUT",
      headers: authHeaders(true),
      body: JSON.stringify(payload),
    });
    setStatus(successMessage || `Task #${taskId} updated.`);
    await loadTasks();
  } catch (err) {
    setStatus(`Task update failed: ${err.message}`, true);
  }
}

async function loadProjects() {
  projects = await apiRequest("/projects", { headers: authHeaders() });
}

async function loadUsers() {
  users = await apiRequest("/users", { headers: authHeaders() });
}

async function loadSavedFilters() {
  savedFilters = await apiRequest("/task-filters", { headers: authHeaders() });
}

async function loadTasks() {
  const params = new URLSearchParams();
  const projectIds = selectedValues("filterProjectIds");
  const userIds = selectedValues("filterUserIds");
  const statuses = selectedValues("filterStatuses");
  const query = document.getElementById("filterQuery").value.trim();

  if (projectIds.length) params.set("projectIds", projectIds.join(","));
  if (userIds.length) params.set("userIds", userIds.join(","));
  if (statuses.length) params.set("statuses", statuses.join(","));
  if (query) params.set("query", query);

  const queryString = params.toString();
  tasks = await apiRequest(`/tasks${queryString ? `?${queryString}` : ""}`, { headers: authHeaders() });
  renderFilterBadges();
  renderKanban();
  renderCalendarView();
  renderTimelineView();
  applyActiveView();
}

async function refreshAll() {
  try {
    refreshCurrentRole();
    applyRoleUi();

    await loadProjects();
    await loadUsers();
    await loadSavedFilters();

    populateSelectOptions();
    renderProjects();
    renderSavedFilters();

    await loadTasks();
    await loadNotifications();
    renderCalendarView();
    renderTimelineView();
    applyActiveView();
    if (!document.getElementById("globalSearchInput").value.trim()) {
      document.getElementById("globalSearchResults").innerHTML = "<p class='hint'>Type to search across tasks, projects, and users.</p>";
    }
  } catch (err) {
    setStatus(`Refresh failed: ${err.message}`, true);
  }
}

async function saveCurrentFilter() {
  const name = window.prompt("Filter name:");
  if (!name || !name.trim()) return;

  const payload = {
    name: name.trim(),
    projectIds: selectedValues("filterProjectIds").map(Number),
    userIds: selectedValues("filterUserIds").map(Number),
    statuses: selectedValues("filterStatuses"),
    query: document.getElementById("filterQuery").value.trim(),
  };

  try {
    await apiRequest("/task-filters", {
      method: "POST",
      headers: authHeaders(true),
      body: JSON.stringify(payload),
    });
    setStatus("Filter saved.");
    await loadSavedFilters();
    renderSavedFilters();
  } catch (err) {
    setStatus(`Could not save filter: ${err.message}`, true);
  }
}

function applySavedFilter(filterId) {
  const filter = savedFilters.find((item) => Number(item.id) === Number(filterId));
  if (!filter) return;
  setSelectedValues("filterProjectIds", filter.projectIds || []);
  setSelectedValues("filterUserIds", filter.userIds || []);
  setSelectedValues("filterStatuses", filter.statuses || []);
  document.getElementById("filterQuery").value = filter.query || "";
  loadTasks();
}

async function deleteSavedFilter(filterId) {
  try {
    await apiRequest(`/task-filters/${filterId}`, {
      method: "DELETE",
      headers: authHeaders(),
    });
    setStatus("Saved filter deleted.");
    await loadSavedFilters();
    renderSavedFilters();
  } catch (err) {
    setStatus(`Could not delete filter: ${err.message}`, true);
  }
}

document.getElementById("logoutBtn").addEventListener("click", () => {
  localStorage.removeItem("token");
  token = "";
  window.location.href = "login.html";
});

document.getElementById("refreshBtn").addEventListener("click", async () => {
  await refreshAll();
  setStatus("Dashboard refreshed.");
});

document.getElementById("globalSearchBtn").addEventListener("click", runGlobalSearch);

document.getElementById("globalSearchInput").addEventListener("input", () => {
  if (searchDebounceTimer) {
    clearTimeout(searchDebounceTimer);
  }
  searchDebounceTimer = setTimeout(() => {
    runGlobalSearch();
  }, 250);
});

document.getElementById("refreshNotificationsBtn").addEventListener("click", async () => {
  try {
    await loadNotifications();
    setStatus("Notifications refreshed.");
  } catch (err) {
    setStatus(`Could not refresh notifications: ${err.message}`, true);
  }
});

document.getElementById("markAllNotificationsBtn").addEventListener("click", async () => {
  try {
    await markAllNotificationsRead();
    await loadNotifications();
    setStatus("All notifications marked as read.");
  } catch (err) {
    setStatus(`Could not mark notifications as read: ${err.message}`, true);
  }
});

document.getElementById("viewKanbanBtn").addEventListener("click", () => {
  activeView = "kanban";
  applyActiveView();
});

document.getElementById("viewCalendarBtn").addEventListener("click", () => {
  activeView = "calendar";
  applyActiveView();
});

document.getElementById("viewTimelineBtn").addEventListener("click", () => {
  activeView = "timeline";
  applyActiveView();
});

document.getElementById("projectForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  if (!isAdmin()) {
    setStatus("Only admins can create projects.", true);
    return;
  }

  const name = document.getElementById("projectName").value.trim();
  try {
    await apiRequest("/projects", {
      method: "POST",
      headers: authHeaders(true),
      body: JSON.stringify({ name }),
    });
    document.getElementById("projectForm").reset();
    await refreshAll();
    setStatus("Project created.");
  } catch (err) {
    setStatus(`Project creation failed: ${err.message}`, true);
  }
});

document.getElementById("taskForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  const payload = {
    description: document.getElementById("taskDesc").value.trim(),
    status: document.getElementById("taskStatus").value,
    projectId: Number(document.getElementById("taskProjectId").value),
    userId: document.getElementById("taskUserId").value ? Number(document.getElementById("taskUserId").value) : null,
    dueDate: document.getElementById("taskDueDate").value || null,
  };

  try {
    await apiRequest("/tasks", {
      method: "POST",
      headers: authHeaders(true),
      body: JSON.stringify(payload),
    });
    document.getElementById("taskForm").reset();
    await loadTasks();
    setStatus("Task created.");
  } catch (err) {
    setStatus(`Task creation failed: ${err.message}`, true);
  }
});

document.getElementById("applyFiltersBtn").addEventListener("click", loadTasks);
document.getElementById("saveFilterBtn").addEventListener("click", saveCurrentFilter);

document.getElementById("clearFiltersBtn").addEventListener("click", () => {
  setSelectedValues("filterProjectIds", []);
  setSelectedValues("filterUserIds", []);
  setSelectedValues("filterStatuses", []);
  document.getElementById("filterQuery").value = "";
  loadTasks();
});

document.getElementById("jumpProjectBtn").addEventListener("click", () => {
  document.getElementById("projectPanel").scrollIntoView({ behavior: "smooth", block: "start" });
});

document.getElementById("jumpTaskBtn").addEventListener("click", () => {
  document.getElementById("taskCreateTitle").scrollIntoView({ behavior: "smooth", block: "start" });
});

document.getElementById("projectList" ).addEventListener("click", async (event) => {
  const target = event.target;
  if (!(target instanceof HTMLElement)) return;

  const action = target.dataset.action;
  const projectId = Number(target.dataset.projectId);
  const userId = Number(target.dataset.userId);

  if (action === "delete-project") {
    try {
      await apiRequest(`/projects/${projectId}`, { method: "DELETE", headers: authHeaders() });
      setStatus("Project deleted.");
      await refreshAll();
    } catch (err) {
      setStatus(`Delete project failed: ${err.message}`, true);
    }
    return;
  }

  if (action === "add-member") {
    const select = document.getElementById(`project-member-${projectId}`);
    const selectedUserId = select && select.value ? Number(select.value) : null;
    if (!selectedUserId) {
      setStatus("Select a user to add.", true);
      return;
    }
    try {
      await apiRequest(`/projects/${projectId}/members/${selectedUserId}`, {
        method: "POST",
        headers: authHeaders(),
      });
      setStatus("Member added.");
      await refreshAll();
    } catch (err) {
      setStatus(`Add member failed: ${err.message}`, true);
    }
    return;
  }

  if (action === "remove-member") {
    try {
      await apiRequest(`/projects/${projectId}/members/${userId}`, {
        method: "DELETE",
        headers: authHeaders(),
      });
      setStatus("Member removed.");
      await refreshAll();
    } catch (err) {
      setStatus(`Remove member failed: ${err.message}`, true);
    }
  }
});

document.getElementById("savedFilters").addEventListener("click", (event) => {
  const target = event.target;
  if (!(target instanceof HTMLElement)) return;
  const action = target.dataset.action;
  const id = Number(target.dataset.id);
  if (!action || !id) return;

  if (action === "apply-saved-filter") {
    applySavedFilter(id);
  } else if (action === "delete-saved-filter") {
    deleteSavedFilter(id);
  }
});

document.getElementById("globalSearchResults").addEventListener("click", async (event) => {
  const target = event.target;
  if (!(target instanceof HTMLElement)) return;
  const button = target.closest("button[data-search-type]");
  if (!(button instanceof HTMLElement)) return;

  const type = button.dataset.searchType;
  const id = Number(button.dataset.id);
  if (!type || !id) return;

  if (type === "task") {
    await openTaskDetails(id);
    return;
  }

  if (type === "project") {
    setSelectedValues("filterProjectIds", [String(id)]);
    await loadTasks();
    setStatus(`Filtered to project #${id}.`);
    return;
  }

  if (type === "user") {
    setSelectedValues("filterUserIds", [String(id)]);
    await loadTasks();
    setStatus("Filtered tasks by selected user.");
  }
});

document.getElementById("notificationList").addEventListener("click", async (event) => {
  const target = event.target;
  if (!(target instanceof HTMLElement)) return;
  if (target.dataset.action !== "read-notification") return;

  const id = Number(target.dataset.id);
  if (!id) return;

  try {
    await markNotificationRead(id);
    await loadNotifications();
  } catch (err) {
    setStatus(`Failed to update notification: ${err.message}`, true);
  }
});

const lanes = [
  { id: "todoLane", status: "TODO" },
  { id: "progressLane", status: "IN_PROGRESS" },
  { id: "doneLane", status: "DONE" },
];

lanes.forEach((lane) => {
  const zone = document.getElementById(lane.id);
  zone.addEventListener("dragover", (event) => {
    event.preventDefault();
    zone.classList.add("drag-over");
  });
  zone.addEventListener("dragleave", () => zone.classList.remove("drag-over"));
  zone.addEventListener("drop", async (event) => {
    event.preventDefault();
    zone.classList.remove("drag-over");
    const taskId = Number(event.dataTransfer.getData("text/plain"));
    if (!taskId) return;
    await updateTask(taskId, { status: lane.status }, `Task #${taskId} moved to ${lane.status}.`);
  });
});

document.getElementById("kanbanBoard").addEventListener("dragstart", (event) => {
  const target = event.target;
  if (!(target instanceof HTMLElement)) return;
  const card = target.closest(".kanban-card");
  if (!card) return;
  event.dataTransfer.setData("text/plain", card.dataset.taskId || "");
});

document.getElementById("kanbanBoard").addEventListener("click", (event) => {
  const target = event.target;
  if (!(target instanceof HTMLElement)) return;
  const action = target.dataset.action;
  if (action !== "open-task") return;
  const taskId = Number(target.dataset.taskId);
  if (taskId) {
    openTaskDetails(taskId);
  }
});

document.getElementById("kanbanBoard").addEventListener("keydown", async (event) => {
  const target = event.target;
  if (!(target instanceof HTMLElement)) return;

  const card = target.closest(".kanban-card");
  if (!(card instanceof HTMLElement)) return;
  if (target.tagName === "BUTTON") return;

  const taskId = Number(card.dataset.taskId);
  if (!taskId) return;

  if (event.key === "Enter" || event.key === " ") {
    event.preventDefault();
    await openTaskDetails(taskId);
    return;
  }

  if (event.key === "ArrowRight" || event.key === "ArrowLeft") {
    event.preventDefault();
    const direction = event.key === "ArrowRight" ? 1 : -1;
    const currentStatus = card.dataset.status || "TODO";
    const nextStatus = shiftStatus(currentStatus, direction);
    if (nextStatus !== currentStatus) {
      await updateTask(taskId, { status: nextStatus }, `Task #${taskId} moved to ${nextStatus}.`);
    }
  }
});

document.getElementById("calendarView").addEventListener("click", (event) => {
  const target = event.target;
  if (!(target instanceof HTMLElement)) return;
  const action = target.dataset.action;
  if (action !== "open-task") return;
  const taskId = Number(target.dataset.taskId);
  if (taskId) {
    openTaskDetails(taskId);
  }
});

document.getElementById("timelineView").addEventListener("click", (event) => {
  const target = event.target;
  if (!(target instanceof HTMLElement)) return;
  const action = target.dataset.action;
  if (action !== "open-task") return;
  const taskId = Number(target.dataset.taskId);
  if (taskId) {
    openTaskDetails(taskId);
  }
});

document.getElementById("closeDrawerBtn").addEventListener("click", closeDrawer);

document.addEventListener("keydown", (event) => {
  if (event.key !== "Escape") return;
  const drawer = document.getElementById("taskDrawer");
  if (!drawer.classList.contains("open")) return;
  event.preventDefault();
  closeDrawer();
});

document.getElementById("drawerTaskForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  const taskId = Number(document.getElementById("drawerTaskId").value);
  if (!taskId) return;

  await updateTask(taskId, {
    description: document.getElementById("drawerDescription").value.trim(),
    status: document.getElementById("drawerStatus").value,
    userId: document.getElementById("drawerAssignee").value ? Number(document.getElementById("drawerAssignee").value) : 0,
    dueDate: document.getElementById("drawerDueDate").value || null,
  }, "Task details saved.");

  await openTaskDetails(taskId);
});

document.getElementById("drawerCommentForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  const taskId = Number(document.getElementById("drawerTaskId").value);
  const content = document.getElementById("drawerCommentInput").value.trim();
  if (!taskId || !content) return;

  try {
    await apiRequest(`/tasks/${taskId}/comments`, {
      method: "POST",
      headers: authHeaders(true),
      body: JSON.stringify({ content }),
    });
    document.getElementById("drawerCommentInput").value = "";
    await openTaskDetails(taskId);
    await loadTasks();
  } catch (err) {
    setStatus(`Failed to add comment: ${err.message}`, true);
  }
});

function startAutoRefresh() {
  if (refreshTimer) clearInterval(refreshTimer);
  refreshTimer = setInterval(() => {
    loadTasks().catch(() => {});
  }, 20000);
}

ensureLoggedIn();
if (token) {
  refreshCurrentRole();
  applyRoleUi();
  refreshAll();
  startAutoRefresh();
}
