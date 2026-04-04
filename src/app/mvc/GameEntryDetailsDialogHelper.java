package app.mvc;

import javax.swing.*;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

public class GameEntryDetailsDialogHelper {
    /**
     * Builds and shows the details dialog used when a game entry is double-clicked.
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
