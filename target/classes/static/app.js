const API_BASE = "http://localhost:8080/api";
let token = localStorage.getItem("token") || "";

function authHeaders(includeJson = false) {
  const headers = {};
  if (includeJson) headers["Content-Type"] = "application/json";
  if (token) headers.Authorization = `Bearer ${token}`;
  return headers;
}

function setStatus(message, isError = false) {
  const statusEl = document.getElementById("statusMessage");
  statusEl.textContent = message;
  statusEl.style.color = isError ? "#b00020" : "#0f5132";
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

document.getElementById("registerForm").addEventListener("submit", async (e) => {
  e.preventDefault();

  const username = document.getElementById("registerUsername").value.trim();
  const password = document.getElementById("registerPassword").value;
  const role = document.getElementById("registerRole").value;

  const res = await fetch(`${API_BASE}/auth/register`, {
    method: "POST",
    headers: authHeaders(true),
    body: JSON.stringify({ username, password, role }),
  });

  const body = await readResponseBody(res);
  if (res.ok) {
    setStatus("Registration successful. You can now log in.");
    document.getElementById("registerForm").reset();
  } else {
    setStatus(`Register failed (${res.status}): ${JSON.stringify(body)}`, true);
  }
});

document.getElementById("loginForm").addEventListener("submit", async (e) => {
  e.preventDefault();

  const username = document.getElementById("loginUsername").value.trim();
  const password = document.getElementById("loginPassword").value;

  const res = await fetch(`${API_BASE}/auth/login`, {
    method: "POST",
    headers: authHeaders(true),
    body: JSON.stringify({ username, password }),
  });

  const body = await readResponseBody(res);

  if (!res.ok) {
    setStatus(`Login failed (${res.status})`, true);
    return;
  }

  // Backend currently returns either a plain JWT string or {"error":"..."} with 200.
  if (typeof body === "object" && body.error) {
    setStatus(`Login failed: ${body.error}`, true);
    return;
  }

  if (typeof body === "string" && body.length > 20) {
    token = body;
    localStorage.setItem("token", token);
    setStatus("Login successful.");
    await loadProjects();
    await loadTasks();
    return;
  }

  setStatus("Login response format was unexpected.", true);
});

document.getElementById("projectForm").addEventListener("submit", async (e) => {
  e.preventDefault();

  const name = document.getElementById("projectName").value.trim();
  const res = await fetch(`${API_BASE}/projects`, {
    method: "POST",
    headers: authHeaders(true),
    body: JSON.stringify({ name }),
  });

  const body = await readResponseBody(res);
  if (res.ok) {
    setStatus("Project created.");
    await loadProjects();
  } else {
    setStatus(`Create project failed (${res.status}): ${JSON.stringify(body)}`, true);
  }
});

async function loadProjects() {
  const res = await fetch(`${API_BASE}/projects`, {
    headers: authHeaders(),
  });

  const body = await readResponseBody(res);
  const list = document.getElementById("projectList");

  if (!res.ok) {
    list.innerHTML = "";
    setStatus(`Load projects failed (${res.status}).`, true);
    return;
  }

  const projects = Array.isArray(body) ? body : [];
  list.innerHTML = projects.map((p) => `<li>${p.id}: ${p.name}</li>`).join("");
}

document.getElementById("taskForm").addEventListener("submit", async (e) => {
  e.preventDefault();

  const title = document.getElementById("taskTitle").value.trim();
  const description = document.getElementById("taskDesc").value.trim();
  const status = document.getElementById("taskStatus").value;
  const projectId = Number(document.getElementById("taskProjectId").value);
  const userId = Number(document.getElementById("taskUserId").value);

  const res = await fetch(`${API_BASE}/tasks`, {
    method: "POST",
    headers: authHeaders(true),
    body: JSON.stringify({ title, description, status, projectId, userId }),
  });

  const body = await readResponseBody(res);
  if (res.ok) {
    setStatus("Task created.");
    await loadTasks();
  } else {
    setStatus(`Create task failed (${res.status}): ${JSON.stringify(body)}`, true);
  }
});

async function loadTasks() {
  const res = await fetch(`${API_BASE}/tasks`, {
    headers: authHeaders(),
  });

  const body = await readResponseBody(res);
  const list = document.getElementById("taskList");

  if (!res.ok) {
    list.innerHTML = "";
    setStatus(`Load tasks failed (${res.status}).`, true);
    return;
  }

  const tasks = Array.isArray(body) ? body : [];
  list.innerHTML = tasks
    .map((t) => `<li>${t.id}: ${t.description} (${t.status})</li>`)
    .join("");
}

if (token) {
  loadProjects();
  loadTasks();
}
