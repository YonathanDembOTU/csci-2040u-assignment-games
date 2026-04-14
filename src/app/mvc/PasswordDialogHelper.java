package app.mvc;

import app.auth.AuthManager;

import javax.swing.*;
import java.awt.*;

/**
 * Encapsulates the admin-only password management dialogs for publisher
 * accounts.
 * <p>
 * The helper presents themed dialogs for viewing current publisher credentials,
 * changing passwords, and creating new publisher users through the
 * {@code AuthManager}.
 */
public class PasswordDialogHelper {
    /**
     * Opens the top-level password handling menu and routes the user to the
     * selected admin action.
     *
     * @param parent owning frame used for dialog placement and theme lookup
     */
    public static void openPasswordHandlingMenu(JFrame parent) {
        String[] options = {"View Publisher Passwords", "Change Publisher Password", "Add Publisher User"};
        int choice = AppDialogThemeHelper.showOptionDialog(
                parent,
                "Password Handling",
                "Choose a password handling option:",
                options,
                0);

        if (choice == JOptionPane.CLOSED_OPTION) {
            return;
        }

        if (choice == 0) {
            showPublisherPasswordTable(parent);
        } else if (choice == 1) {
            changePublisherPassword(parent);
        } else if (choice == 2) {
            addPublisherUser(parent);
        }
    }

    /**
     * Displays the current publisher account table in a themed read-only view.
     *
     * @param parent owning frame used for dialog placement and theme lookup
     */
    private static void showPublisherPasswordTable(JFrame parent) {
        boolean dark = AppDialogThemeHelper.isDark(parent);

        AuthManager.syncPublisherAccountsFromCsvAndAutoGeneratePasswords();

        Object[][] data = AuthManager.getPublisherAccountTableData();
        String[] columns = {"Username", "Publisher", "Password"};

        JTable passwordTable = new JTable(data, columns);
        passwordTable.setEnabled(false);
        AppDialogThemeHelper.styleTable(passwordTable, dark);

        JScrollPane tablePane = new JScrollPane(passwordTable);
        tablePane.setPreferredSize(new Dimension(620, 280));
        AppDialogThemeHelper.styleScrollPane(tablePane, dark);

        JTextArea infoArea = new JTextArea(
                "Publisher accounts shown below are refreshed from data/data.csv when this page opens. " +
                "Each listed publisher is assigned the auto-generated password used by the authentication helper.");
        infoArea.setEditable(false);
        infoArea.setWrapStyleWord(true);
        infoArea.setLineWrap(true);
        infoArea.setOpaque(false);
        infoArea.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        infoArea.setFont(new Font("Inter", Font.PLAIN, 13));
        infoArea.setForeground(AppDialogThemeHelper.getTheme(parent).mutedText);

        JPanel content = AppDialogThemeHelper.createSurfacePanel(new BorderLayout(0, 6), dark);
        content.add(infoArea, BorderLayout.NORTH);
        content.add(tablePane, BorderLayout.CENTER);

        AppDialogThemeHelper.showContentDialog(parent, "Publisher Passwords", content);
    }

    /**
     * Prompts the admin to select a publisher account and change its password.
     *
     * @param parent owning frame used for dialog placement and theme lookup
     */
    private static void changePublisherPassword(JFrame parent) {
        boolean dark = AppDialogThemeHelper.isDark(parent);
        String[] usernames = AuthManager.getPublisherUsernames();
        if (usernames.length == 0) {
            AppDialogThemeHelper.showMessageDialog(parent, "Password Handling", "No publisher users found.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JComboBox<String> userCombo = new JComboBox<>(usernames);
        AppDialogThemeHelper.styleComboBox(userCombo, dark);

        JPasswordField passwordField = new JPasswordField();
        AppDialogThemeHelper.stylePasswordField(passwordField, dark);

        JCheckBox showBox = new JCheckBox("Show Password");
        AppDialogThemeHelper.styleCheckBox(showBox, dark);
        showBox.addActionListener(e -> passwordField.setEchoChar(showBox.isSelected() ? (char) 0 : '•'));

        JPanel panel = AppDialogThemeHelper.createSurfacePanel(new GridLayout(0, 1, 6, 6), dark);
        JLabel selectLabel = new JLabel("Select Publisher Username:");
        JLabel passwordLabel = new JLabel("New Password:");
        AppDialogThemeHelper.styleLabel(selectLabel, false, dark);
        AppDialogThemeHelper.styleLabel(passwordLabel, false, dark);
        panel.add(selectLabel);
        panel.add(userCombo);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(showBox);

        int result = AppDialogThemeHelper.showConfirmDialog(parent, "Change Publisher Password", panel);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String username = (String) userCombo.getSelectedItem();
        String newPassword = new String(passwordField.getPassword());

        if (newPassword.trim().isEmpty()) {
            AppDialogThemeHelper.showMessageDialog(parent, "Password Handling", "Password cannot be blank.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (AuthManager.updatePublisherPassword(username, newPassword)) {
            AppDialogThemeHelper.showMessageDialog(parent, "Password Handling", "Publisher password updated.", JOptionPane.INFORMATION_MESSAGE);
        } else {
            AppDialogThemeHelper.showMessageDialog(parent, "Password Handling", "Could not update publisher password.", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Prompts the admin to create a new publisher user account.
     *
     * @param parent owning frame used for dialog placement and theme lookup
     */
    private static void addPublisherUser(JFrame parent) {
        boolean dark = AppDialogThemeHelper.isDark(parent);

        JTextField usernameField = new JTextField();
        JTextField publisherField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JCheckBox showBox = new JCheckBox("Show Password");

        AppDialogThemeHelper.styleTextField(usernameField, dark);
        AppDialogThemeHelper.styleTextField(publisherField, dark);
        AppDialogThemeHelper.stylePasswordField(passwordField, dark);
        AppDialogThemeHelper.styleCheckBox(showBox, dark);

        showBox.addActionListener(e -> passwordField.setEchoChar(showBox.isSelected() ? (char) 0 : '•'));

        JPanel panel = AppDialogThemeHelper.createSurfacePanel(new GridLayout(0, 1, 6, 6), dark);
        JLabel usernameLabel = new JLabel("Username:");
        JLabel publisherLabel = new JLabel("Publisher Name:");
        JLabel passwordLabel = new JLabel("Password:");
        AppDialogThemeHelper.styleLabel(usernameLabel, false, dark);
        AppDialogThemeHelper.styleLabel(publisherLabel, false, dark);
        AppDialogThemeHelper.styleLabel(passwordLabel, false, dark);

        panel.add(usernameLabel);
        panel.add(usernameField);
        panel.add(publisherLabel);
        panel.add(publisherField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(showBox);

        int result = AppDialogThemeHelper.showConfirmDialog(parent, "Add Publisher User", panel);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String username = usernameField.getText().trim();
        String publisherName = publisherField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || publisherName.isEmpty() || password.trim().isEmpty()) {
            AppDialogThemeHelper.showMessageDialog(parent, "Password Handling", "All fields are required.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (AuthManager.addPublisherUser(username, publisherName, password)) {
            AppDialogThemeHelper.showMessageDialog(parent, "Password Handling", "Publisher user added.", JOptionPane.INFORMATION_MESSAGE);
        } else {
            AppDialogThemeHelper.showMessageDialog(parent, "Password Handling", "Could not add publisher user.", JOptionPane.ERROR_MESSAGE);
        }
    }
}
