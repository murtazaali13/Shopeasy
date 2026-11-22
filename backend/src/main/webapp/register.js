const registrationForm = document.querySelector("#registration-form");
const registrationMessage = document.querySelector("#registration-message");

registrationForm.addEventListener("submit", async (event) => {
  event.preventDefault();

  const submitButton = registrationForm.querySelector(
    'button[type="submit"]'
  );

  const formData = new FormData(registrationForm);
  const registrationDetails = Object.fromEntries(formData.entries());

  submitButton.disabled = true;
  registrationMessage.textContent = "Creating your account...";

  try {
    const response = await fetch("./api/register", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(registrationDetails)
    });

    const result = await response.json();
    registrationMessage.textContent =
      result.message || "The request has finished.";

    if (response.ok) {
      registrationForm.reset();
    }
  } catch (error) {
    console.error("Registration request failed:", error);
    registrationMessage.textContent =
      "Could not contact ShopEasy. Check that Tomcat is running, then try again.";
  } finally {
    submitButton.disabled = false;
  }
});