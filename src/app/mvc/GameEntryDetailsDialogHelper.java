package app.mvc;

import javax.swing.*;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

/**
 * Builds the themed details dialog shown when a game entry is opened for viewing.
 */
public class GameEntryDetailsDialogHelper {
    /**
     * Builds, shows, and returns the result of the themed dialog for the supplied data.
     *
     * @param parent the parent component used for ownership and theme lookup
     * @param columns the column names involved in the operation
     * @param row the row values involved in the operation
     */
    public static void showDialog(Component parent, String[] columns, String[] row) {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Inter", Font.PLAIN, 13));

        StringBuilder details = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {
            String label = columns[i];
            String value = i < row.length ? row[i] : "";

            if ("Platform".equalsIgnoreCase(label)) {
                label = "Platform(s)";
                value = value.replace(" | ", "\n • ");
                if (!value.isBlank()) {
                    value = "• " + value;
                }
            }

            details.append(label).append(": ").append(value).append("\n");
        }

        area.setText(details.toString());
        area.setCaretPosition(0);

        JScrollPane pane = new JScrollPane(area);
        pane.setPreferredSize(new Dimension(460, 320));

        JOptionPane.showMessageDialog(
                parent,
                pane,
                "Game Details",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
