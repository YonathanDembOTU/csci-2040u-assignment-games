package app.mvc;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds and validates the add/edit game dialog used by the main desktop data
 * view.
 * <p>
 * The helper keeps the entry form layout, themed field styling, and dialog
 * result collection outside of the controller so the controller can focus on
 * application flow instead of Swing form construction.
 */
public class DataEntryDialogHelper {
    private static final String MULTI_VALUE_SEPARATOR = "|";

    /**
     * Builds and displays the themed data-entry dialog, then returns the row
     * values entered by the user.
     *
     * @param parent owning frame used for dialog placement and theme lookup
     * @param title dialog title shown in the header
     * @param columns CSV column names used to build the form
     * @param defaults optional default values for edit/import workflows
     * @param lockPublisherField whether the publisher field should be read-only
     * @param publisherName publisher value applied when the field is locked
     * @param generatedId generated identifier displayed for id columns
     * @return the completed row values, or {@code null} when the dialog is
     *         cancelled
     */
    public static String[] showDialog(JFrame parent, String title, String[] columns, String[] defaults,
                                      boolean lockPublisherField, String publisherName, String generatedId) {
        boolean dark = AppDialogThemeHelper.isDark(parent);
        AppDialogThemeHelper.Theme theme = AppDialogThemeHelper.getTheme(parent);

        int pubIdx = getColumnIndex(columns, "Publisher");
        int descriptionIdx = getColumnIndex(columns, "Description");
        int idIdx = getIdColumnIndex(columns);

        JPanel fieldsGrid = AppDialogThemeHelper.createSurfacePanel(new GridBagLayout(), dark);
        JTextField[] fields = new JTextField[columns.length];
        JTextArea descriptionArea = null;

        fieldsGrid.setBorder(BorderFactory.createEmptyBorder(6, 8, 2, 8));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;

        int visualFieldIndex = 0;
        Font labelFont = new Font("Inter", Font.BOLD, 13);
        Font fieldFont = new Font("Inter", Font.PLAIN, 14);

        for (int i = 0; i < columns.length; i++) {
            if (i == descriptionIdx || i == idIdx) {
                continue;
            }

            String columnName = columns[i];
            JPanel labelPanel = AppDialogThemeHelper.createSurfacePanel(new FlowLayout(FlowLayout.LEFT, 4, 0), dark);

            JLabel label = new JLabel(columnName);
            label.setFont(labelFont);
            AppDialogThemeHelper.styleLabel(label, false, dark);
            labelPanel.add(label);

            if ("Platform".equalsIgnoreCase(columnName)) {
                JButton infoButton = new JButton("(plural)");
                infoButton.setMargin(new Insets(2, 8, 2, 8));
                infoButton.setFocusable(false);
                infoButton.setFont(new Font("Inter", Font.BOLD, 11));
                AppDialogThemeHelper.styleButton(infoButton, false, dark);
                infoButton.addActionListener(e -> AppDialogThemeHelper.showMessageDialog(
                        parent,
                        "Platform Entry Help",
                        "Enter one or more platforms using the | symbol between each value.\n\n"
                                + "Example:\nPC | PlayStation 5 | Xbox Series X/S",
                        JOptionPane.INFORMATION_MESSAGE));
                labelPanel.add(infoButton);
            }

            JTextField field = new JTextField(defaults == null ? "" : defaults[i]);
            field.setFont(fieldFont);
            field.setPreferredSize(new Dimension(190, 30));
            AppDialogThemeHelper.styleTextField(field, dark);
            fields[i] = field;

            if (lockPublisherField && i == pubIdx) {
                field.setText(publisherName);
                field.setEditable(false);
                field.setBackground(theme.cardBg);
                field.setForeground(theme.mutedText);
            }

            int pairColumn = visualFieldIndex % 2;
            int rowGroup = visualFieldIndex / 2;

            gbc.gridx = pairColumn * 2;
            gbc.gridy = rowGroup;
            gbc.weightx = 0;
            fieldsGrid.add(labelPanel, gbc);

            gbc.gridx = pairColumn * 2 + 1;
            gbc.weightx = 1.0;
            fieldsGrid.add(field, gbc);

            visualFieldIndex++;
        }

        JPanel content = AppDialogThemeHelper.createSurfacePanel(new BorderLayout(0, 10), dark);
        content.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        content.setPreferredSize(new Dimension(960, 520));
        content.setMinimumSize(new Dimension(900, 500));

        if (idIdx != -1) {
            JPanel idPanel = AppDialogThemeHelper.createSurfacePanel(new FlowLayout(FlowLayout.LEFT, 12, 0), dark);
            idPanel.setOpaque(false);
            idPanel.setBorder(BorderFactory.createEmptyBorder(4, 12, 0, 12));
            JLabel idLabel = new JLabel("Game ID:");
            AppDialogThemeHelper.styleLabel(idLabel, false, dark);
            idPanel.add(idLabel);
            JLabel idValueLabel = new JLabel(generatedId == null ? "" : generatedId);
            idValueLabel.setFont(new Font("Inter", Font.BOLD, 15));
            idValueLabel.setForeground(theme.accent);
            idPanel.add(idValueLabel);
            content.add(idPanel, BorderLayout.NORTH);
        }

        fieldsGrid.setOpaque(false);
        content.add(fieldsGrid, BorderLayout.CENTER);

        if (descriptionIdx != -1) {
            JLabel descriptionLabel = new JLabel("Description");
            descriptionLabel.setFont(labelFont);
            AppDialogThemeHelper.styleLabel(descriptionLabel, false, dark);

            descriptionArea = new JTextArea(defaults == null ? "" : defaults[descriptionIdx], 5, 38);
            descriptionArea.setFont(fieldFont);
            AppDialogThemeHelper.styleTextArea(descriptionArea, dark);

            descriptionArea.setPreferredSize(new Dimension(820, 150));
            descriptionArea.setMinimumSize(new Dimension(820, 150));

            JPanel descriptionPanel = AppDialogThemeHelper.createSurfacePanel(new BorderLayout(0, 5), dark);
            descriptionPanel.setOpaque(false);
            descriptionPanel.setBorder(BorderFactory.createEmptyBorder(0, 12, 6, 12));
            descriptionPanel.add(descriptionLabel, BorderLayout.NORTH);
            descriptionPanel.add(descriptionArea, BorderLayout.CENTER);
            content.add(descriptionPanel, BorderLayout.SOUTH);
        }

        int result = AppDialogThemeHelper.showConfirmDialog(
                parent,
                title,
                content,
                "Save",
                "Cancel",
                new Dimension(980, 620));
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        String[] row = new String[columns.length];
        for (int i = 0; i < columns.length; i++) {
            if (i == idIdx) {
                row[i] = generatedId == null ? "" : generatedId.trim();
            } else if (i == descriptionIdx) {
                row[i] = descriptionArea == null ? "" : descriptionArea.getText().trim();
            } else {
                row[i] = fields[i] == null ? "" : fields[i].getText().trim();
            }
        }

        if (lockPublisherField && pubIdx != -1) {
            row[pubIdx] = publisherName == null ? "" : publisherName.trim();
        }

        int platformIdx = getColumnIndex(columns, "Platform");
        if (platformIdx != -1) {
            row[platformIdx] = normalizeMultiValueCell(row[platformIdx]);
        }

        return row;
    }

