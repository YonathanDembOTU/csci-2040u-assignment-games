package app;

import app.auth.AuthManager;
import app.mvc.DataController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class StartUp extends JFrame {

    private final Color neutralGrey = new Color(210, 210, 210);
    private final Color textDark = new Color(45, 45, 45);
    private final Color buttonGrey = new Color(170, 170, 170, 110);
    private final Color buttonHover = new Color(199, 199, 199, 225);
    private final Color buttonPressed = new Color(92, 92, 92, 235);
    private final Color buttonBorder = new Color(90, 90, 90);

    public StartUp() {
        setTitle("Turn for Turn Co.");
        setSize(760, 540);
        setMinimumSize(new Dimension(640, 480));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        BackgroundPanel mainPanel = new BackgroundPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(neutralGrey);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel logoLabel = createLogoLabel("assets/logo.png", 320, 200);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel welcomeLabel = new JLabel("Welcome to Turn for Turn Co.");
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomeLabel.setForeground(textDark);
        welcomeLabel.setFont(new Font("Inter", Font.BOLD, 28));

        JLabel subtitleLabel = new JLabel("Game Database Management System");
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setForeground(new Color(80, 80, 80));
        subtitleLabel.setFont(new Font("Inter", Font.PLAIN, 18));

        JButton adminButton = createMenuButton("Admin Login", buttonGrey, buttonBorder);
        JButton publisherButton = createMenuButton("Publisher Login", buttonGrey, buttonBorder);
        JButton guestButton = createMenuButton("Guest Access", buttonGrey, buttonBorder);

        adminButton.setForeground(textDark);
        publisherButton.setForeground(textDark);
        guestButton.setForeground(textDark);

        adminButton.addActionListener(e -> showLoginDialog(AuthManager.UserRole.ADMIN));
        publisherButton.addActionListener(e -> showLoginDialog(AuthManager.UserRole.PUBLISHER));

        guestButton.addActionListener(e -> {
            dispose();
            DataController.launchMainUI(AuthManager.createGuestSession());
        });

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
        button.setPreferredSize(new Dimension(220, 42));
        button.setMaximumSize(new Dimension(220, 42));
        button.setFocusPainted(false);
        button.setBackground(bg);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorder(BorderFactory.createLineBorder(border));
        button.setFont(new Font("Inter", Font.BOLD, 14));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(buttonHover);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bg);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(buttonPressed);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(button.contains(e.getPoint()) ? buttonHover : bg);
                }
            }
        });

        return button;
    }

    public static JLabel createLogoLabel(String path, int maxWidth, int maxHeight) {
        JLabel label = new JLabel();
        label.setHorizontalAlignment(SwingConstants.CENTER);

        ImageIcon icon = new ImageIcon(path);

        if (icon.getIconWidth() <= 0 || icon.getIconHeight() <= 0) {
            label.setText("Turn for Turn Co.");
            label.setForeground(new Color(70, 70, 70));
            label.setFont(new Font("Inter", Font.BOLD, 24));
            return label;
        }

        int originalWidth = icon.getIconWidth();
        int originalHeight = icon.getIconHeight();

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

    private class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // base background
            g2.setColor(getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());

            // hex pattern (subtle)
            Color lattice = new Color(255, 170, 0, 20); // LOWER alpha fixes overlap issue
            int radius = 16;

            double hexHeight = Math.sqrt(3) * radius;
            double xStep = 1.5 * radius;
            double yStep = hexHeight;

            g2.setColor(lattice);
            g2.setStroke(new BasicStroke(1.2f)); // thinner lines = no visual collision

            for (int col = -2; col < (int)(getWidth() / xStep) + 3; col++) {
                double x = col * xStep;
                double yOffset = (col % 2 == 0) ? 0 : hexHeight / 2.0;

                for (int row = -2; row < (int)(getHeight() / yStep) + 3; row++) {
                    double y = row * yStep + yOffset;
                    Polygon hex = createHexagon((int)x, (int)y, radius);
                    g2.drawPolygon(hex);
                }
            }

            g2.dispose();
        }

        private Polygon createHexagon(int x, int y, int size) {
            int[] xs = {
                    x + size / 2,
                    x + (3 * size) / 2,
                    x + 2 * size,
                    x + (3 * size) / 2,
                    x + size / 2,
                    x
            };
            int h = (int) (Math.sqrt(3) * size / 2);
            int[] ys = {
                    y,
                    y,
                    y + h,
                    y + 2 * h,
                    y + 2 * h,
                    y + h
            };
            return new Polygon(xs, ys, 6);
        }
    }
}
