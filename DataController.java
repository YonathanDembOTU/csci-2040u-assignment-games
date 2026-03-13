import javax.swing.*;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataController {
    // Reference to the model (data), view (GUI), and current user session
    private final DataModel model;
    private final DataView view;
    private final AuthManager.UserSession session;

    // Tracks whether all columns are currently being shown
    private boolean showingAllColumns = false;

    // Maps visible row indexes back to the real model row indexes
    private final List<Integer> visibleRowIndexes = new ArrayList<>();

    public DataController(DataModel model, DataView view, AuthManager.UserSession session) {
        this.model = model;
        this.view = view;
        this.session = session;

        // Load CSV data first, then attach button/listener behavior
        loadFile();
        attachHandlers();
        applyPermissionsToView();
        updateWindowTitle();
    }

    /**
     * Opens the main table UI for the logged-in user.
     *
     * @param session active login session
     */
    public static void launchMainUI(AuthManager.UserSession session) {
        SwingUtilities.invokeLater(() -> {
            DataView view = new DataView();
            DataModel model = new DataModel();
            new DataController(model, view, session);
        });
    }

    /**
     * Updates the window title to show the active user role.
     */
    private void updateWindowTitle() {
        String title = "Turn for Turn Co. - Database Editor";

        if (session.isAdmin()) {
            title += " [Admin]";
        } else if (session.isPublisher()) {
            title += " [Publisher: " + session.getPublisherName() + "]";
        } else {
            title += " [Guest]";
        }

        view.setTitle(title);
    }

    /**
     * Enables or disables buttons depending on the user's permissions.
     */
    private void applyPermissionsToView() {
        view.toggleColumnsBtn.setEnabled(true);
        view.toggleThemeBtn.setEnabled(true);
        view.logoutBtn.setEnabled(true);

        boolean canModify = session.canModify();
        view.addBtn.setEnabled(canModify);
        view.editBtn.setEnabled(canModify);
        view.deleteBtn.setEnabled(canModify);
        view.saveBtn.setEnabled(canModify);
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

            // Publisher accounts require a Publisher column
            if (session.isPublisher() && getPublisherColumnIndex() == -1) {
                JOptionPane.showMessageDialog(
                        view,
                        "Publisher login requires a Publisher column in data.csv.",
                        "Missing Publisher Column",
                        JOptionPane.ERROR_MESSAGE
                );

                view.setTableData(new Object[0][0], new String[0]);
                view.setInteractionEnabled(false);
                return;
            }

            refreshVisibleTable();
            view.setInteractionEnabled(true);
            applyPermissionsToView();

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
        visibleRowIndexes.clear();

        int[] visibleIndexes = showingAllColumns
                ? getExpandedColumnIndexes(allColumns)
                : getDefaultColumnIndexes(allColumns);

        String[] visibleColumns = new String[visibleIndexes.length];
        List<Object[]> visibleRows = new ArrayList<>();

        // Build visible header list
        for (int i = 0; i < visibleIndexes.length; i++) {
            visibleColumns[i] = allColumns[visibleIndexes[i]];
        }

        // Build visible row data
        for (int rowIndex = 0; rowIndex < model.getRowCount(); rowIndex++) {
            String[] fullRow = model.getRow(rowIndex);

            if (!canViewRow(fullRow)) {
                continue;
            }

            Object[] visibleRow = new Object[visibleIndexes.length];

            for (int col = 0; col < visibleIndexes.length; col++) {
                visibleRow[col] = fullRow[visibleIndexes[col]];
            }

            visibleRows.add(visibleRow);
            visibleRowIndexes.add(rowIndex);
        }

        Object[][] visibleData = new Object[visibleRows.size()][visibleIndexes.length];

        for (int i = 0; i < visibleRows.size(); i++) {
            visibleData[i] = visibleRows.get(i);
        }

        view.setTableData(visibleData, visibleColumns);
        view.resizeColumnsToFitContent();
        view.fitWindowToTable(showingAllColumns);
        view.toggleColumnsBtn.setText(showingAllColumns ? "Show Less" : "Show More");
        applyPermissionsToView();
    }

    /**
     * Returns the default set of key columns for compact mode.
     *
     * @param columns all column headers
     * @return indexes of key columns
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
     * Returns all non-ID columns for expanded mode.
     *
     * @param columns all column headers
     * @return indexes of visible expanded columns
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
     * Returns all non-ID columns for the details popup.
     *
     * @param columns all column headers
     * @return indexes used in the details view
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
     * Checks whether a column is an ID-style column that should be hidden.
     *
     * @param columnName column name
     * @return true if the column is an ID field
     */
    private boolean isHiddenIdColumn(String columnName) {
        String normalized = columnName.trim().toLowerCase();
        return normalized.equals("id")
                || normalized.equals("gameid")
                || normalized.equals("game id")
                || normalized.equals("game_id");
    }

    /**
     * Converts a list of Integer indexes into a primitive int array.
     *
     * @param indexes list of indexes
     * @return primitive int array
     */
    private int[] toIntArray(List<Integer> indexes) {
        int[] result = new int[indexes.size()];

        for (int i = 0; i < indexes.size(); i++) {
            result[i] = indexes.get(i);
        }

        return result;
    }

    /**
     * Finds the Publisher column index.
     *
     * @return Publisher column index, or -1 if not found
     */
    private int getPublisherColumnIndex() {
        String[] columns = model.getColumns();

        if (columns == null) {
            return -1;
        }

        for (int i = 0; i < columns.length; i++) {
            if (columns[i].trim().equalsIgnoreCase("Publisher")) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Returns whether the current user is allowed to see a row.
     *
     * @param row row data
     * @return true if visible to the current session
     */
    private boolean canViewRow(String[] row) {
        if (session.isAdmin() || session.isGuest()) {
            return true;
        }

        int publisherIndex = getPublisherColumnIndex();

        if (publisherIndex == -1 || publisherIndex >= row.length) {
            return false;
        }

        return row[publisherIndex].trim().equalsIgnoreCase(session.getPublisherName());
    }

    /**
     * Returns whether the current user is allowed to modify a row.
     *
     * @param row row data
     * @return true if editable/deletable by the current session
     */
    private boolean canModifyRow(String[] row) {
        if (session.isAdmin()) {
            return true;
        }

        if (session.isGuest()) {
            return false;
        }

        int publisherIndex = getPublisherColumnIndex();

        if (publisherIndex == -1 || publisherIndex >= row.length) {
            return false;
        }

        return row[publisherIndex].trim().equalsIgnoreCase(session.getPublisherName());
    }

    /**
     * Converts the selected visible table row into the real model row index.
     *
     * @return model row index, or -1 if invalid
     */
    private int getSelectedModelRowIndex() {
        int selected = view.table.getSelectedRow();

        if (selected == -1) {
            return -1;
        }

        if (selected < 0 || selected >= visibleRowIndexes.size()) {
            return -1;
        }

        return visibleRowIndexes.get(selected);
    }

    /**
     * Logs the user out and returns to the startup screen.
     */
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
                view,
                "Are you sure you want to log out?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            view.dispose();
            SwingUtilities.invokeLater(StartUp::new);
        }
    }

    /**
     * Attaches all button and table listeners.
     */
    private void attachHandlers() {
        view.addBtn.addActionListener(e -> {
            if (!session.canModify()) {
                JOptionPane.showMessageDialog(view, "Guest users cannot add entries.");
                return;
            }

            String[] row = promptRow("Add Entry", null);

            if (row != null) {
                model.addRow(row);
                refreshVisibleTable();
            }
        });

        view.editBtn.addActionListener(e -> {
            if (!session.canModify()) {
                JOptionPane.showMessageDialog(view, "Guest users cannot edit entries.");
                return;
            }

            int selectedModelIndex = getSelectedModelRowIndex();

            if (selectedModelIndex == -1) {
                JOptionPane.showMessageDialog(view, "Select a row first");
                return;
            }

            String[] current = model.getRow(selectedModelIndex);

            // Publisher accounts can only edit their own games
            if (!canModifyRow(current)) {
                JOptionPane.showMessageDialog(
                        view,
                        "You can only edit games belonging to your publisher account."
                );
                return;
            }

            String[] updated = promptRow("Edit Entry", current);

            if (updated != null) {
                model.updateRow(selectedModelIndex, updated);
                refreshVisibleTable();
            }
        });

        view.deleteBtn.addActionListener(e -> {
            if (!session.canModify()) {
                JOptionPane.showMessageDialog(view, "Guest users cannot delete entries.");
                return;
            }

            int selectedModelIndex = getSelectedModelRowIndex();

            if (selectedModelIndex == -1) {
                JOptionPane.showMessageDialog(view, "Select a row to delete");
                return;
            }

            String[] current = model.getRow(selectedModelIndex);

            // Publisher accounts can only delete their own games
            if (!canModifyRow(current)) {
                JOptionPane.showMessageDialog(
                        view,
                        "You can only delete games belonging to your publisher account."
                );
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    view,
                    "Are you sure you want to delete this entry?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                model.removeRow(selectedModelIndex);
                refreshVisibleTable();
            }
        });

        view.saveBtn.addActionListener(e -> {
            if (!session.canModify()) {
                JOptionPane.showMessageDialog(view, "Guest users cannot save changes.");
                return;
            }

            try {
                model.saveToFile();
                JOptionPane.showMessageDialog(view, "Saved successfully");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(view, "Save failed");
            }
        });

        view.logoutBtn.addActionListener(e -> logout());

        // Toggle between default columns and expanded columns
        view.toggleColumnsBtn.addActionListener(e -> {
            showingAllColumns = !showingAllColumns;
            refreshVisibleTable();
        });

        view.toggleThemeBtn.addActionListener(e -> view.toggleTheme());

        view.table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedVisibleRow = view.table.rowAtPoint(e.getPoint());

                if (selectedVisibleRow >= 0 && e.getClickCount() == 1) {
                    showDetailsForVisibleRow(selectedVisibleRow);
                }
            }
        });
    }

    /**
     * Builds and displays the detail view for one selected visible row.
     * Hidden ID columns are omitted.
     *
     * @param visibleRowIndex selected visible row index
     */
    private void showDetailsForVisibleRow(int visibleRowIndex) {
        if (visibleRowIndex < 0 || visibleRowIndex >= visibleRowIndexes.size()) {
            return;
        }

        int modelRowIndex = visibleRowIndexes.get(visibleRowIndex);
        String[] fullColumns = model.getColumns();
        String[] fullRow = model.getRow(modelRowIndex);

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
        int publisherIndex = getPublisherColumnIndex();

        // Create one label/text field pair for each column
        for (int i = 0; i < fields.length; i++) {
            panel.add(new JLabel(model.getColumns()[i]));

            String initialValue = defaults == null ? "" : defaults[i];

            // Publisher accounts are locked to their own publisher name
            if (session.isPublisher() && i == publisherIndex) {
                initialValue = session.getPublisherName();
            }

            fields[i] = new JTextField(initialValue);

            if (session.isPublisher() && i == publisherIndex) {
                fields[i].setEditable(false);
                fields[i].setBackground(new java.awt.Color(230, 230, 230));
            }

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

            // Enforce publisher ownership again before saving
            if (session.isPublisher() && publisherIndex >= 0) {
                row[publisherIndex] = session.getPublisherName();
            }

            return row;
        }

        return null;
    }

    /**
     * Program entry point.
     * Opens the StartUp screen first.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(StartUp::new);
    }
}