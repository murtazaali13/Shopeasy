const loginForm = document.querySelector("#login-form");
const loginMessage = document.querySelector("#login-message");

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
      loginMessage.textContent = `Welcome, ${result.fullName}!`;
      loginForm.reset();
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