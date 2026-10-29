// View elements
const menuView = document.getElementById("menuView");
const signUpView = document.getElementById("signUpView");
const signInView = document.getElementById("signInView");

// App Screens
const authScreen = document.getElementById("authScreen");
const lobbyScreen = document.getElementById("lobbyScreen");
const displayUsername = document.getElementById("displayUsername");

// Menu Buttons
document
  .getElementById("showSignUp")
  .addEventListener("click", () => switchView(signUpView));
document
  .getElementById("showSignIn")
  .addEventListener("click", () => switchView(signInView));
document
  .getElementById("backFromRegister")
  .addEventListener("click", () => switchView(menuView));
document
  .getElementById("backFromLogin")
  .addEventListener("click", () => switchView(menuView));

function switchView(targetView) {
  menuView.classAll = [signUpView, signInView, menuView].forEach((view) =>
    view.classList.add("hidden-form"),
  );
  targetView.classList.remove("hidden-form");
}

// Guest Bypass handler
document.getElementById("guestBtn").addEventListener("click", () => {
  enterGameLobby("GUEST_PLAYER");
});

// Handle Account Creation with matching check
document.getElementById("signUpForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const username = document.getElementById("regUser").value;
  const password = document.getElementById("regPass").value;
  const passwordConfirm = document.getElementById("regPassConfirm").value; // Second password input
  const msg = document.getElementById("signUpMsg");

  // Requirement Check: Confirm passwords match locally before making server requests
  if (password !== passwordConfirm) {
    msg.textContent = "VERIFICATION ERROR: PASSWORDS DO NOT MATCH";
    msg.style.color = "#ff4444";
    return; // Exits function immediately, preventing registration
  }

  try {
    const response = await fetch("gateway.php", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ operation: "register", username, password }),
    });
    const result = await response.json();

    msg.textContent = result.message;
    msg.style.color = result.status === "success" ? "#ffcc00" : "#ff4444";

    if (result.status === "success") {
      document.getElementById("signUpForm").reset();
      setTimeout(() => switchView(signInView), 1500);
    }
  } catch (err) {
    msg.textContent = "GATEWAY ERROR: CHECK XAMPP MODULES";
    msg.style.color = "#ff4444";
  }
});

// Handle Account Verification / Login
document.getElementById("signInForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const username = document.getElementById("loginUser").value;
  const password = document.getElementById("loginPass").value;
  const msg = document.getElementById("signInMsg");

  try {
    const response = await fetch("gateway.php", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ operation: "login", username, password }),
    });
    const result = await response.json();

    if (result.status === "success") {
      msg.textContent = "ACCESS AUTHORIZED";
      msg.style.color = "#ffcc00";
      setTimeout(() => enterGameLobby(username), 1000);
    } else {
      msg.textContent = result.message;
      msg.style.color = "#ff4444";
    }
  } catch (err) {
    msg.textContent = "GATEWAY ERROR: CHECK INTERNET LINK";
    msg.style.color = "#ff4444";
  }
});

function enterGameLobby(user) {
  displayUsername.textContent = user.toUpperCase();
  authScreen.classList.remove("active");
  lobbyScreen.classList.add("active");

  const subText = document.getElementById("lobbySubText");
  if (subText) {
    subText.innerHTML =
      "SUCCESSFULLY LOGGED IN! YOU CAN NOW CLOSE THIS TAB AND RETURN TO THE GAME.";
    subText.style.color = "#ffcc00";
  }
}

// Log Out Handler
document.getElementById("logOutBtn").addEventListener("click", () => {
  lobbyScreen.classList.remove("active");
  authScreen.classList.add("active");
  switchView(menuView);
});
