package app.mvc;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DataEntryDialogHelper {
    // Multi-platform values are stored inside one field using | as a separator.
    private static final String MULTI_VALUE_SEPARATOR = "|";

    /**
     * Builds and shows the add/edit entry dialog used by the main data view.
     */
    public static String[] showDialog(JFrame parent, String title, String[] columns, String[] defaults,
                                      boolean lockPublisherField, String publisherName, String generatedId) {
        int pubIdx = getColumnIndex(columns, "Publisher");
        int descriptionIdx = getColumnIndex(columns, "Description");
        int idIdx = getIdColumnIndex(columns);

        JPanel fieldsGrid = new JPanel(new GridBagLayout());
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
            JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            labelPanel.setOpaque(false);

            JLabel label = new JLabel(columnName);
            label.setFont(labelFont);
            labelPanel.add(label);

            if ("Platform".equalsIgnoreCase(columnName)) {
                JButton infoButton = new JButton("(plural)");
                infoButton.setMargin(new Insets(2, 8, 2, 8));
                infoButton.setFocusable(false);
                infoButton.setFont(new Font("Inter", Font.BOLD, 11));
                infoButton.addActionListener(e -> JOptionPane.showMessageDialog(
                        parent,
                        "Enter one or more platforms using the | symbol between each value.\n\n" +
                                "Example:\nPC | PlayStation 5 | Xbox Series X/S",
                        "Platform Entry Help",
                        JOptionPane.INFORMATION_MESSAGE));
                labelPanel.add(infoButton);
            }

            JTextField field = new JTextField(defaults == null ? "" : defaults[i]);
            field.setFont(fieldFont);
            field.setPreferredSize(new Dimension(190, 30));
            fields[i] = field;

            if (lockPublisherField && i == pubIdx) {
                field.setText(publisherName);
                field.setEditable(false);
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

        JPanel content = new JPanel(new BorderLayout(0, 8));

        if (idIdx != -1) {
            JPanel idPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
            idPanel.setBorder(BorderFactory.createEmptyBorder(4, 12, 0, 12));
            idPanel.add(new JLabel("Game ID:"));
            JLabel idValueLabel = new JLabel(generatedId == null ? "" : generatedId);
            idValueLabel.setFont(new Font("Inter", Font.BOLD, 15));
            idPanel.add(idValueLabel);
            content.add(idPanel, BorderLayout.NORTH);
        }

        content.add(fieldsGrid, BorderLayout.CENTER);

        if (descriptionIdx != -1) {
            JLabel descriptionLabel = new JLabel("Description");
            descriptionLabel.setFont(labelFont);

            descriptionArea = new JTextArea(defaults == null ? "" : defaults[descriptionIdx], 5, 38);
            descriptionArea.setLineWrap(true);
            descriptionArea.setWrapStyleWord(true);
            descriptionArea.setFont(fieldFont);
            descriptionArea.setMargin(new Insets(8, 8, 8, 8));

            JScrollPane descriptionPane = new JScrollPane(descriptionArea);
            descriptionPane.setPreferredSize(new Dimension(620, 125));

            JPanel descriptionPanel = new JPanel(new BorderLayout(0, 5));
            descriptionPanel.setBorder(BorderFactory.createEmptyBorder(0, 12, 6, 12));
            descriptionPanel.add(descriptionLabel, BorderLayout.NORTH);
            descriptionPanel.add(descriptionPane, BorderLayout.CENTER);
            content.add(descriptionPanel, BorderLayout.SOUTH);
        }

        JScrollPane wrapper = new JScrollPane(content);
        wrapper.setBorder(BorderFactory.createEmptyBorder());
        wrapper.setPreferredSize(new Dimension(700, 350));
        wrapper.getVerticalScrollBar().setUnitIncrement(16);

        int result = JOptionPane.showConfirmDialog(
                parent,
                wrapper,
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String[] row = new String[columns.length];

            for (int i = 0; i < columns.length; i++) {
                String value;

                if (i == idIdx) {
                    value = generatedId == null ? "" : generatedId;
                } else if (i == descriptionIdx && descriptionArea != null) {
                    value = descriptionArea.getText().trim();
                } else {
                    value = fields[i] == null ? "" : fields[i].getText().trim();
                }

                if (value.isEmpty()) {
                    JOptionPane.showMessageDialog(parent, "All fields must be filled.");
                    return null;
                }

                if ("Platform".equalsIgnoreCase(columns[i])) {
                    value = normalizeMultiValueCell(value);
                }

                row[i] = value;
            }

            return row;
        }

        return null;
    }

    private static int getColumnIndex(String[] columns, String columnName) {
        if (columns == null || columnName == null) {
            return -1;
        }

        for (int i = 0; i < columns.length; i++) {
            if (columns[i].equalsIgnoreCase(columnName)) {
                return i;
            }
        }

        return -1;
    }

    private static int getIdColumnIndex(String[] columns) {
        int idx = getColumnIndex(columns, "GameID");
        if (idx == -1) {
            idx = getColumnIndex(columns, "ID");
        }
        return idx;
    }

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

    private static String normalizeMultiValueCell(String value) {
        String[] parts = splitMultiValueCell(value);
        return String.join(" " + MULTI_VALUE_SEPARATOR + " ", parts);
    }
}
