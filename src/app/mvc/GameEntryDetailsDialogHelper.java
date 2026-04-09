package app.mvc;

import javax.swing.*;
import java.awt.*;

/**
 * Builds the themed read-only details dialog for a selected game entry.
 * <p>
 * The helper converts the current row values into a formatted text view so the
 * main table can show expanded details without duplicating dialog code inside
 * the controller or view.
 */
public class GameEntryDetailsDialogHelper {
    /**
     * Displays the selected game row in a themed scrollable details dialog.
     *
     * @param parent parent component used for dialog placement and theme lookup
     * @param columns ordered column names for the row
     * @param row row values to display
     */
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
