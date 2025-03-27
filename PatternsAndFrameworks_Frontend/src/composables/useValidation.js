export function validateLogin({ username, password }) {
    if (!username) {
        return 'Username or email is required.';
    }
    
    // For demo purposes, we're only validating the password format
    // but not actually checking it against a stored password
    if (!password) {
        return 'Password is required.';
    }
    
    return null;
}

export function validateRegister({ username, email, password, confirmPassword }) {
    if (!username || !email || !password || !confirmPassword) {
        return 'All fields are required.';
    }
    if (username.length < 3) {
        return 'Username must be at least 3 characters long.';
    }
    const emailPattern = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;
    if (!emailPattern.test(email)) {
        return 'Please enter a valid email address.';
    }
    if (password !== confirmPassword) {
        return 'Passwords do not match.';
    }
    return null;
}