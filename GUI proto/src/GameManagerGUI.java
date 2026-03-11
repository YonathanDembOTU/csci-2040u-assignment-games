import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class GameManagerGUI {

    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    private ArrayList<Game> games = new ArrayList<>();
    private DefaultTableModel tableModel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameManagerGUI());
    }

    public GameManagerGUI() {
        frame = new JFrame("Game Manager System");
        frame.setSize(700, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createLoginPanel(), "Login");
        mainPanel.add(createDashboardPanel(), "Dashboard");

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));

        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField();

        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField();

        JButton loginButton = new JButton("Login");

        loginButton.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());

            if (username.equals("admin") && password.equals("1234")) {
                cardLayout.show(mainPanel, "Dashboard");
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid credentials!");
            }
        });

        panel.add(userLabel);
        panel.add(userField);
        panel.add(passLabel);
        panel.add(passField);
        panel.add(loginButton);

        return panel;
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = {"Title", "Genre", "Platform"};
        tableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(tableModel);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        JButton addBtn = new JButton("Add Game");
        JButton editBtn = new JButton("Edit Game");
        JButton deleteBtn = new JButton("Delete Game");
        JButton updateBtn = new JButton("Update Game Info");

        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(updateBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> {
            Game game = showGameDialog(null);
            if (game != null) {
                games.add(game);
                tableModel.addRow(new Object[]{
                        game.getTitle(),
                        game.getGenre(),
                        game.getPlatform()
                });
            }
        });

        editBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                Game existing = games.get(selectedRow);
                Game updated = showGameDialog(existing);

                if (updated != null) {
                    games.set(selectedRow, updated);
                    tableModel.setValueAt(updated.getTitle(), selectedRow, 0);
                    tableModel.setValueAt(updated.getGenre(), selectedRow, 1);
                    tableModel.setValueAt(updated.getPlatform(), selectedRow, 2);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Select a game first!");
            }
        });

        deleteBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                games.remove(selectedRow);
                tableModel.removeRow(selectedRow);
            } else {
                JOptionPane.showMessageDialog(frame, "Select a game first!");
            }
        });

        updateBtn.addActionListener(e -> {
            table.repaint();
            JOptionPane.showMessageDialog(frame, "Game information updated!");
        });

        return panel;
    }

    private Game showGameDialog(Game existingGame) {

        JTextField titleField = new JTextField();
        JTextField genreField = new JTextField();
        JTextField platformField = new JTextField();

        if (existingGame != null) {
            titleField.setText(existingGame.getTitle());
            genreField.setText(existingGame.getGenre());
            platformField.setText(existingGame.getPlatform());
        }

        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Genre:"));
        panel.add(genreField);
        panel.add(new JLabel("Platform:"));
        panel.add(platformField);

        int result = JOptionPane.showConfirmDialog(
                frame, panel,
                "Game Entry",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION) {
            return new Game(
                    titleField.getText(),
                    genreField.getText(),
                    platformField.getText()
            );
        }

        return null;
    }

    class Game {
        private String title;
        private String genre;
        private String platform;

        public Game(String title, String genre, String platform) {
            this.title = title;
            this.genre = genre;
            this.platform = platform;
        }

        public String getTitle() { return title; }
        public String getGenre() { return genre; }
        public String getPlatform() { return platform; }
    }
}