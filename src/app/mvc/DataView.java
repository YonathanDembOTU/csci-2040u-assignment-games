package app.mvc;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.util.regex.Pattern;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DataView extends JFrame {
    // This view supports hover highlighting, live search counters, and advanced filters.
    private static final int DEFAULT_ROW_HEIGHT = 30;
    private static final Dimension MIN_WINDOW_SIZE = new Dimension(1200, 720);

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
    public JButton attributionBtn;

    DefaultTableModel tableModel;
    JScrollPane scrollPane;
    JScrollPane pageScrollPane;

    JPanel contentPanel;
    JPanel tablePanel;
    JPanel topBar;
    JPanel buttonPanel;
    JPanel advancedSearchPanel;
    JPanel mainBodyPanel;
    JPanel headerPanel;
    JPanel summaryPanel;
    BackgroundPanel backgroundPanel;

    public JTextField searchField;
    public JComboBox<String> searchColumnCombo;
    public JComboBox<String> genreFilterCombo;
    public JComboBox<String> ratingFilterCombo;
    public JComboBox<String> platformFilterCombo;
    public JComboBox<String> multiplayerFilterCombo;
    public JComboBox<String> singlePlayerFilterCombo;

    public JLabel searchCounterLabel;
    public JLabel emptyStateLabel;

    private boolean darkMode = false;
    private int hoverRow = -1;

    private final Color LIGHT_WINDOW_BG = new Color(245, 245, 245);
    private final Color LIGHT_PANEL_BG = new Color(240, 240, 240, 190);
    private final Color LIGHT_BUTTON_BG = new Color(235, 235, 235);
    private final Color LIGHT_BUTTON_HOVER = new Color(220, 226, 234);
    private final Color LIGHT_BUTTON_PRESSED = new Color(205, 212, 222);
    private final Color LIGHT_ROW_EVEN = new Color(210, 210, 210, 150);
    private final Color LIGHT_ROW_ODD = new Color(185, 185, 185, 138);
    private final Color LIGHT_ROW_HOVER = new Color(255, 170, 60, 180);
    private final Color LIGHT_TEXT = Color.BLACK;
    private final Color LIGHT_HEADER_BG = new Color(210, 210, 210);
    private final Color LIGHT_HEADER_TEXT = Color.BLACK;
    private final Color LIGHT_SELECTION = new Color(170, 170, 170, 205);
    private final Color LIGHT_GRID = new Color(200, 200, 200);
    private final Color LIGHT_SCROLL_TRACK = new Color(230, 230, 230);
    private final Color LIGHT_SCROLL_THUMB = new Color(130, 130, 130);
    private final Color LIGHT_EMPTY_BG = new Color(242, 245, 250);

    private final Color DARK_WINDOW_BG = new Color(28, 28, 28);
    private final Color DARK_PANEL_BG = new Color(55, 55, 55, 185);
    private final Color DARK_BUTTON_BG = new Color(55, 55, 55);
    private final Color DARK_BUTTON_HOVER = new Color(74, 74, 74);
    private final Color DARK_BUTTON_PRESSED = new Color(92, 92, 92);
    private final Color DARK_ROW_EVEN = new Color(70, 70, 70, 145);
    private final Color DARK_ROW_ODD = new Color(88, 88, 88, 138);
    private final Color DARK_ROW_HOVER = new Color(255, 140, 0, 180);
    private final Color DARK_TEXT = new Color(235, 235, 235);
    private final Color DARK_HEADER_BG = new Color(65, 65, 65);
    private final Color DARK_HEADER_TEXT = Color.WHITE;
    private final Color DARK_SELECTION = new Color(132, 132, 132, 205);
    private final Color DARK_GRID = new Color(85, 85, 85);
    private final Color DARK_SCROLL_TRACK = new Color(38, 38, 38);
    private final Color DARK_SCROLL_THUMB = new Color(180, 180, 180);
    private final Color DARK_EMPTY_BG = new Color(35, 35, 35);

    /**
     * Builds the main database editor interface.
     */
    public DataView() {
        setTitle("Turn for Turn Co. - Database Editor");
        setSize(1100, 720);
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

                if (isRowSelected(row)) {
                    component.setBackground(darkMode ? DARK_SELECTION : LIGHT_SELECTION);
                    component.setForeground(darkMode ? DARK_TEXT : LIGHT_TEXT);
                } else if (row == hoverRow) {
                    component.setBackground(darkMode ? DARK_ROW_HOVER : LIGHT_ROW_HOVER);
                    component.setForeground(darkMode ? DARK_TEXT : LIGHT_TEXT);
                } else if (darkMode) {
                    component.setBackground(row % 2 == 0 ? DARK_ROW_EVEN : DARK_ROW_ODD);
                    component.setForeground(DARK_TEXT);
                } else {
                    component.setBackground(row % 2 == 0 ? LIGHT_ROW_EVEN : LIGHT_ROW_ODD);
                    component.setForeground(LIGHT_TEXT);
                }

                if (component instanceof JComponent jc) {
                    jc.setOpaque(true);
                }
                return component;
            }
        };

        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setRowHeight(DEFAULT_ROW_HEIGHT);
        table.setRowMargin(6);
        table.setFont(new Font("Inter", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Inter", Font.BOLD, 13));
        table.setOpaque(false);

        table.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoverRow) {
                    hoverRow = row;
                    table.repaint();
                }
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoverRow = -1;
                table.repaint();
            }
        });

        scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
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

        toggleColumnsBtn = new JButton("▤ Show More");
        toggleThemeBtn = new JButton("☾ Dark Mode");
        advancedSearchBtn = new JButton("▼ Filters");
        searchClearBtn = new JButton("✕ Clear");

        searchColumnCombo = new JComboBox<>(new String[]{"Title", "Description"});
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

        JLabel catalogueTitleLabel = new JLabel("GAME CATALOGUE");
        catalogueTitleLabel.setFont(new Font("Inter", Font.BOLD, 22));
        catalogueTitleLabel.setForeground(new Color(255, 140, 0));
        searchCounterLabel = new JLabel("Showing 0 results");
        searchCounterLabel.setFont(new Font("Inter", Font.BOLD, 13));

        summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        summaryPanel.setOpaque(false);
        summaryPanel.add(catalogueTitleLabel);
        summaryPanel.add(searchCounterLabel);

        topBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        topBar.setOpaque(false);
        topBar.add(toggleColumnsBtn);
        topBar.add(toggleThemeBtn);
        topBar.add(searchColumnCombo);
        topBar.add(searchField);
        topBar.add(searchClearBtn);
        topBar.add(advancedSearchBtn);

        advancedSearchPanel = new JPanel();
        advancedSearchPanel.setLayout(new BoxLayout(advancedSearchPanel, BoxLayout.Y_AXIS));
        advancedSearchPanel.setVisible(false);
        advancedSearchPanel.setOpaque(false);

        genreFilterCombo = createFilterCombo();
        ratingFilterCombo = createFilterCombo();
        platformFilterCombo = createFilterCombo();
        multiplayerFilterCombo = createFilterCombo();
        singlePlayerFilterCombo = createFilterCombo();

        JPanel filterRow1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        filterRow1.setOpaque(false);
        filterRow1.add(new JLabel("Genre:"));
        filterRow1.add(genreFilterCombo);
        filterRow1.add(new JLabel("Age Rating:"));
        filterRow1.add(ratingFilterCombo);
        filterRow1.add(new JLabel("Platform:"));
        filterRow1.add(platformFilterCombo);

        JPanel filterRow2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        filterRow2.setOpaque(false);
        filterRow2.add(new JLabel("Multiplayer:"));
        filterRow2.add(multiplayerFilterCombo);
        filterRow2.add(new JLabel("Single Player:"));
        filterRow2.add(singlePlayerFilterCombo);

        advancedSearchPanel.add(filterRow1);
        advancedSearchPanel.add(filterRow2);

        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.add(summaryPanel, BorderLayout.NORTH);
        headerPanel.add(topBar, BorderLayout.CENTER);
        headerPanel.add(advancedSearchPanel, BorderLayout.SOUTH);

        tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(false);
        tablePanel.add(headerPanel, BorderLayout.NORTH);

        emptyStateLabel = new JLabel("No games match your current search or filters.", SwingConstants.CENTER);
        emptyStateLabel.setFont(new Font("Inter", Font.BOLD, 16));
        emptyStateLabel.setVisible(false);

        JPanel centerPanel = new JPanel(new CardLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(scrollPane, "TABLE");
        centerPanel.add(emptyStateLabel, "EMPTY");
        tablePanel.add(centerPanel, BorderLayout.CENTER);

        buttonPanel = new JPanel(new GridLayout(0, 1, 8, 8));
        buttonPanel.setPreferredSize(new Dimension(170, 0));
        buttonPanel.setOpaque(false);

        addBtn = new JButton("＋ Add");
        editBtn = new JButton("✎ Edit");
        deleteBtn = new JButton("－ Delete");
        saveBtn = new JButton("⇩ Save");
        passwordMenuBtn = new JButton("⚿ Passwords");
        attributionBtn = new JButton("ⓘ Attribution");
        logoutBtn = new JButton("⇦ Logout");

        attributionBtn.setToolTipText("Open the RAWG attribution page in your browser.");

        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(saveBtn);
        buttonPanel.add(passwordMenuBtn);
        buttonPanel.add(attributionBtn);
        buttonPanel.add(logoutBtn);

        mainBodyPanel = new JPanel(new BorderLayout(12, 0));
        mainBodyPanel.setOpaque(false);
        mainBodyPanel.add(tablePanel, BorderLayout.CENTER);
        mainBodyPanel.add(buttonPanel, BorderLayout.EAST);

        contentPanel = new JPanel(new BorderLayout(0, 12));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPanel.setOpaque(false);
        contentPanel.add(mainBodyPanel, BorderLayout.CENTER);

        backgroundPanel = new BackgroundPanel();
        backgroundPanel.setLayout(new BorderLayout());
        backgroundPanel.add(contentPanel, BorderLayout.CENTER);

        pageScrollPane = new JScrollPane(backgroundPanel);
        pageScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        pageScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pageScrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(18, 0));
        pageScrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 18));

        add(pageScrollPane, BorderLayout.CENTER);

        applyTheme();
        installPressAndHoverStates();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JComboBox<String> createFilterCombo() {
        JComboBox<String> combo = new JComboBox<>(new String[]{"All"});
        combo.setPreferredSize(new Dimension(150, 34));
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
        applyMultiLineRowHeights();
        SwingUtilities.invokeLater(this::stretchTableToViewport);
        setEmptyStateVisible(data.length == 0);
    }

    private void applyMultiLineRowHeights() {
        for (int row = 0; row < table.getRowCount(); row++) {
            int maxLines = 1;

            for (int col = 0; col < table.getColumnCount(); col++) {
                Object value = table.getValueAt(row, col);
                maxLines = Math.max(maxLines, countDisplayLines(value));
            }

            int newHeight = Math.max(DEFAULT_ROW_HEIGHT, 24 + ((maxLines - 1) * 18));
            table.setRowHeight(row, newHeight);
        }
    }

    private int countDisplayLines(Object value) {
        if (value == null) {
            return 1;
        }

        String text = value.toString();
        if (text.isBlank()) {
            return 1;
        }

        String normalized = text.replaceAll("(?i)<br\\s*/?>", "\n");
        normalized = normalized.replaceAll("(?i)</?html>", "");
        return Math.max(1, normalized.split("\n", -1).length);
    }

    private String stripHtml(String text) {
        if (text == null) {
            return "";
        }
        return Pattern.compile("<[^>]*>").matcher(text).replaceAll("");
    }

    public void setSearchStatus(int visibleCount, int totalCount, String query) {
        if (visibleCount == 0) {
            if (query == null || query.isBlank()) {
                searchCounterLabel.setText("Showing 0 of " + totalCount + " games");
            } else {
                searchCounterLabel.setText("0 results for \"" + query + "\"");
            }
        } else if (query != null && !query.isBlank()) {
            searchCounterLabel.setText("Showing " + visibleCount + " of " + totalCount + " games for \"" + query + "\"");
        } else {
            searchCounterLabel.setText("Showing " + visibleCount + " of " + totalCount + " games");
        }
    }

    public void setEmptyStateVisible(boolean visible) {
        emptyStateLabel.setVisible(visible);
        scrollPane.setVisible(!visible);
        revalidate();
        repaint();
    }

    public void setInteractionEnabled(boolean enabled) {
        table.setEnabled(enabled);
        addBtn.setEnabled(enabled);
        editBtn.setEnabled(enabled);
        deleteBtn.setEnabled(enabled);
        saveBtn.setEnabled(enabled);
        passwordMenuBtn.setEnabled(enabled);
        attributionBtn.setEnabled(true);
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
                    int cellWidth = cellMetrics.stringWidth(stripHtml(value.toString()).replace("\n", " ")) + PADDING;
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
        int targetHeight = advancedSearchPanel.isVisible() ? 780 : 720;
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
        Color emptyBg = darkMode ? DARK_EMPTY_BG : LIGHT_EMPTY_BG;

        getContentPane().setBackground(windowBg);
        backgroundPanel.baseColor = windowBg;
        backgroundPanel.repaint();

        contentPanel.setBackground(windowBg);
        mainBodyPanel.setBackground(windowBg);
        tablePanel.setBackground(windowBg);
        headerPanel.setBackground(panelBg);
        summaryPanel.setBackground(panelBg);
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

        emptyStateLabel.setBackground(emptyBg);
        emptyStateLabel.setForeground(text);
        emptyStateLabel.setOpaque(true);
        searchCounterLabel.setForeground(text);

        scrollPane.setBackground(panelBg);
        scrollPane.getViewport().setBackground(new Color(panelBg.getRed(), panelBg.getGreen(), panelBg.getBlue(), Math.min(panelBg.getAlpha(), 160)));
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
        styleButton(attributionBtn, buttonBg, text);
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

        toggleThemeBtn.setText(darkMode ? "☀ Light Mode" : "☾ Dark Mode");
        advancedSearchBtn.setText(advancedSearchPanel.isVisible() ? "▲ Hide Filters" : "▼ Filters");

        for (Component component : advancedSearchPanel.getComponents()) {
            component.setBackground(panelBg);

            if (component instanceof JPanel panel) {
                for (Component child : panel.getComponents()) {
                    child.setForeground(text);
                    child.setBackground(panelBg);
                    if (child instanceof JLabel label) {
                        label.setFont(new Font("Inter", Font.BOLD, 12));
                    }
                }
            } else {
                component.setForeground(text);
            }
        }

        repaint();
    }

    private void styleButton(JButton button, Color bg, Color fg) {
        button.putClientProperty("baseBg", bg);
        button.putClientProperty("hoverBg", darkMode ? DARK_BUTTON_HOVER : LIGHT_BUTTON_HOVER);
        button.putClientProperty("pressedBg", darkMode ? DARK_BUTTON_PRESSED : LIGHT_BUTTON_PRESSED);

        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setFont(new Font("Inter", Font.BOLD, 13));
        button.setBorder(BorderFactory.createLineBorder(
                darkMode ? new Color(90, 90, 90) : new Color(190, 190, 190)));
        button.setPreferredSize(button.getPreferredSize().width <= 100
                ? new Dimension(100, 34)
                : new Dimension(150, 34));
    }

    private void installPressAndHoverStates() {
        JButton[] buttons = {
                addBtn, editBtn, deleteBtn, saveBtn, logoutBtn,
                toggleColumnsBtn, toggleThemeBtn, advancedSearchBtn,
                searchClearBtn, passwordMenuBtn, attributionBtn
        };

        for (JButton button : buttons) {
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (button.isEnabled()) {
                        Color hover = (Color) button.getClientProperty("hoverBg");
                        if (hover != null) {
                            button.setBackground(hover);
                        }
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    Color base = (Color) button.getClientProperty("baseBg");
                    if (base != null) {
                        button.setBackground(base);
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (button.isEnabled()) {
                        Color pressed = (Color) button.getClientProperty("pressedBg");
                        if (pressed != null) {
                            button.setBackground(pressed);
                        }
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (button.isEnabled()) {
                        if (button.contains(e.getPoint())) {
                            Color hover = (Color) button.getClientProperty("hoverBg");
                            if (hover != null) {
                                button.setBackground(hover);
                            }
                        } else {
                            Color base = (Color) button.getClientProperty("baseBg");
                            if (base != null) {
                                button.setBackground(base);
                            }
                        }
                    }
                }
            });
        }
    }

    private void styleInput(JTextField field, Color bg, Color fg, Color border) {
        field.setBackground(bg);
        field.setForeground(fg);
        field.setCaretColor(fg);
        field.setFont(new Font("Inter", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
    }

    /**
     * Styles a combo box so it matches the light or dark theme correctly.
     */
    private void styleCombo(JComboBox<String> combo, Color bg, Color fg) {
        combo.setBackground(bg);
        combo.setForeground(fg);
        combo.setFocusable(false);
        combo.setOpaque(true);
        combo.setFont(new Font("Inter", Font.PLAIN, 13));
        combo.setBorder(BorderFactory.createLineBorder(
                darkMode ? new Color(90, 90, 90) : new Color(190, 190, 190)));

        combo.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = new JButton("▼");
                button.setBorder(BorderFactory.createEmptyBorder());
                button.setFocusPainted(false);
                button.setContentAreaFilled(true);
                button.setBackground(bg);
                button.setForeground(fg);
                button.setOpaque(true);
                button.setFont(new Font("Inter", Font.BOLD, 12));
                return button;
            }
        });

        ListCellRenderer<? super String> baseRenderer = combo.getRenderer();
        combo.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            Component c = baseRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            c.setBackground(isSelected ? (darkMode ? DARK_SELECTION : LIGHT_SELECTION) : bg);
            c.setForeground(fg);

            if (c instanceof JComponent component) {
                component.setOpaque(true);
                component.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
            }

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
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
        });
    }

    /**
     * Opens the extracted data entry helper dialog for adding or editing a row.
     */
    public String[] showDataEntryDialog(String title, String[] columns, String[] defaults,
                                        boolean lockPublisherField, String publisherName, String generatedId) {
        return DataEntryDialogHelper.showDialog(
                this,
                title,
                columns,
                defaults,
                lockPublisherField,
                publisherName,
                generatedId);
    }

    /**
     * Opens the extracted password management helper for admin actions.
     */
    public void showPasswordManagementDialog() {
        PasswordDialogHelper.openPasswordHandlingMenu(this);
    }

    /**
     * Opens the extracted helper dialog used for double-click game entry details.
     */
    public void showRowDetails(String[] columns, String[] row) {
        GameEntryDetailsDialogHelper.showDialog(this, columns, row);
    }

    private static class BackgroundPanel extends JPanel {

        private Color baseColor = new Color(245, 245, 245);

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(baseColor);
            g2.fillRect(0, 0, getWidth(), getHeight());

            Color lattice = new Color(255, 170, 0, 34);
            int radius = 18;

            double hexHeight = Math.sqrt(3) * radius;
            double xStep = 1.5 * radius;
            double yStep = hexHeight;

            g2.setColor(lattice);
            g2.setStroke(new BasicStroke(1.8f));

            for (int col = -2; col < (int) (getWidth() / xStep) + 3; col++) {
                double x = col * xStep;
                double yOffset = (col % 2 == 0) ? 0 : hexHeight / 2.0;

                for (int row = -2; row < (int) (getHeight() / yStep) + 3; row++) {
                    double y = row * yStep + yOffset;
                    Polygon hex = createHexagon((int) Math.round(x), (int) Math.round(y), radius);
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
