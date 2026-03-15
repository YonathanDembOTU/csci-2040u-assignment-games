import javax.swing.*;
import java.awt.*;

public class StartUp extends JFrame {

    public StartUp() {
        setTitle("Turn for Turn Co.");
        setSize(760, 540);
        setMinimumSize(new Dimension(640, 480));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        Color neutralGrey = new Color(210, 210, 210);
        Color textDark = new Color(45, 45, 45);
        Color buttonGrey = new Color(120, 120, 120);
        Color buttonBorder = new Color(90, 90, 90);

        // Main background panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(neutralGrey);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Logo
        JLabel logoLabel = createLogoLabel("logo.png", 320, 200);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Welcome text
        JLabel welcomeLabel = new JLabel("Welcome to Turn for Turn Co.");
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomeLabel.setForeground(textDark);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 28));

        JLabel subtitleLabel = new JLabel("Game Database Management System");
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setForeground(new Color(80, 80, 80));
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 18));

        // Login option buttons
        JButton adminButton = createMenuButton("Admin Login", buttonGrey, buttonBorder);
        JButton publisherButton = createMenuButton("Publisher Login", buttonGrey, buttonBorder);
        JButton guestButton = createMenuButton("Guest Access", buttonGrey, buttonBorder);

        adminButton.addActionListener(e -> showLoginDialog(AuthManager.UserRole.ADMIN));
        publisherButton.addActionListener(e -> showLoginDialog(AuthManager.UserRole.PUBLISHER));

        // Guest button opens the UI with view-only access
        guestButton.addActionListener(e -> {
            dispose();
            DataController.launchMainUI(AuthManager.createGuestSession());
        });

        // Layout spacing
        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(logoLabel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(welcomeLabel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createVerticalStrut(30));
        mainPanel.add(adminButton);
        mainPanel.add(Box.createVerticalStrut(12));
        mainPanel.add(publisherButton);
        mainPanel.add(Box.createVerticalStrut(12));
        mainPanel.add(guestButton);
        mainPanel.add(Box.createVerticalGlue());

        add(mainPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    /**
     * Opens a login popup for admin or publisher access.
     */
    private void showLoginDialog(AuthManager.UserRole expectedRole) {
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JCheckBox showPasswordBox = new JCheckBox("Show Password");

        showPasswordBox.addActionListener(e -> {
            if (showPasswordBox.isSelected()) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('•');
            }
        });

        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(showPasswordBox);

        String title = expectedRole == AuthManager.UserRole.ADMIN ? "Admin Login" : "Publisher Login";

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        AuthManager.UserSession session = AuthManager.login(username, password);

        if (session == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Invalid username or password.",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (session.getRole() != expectedRole) {
            JOptionPane.showMessageDialog(
                    this,
                    "That account does not belong to this login type.",
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        dispose();
        DataController.launchMainUI(session);
    }

    private JButton createMenuButton(String text, Color bg, Color border) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setPreferredSize(new Dimension(200, 40));
        button.setMaximumSize(new Dimension(200, 40));
        button.setFocusPainted(false);
        button.setBackground(bg);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(border));
        return button;
    }

    /**
     * Creates a JLabel containing the scaled logo image.
     */
    public static JLabel createLogoLabel(String path, int maxWidth, int maxHeight) {
        JLabel label = new JLabel();
        label.setHorizontalAlignment(SwingConstants.CENTER);

        ImageIcon icon = new ImageIcon(path);

        if (icon.getIconWidth() <= 0 || icon.getIconHeight() <= 0) {
            label.setText("Turn for Turn Co.");
            label.setForeground(new Color(70, 70, 70));
            label.setFont(new Font("Arial", Font.BOLD, 24));
            return label;
        }

        int originalWidth = icon.getIconWidth();
        int originalHeight = icon.getIconHeight();

        // Preserve aspect ratio so the logo is not stretched
        double widthRatio = (double) maxWidth / originalWidth;
        double heightRatio = (double) maxHeight / originalHeight;
        double scale = Math.min(widthRatio, heightRatio);

        int scaledWidth = (int) (originalWidth * scale);
        int scaledHeight = (int) (originalHeight * scale);

        Image scaledImage = icon.getImage().getScaledInstance(
                scaledWidth,
                scaledHeight,
                Image.SCALE_SMOOTH
        );

        label.setIcon(new ImageIcon(scaledImage));
        return label;
    }
}
