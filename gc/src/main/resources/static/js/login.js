function validateLoginForm() {
    var email = document.getElementById("email").value.trim();
    var pass = document.getElementById("pass").value.trim();
    var emailError = document.getElementById("email-error");
    var passError = document.getElementById("pass-error");
    var isValid = true;

    emailError.textContent = "";
    passError.textContent = "";

    // Email validation: Check if the email is not empty and is valid
    var emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (email === "") {
        emailError.textContent = "Email is required";
        isValid = false;
    } else if (!emailPattern.test(email)) {
        emailError.textContent = "Please enter a valid email address";
        isValid = false;
    }

    // Password validation: Ensure password is not empty
    if (pass === "") {
        passError.textContent = "Password is required";
        isValid = false;
    } else if (pass.length < 6) {
        passError.textContent = "Password must be at least 6 characters";
        isValid = false;
    }

    return isValid;
}

    function getParameterByName(name, url) {
        var errormess = document.getElementById("Error-login");
        if (!url) url = window.location.href;
        name = name.replace(/[\[\]]/g, "\\$&");
        var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
            results = regex.exec(url);
        if (!results) return null;
        if (!results[2]) return '';
        return decodeURIComponent(results[2].replace(/\+/g, " "));
    }

    var errorMessage = getParameterByName('error');
    if (errorMessage) {
        var errorLogin = document.getElementById("Error-login");
        errorLogin.textContent = errorMessage;
    }