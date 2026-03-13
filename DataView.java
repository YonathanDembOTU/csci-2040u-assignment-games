import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;

public class DataView extends JFrame {
    JTable table;
    JButton addBtn, editBtn, deleteBtn, saveBtn, logoutBtn, toggleColumnsBtn, toggleThemeBtn;
    DefaultTableModel tableModel;
    JScrollPane scrollPane;

    JPanel tablePanel;
    JPanel topBar;
    JPanel buttonPanel;

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
        setSize(700, 450);
        setMinimumSize(new Dimension(620, 420));
        setResizable(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

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

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);

        scrollPane = new JScrollPane(table);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(18, 0));
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 18));

        toggleColumnsBtn = new JButton("Show More");
        toggleThemeBtn = new JButton("Dark Mode");

        Dimension wideButtonSize = new Dimension(140, 34);
        toggleColumnsBtn.setPreferredSize(wideButtonSize);
        toggleThemeBtn.setPreferredSize(wideButtonSize);

        topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        topBar.add(toggleColumnsBtn);
        topBar.add(toggleThemeBtn);

        tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(topBar, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(tablePanel, BorderLayout.CENTER);

        buttonPanel = new JPanel(new GridLayout(0, 1, 8, 8));

        addBtn = new JButton("Add");
        editBtn = new JButton("Edit");
        deleteBtn = new JButton("Delete");
        saveBtn = new JButton("Save");
        logoutBtn = new JButton("Logout");

        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(saveBtn);
        buttonPanel.add(logoutBtn);

        add(buttonPanel, BorderLayout.EAST);

        applyTheme();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Replaces the visible table data with the provided rows and columns.
     *
     * @param data visible row data
     * @param columns visible column headers
     */
    public void setTableData(Object[][] data, String[] columns) {
        tableModel.setDataVector(data, columns);
    }

    /**
     * Enables or disables all main interaction controls.
     *
     * @param enabled true to enable interaction, false to disable it
     */
    public void setInteractionEnabled(boolean enabled) {
        table.setEnabled(enabled);
        addBtn.setEnabled(enabled);
        editBtn.setEnabled(enabled);
        deleteBtn.setEnabled(enabled);
        saveBtn.setEnabled(enabled);
        logoutBtn.setEnabled(true);
        toggleColumnsBtn.setEnabled(enabled);
        toggleThemeBtn.setEnabled(enabled);
    }

    /**
     * Resizes each table column to fit its content with a reasonable maximum width.
     */
    public void resizeColumnsToFitContent() {
        final int PADDING = 24;
        final int MAX_WIDTH = 260;

        FontMetrics headerMetrics = table.getTableHeader().getFontMetrics(table.getTableHeader().getFont());
        FontMetrics cellMetrics = table.getFontMetrics(table.getFont());

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

            width = Math.min(width, MAX_WIDTH);
            column.setPreferredWidth(width);
        }
    }

    public void fitWindowToTable(boolean expanded) {
        int totalWidth = 80;

        for (int col = 0; col < table.getColumnCount(); col++) {
            totalWidth += table.getColumnModel().getColumn(col).getPreferredWidth();
        }

        totalWidth += 180;

        int targetWidth;
        int targetHeight = 450;

        if (expanded) {
            targetWidth = Math.min(Math.max(totalWidth, 820), 1150);
        } else {
            targetWidth = Math.min(Math.max(totalWidth, 700), 900);
        }

        setSize(targetWidth, targetHeight);
        setMinimumSize(new Dimension(620, 420));
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
        tablePanel.setBackground(windowBg);
        topBar.setBackground(darkMode ? DARK_PANEL_BG : LIGHT_PANEL_BG);
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

        applyScrollBarTheme(scrollPane.getVerticalScrollBar(), scrollTrack, scrollThumb);
        applyScrollBarTheme(scrollPane.getHorizontalScrollBar(), scrollTrack, scrollThumb);

        styleButton(addBtn, buttonBg, text);
        styleButton(editBtn, buttonBg, text);
        styleButton(deleteBtn, buttonBg, text);
        styleButton(saveBtn, buttonBg, text);
        styleButton(logoutBtn, buttonBg, text);
        styleButton(toggleColumnsBtn, buttonBg, text);
        styleButton(toggleThemeBtn, buttonBg, text);

        toggleThemeBtn.setText(darkMode ? "Light Mode" : "Dark Mode");

        repaint();
    }

    private void styleButton(JButton button, Color bg, Color fg) {
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createLineBorder(
                darkMode ? new Color(90, 90, 90) : new Color(190, 190, 190)
        ));
        button.setPreferredSize(new Dimension(140, 34));
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
                        10
                );

                g2.setColor(DataView.this.darkMode ? new Color(170, 170, 170) : new Color(120, 120, 120));
                g2.drawRoundRect(
                        thumbBounds.x + 1,
                        thumbBounds.y + 1,
                        Math.max(thumbBounds.width - 3, 9),
                        Math.max(thumbBounds.height - 3, 9),
                        10,
                        10
                );

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
            details.append(columns[i]).append(": ").append(row[i]).append("\n\n");
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
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}