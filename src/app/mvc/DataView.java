package app.mvc;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class DataView extends JFrame {
    private static final int DEFAULT_ROW_HEIGHT = 24;
    private static final Dimension MIN_WINDOW_SIZE = new Dimension(1200, 700);

    // Main table and action buttons
    public JTable table;
    public JButton addBtn;
    public JButton editBtn;
    public JButton deleteBtn;
    public JButton saveBtn;
    public JButton logoutBtn;
    public JButton toggleColumnsBtn;
    public JButton toggleThemeBtn;
    public JButton advancedSearchBtn;
    public JButton searchClearBtn;
    public JButton passwordMenuBtn;

    // Table model and scroll areas
    DefaultTableModel tableModel;
    JScrollPane scrollPane;
    JScrollPane pageScrollPane;

    // Main panels
    JPanel contentPanel;
    JPanel tablePanel;
    JPanel topBar;
    JPanel buttonPanel;
    JPanel advancedSearchPanel;
    JPanel mainBodyPanel;
    JPanel headerPanel;

    // Search and filter controls
    public JTextField searchField;
    public JComboBox<String> searchColumnCombo;
    public JComboBox<String> genreFilterCombo;
    public JComboBox<String> ratingFilterCombo;
    public JComboBox<String> platformFilterCombo;
    public JComboBox<String> multiplayerFilterCombo;
    public JComboBox<String> singlePlayerFilterCombo;

    // Tracks current theme mode
    private boolean darkMode = false;

    // Light mode colors
    private final Color LIGHT_WINDOW_BG = new Color(245, 245, 245);
    private final Color LIGHT_PANEL_BG = new Color(250, 250, 250);
    private final Color LIGHT_BUTTON_BG = new Color(235, 235, 235);
    private final Color LIGHT_ROW_EVEN = Color.WHITE;
    private final Color LIGHT_ROW_ODD = new Color(235, 235, 235);
    private final Color LIGHT_TEXT = Color.BLACK;
    private final Color LIGHT_HEADER_BG = new Color(210, 210, 210);
    private final Color LIGHT_HEADER_TEXT = Color.BLACK;
    private final Color LIGHT_SELECTION = new Color(180, 205, 255);
    private final Color LIGHT_GRID = new Color(200, 200, 200);
    private final Color LIGHT_SCROLL_TRACK = new Color(230, 230, 230);
    private final Color LIGHT_SCROLL_THUMB = new Color(130, 130, 130);

    // Dark mode colors
    private final Color DARK_WINDOW_BG = new Color(28, 28, 28);
    private final Color DARK_PANEL_BG = new Color(40, 40, 40);
    private final Color DARK_BUTTON_BG = new Color(55, 55, 55);
    private final Color DARK_ROW_EVEN = new Color(45, 45, 45);
    private final Color DARK_ROW_ODD = new Color(60, 60, 60);
    private final Color DARK_TEXT = new Color(235, 235, 235);
    private final Color DARK_HEADER_BG = new Color(65, 65, 65);
    private final Color DARK_HEADER_TEXT = Color.WHITE;
    private final Color DARK_SELECTION = new Color(90, 130, 190);
    private final Color DARK_GRID = new Color(85, 85, 85);
    private final Color DARK_SCROLL_TRACK = new Color(38, 38, 38);
    private final Color DARK_SCROLL_THUMB = new Color(180, 180, 180);

    public DataView() {
        setTitle("Turn for Turn Co. - Database Editor");
        setSize(1100, 700);
        setMinimumSize(MIN_WINDOW_SIZE);
        setResizable(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component component = super.prepareRenderer(renderer, row, column);

                if (!isRowSelected(row)) {
                    if (darkMode) {
                        component.setBackground(row % 2 == 0 ? DARK_ROW_EVEN : DARK_ROW_ODD);
                        component.setForeground(DARK_TEXT);
                    } else {
                        component.setBackground(row % 2 == 0 ? LIGHT_ROW_EVEN : LIGHT_ROW_ODD);
                        component.setForeground(LIGHT_TEXT);
                    }
                } else {
                    component.setBackground(darkMode ? DARK_SELECTION : LIGHT_SELECTION);
                    component.setForeground(darkMode ? DARK_TEXT : LIGHT_TEXT);
                }

                return component;
            }
        };

        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setRowHeight(DEFAULT_ROW_HEIGHT);

        scrollPane = new JScrollPane(table);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(18, 0));
        scrollPane.setPreferredSize(new Dimension(860, 520));
        scrollPane.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                stretchTableToViewport();
            }
        });

        toggleColumnsBtn = new JButton("Show More");
        toggleThemeBtn = new JButton("Dark Mode");
        advancedSearchBtn = new JButton("Adv Search");
        searchClearBtn = new JButton("Clear");

        searchColumnCombo = new JComboBox<>(new String[]{"Title"});
        searchField = new JTextField(16);
        searchField.setToolTipText("Search for games using the selected column.");
        searchClearBtn.setToolTipText("Clear search text and all active filters.");
        advancedSearchBtn.setToolTipText("Show or hide advanced search filters.");
        toggleColumnsBtn.setToolTipText("Switch between compact and expanded table view.");
        toggleThemeBtn.setToolTipText("Toggle between light mode and dark mode.");

        Dimension wideButtonSize = new Dimension(140, 34);
        toggleColumnsBtn.setPreferredSize(wideButtonSize);
        toggleThemeBtn.setPreferredSize(wideButtonSize);
        advancedSearchBtn.setPreferredSize(wideButtonSize);
        searchClearBtn.setPreferredSize(new Dimension(100, 34));
        searchColumnCombo.setPreferredSize(new Dimension(140, 34));
        searchField.setPreferredSize(new Dimension(170, 34));

        topBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        topBar.add(toggleColumnsBtn);
        topBar.add(toggleThemeBtn);
        topBar.add(searchColumnCombo);
        topBar.add(searchField);
        topBar.add(searchClearBtn);
        topBar.add(advancedSearchBtn);

        advancedSearchPanel = new JPanel();
        advancedSearchPanel.setLayout(new BoxLayout(advancedSearchPanel, BoxLayout.Y_AXIS));
        advancedSearchPanel.setVisible(false);

        genreFilterCombo = createFilterCombo();
        ratingFilterCombo = createFilterCombo();
        platformFilterCombo = createFilterCombo();
        multiplayerFilterCombo = createFilterCombo();
        singlePlayerFilterCombo = createFilterCombo();

        JPanel filterRow1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        filterRow1.add(new JLabel("Genre:"));
        filterRow1.add(genreFilterCombo);
        filterRow1.add(new JLabel("Age Rating:"));
        filterRow1.add(ratingFilterCombo);
        filterRow1.add(new JLabel("Platform:"));
        filterRow1.add(platformFilterCombo);

        JPanel filterRow2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        filterRow2.add(new JLabel("Multiplayer:"));
        filterRow2.add(multiplayerFilterCombo);
        filterRow2.add(new JLabel("Single Player:"));
        filterRow2.add(singlePlayerFilterCombo);

        advancedSearchPanel.add(filterRow1);
        advancedSearchPanel.add(filterRow2);

        headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(topBar, BorderLayout.NORTH);
        headerPanel.add(advancedSearchPanel, BorderLayout.CENTER);

        tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(headerPanel, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        buttonPanel = new JPanel(new GridLayout(0, 1, 8, 8));
        buttonPanel.setPreferredSize(new Dimension(170, 0));

        addBtn = new JButton("Add");
        editBtn = new JButton("Edit");
        deleteBtn = new JButton("Delete");
        saveBtn = new JButton("Save");
        passwordMenuBtn = new JButton("Passwords");
        logoutBtn = new JButton("Logout");

        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(saveBtn);
        buttonPanel.add(passwordMenuBtn);
        buttonPanel.add(logoutBtn);

        mainBodyPanel = new JPanel(new BorderLayout(12, 0));
        mainBodyPanel.add(tablePanel, BorderLayout.CENTER);
        mainBodyPanel.add(buttonPanel, BorderLayout.EAST);

        contentPanel = new JPanel(new BorderLayout(0, 12));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPanel.add(mainBodyPanel, BorderLayout.CENTER);

        pageScrollPane = new JScrollPane(contentPanel);
        pageScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        pageScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pageScrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(18, 0));
        pageScrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 18));

        add(pageScrollPane, BorderLayout.CENTER);

        applyTheme();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JComboBox<String> createFilterCombo() {
        JComboBox<String> combo = new JComboBox<>(new String[]{"All"});
        combo.setPreferredSize(new Dimension(125, 34));
        return combo;
    }


    private void stretchTableToViewport() {
        if (table.getColumnCount() <= 0) {
            return;
        }

        int viewportWidth = scrollPane.getViewport().getWidth();
        if (viewportWidth <= 0) {
            return;
        }

        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        int columnWidth = Math.max(90, viewportWidth / table.getColumnCount());

        for (int col = 0; col < table.getColumnCount(); col++) {
            TableColumn column = table.getColumnModel().getColumn(col);
            column.setPreferredWidth(columnWidth);
        }

        table.revalidate();
        table.repaint();
    }

    public void setTableData(Object[][] data, String[] columns) {
        tableModel.setDataVector(data, columns);
        SwingUtilities.invokeLater(this::stretchTableToViewport);
    }

    public void setInteractionEnabled(boolean enabled) {
        table.setEnabled(enabled);
        addBtn.setEnabled(enabled);
        editBtn.setEnabled(enabled);
        deleteBtn.setEnabled(enabled);
        saveBtn.setEnabled(enabled);
        passwordMenuBtn.setEnabled(enabled);
        logoutBtn.setEnabled(true);
        toggleColumnsBtn.setEnabled(enabled);
        toggleThemeBtn.setEnabled(enabled);
        advancedSearchBtn.setEnabled(enabled);
        searchClearBtn.setEnabled(enabled);
        searchField.setEnabled(enabled);
        searchColumnCombo.setEnabled(enabled);
        genreFilterCombo.setEnabled(enabled);
        ratingFilterCombo.setEnabled(enabled);
        platformFilterCombo.setEnabled(enabled);
        multiplayerFilterCombo.setEnabled(enabled);
        singlePlayerFilterCombo.setEnabled(enabled);
    }

    public void resizeColumnsToFitContent() {
        final int PADDING = 24;
        final int MIN_WIDTH = 90;

        FontMetrics headerMetrics = table.getTableHeader().getFontMetrics(table.getTableHeader().getFont());
        FontMetrics cellMetrics = table.getFontMetrics(table.getFont());
        int totalPreferredWidth = 0;

        for (int col = 0; col < table.getColumnCount(); col++) {
            TableColumn column = table.getColumnModel().getColumn(col);
            int width = headerMetrics.stringWidth(table.getColumnName(col)) + PADDING;

            for (int row = 0; row < table.getRowCount(); row++) {
                Object value = table.getValueAt(row, col);
                if (value != null) {
                    int cellWidth = cellMetrics.stringWidth(value.toString()) + PADDING;
                    width = Math.max(width, cellWidth);
                }
            }

            width = Math.max(width, MIN_WIDTH);
            column.setPreferredWidth(width);
            totalPreferredWidth += width;
        }

        int viewportWidth = scrollPane.getViewport().getWidth();
        if (viewportWidth > totalPreferredWidth) {
            stretchTableToViewport();
        }
    }

    public void fitWindowToTable(boolean expanded) {
        int targetWidth = expanded ? 1280 : 1100;
        int targetHeight = advancedSearchPanel.isVisible() ? 760 : 700;
        setSize(targetWidth, targetHeight);
        setMinimumSize(MIN_WINDOW_SIZE);
        revalidate();
        repaint();
    }

    public void toggleTheme() {
        darkMode = !darkMode;
        applyTheme();
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public boolean isAdvancedSearchVisible() {
        return advancedSearchPanel.isVisible();
    }

    public void setAdvancedSearchVisible(boolean visible) {
        advancedSearchPanel.setVisible(visible);
        revalidate();
        repaint();
        SwingUtilities.invokeLater(this::stretchTableToViewport);
    }

    public void applyTheme() {
        Color windowBg = darkMode ? DARK_WINDOW_BG : LIGHT_WINDOW_BG;
        Color panelBg = darkMode ? DARK_PANEL_BG : LIGHT_PANEL_BG;
        Color buttonBg = darkMode ? DARK_BUTTON_BG : LIGHT_BUTTON_BG;
        Color text = darkMode ? DARK_TEXT : LIGHT_TEXT;
        Color headerBg = darkMode ? DARK_HEADER_BG : LIGHT_HEADER_BG;
        Color headerText = darkMode ? DARK_HEADER_TEXT : LIGHT_HEADER_TEXT;
        Color selection = darkMode ? DARK_SELECTION : LIGHT_SELECTION;
        Color grid = darkMode ? DARK_GRID : LIGHT_GRID;
        Color scrollTrack = darkMode ? DARK_SCROLL_TRACK : LIGHT_SCROLL_TRACK;
        Color scrollThumb = darkMode ? DARK_SCROLL_THUMB : LIGHT_SCROLL_THUMB;

        getContentPane().setBackground(windowBg);
        contentPanel.setBackground(windowBg);
        mainBodyPanel.setBackground(windowBg);
        tablePanel.setBackground(windowBg);
        headerPanel.setBackground(panelBg);
        topBar.setBackground(panelBg);
        advancedSearchPanel.setBackground(panelBg);
        buttonPanel.setBackground(windowBg);

        table.setBackground(panelBg);
        table.setForeground(text);
        table.setGridColor(grid);
        table.setSelectionBackground(selection);
        table.setSelectionForeground(text);

        table.getTableHeader().setBackground(headerBg);
        table.getTableHeader().setForeground(headerText);
        table.getTableHeader().setOpaque(true);

        scrollPane.setBackground(panelBg);
        scrollPane.getViewport().setBackground(panelBg);
        scrollPane.setBorder(BorderFactory.createLineBorder(grid));

        pageScrollPane.setBackground(windowBg);
        pageScrollPane.getViewport().setBackground(windowBg);
        pageScrollPane.setBorder(BorderFactory.createEmptyBorder());

        applyScrollBarTheme(scrollPane.getVerticalScrollBar(), scrollTrack, scrollThumb);
        applyScrollBarTheme(pageScrollPane.getVerticalScrollBar(), scrollTrack, scrollThumb);
        applyScrollBarTheme(pageScrollPane.getHorizontalScrollBar(), scrollTrack, scrollThumb);

        styleButton(addBtn, buttonBg, text);
        styleButton(editBtn, buttonBg, text);
        styleButton(deleteBtn, buttonBg, text);
        styleButton(saveBtn, buttonBg, text);
        styleButton(passwordMenuBtn, buttonBg, text);
        styleButton(logoutBtn, buttonBg, text);
        styleButton(toggleColumnsBtn, buttonBg, text);
        styleButton(toggleThemeBtn, buttonBg, text);
        styleButton(advancedSearchBtn, buttonBg, text);
        styleButton(searchClearBtn, buttonBg, text);

        styleInput(searchField, panelBg, text, grid);
        styleCombo(searchColumnCombo, panelBg, text);
        styleCombo(genreFilterCombo, panelBg, text);
        styleCombo(ratingFilterCombo, panelBg, text);
        styleCombo(platformFilterCombo, panelBg, text);
        styleCombo(multiplayerFilterCombo, panelBg, text);
        styleCombo(singlePlayerFilterCombo, panelBg, text);

        toggleThemeBtn.setText(darkMode ? "Light Mode" : "Dark Mode");
        advancedSearchBtn.setText(advancedSearchPanel.isVisible() ? "Hide Filters" : "Adv Search");

        for (Component component : advancedSearchPanel.getComponents()) {
            component.setBackground(panelBg);

            if (component instanceof JPanel panel) {
                for (Component child : panel.getComponents()) {
                    child.setForeground(text);
                    child.setBackground(panelBg);
                }
            } else {
                component.setForeground(text);
            }
        }

        repaint();
    }

    private void styleButton(JButton button, Color bg, Color fg) {
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createLineBorder(
                darkMode ? new Color(90, 90, 90) : new Color(190, 190, 190)));
        button.setPreferredSize(button.getPreferredSize().width <= 100
                ? new Dimension(100, 34)
                : new Dimension(140, 34));
    }

    private void styleInput(JTextField field, Color bg, Color fg, Color border) {
        field.setBackground(bg);
        field.setForeground(fg);
        field.setCaretColor(fg);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
    }

    private void styleCombo(JComboBox<String> combo, Color bg, Color fg) {
        combo.setBackground(bg);
        combo.setForeground(fg);
        combo.setFocusable(false);

        ListCellRenderer<? super String> baseRenderer = combo.getRenderer();
        combo.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            Component c = baseRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            c.setBackground(isSelected ? (darkMode ? DARK_SELECTION : LIGHT_SELECTION) : bg);
            c.setForeground(fg);
            return c;
        });
    }

    private void applyScrollBarTheme(JScrollBar scrollBar, Color trackColor, Color thumbColor) {
        scrollBar.setOpaque(true);
        scrollBar.setBackground(trackColor);
        scrollBar.setForeground(thumbColor);

        scrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                trackColor = DataView.this.darkMode ? DARK_SCROLL_TRACK : LIGHT_SCROLL_TRACK;
                thumbColor = DataView.this.darkMode ? DARK_SCROLL_THUMB : LIGHT_SCROLL_THUMB;
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(DataView.this.darkMode ? DARK_SCROLL_TRACK : LIGHT_SCROLL_TRACK);
                g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
                g2.dispose();
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                if (thumbBounds == null || thumbBounds.width <= 0 || thumbBounds.height <= 0) {
                    return;
                }

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color thumb = DataView.this.darkMode ? DARK_SCROLL_THUMB : LIGHT_SCROLL_THUMB;

                g2.setColor(thumb);
                g2.fillRoundRect(
                        thumbBounds.x + 1,
                        thumbBounds.y + 1,
                        Math.max(thumbBounds.width - 2, 10),
                        Math.max(thumbBounds.height - 2, 10),
                        10,
                        10);

                g2.setColor(DataView.this.darkMode ? new Color(170, 170, 170) : new Color(120, 120, 120));
                g2.drawRoundRect(
                        thumbBounds.x + 1,
                        thumbBounds.y + 1,
                        Math.max(thumbBounds.width - 3, 9),
                        Math.max(thumbBounds.height - 3, 9),
                        10,
                        10);

                g2.dispose();
            }

            @Override
            protected Dimension getMinimumThumbSize() {
                return new Dimension(12, 40);
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createInvisibleButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createInvisibleButton();
            }

            private JButton createInvisibleButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
        });

        scrollBar.revalidate();
        scrollBar.repaint();
    }

    public void showRowDetails(String[] columns, String[] row) {
        JTextArea textArea = new JTextArea(16, 40);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        StringBuilder details = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {
            details.append("• ").append(columns[i]).append(": ").append(row[i]).append("\n\n");
        }

        textArea.setText(details.toString());
        textArea.setCaretPosition(0);

        if (darkMode) {
            textArea.setBackground(DARK_PANEL_BG);
            textArea.setForeground(DARK_TEXT);
            textArea.setCaretColor(DARK_TEXT);
        } else {
            textArea.setBackground(Color.WHITE);
            textArea.setForeground(Color.BLACK);
            textArea.setCaretColor(Color.BLACK);
        }

        JScrollPane detailScroll = new JScrollPane(textArea);
        detailScroll.setPreferredSize(new Dimension(520, 320));

        JOptionPane.showMessageDialog(
                this,
                detailScroll,
                "Game Details",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
