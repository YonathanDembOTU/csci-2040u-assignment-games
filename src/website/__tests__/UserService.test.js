const fs = require("fs");
const path = require("path");
const os = require("os");
const UserService = require("../services/UserService");

let tempFile;

beforeEach(() => {
    tempFile = path.join(os.tmpdir(), `users_test_${Date.now()}.csv`);
});

afterEach(() => {
    if (fs.existsSync(tempFile)) fs.unlinkSync(tempFile);
});

// constructor
describe("UserService - constructor", () => {
    test("creates CSV with header if file does not exist", () => {
        new UserService(tempFile);
        expect(fs.existsSync(tempFile)).toBe(true);
        expect(fs.readFileSync(tempFile, "utf-8")).toContain("username,password,role");
    });

    test("does not overwrite an existing CSV", () => {
        fs.writeFileSync(tempFile, "username,password,role\r\nalice,pass,user\r\n");
        new UserService(tempFile);
        expect(fs.readFileSync(tempFile, "utf-8")).toContain("alice");
    });
});

// register()
describe("UserService - register()", () => {
    test("returns success for a new user", () => {
        const svc = new UserService(tempFile);
        expect(svc.register("alice", "pass123")).toMatchObject({ success: true });
    });

    test("persists the user to the CSV", () => {
        const svc = new UserService(tempFile);
        svc.register("alice", "pass123");
        expect(fs.readFileSync(tempFile, "utf-8")).toContain("alice,pass123,user");
    });

    test("assigns 'user' role by default", () => {
        const svc = new UserService(tempFile);
        svc.register("alice", "pass123");
        expect(svc.getRole("alice")).toBe("user");
    });

    test("rejects a duplicate username", () => {
        const svc = new UserService(tempFile);
        svc.register("alice", "pass123");
        expect(svc.register("alice", "other")).toMatchObject({ success: false, message: "User already exists" });
    });

    test("rejects an empty username", () => {
        const svc = new UserService(tempFile);
        expect(svc.register("", "pass123").success).toBe(false);
    });

    test("rejects an empty password", () => {
        const svc = new UserService(tempFile);
        expect(svc.register("alice", "").success).toBe(false);
    });

    test("allows multiple distinct users", () => {
        const svc = new UserService(tempFile);
        svc.register("alice", "p1");
        svc.register("bob", "p2");
        expect(svc.userExists("alice")).toBe(true);
        expect(svc.userExists("bob")).toBe(true);
    });
});

// login()
describe("UserService - login()", () => {
    test("succeeds with correct credentials", () => {
        const svc = new UserService(tempFile);
        svc.register("alice", "pass123");
        expect(svc.login("alice", "pass123")).toMatchObject({ success: true, message: "Login successful!" });
    });

    test("returns the user role on success", () => {
        const svc = new UserService(tempFile);
        svc.register("alice", "pass123");
        expect(svc.login("alice", "pass123").role).toBe("user");
    });

    test("fails with wrong password", () => {
        const svc = new UserService(tempFile);
        svc.register("alice", "pass123");
        expect(svc.login("alice", "wrong").success).toBe(false);
    });

    test("fails for a non-existent user", () => {
        const svc = new UserService(tempFile);
        expect(svc.login("ghost", "pass").success).toBe(false);
    });

    test("is case-sensitive for username", () => {
        const svc = new UserService(tempFile);
        svc.register("alice", "pass123");
        expect(svc.login("Alice", "pass123").success).toBe(false);
    });

    test("is case-sensitive for password", () => {
        const svc = new UserService(tempFile);
        svc.register("alice", "Pass123");
        expect(svc.login("alice", "pass123").success).toBe(false);
    });

    test("rejects an empty username", () => {
        const svc = new UserService(tempFile);
        expect(svc.login("", "pass123").success).toBe(false);
    });

    test("rejects an empty password", () => {
        const svc = new UserService(tempFile);
        expect(svc.login("alice", "").success).toBe(false);
    });
});

// userExists()
describe("UserService - userExists()", () => {
    test("returns true for a registered user", () => {
        const svc = new UserService(tempFile);
        svc.register("alice", "pass");
        expect(svc.userExists("alice")).toBe(true);
    });

    test("returns false for an unknown user", () => {
        const svc = new UserService(tempFile);
        expect(svc.userExists("ghost")).toBe(false);
    });
});

// getRole()
describe("UserService - getRole()", () => {
    test("returns 'user' for a registered account", () => {
        const svc = new UserService(tempFile);
        svc.register("alice", "pass");
        expect(svc.getRole("alice")).toBe("user");
    });

    test("returns 'admin' for a manually seeded admin account", () => {
        fs.writeFileSync(tempFile, "username,password,role\r\nadmin,adminpass,admin\r\n");
        const svc = new UserService(tempFile);
        expect(svc.getRole("admin")).toBe("admin");
    });

    test("returns null for a non-existent user", () => {
        const svc = new UserService(tempFile);
        expect(svc.getRole("nobody")).toBeNull();
    });
});