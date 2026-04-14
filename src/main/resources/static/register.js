const API_BASE = window.location.port === "8080" ? "/api" : "http://localhost:8080/api";

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

document.getElementById("registerForm").addEventListener("submit", async (e) => {
  e.preventDefault();

  const username = document.getElementById("username").value.trim();
  const password = document.getElementById("password").value;

  let res;
  let body;
  try {
    res = await fetch(`${API_BASE}/auth/register`, {
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
    setStatus(`Register failed (${res.status}): ${JSON.stringify(body)}`, true);
    return;
  }

  setStatus("Registration successful. You can now login.");
  document.getElementById("registerForm").reset();
});
