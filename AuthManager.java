import java.util.HashMap;
import java.util.Map;

public class AuthManager {

    // User access levels supported by the system
    public enum UserRole {
        ADMIN,
        PUBLISHER,
        GUEST
    }

    // Stores information about the currently logged-in user
    public static class UserSession {
        private final UserRole role;
        private final String username;
        private final String publisherName;

        public UserSession(UserRole role, String username, String publisherName) {
            this.role = role;
            this.username = username == null ? "" : username.trim();
            this.publisherName = publisherName == null ? "" : publisherName.trim();
        }

        public UserRole getRole() {
            return role;
        }

        public String getUsername() {
            return username;
        }

        public String getPublisherName() {
            return publisherName;
        }

        /**
         * Returns true if the user is an admin.
         *
         * @return whether the role is ADMIN
         */
        public boolean isAdmin() {
            return role == UserRole.ADMIN;
        }

        /**
         * Returns true if the user is a publisher.
         *
         * @return whether the role is PUBLISHER
         */
        public boolean isPublisher() {
            return role == UserRole.PUBLISHER;
        }

        /**
         * Returns true if the user is a guest.
         *
         * @return whether the role is GUEST
         */
        public boolean isGuest() {
            return role == UserRole.GUEST;
        }

        /**
         * Returns whether the user can modify data.
         *
         * @return true for admin and publisher users
         */
        public boolean canModify() {
            return role == UserRole.ADMIN || role == UserRole.PUBLISHER;
        }
    }

    // Internal account record used by the login system
    private static class Account {
        private final String username;
        private final String password;
        private final UserRole role;
        private final String publisherName;

        public Account(String username, String password, UserRole role, String publisherName) {
            this.username = username;
            this.password = password;
            this.role = role;
            this.publisherName = publisherName == null ? "" : publisherName;
        }
    }

    // Stores all hardcoded login accounts
    private static final Map<String, Account> accounts = new HashMap<>();

    static {
        // Admin accounts
        accounts.put("admin", new Account("admin", "admin123", UserRole.ADMIN, ""));
        accounts.put("owner", new Account("owner", "turnforturn", UserRole.ADMIN, ""));

        // Publisher accounts
        accounts.put("nintendo", new Account("nintendo", "mario123", UserRole.PUBLISHER, "Nintendo"));
        accounts.put("sony", new Account("sony", "ps123", UserRole.PUBLISHER, "Sony"));
        accounts.put("capcom", new Account("capcom", "ryu123", UserRole.PUBLISHER, "Capcom"));
    }

    /**
     * Attempts to log in using the given username and password.
     *
     * @param username entered username
     * @param password entered password
     * @return a user session if login succeeds, otherwise null
     */
    public static UserSession login(String username, String password) {
        if (username == null || password == null) {
            return null;
        }

        // Usernames are matched without case sensitivity
        Account account = accounts.get(username.trim().toLowerCase());

        if (account == null) {
            return null;
        }

        if (!account.password.equals(password)) {
            return null;
        }

        return new UserSession(account.role, account.username, account.publisherName);
    }

    /**
     * Creates a guest session with view-only access.
     *
     * @return guest session
     */
    public static UserSession createGuestSession() {
        return new UserSession(UserRole.GUEST, "guest", "");
    }
}