import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.awt.GridLayout;

public class DataController {
    private final DataModel model;
    private final DataView view;

    public DataController(DataModel model, DataView view) {
        this.model = model;
        this.view = view;

        loadFile();
        attachHandlers();
    }

    /**
     * Attempts to load data from data.csv.
     * If the file does not exist, show an error message and disable
     * interaction with the UI so the user cannot add/edit/delete/save.
     */
    private void loadFile() {
        File dataFile = new File("data.csv");

        // If the CSV file does not exist, show an error and block the UI.
        if (!dataFile.exists()) {
            JOptionPane.showMessageDialog(
                    view,
                    "Error: data.csv was not found.\nPlease add the file to the project folder and restart the program.",
                    "Missing File",
                    JOptionPane.ERROR_MESSAGE
            );

            // Show an empty table and disable all interaction.
            view.setTableData(new Object[0][0], new String[0]);
            view.setInteractionEnabled(false);
            return;
        }

        try {
            model.loadFromFile(dataFile);

            // If the file exists but contains no valid header row,
            // also disable interaction.
            if (!model.hasValidData()) {
                JOptionPane.showMessageDialog(
                        view,
                        "Error: data.csv is empty or invalid.",
                        "Invalid File",
                        JOptionPane.ERROR_MESSAGE
                );

                view.setTableData(new Object[0][0], new String[0]);
                view.setInteractionEnabled(false);
                return;
            }

            // Load the data into the table and enable interaction.
            view.setTableData(model.getData(), model.getColumns());
            view.setInteractionEnabled(true);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    view,
                    "Failed to load file.",
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE
            );

            view.setTableData(new Object[0][0], new String[0]);
            view.setInteractionEnabled(false);
        }
    }

    /**
     * Attaches button event handlers to the view.
     */
    private void attachHandlers() {
        // Add button: prompt the user for a new row and insert it into the model.
        view.addBtn.addActionListener(e -> {
            String[] row = promptRow("Add Entry", null);
            if (row != null) {
                model.addRow(row);
                view.setTableData(model.getData(), model.getColumns());
            }
        });

        // Edit button: update the currently selected row.
        view.editBtn.addActionListener(e -> {
            int selected = view.table.getSelectedRow();

            if (selected == -1) {
                JOptionPane.showMessageDialog(view, "Select a row first");
                return;
            }

            String[] current = new String[model.getColumns().length];
            for (int i = 0; i < current.length; i++) {
                current[i] = view.table.getValueAt(selected, i).toString();
            }

            String[] updated = promptRow("Edit Entry", current);
            if (updated != null) {
                model.updateRow(selected, updated);
                view.setTableData(model.getData(), model.getColumns());
            }
        });

        // Delete button: remove the currently selected row after confirmation.
        view.deleteBtn.addActionListener(e -> {
            int selected = view.table.getSelectedRow();

            if (selected == -1) {
                JOptionPane.showMessageDialog(view, "Select a row to delete");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    view,
                    "Are you sure you want to delete this entry?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                model.removeRow(selected);
                view.setTableData(model.getData(), model.getColumns());
            }
        });

        // Save button: write the current data back to the CSV file.
        view.saveBtn.addActionListener(e -> {
            try {
                model.saveToFile();
                JOptionPane.showMessageDialog(view, "Saved successfully");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(view, "Save failed");
            }
        });
    }

    /**
     * Creates a popup form for adding or editing a row.
     *
     * @param title    title of the popup window
     * @param defaults default values for edit mode, or null for add mode
     * @return the completed row, or null if cancelled
     */
    private String[] promptRow(String title, String[] defaults) {
        // Extra safety: if columns were never loaded, do not allow prompting.
        if (!model.hasValidData()) {
            JOptionPane.showMessageDialog(view, "No valid CSV data is loaded.");
            return null;
        }

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        JTextField[] fields = new JTextField[model.getColumns().length];

        // Build a label/text field pair for each column.
        for (int i = 0; i < fields.length; i++) {
            panel.add(new JLabel(model.getColumns()[i]));
            fields[i] = new JTextField(defaults == null ? "" : defaults[i]);
            panel.add(fields[i]);
        }

        int result = JOptionPane.showConfirmDialog(
                view,
                panel,
                title,
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION) {
            String[] row = new String[fields.length];

            for (int i = 0; i < fields.length; i++) {
                row[i] = fields[i].getText();
            }

            return row;
        }

        return null;
    }

    /**
     * Program entry point.
     */
    public static void main(String[] args) {
        new DataController(new DataModel(), new DataView());
    }
}
