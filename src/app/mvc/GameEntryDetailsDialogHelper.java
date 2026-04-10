package app.mvc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.IntConsumer;

/**
 * Centralizes the user interaction and themed detail presentation for game
 * entries shown throughout the application.
 * <p>
 * This merged helper now owns both responsibilities that were previously split
 * across separate files:
 * <ul>
 *     <li>installing the double-click listener used by tables that preview a
 *     selected entry</li>
 *     <li>building the themed details window used to display the full contents
 *     of a chosen row</li>
 * </ul>
 * Keeping both behaviors together makes the entry-preview workflow easier to
 * reuse and reduces controller wiring.
 */
public final class GameEntryDetailsDialogHelper {
    private GameEntryDetailsDialogHelper() {
    }

    /**
     * Installs a double-click listener that forwards the clicked visible row
     * index to the supplied callback.
     * <p>
     * The controller or dialog using the table remains responsible for mapping
     * the visible row index back to the correct model row and deciding what to
     * do with that selection.
     *
     * @param table table that should react to double-clicks
     * @param onRowDoubleClick callback that receives the clicked visible row
     *                         index
     */
    public static void installDoubleClickPreview(JTable table, IntConsumer onRowDoubleClick) {
        if (table == null || onRowDoubleClick == null) {
            return;
        }

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e) || e.getClickCount() != 2) {
                    return;
                }

                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    onRowDoubleClick.accept(row);
                }
            }
        });
    }

    /**
     * Displays the selected game row in a themed, scrollable details window.
     *
     * @param parent parent component used for theme lookup and dialog placement
     * @param columns ordered column names for the row
     * @param row row values to display
     */
    public static void showDialog(Component parent, String[] columns, String[] row) {
        if (columns == null || row == null || columns.length == 0) {
            AppDialogThemeHelper.showMessageDialog(
                    parent,
                    "Game Details",
                    "No game details were available for the selected entry.",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean dark = AppDialogThemeHelper.isDark(parent);
        AppDialogThemeHelper.Theme theme = AppDialogThemeHelper.getTheme(parent);

        String title = valueFor(columns, row, "Title", "Selected Game");
        String developer = valueFor(columns, row, "Developer", "Unknown Developer");
        String publisher = valueFor(columns, row, "Publisher", "Unknown Publisher");
        String description = valueFor(columns, row, "Description", "No description available.");

        JPanel root = AppDialogThemeHelper.createSurfacePanel(new BorderLayout(0, 12), dark);
        root.setPreferredSize(new Dimension(820, 560));
        root.setOpaque(false);

        JPanel summaryCard = AppDialogThemeHelper.createCardPanel(new BorderLayout(0, 10), dark);
        summaryCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(theme.softBorder),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Inter", Font.BOLD, 24));
        AppDialogThemeHelper.styleLabel(titleLabel, true, dark);
        summaryCard.add(titleLabel, BorderLayout.NORTH);

        JLabel subtitleLabel = new JLabel(developer + "  •  " + publisher);
        subtitleLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        subtitleLabel.setForeground(theme.mutedText);
        summaryCard.add(subtitleLabel, BorderLayout.CENTER);

        root.add(summaryCard, BorderLayout.NORTH);

        JPanel body = AppDialogThemeHelper.createSurfacePanel(new BorderLayout(12, 0), dark);
        body.setOpaque(false);

        JPanel factsGrid = AppDialogThemeHelper.createSurfacePanel(new GridLayout(0, 2, 10, 10), dark);
        factsGrid.setOpaque(false);

        for (int i = 0; i < columns.length; i++) {
            String columnName = columns[i];
            if ("Title".equalsIgnoreCase(columnName) || "Description".equalsIgnoreCase(columnName)) {
                continue;
            }

            String value = i < row.length && row[i] != null ? row[i].trim() : "";
            if (value.isEmpty()) {
                value = "N/A";
            }

            if ("Platform".equalsIgnoreCase(columnName)) {
                columnName = "Platform(s)";
                value = value.replace(" | ", ", ").replace("|", ", ");
            }

            factsGrid.add(createFactCard(columnName, value, dark));
        }

        JScrollPane factsScroll = new JScrollPane(factsGrid);
        factsScroll.setPreferredSize(new Dimension(390, 360));
        AppDialogThemeHelper.styleScrollPane(factsScroll, dark);
        body.add(factsScroll, BorderLayout.WEST);

        JPanel descriptionCard = AppDialogThemeHelper.createCardPanel(new BorderLayout(0, 10), dark);
        descriptionCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(theme.softBorder),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));

        JLabel descriptionLabel = new JLabel("Description");
        AppDialogThemeHelper.styleLabel(descriptionLabel, true, dark);
        descriptionCard.add(descriptionLabel, BorderLayout.NORTH);

        JTextArea descriptionArea = new JTextArea(description);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setEditable(false);
        descriptionArea.setCaretPosition(0);
        AppDialogThemeHelper.styleTextArea(descriptionArea, dark);

        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        AppDialogThemeHelper.styleScrollPane(descriptionScroll, dark);
        descriptionCard.add(descriptionScroll, BorderLayout.CENTER);

        body.add(descriptionCard, BorderLayout.CENTER);
        root.add(body, BorderLayout.CENTER);

        AppDialogThemeHelper.showContentDialog(parent, "Game Details", root);
    }

    /**
     * Builds one compact, themed fact card used by the details window for any
     * non-description field.
     *
     * @param labelText display label for the field
     * @param valueText display value for the field
     * @param dark whether the current helper theme is dark mode
     * @return fully styled fact card panel
     */
    private static JPanel createFactCard(String labelText, String valueText, boolean dark) {
        JPanel card = AppDialogThemeHelper.createCardPanel(new BorderLayout(0, 6), dark);
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel label = new JLabel(labelText);
        AppDialogThemeHelper.styleLabel(label, true, dark);
        label.setFont(new Font("Inter", Font.BOLD, 13));

        JTextArea value = new JTextArea(valueText);
        value.setEditable(false);
        value.setLineWrap(true);
        value.setWrapStyleWord(true);
        value.setRows(Math.max(2, Math.min(5, (valueText.length() / 24) + 1)));
        AppDialogThemeHelper.styleTextArea(value, dark);

        card.add(label, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    /**
     * Returns the first non-blank value that matches the requested column name.
     *
     * @param columns ordered column names for the row
     * @param row row data aligned to the provided columns
     * @param wantedColumn target column to read
     * @param fallback fallback value when the requested column is missing or
     *                 blank
     * @return resolved value for the details view header/body
     */
    private static String valueFor(String[] columns, String[] row, String wantedColumn, String fallback) {
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].equalsIgnoreCase(wantedColumn)) {
                if (i < row.length && row[i] != null && !row[i].trim().isEmpty()) {
                    return row[i].trim();
                }
                break;
            }
        }
        return fallback;
    }
}
