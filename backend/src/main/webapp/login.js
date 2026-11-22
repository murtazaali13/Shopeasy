const loginForm = document.querySelector("#login-form");
const loginMessage = document.querySelector("#login-message");
const logoutButton = document.querySelector("#logout-button");

async function updateSessionStatus() {
  try {
    const response = await fetch("./api/session");
    const result = await response.json();

    if (response.ok && result.authenticated) {
      loginMessage.textContent = `Signed in as ${result.fullName}.`;
      loginForm.hidden = true;
      logoutButton.hidden = false;
    } else {
      loginForm.hidden = false;
      logoutButton.hidden = true;
    }
  } catch (error) {
    console.error("Could not check the login session:", error);
  }
}

loginForm.addEventListener("submit", async (event) => {
  event.preventDefault();

  const submitButton = loginForm.querySelector('button[type="submit"]');
  const formData = new FormData(loginForm);
  const loginDetails = Object.fromEntries(formData.entries());

  submitButton.disabled = true;
  loginMessage.textContent = "Signing in...";

  try {
    const response = await fetch("./api/login", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(loginDetails)
    });

    const result = await response.json();

    if (response.ok) {
      loginForm.reset();
      await updateSessionStatus();
    } else {
      loginMessage.textContent =
        result.message || "Could not sign in. Check your details and try again.";
    }
  } catch (error) {
    console.error("Login request failed:", error);
    loginMessage.textContent =
      "Could not contact ShopEasy. Check that Tomcat is running, then try again.";
  } finally {
    submitButton.disabled = false;
  }
});

logoutButton.addEventListener("click", async () => {
  logoutButton.disabled = true;
  loginMessage.textContent = "Signing out...";

  try {
    const response = await fetch("./api/session", {
      method: "POST"
    });

    const result = await response.json();

    if (response.ok) {
      loginForm.hidden = false;
      logoutButton.hidden = true;
      loginMessage.textContent = result.message;
    } else {
      loginMessage.textContent = "Could not sign out. Please try again.";
    }
  } catch (error) {
    console.error("Sign-out request failed:", error);
    loginMessage.textContent =
      "Could not contact ShopEasy. Check that Tomcat is running, then try again.";
  } finally {
    logoutButton.disabled = false;
  }
});

updateSessionStatus();