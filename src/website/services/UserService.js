const fs = require("fs");

class UserService {
    constructor(filePath) {
        this.filePath = filePath;

        // Ensure the file exists with a header row
        if (!fs.existsSync(this.filePath)) {
            fs.writeFileSync(this.filePath, "username,password,role\r\n");
        }
    }

    // Read all lines from the file
    _readLines() {
        const data = fs.readFileSync(this.filePath, "utf-8");
        return data.split(/\r?\n/);
    }

    // Register a new user if username is not already taken
    register(username, password) {
        if (!username || !password) {
            return { success: false, message: "Username and password are required." };
        }

        const lines = this._readLines();

        // Check for duplicate username
        for (let i = 1; i < lines.length; i++) {
            const [user] = lines[i].split(",");
            if (user === username) {
                return { success: false, message: "User already exists" };
            }
        }

        // Append new user with default role "user"
        fs.appendFileSync(this.filePath, `${username},${password},user\r\n`);
        return { success: true, message: "Account created!" };
    }

    // Validate login credentials
    login(username, password) {
        if (!username || !password) {
            return { success: false, message: "Username and password are required." };
        }

        const lines = this._readLines();

        // Search for matching username and password
        for (let i = 1; i < lines.length; i++) {
            const [user, pass, role] = lines[i].split(",");
            if (user === username && pass === password) {
                return { success: true, message: "Login successful!", role };
            }
        }

        return { success: false, message: "Invalid username or password" };
    }

    // Check if a user exists by username
    userExists(username) {
        const lines = this._readLines();

        for (let i = 1; i < lines.length; i++) {
            const [user] = lines[i].split(",");
            if (user === username) return true;
        }

        return false;
    }

    // Get the role of a user
    getRole(username) {
        const lines = this._readLines();

        for (let i = 1; i < lines.length; i++) {
            const [user, , role] = lines[i].split(",");
            if (user === username) return role?.trim() || null;
        }

        return null;
    }
}

module.exports = UserService;