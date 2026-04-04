package app.mvc;

import javax.swing.*;
import java.awt.*;

public class GameEntryDetailsDialogHelper {
    public static void showDialog(Component parent, String[] columns, String[] row) {
        boolean dark = AppDialogThemeHelper.isDark(parent);

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Inter", Font.PLAIN, 13));
        AppDialogThemeHelper.styleTextArea(area, dark);

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
        AppDialogThemeHelper.styleScrollPane(pane, dark);

        AppDialogThemeHelper.showContentDialog(parent, "Game Details", pane);
    }
}
