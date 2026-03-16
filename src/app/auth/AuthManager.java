package app.auth;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AuthManager {
    /**
     * Defines the available user roles in the application.
     */
    public enum UserRole {
        ADMIN,
        PUBLISHER,
        GUEST
    }

    /**
     * Stores the current logged-in user's role and access information.
     */
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

        public boolean isAdmin() {
            return role == UserRole.ADMIN;
        }

        public boolean isPublisher() {
            return role == UserRole.PUBLISHER;
        }

        public boolean isGuest() {
            return role == UserRole.GUEST;
        }

        public boolean canModify() {
            return role == UserRole.ADMIN || role == UserRole.PUBLISHER;
        }
    }

    /**
     * Internal account record used by the login system.
     */
    private static class Account {
        private final String username;
        private final String password;
        private final UserRole role;
        private final String publisherName;

        public Account(String username, String password, UserRole role, String publisherName) {
            this.username = username == null ? "" : username.trim();
            this.password = password == null ? "" : password;
            this.role = role;
            this.publisherName = publisherName == null ? "" : publisherName.trim();
        }
    }

    // Encrypted credentials file and AES key file
    private static final String USERS_FILE_NAME = "users.enc";
    private static final String KEY_FILE_NAME = "secret.key";

    // Stores all login accounts in memory
    private static final Map<String, Account> accounts = new HashMap<>();

    static {
        initializeAccountStorage();
    }

    /**
     * Sets up the key file and encrypted user file.
     * If the encrypted file does not exist yet, it is created from default accounts.
     */
    private static void initializeAccountStorage() {
        try {
            ensureKeyFileExists();

            File usersFile = new File(USERS_FILE_NAME);
            if (!usersFile.exists()) {
                seedDefaultAccounts();
                saveAccountsToEncryptedFile();
            } else {
                loadAccountsFromEncryptedFile();
            }
        } catch (Exception e) {
            accounts.clear();
            seedDefaultAccounts();
        }
    }

    /**
     * Creates the starting admin and publisher accounts used when users.enc does not exist.
     */
    private static void seedDefaultAccounts() {
        accounts.clear();

        // Default admin accounts
        accounts.put("admin", new Account("admin", "admin123", UserRole.ADMIN, ""));
        accounts.put("owner", new Account("owner", "turnforturn", UserRole.ADMIN, ""));

        // Starting publisher accounts from the CSV file
        loadPublisherAccountsFromCsvIntoMap();
    }

    /**
     * Builds publisher accounts from the Publisher column in data.csv.
     * Password format starts as username + 123.
     */
    private static void loadPublisherAccountsFromCsvIntoMap() {
        File file = new File("data.csv");
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return;
            }

            String[] headers = headerLine.split(",");
            int publisherIndex = -1;

            for (int i = 0; i < headers.length; i++) {
                if (headers[i].trim().equalsIgnoreCase("Publisher")) {
                    publisherIndex = i;
                    break;
                }
            }

            if (publisherIndex == -1) {
                return;
            }

            Set<String> seenPublishers = new HashSet<>();
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (publisherIndex >= parts.length) {
                    continue;
                }

                String publisherName = parts[publisherIndex].trim();
                if (publisherName.isEmpty()) {
                    continue;
                }

                String normalizedPublisher = publisherName.toLowerCase();
                if (seenPublishers.contains(normalizedPublisher)) {
                    continue;
                }
                seenPublishers.add(normalizedPublisher);

                String username = makePublisherUsername(publisherName);
                String password = username + "123";

                if (!accounts.containsKey(username.toLowerCase())) {
                    accounts.put(username.toLowerCase(), new Account(
                            username,
                            password,
                            UserRole.PUBLISHER,
                            publisherName
                    ));
                }
            }
        } catch (IOException e) {
            // Keep app running even if CSV publisher loading fails
        }
    }

    /**
     * Creates the AES key file if it does not already exist.
     */
    private static void ensureKeyFileExists() throws IOException, GeneralSecurityException {
        File keyFile = new File(KEY_FILE_NAME);
        if (keyFile.exists()) {
            return;
        }

        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        SecretKey key = keyGenerator.generateKey();
        String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(keyFile))) {
            writer.write(encodedKey);
        }
    }

    /**
     * Reads the AES key from secret.key.
     */
    private static SecretKey getSecretKey() throws IOException {
        String encodedKey = Files.readString(new File(KEY_FILE_NAME).toPath(), StandardCharsets.UTF_8).trim();
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        return new SecretKeySpec(decodedKey, "AES");
    }

    /**
     * Saves all in-memory accounts into the encrypted credentials file.
     */
    private static void saveAccountsToEncryptedFile() throws IOException, GeneralSecurityException {
        StringBuilder plainText = new StringBuilder();
        List<String> usernames = new ArrayList<>(accounts.keySet());
        Collections.sort(usernames);

        for (String key : usernames) {
            Account account = accounts.get(key);
            plainText.append(account.username).append("|")
                    .append(account.role.name()).append("|")
                    .append(account.publisherName).append("|")
                    .append(account.password).append(System.lineSeparator());
        }

        String encryptedText = encrypt(plainText.toString(), getSecretKey());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE_NAME))) {
            writer.write(encryptedText);
        }
    }

    /**
     * Loads accounts from the encrypted credentials file.
     */
    private static void loadAccountsFromEncryptedFile() throws IOException, GeneralSecurityException {
        File usersFile = new File(USERS_FILE_NAME);
        if (!usersFile.exists()) {
            return;
        }

        String encryptedText = Files.readString(usersFile.toPath(), StandardCharsets.UTF_8).trim();
        if (encryptedText.isEmpty()) {
            accounts.clear();
            return;
        }

        String plainText = decrypt(encryptedText, getSecretKey());
        accounts.clear();

        String[] lines = plainText.split("\\R");
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                continue;
            }

            String[] parts = line.split("\\|", -1);
            if (parts.length < 4) {
                continue;
            }

            String username = parts[0].trim();
            String roleText = parts[1].trim();
            String publisherName = parts[2].trim();
            String password = parts[3];

            try {
                UserRole role = UserRole.valueOf(roleText);
                accounts.put(username.toLowerCase(), new Account(username, password, role, publisherName));
            } catch (IllegalArgumentException e) {
                // Ignore malformed lines
            }
        }
    }

    /**
     * Encrypts plain text using AES and returns Base64 text.
     */
    private static String encrypt(String plainText, SecretKey secretKey) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * Decrypts Base64 text using AES.
     */
    private static String decrypt(String encryptedText, SecretKey secretKey) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    /**
     * Converts a publisher name into a simple login username.
     */
    private static String makePublisherUsername(String publisherName) {
        String username = publisherName.toLowerCase().replaceAll("[^a-z0-9]", "");
        return username.isEmpty() ? "publisher" : username;
    }

    /**
     * Attempts to log in using the given username and password.
     */
    public static UserSession login(String username, String password) {
        if (username == null || password == null) {
            return null;
        }

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
     */
    public static UserSession createGuestSession() {
        return new UserSession(UserRole.GUEST, "guest", "");
    }

    /**
     * Returns a table-friendly list of publisher account information.
     */
    public static Object[][] getPublisherAccountTableData() {
        List<Account> publisherAccounts = new ArrayList<>();
        for (Account account : accounts.values()) {
            if (account.role == UserRole.PUBLISHER) {
                publisherAccounts.add(account);
            }
        }

        publisherAccounts.sort((a, b) -> a.username.compareToIgnoreCase(b.username));

        Object[][] rows = new Object[publisherAccounts.size()][3];
        for (int i = 0; i < publisherAccounts.size(); i++) {
            Account account = publisherAccounts.get(i);
            rows[i][0] = account.username;
            rows[i][1] = account.publisherName;
            rows[i][2] = account.password;
        }
        return rows;
    }

    /**
     * Returns all publisher usernames.
     */
    public static String[] getPublisherUsernames() {
        List<String> usernames = new ArrayList<>();
        for (Account account : accounts.values()) {
            if (account.role == UserRole.PUBLISHER) {
                usernames.add(account.username);
            }
        }
        usernames.sort(String::compareToIgnoreCase);
        return usernames.toArray(new String[0]);
    }

    /**
     * Returns the decrypted password for one publisher user.
     */
    public static String getPublisherPassword(String username) {
        if (username == null) {
            return null;
        }

        Account account = accounts.get(username.trim().toLowerCase());
        if (account == null || account.role != UserRole.PUBLISHER) {
            return null;
        }

        return account.password;
    }

    /**
     * Adds a new publisher user and saves the encrypted file.
     */
    public static boolean addPublisherUser(String username, String publisherName, String password) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        if (publisherName == null || publisherName.trim().isEmpty()) {
            return false;
        }
        if (password == null || password.isEmpty()) {
            return false;
        }

        String cleanUsername = username.trim().toLowerCase();
        String cleanPublisherName = publisherName.trim();

        if (accounts.containsKey(cleanUsername)) {
            return false;
        }

        accounts.put(cleanUsername, new Account(cleanUsername, password, UserRole.PUBLISHER, cleanPublisherName));

        try {
            saveAccountsToEncryptedFile();
            return true;
        } catch (Exception e) {
            accounts.remove(cleanUsername);
            return false;
        }
    }

    /**
     * Updates one publisher password and saves the encrypted file.
     */
    public static boolean updatePublisherPassword(String username, String newPassword) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        if (newPassword == null || newPassword.isEmpty()) {
            return false;
        }

        String cleanUsername = username.trim().toLowerCase();
        Account account = accounts.get(cleanUsername);
        if (account == null || account.role != UserRole.PUBLISHER) {
            return false;
        }

        accounts.put(cleanUsername, new Account(account.username, newPassword, account.role, account.publisherName));

        try {
            saveAccountsToEncryptedFile();
            return true;
        } catch (Exception e) {
            accounts.put(cleanUsername, account);
            return false;
        }
    }
}