    /**
     * Finds the index of a named column using case-insensitive matching.
     *
     * @param columns available column names
     * @param wantedName target column name
     * @return matching index, or {@code -1} when not found
     */
    private static int getColumnIndex(String[] columns, String wantedName) {
        if (columns == null || wantedName == null) {
            return -1;
        }

        for (int i = 0; i < columns.length; i++) {
            if (wantedName.equalsIgnoreCase(columns[i])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the game identifier column used by the entry form.
     *
     * @param columns available column names
     * @return index of the id column, or {@code -1} when none exists
     */
    /**
     * Finds the game identifier column used by the entry form.
     *
     * @param columns available column names
     * @return index of the id column, or {@code -1} when none exists
     */
    private static int getIdColumnIndex(String[] columns) {
        int idIdx = getColumnIndex(columns, "GameID");
        if (idIdx == -1) {
            idIdx = getColumnIndex(columns, "ID");
        }
        return idIdx;
    }

    /**
     * Normalizes a multi-value cell before it is stored back into the row.
     *
     * @param value raw multi-value text
     * @return normalized cell text
     */
    private static String normalizeMultiValueCell(String value) {
        String[] parts = splitMultiValueCell(value);
        return String.join(" " + MULTI_VALUE_SEPARATOR + " ", parts);
    }

    /**
     * Splits a pipe-separated multi-value cell into individual entries.
     *
     * @param value stored cell text
     * @return normalized individual values
     */
    private static String[] splitMultiValueCell(String value) {
        if (value == null || value.isBlank()) {
            return new String[0];
        }

        String[] rawParts = value.split("\\s*\\" + MULTI_VALUE_SEPARATOR + "\\s*");
        List<String> cleaned = new ArrayList<>();
        for (String part : rawParts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                cleaned.add(trimmed);
            }
        }
        return cleaned.toArray(new String[0]);
    }
}
