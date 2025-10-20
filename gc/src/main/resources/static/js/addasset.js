function disableFields(disable, container) {
    container.querySelectorAll("input, select").forEach(function (input) {
        input.disabled = disable;
    });
}

function validateForm() {
    var email = document.getElementById('email').value.trim();
    var password = document.getElementById('password').value.trim();
    var fname = document.getElementById('firstname').value.trim();
    var lname = document.getElementById('lastname').value.trim();
    var age = document.getElementById('age').value.trim();
    var phone = document.getElementById('phone').value.trim();
    var address = document.getElementById('address').value.trim();
    var emailError = document.getElementById("error-email");
    var passError = document.getElementById("error-pass");
    var lname_error = document.getElementById("error-lname");
    var fname_error = document.getElementById("error-fname");
    var age_error = document.getElementById("error-age");
    var address_error = document.getElementById("error-address");
    var phone_error = document.getElementById("error-phone");

    var isValid = true;

    if (!email) {
        emailError.textContent = "Email is required";
        isValid = false;
        return false;
    }

    if (password === "") {
        passError.textContent = "Password can't be empty";
        isValid = false;
        return false;
    }
    if (password.length < 6) {
        passError.textContent = "Password cannot be less than 6 characters";
        isValid = false;
        return false; 
    }

    if (!isValid) {
        event.preventDefault();
    }
    return isValid;
}