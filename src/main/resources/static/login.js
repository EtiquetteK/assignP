const API_BASE = window.location.port === "8080" ? "/api" : "https://assignp-task-management-app-9bf075cabf2c.herokuapp.com/api";

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

document.getElementById("loginForm").addEventListener("submit", async (e) => {
  e.preventDefault();

  const username = document.getElementById("username").value.trim();
  const password = document.getElementById("password").value;

  let res;
  let body;
  try {
    res = await fetch(`${API_BASE}/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password }),
    });
    body = await readResponseBody(res);
  } catch (err) {
    setStatus(`Cannot reach server: ${err.message}`, true);
    return;
  }

  if (!res.ok) {
    setStatus(`Login failed (${res.status})`, true);
    return;
  }

  if (typeof body === "object" && body.error) {
    setStatus(`Login failed: ${body.error}`, true);
    return;
  }

  if (typeof body === "string" && body.length > 20) {
    localStorage.setItem("token", body);
    setStatus("Login successful. Redirecting to dashboard...");
    window.location.href = "dashboard.html";
    return;
  }

  setStatus("Unexpected login response format.", true);
});
