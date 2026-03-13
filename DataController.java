import javax.swing.*;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataController {
    // Reference to the model (data) and view (GUI)
    private final DataModel model;
    private final DataView view;

    // Tracks whether all columns are currently being shown
    private boolean showingAllColumns = false;

    public DataController(DataModel model, DataView view) {
        this.model = model;
        this.view = view;

        // Load CSV data first, then attach button/listener behavior
        loadFile();
        attachHandlers();
    }

    /**
     * Loads data from data.csv.
     * If the file is missing or invalid, interaction is disabled.
     */
    private void loadFile() {
        File dataFile = new File("data.csv");

        // Stop interaction if the file does not exist
        if (!dataFile.exists()) {
            JOptionPane.showMessageDialog(
                    view,
                    "Error: data.csv was not found.\nPlease add the file to the project folder and restart the program.",
                    "Missing File",
                    JOptionPane.ERROR_MESSAGE
            );

            view.setTableData(new Object[0][0], new String[0]);
            view.setInteractionEnabled(false);
            return;
        }

        try {
            model.loadFromFile(dataFile);

            // Stop interaction if the file exists but has no usable headers
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

            // Load the visible table normally
            refreshVisibleTable();
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
     * Rebuilds the visible table depending on the current mode.
     * Default mode shows only key columns.
     * Expanded mode shows all non-ID columns.
     */
    private void refreshVisibleTable() {
        String[] allColumns = model.getColumns();
        Object[][] allData = model.getData();

        int[] visibleIndexes = showingAllColumns
                ? getExpandedColumnIndexes(allColumns)
                : getDefaultColumnIndexes(allColumns);

        String[] visibleColumns = new String[visibleIndexes.length];
        Object[][] visibleData = new Object[allData.length][visibleIndexes.length];

        // Build visible header list
        for (int i = 0; i < visibleIndexes.length; i++) {
            visibleColumns[i] = allColumns[visibleIndexes[i]];
        }

        // Build visible row data
        for (int row = 0; row < allData.length; row++) {
            for (int col = 0; col < visibleIndexes.length; col++) {
                visibleData[row][col] = allData[row][visibleIndexes[col]];
            }
        }

        view.setTableData(visibleData, visibleColumns);
        view.resizeColumnsToFitContent();
        view.fitWindowToTable(showingAllColumns);
        view.toggleColumnsBtn.setText(showingAllColumns ? "Show Less" : "Show More");
    }

    /**
     * Returns indexes for the default visible columns:
     * Title, Developer, Publisher, Rating, Platform, Genre
     *
     * If none are found, fallback is the expanded non-ID column list.
     *
     * @param columns all available column names
     * @return indexes of default visible columns
     */
    private int[] getDefaultColumnIndexes(String[] columns) {
        String[] preferred = {"Title", "Developer", "Publisher", "Rating", "Platform", "Genre"};
        List<Integer> indexes = new ArrayList<>();

        for (String wanted : preferred) {
            for (int i = 0; i < columns.length; i++) {
                if (columns[i].equalsIgnoreCase(wanted)) {
                    indexes.add(i);
                    break;
                }
            }
        }

        if (indexes.isEmpty()) {
            return getExpandedColumnIndexes(columns);
        }

        return toIntArray(indexes);
    }

    /**
     * Returns indexes for all columns except hidden ID columns.
     *
     * @param columns all available column names
     * @return indexes of expanded visible columns
     */
    private int[] getExpandedColumnIndexes(String[] columns) {
        List<Integer> indexes = new ArrayList<>();

        for (int i = 0; i < columns.length; i++) {
            if (!isHiddenIdColumn(columns[i])) {
                indexes.add(i);
            }
        }

        return toIntArray(indexes);
    }

    /**
     * Returns indexes used in the row detail popup.
     * ID columns are also hidden here.
     *
     * @param columns all available column names
     * @return indexes for row detail display
     */
    private int[] getDetailColumnIndexes(String[] columns) {
        List<Integer> indexes = new ArrayList<>();

        for (int i = 0; i < columns.length; i++) {
            if (!isHiddenIdColumn(columns[i])) {
                indexes.add(i);
            }
        }

        return toIntArray(indexes);
    }

    /**
     * Checks whether a column should be hidden as a game ID field.
     *
     * @param columnName the column name to test
     * @return true if it is an ID column that should be hidden
     */
    private boolean isHiddenIdColumn(String columnName) {
        String normalized = columnName.trim().toLowerCase();
        return normalized.equals("id")
                || normalized.equals("gameid")
                || normalized.equals("game id")
                || normalized.equals("game_id");
    }

    /**
     * Utility method to convert a list of Integer indexes into an int array.
     *
     * @param indexes list of indexes
     * @return primitive int array of indexes
     */
    private int[] toIntArray(List<Integer> indexes) {
        int[] result = new int[indexes.size()];
        for (int i = 0; i < indexes.size(); i++) {
            result[i] = indexes.get(i);
        }
        return result;
    }

    /**
     * Attaches button and table event handlers.
     */
    private void attachHandlers() {
        // Add a new row
        view.addBtn.addActionListener(e -> {
            String[] row = promptRow("Add Entry", null);
            if (row != null) {
                model.addRow(row);
                refreshVisibleTable();
            }
        });

        // Edit the currently selected row
        view.editBtn.addActionListener(e -> {
            int selected = view.table.getSelectedRow();

            if (selected == -1) {
                JOptionPane.showMessageDialog(view, "Select a row first");
                return;
            }

            String[] current = model.getRow(selected);
            String[] updated = promptRow("Edit Entry", current);

            if (updated != null) {
                model.updateRow(selected, updated);
                refreshVisibleTable();
            }
        });

        // Delete the currently selected row after confirmation
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
                refreshVisibleTable();
            }
        });

        // Save current in-memory data back to data.csv
        view.saveBtn.addActionListener(e -> {
            try {
                model.saveToFile();
                JOptionPane.showMessageDialog(view, "Saved successfully");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(view, "Save failed");
            }
        });

        // Toggle between default columns and expanded columns
        view.toggleColumnsBtn.addActionListener(e -> {
            showingAllColumns = !showingAllColumns;
            refreshVisibleTable();
        });

        // Toggle between light and dark mode
        view.toggleThemeBtn.addActionListener(e -> view.toggleTheme());

        // Click a row to show expanded details for only that row
        view.table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = view.table.rowAtPoint(e.getPoint());

                if (selectedRow >= 0 && e.getClickCount() == 1) {
                    showDetailsForRow(selectedRow);
                }
            }
        });
    }

    /**
     * Builds and displays the detail view for one selected row.
     * Hidden ID columns are omitted.
     *
     * @param rowIndex the selected row index
     */
    private void showDetailsForRow(int rowIndex) {
        String[] fullColumns = model.getColumns();
        String[] fullRow = model.getRow(rowIndex);

        int[] detailIndexes = getDetailColumnIndexes(fullColumns);

        String[] visibleDetailColumns = new String[detailIndexes.length];
        String[] visibleDetailRow = new String[detailIndexes.length];

        // Copy only the non-ID fields for display
        for (int i = 0; i < detailIndexes.length; i++) {
            visibleDetailColumns[i] = fullColumns[detailIndexes[i]];
            visibleDetailRow[i] = fullRow[detailIndexes[i]];
        }

        view.showRowDetails(visibleDetailColumns, visibleDetailRow);
    }

    /**
     * Creates the popup form used for adding or editing a row.
     *
     * @param title dialog title
     * @param defaults existing row values, or null for a blank form
     * @return the entered row values, or null if cancelled
     */
    private String[] promptRow(String title, String[] defaults) {
        if (!model.hasValidData()) {
            JOptionPane.showMessageDialog(view, "No valid CSV data is loaded.");
            return null;
        }

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        JTextField[] fields = new JTextField[model.getColumns().length];

        // Create one label/text field pair for each column
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
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        new DataController(new DataModel(), new DataView());
    }
}