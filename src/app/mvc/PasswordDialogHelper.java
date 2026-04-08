package app.mvc;

import app.auth.AuthManager;

import javax.swing.*;
import java.awt.*;

/**
 * Provides themed password-management dialogs for viewing and updating publisher credentials.
 */
public class PasswordDialogHelper {
    /**
     * Shows the password-management entry menu and routes the user to the selected operation.
     *
     * @param parent the parent component used for ownership and theme lookup
     */
    public static void openPasswordHandlingMenu(JFrame parent) {
        String[] options = { "View Publisher Passwords", "Change Publisher Password", "Add Publisher User" };
        String choice = (String) JOptionPane.showInputDialog(
                parent,
                "Choose a password handling option:",
                "Password Handling",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == null) {
            return;
        }

        if (choice.equals(options[0])) {
            showPublisherPasswordTable(parent);
        } else if (choice.equals(options[1])) {
            changePublisherPassword(parent);
        } else if (choice.equals(options[2])) {
            addPublisherUser(parent);
        }
    }

    /**
     * Shows a read-only table of publisher credentials.
     *
     * @param parent the parent component used for ownership and theme lookup
     */
    private static void showPublisherPasswordTable(JFrame parent) {
        Object[][] data = AuthManager.getPublisherAccountTableData();
        String[] columns = { "Username", "Publisher", "Password" };

        JTable passwordTable = new JTable(data, columns);
        passwordTable.setEnabled(false);
        JScrollPane pane = new JScrollPane(passwordTable);
        pane.setPreferredSize(new Dimension(620, 280));

        JOptionPane.showMessageDialog(
                parent,
                pane,
                "Publisher Passwords",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Shows the password-change dialog for an existing publisher account.
     *
     * @param parent the parent component used for ownership and theme lookup
     */
    private static void changePublisherPassword(JFrame parent) {
        String[] usernames = AuthManager.getPublisherUsernames();
        if (usernames.length == 0) {
            JOptionPane.showMessageDialog(parent, "No publisher users found.");
            return;
        }

        JComboBox<String> userCombo = new JComboBox<>(usernames);
        JPasswordField passwordField = new JPasswordField();
        JCheckBox showBox = new JCheckBox("Show Password");
        showBox.addActionListener(e -> passwordField.setEchoChar(showBox.isSelected() ? (char) 0 : '•'));

        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.add(new JLabel("Select Publisher Username:"));
        panel.add(userCombo);
        panel.add(new JLabel("New Password:"));
        panel.add(passwordField);
        panel.add(showBox);

        int result = JOptionPane.showConfirmDialog(parent, panel, "Change Publisher Password",
                JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String username = (String) userCombo.getSelectedItem();
        String newPassword = new String(passwordField.getPassword());

        if (newPassword.trim().isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Password cannot be blank.");
            return;
        }

        if (AuthManager.updatePublisherPassword(username, newPassword)) {
            JOptionPane.showMessageDialog(parent, "Publisher password updated.");
        } else {
            JOptionPane.showMessageDialog(parent, "Could not update publisher password.");
        }
    }

    /**
     * Shows the dialog used to create a new publisher account.
     *
     * @param parent the parent component used for ownership and theme lookup
     */
    private static void addPublisherUser(JFrame parent) {
        JTextField usernameField = new JTextField();
        JTextField publisherField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JCheckBox showBox = new JCheckBox("Show Password");
        showBox.addActionListener(e -> passwordField.setEchoChar(showBox.isSelected() ? (char) 0 : '•'));

        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Publisher Name:"));
        panel.add(publisherField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(showBox);

        int result = JOptionPane.showConfirmDialog(parent, panel, "Add Publisher User", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String username = usernameField.getText().trim();
        String publisherName = publisherField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (AuthManager.addPublisherUser(username, publisherName, password)) {
            JOptionPane.showMessageDialog(parent, "Publisher user added.");
        } else {
            JOptionPane.showMessageDialog(parent, "Could not add publisher user. Username may already exist.");
        }
    }
}
