import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class DataController {
    // Main model, view, and current logged-in session
    private final DataModel model;
    private final DataView view;
    private final AuthManager.UserSession session;

    // Tracks whether all columns are currently shown
    private boolean showingAllColumns = false;

    // Stores the real model row indexes for currently visible rows
    private final List<Integer> visibleRowIndexes = new ArrayList<>();

    public DataController(DataModel model, DataView view, AuthManager.UserSession session) {
        this.model = model;
        this.view = view;
        this.session = session;

        // Set up search/filter controls first
        setupSearchAndFilterUI();

        // Load CSV data into the model
        loadFile();

        // Attach all button and table handlers
        attachHandlers();

        // Apply permissions based on the current user role
        applyPermissionsToView();

        // Update the title bar to show current session type
        updateWindowTitle();
    }

    /**
     * Connects the basic search field, advanced filters,
     * clear button, and advanced search toggle button.
     */
    private void setupSearchAndFilterUI() {
        // Re-filter the table whenever the search text changes
        view.searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refreshVisibleTable(); }
            public void removeUpdate(DocumentEvent e) { refreshVisibleTable(); }
            public void changedUpdate(DocumentEvent e) { refreshVisibleTable(); }
        });

        // Re-filter the table whenever a combo box changes
        view.searchColumnCombo.addActionListener(e -> refreshVisibleTable());
        view.genreFilterCombo.addActionListener(e -> refreshVisibleTable());
        view.ratingFilterCombo.addActionListener(e -> refreshVisibleTable());
        view.platformFilterCombo.addActionListener(e -> refreshVisibleTable());
        view.multiplayerFilterCombo.addActionListener(e -> refreshVisibleTable());
        view.singlePlayerFilterCombo.addActionListener(e -> refreshVisibleTable());

        // Clear all search and filter settings
        view.searchClearBtn.addActionListener(e -> clearAllFilters());

        // Show or hide the advanced search panel
        view.advancedSearchBtn.addActionListener(e -> {
            boolean willShow = !view.isAdvancedSearchVisible();
            view.setAdvancedSearchVisible(willShow);
            view.applyTheme();
            view.fitWindowToTable(showingAllColumns);
        });
    }

    /**
     * Launches the main UI after a successful login or guest entry.
     */
    public static void launchMainUI(AuthManager.UserSession session) {
        SwingUtilities.invokeLater(() -> {
            DataView view = new DataView();
            DataModel model = new DataModel();
            new DataController(model, view, session);
        });
    }

    /**
     * Updates the window title depending on whether
     * the user is Admin, Publisher, or Guest.
     */
    private void updateWindowTitle() {
        String title = "Turn for Turn Co. - Database Editor";
        if (session.isAdmin()) title += " [Admin]";
        else if (session.isPublisher()) title += " [Publisher: " + session.getPublisherName() + "]";
        else title += " [Guest]";
        view.setTitle(title);
    }

    /**
     * Enables or disables buttons based on user permissions.
     * Guests cannot modify rows. Admins and publishers can.
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

        view.applyTheme();
    }

    /**
     * Loads the CSV file into the model and then refreshes the UI.
     */
    private void loadFile() {
        File dataFile = new File("data.csv");

        // Stop loading if the file does not exist
        if (!dataFile.exists()) {
            JOptionPane.showMessageDialog(view, "data.csv not found.");
            view.setInteractionEnabled(false);
            return;
        }

        try {
            model.loadFromFile(dataFile);

            // Fill search/filter controls with CSV column info
            populateSearchControls();

            // Show the current visible rows
            refreshVisibleTable();

            view.setInteractionEnabled(true);
            applyPermissionsToView();
        } catch (IOException e) {
            view.setInteractionEnabled(false);
        }
    }

    /**
     * Fills the search column combo box and advanced filter combo boxes
     * using the loaded CSV column names and unique values.
     */
    private void populateSearchControls() {
        String[] columns = model.getColumns();
        if (columns == null || columns.length == 0) {
            return;
        }

        // Fill the basic "search by column" combo box
        view.searchColumnCombo.removeAllItems();
        for (String column : columns) {
            if (!isHiddenIdColumn(column)) {
                view.searchColumnCombo.addItem(column);
            }
        }

        // Default search column should be Title if present
        selectComboValue(view.searchColumnCombo, "Title");

        // Fill each advanced filter combo box with unique values
        populateFilterCombo(view.genreFilterCombo, "Genre");
        populateFilterCombo(view.ratingFilterCombo, "ESRBRating");
        populateFilterCombo(view.platformFilterCombo, "Platform");
        populateFilterCombo(view.multiplayerFilterCombo, "Multiplayer");
        populateFilterCombo(view.singlePlayerFilterCombo, "SinglePlayer");
    }

    /**
     * Adds unique values from one CSV column into one filter combo box.
     */
    private void populateFilterCombo(JComboBox<String> combo, String columnName) {
        combo.removeAllItems();
        combo.addItem("All");

        int columnIndex = getColumnIndex(columnName);
        if (columnIndex == -1) {
            return;
        }

        // TreeSet avoids duplicates and sorts values automatically
        TreeSet<String> values = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        for (int rowIndex = 0; rowIndex < model.getRowCount(); rowIndex++) {
            String[] row = model.getRow(rowIndex);

            // Only include rows the user is allowed to see
            if (!canViewRow(row)) continue;

            String value = row[columnIndex].trim();
            if (!value.isEmpty()) {
                values.add(value);
            }
        }

        for (String value : values) {
            combo.addItem(value);
        }

        combo.setSelectedIndex(0);
    }

    /**
     * Selects a combo box item if that value exists.
     */
    private void selectComboValue(JComboBox<String> combo, String desiredValue) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            String item = combo.getItemAt(i);
            if (item != null && item.equalsIgnoreCase(desiredValue)) {
                combo.setSelectedIndex(i);
                return;
            }
        }

        if (combo.getItemCount() > 0) {
            combo.setSelectedIndex(0);
        }
    }

    /**
     * Resets search text and all advanced filters back to defaults.
     */
    private void clearAllFilters() {
        view.searchField.setText("");

        if (view.searchColumnCombo.getItemCount() > 0) {
            selectComboValue(view.searchColumnCombo, "Title");
        }

        view.genreFilterCombo.setSelectedIndex(0);
        view.ratingFilterCombo.setSelectedIndex(0);
        view.platformFilterCombo.setSelectedIndex(0);
        view.multiplayerFilterCombo.setSelectedIndex(0);
        view.singlePlayerFilterCombo.setSelectedIndex(0);

        refreshVisibleTable();
    }

    /**
     * Rebuilds the visible table based on:
     * - current user permissions
     * - search text
     * - selected search column
     * - advanced filter selections
     * - compact or expanded column mode
     */
    private void refreshVisibleTable() {
        String[] allColumns = model.getColumns();

        // If no columns exist yet, show an empty table
        if (allColumns == null || allColumns.length == 0) {
            view.setTableData(new Object[0][0], new String[0]);
            return;
        }

        visibleRowIndexes.clear();

        // Read search text and search column
        String filterText = view.searchField.getText().trim().toLowerCase();
        String selectedSearchColumn = (String) view.searchColumnCombo.getSelectedItem();
        int filterColIdx = getColumnIndex(selectedSearchColumn);

        // Choose which columns are visible based on compact/expanded mode
        int[] visibleIndexes = showingAllColumns
                ? getExpandedColumnIndexes(allColumns)
                : getDefaultColumnIndexes(allColumns);

        String[] visibleColumns = new String[visibleIndexes.length];
        List<Object[]> visibleRows = new ArrayList<>();

        // Build the visible column header array
        for (int i = 0; i < visibleIndexes.length; i++) {
            visibleColumns[i] = allColumns[visibleIndexes[i]];
        }

        // Check every row in the model
        for (int rowIndex = 0; rowIndex < model.getRowCount(); rowIndex++) {
            String[] fullRow = model.getRow(rowIndex);

            // Only allow rows visible to this user
            if (!canViewRow(fullRow)) continue;

            // Apply basic search text filter
            if (!matchesTextFilter(fullRow, filterColIdx, filterText)) continue;

            // Apply advanced combo box filters
            if (!matchesComboFilter(fullRow, "Genre", view.genreFilterCombo)) continue;
            if (!matchesComboFilter(fullRow, "ESRBRating", view.ratingFilterCombo)) continue;
            if (!matchesComboFilter(fullRow, "Platform", view.platformFilterCombo)) continue;
            if (!matchesComboFilter(fullRow, "Multiplayer", view.multiplayerFilterCombo)) continue;
            if (!matchesComboFilter(fullRow, "SinglePlayer", view.singlePlayerFilterCombo)) continue;

            // Build only the currently visible columns for this row
            Object[] visibleRow = new Object[visibleIndexes.length];
            for (int col = 0; col < visibleIndexes.length; col++) {
                visibleRow[col] = fullRow[visibleIndexes[col]];
            }

            visibleRows.add(visibleRow);
            visibleRowIndexes.add(rowIndex);
        }

        // Convert list into array for the table model
        Object[][] visibleData = new Object[visibleRows.size()][visibleIndexes.length];
        for (int i = 0; i < visibleRows.size(); i++) {
            visibleData[i] = visibleRows.get(i);
        }

        // Update the table display
        view.setTableData(visibleData, visibleColumns);
        view.resizeColumnsToFitContent();
        view.fitWindowToTable(showingAllColumns);
        view.toggleColumnsBtn.setText(showingAllColumns ? "Show Less" : "Show More");
    }

    /**
     * Checks whether a row matches the current search text.
     */
    private boolean matchesTextFilter(String[] row, int filterColIdx, String filterText) {
        if (filterText.isEmpty() || filterColIdx == -1) {
            return true;
        }
        return row[filterColIdx].toLowerCase().contains(filterText);
    }

    /**
     * Checks whether a row matches a selected advanced filter combo value.
     */
    private boolean matchesComboFilter(String[] row, String columnName, JComboBox<String> combo) {
        String selectedValue = (String) combo.getSelectedItem();

        if (selectedValue == null || selectedValue.equalsIgnoreCase("All")) {
            return true;
        }

        int columnIndex = getColumnIndex(columnName);
        if (columnIndex == -1) {
            return true;
        }

        return row[columnIndex].trim().equalsIgnoreCase(selectedValue);
    }

    /**
     * Returns the default compact set of columns shown first.
     */
    private int[] getDefaultColumnIndexes(String[] columns) {
        String[] preferred = {"Title", "Developer", "Publisher", "ESRBRating", "Platform", "Genre"};
        List<Integer> indexes = new ArrayList<>();

        for (String wanted : preferred) {
            for (int i = 0; i < columns.length; i++) {
                if (columns[i].equalsIgnoreCase(wanted)) {
                    indexes.add(i);
                    break;
                }
            }
        }

        return indexes.isEmpty() ? getExpandedColumnIndexes(columns) : toIntArray(indexes);
    }

    /**
     * Returns every non-hidden column for expanded view.
     */
    private int[] getExpandedColumnIndexes(String[] columns) {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < columns.length; i++) {
            if (!isHiddenIdColumn(columns[i])) indexes.add(i);
        }
        return toIntArray(indexes);
    }

    /**
     * Hides ID columns from the visible table.
     */
    private boolean isHiddenIdColumn(String name) {
        String n = name.toLowerCase();
        return n.equals("id") || n.equals("gameid");
    }

    /**
     * Converts a List<Integer> into an int[] array.
     */
    private int[] toIntArray(List<Integer> indexes) {
        int[] res = new int[indexes.size()];
        for (int i = 0; i < indexes.size(); i++) res[i] = indexes.get(i);
        return res;
    }

    /**
     * Returns the index of a column by name.
     */
    private int getColumnIndex(String columnName) {
        if (columnName == null) return -1;

        String[] cols = model.getColumns();
        if (cols == null) return -1;

        for (int i = 0; i < cols.length; i++) {
            if (cols[i].equalsIgnoreCase(columnName)) return i;
        }

        return -1;
    }

    /**
     * Convenience method for finding the Publisher column.
     */
    private int getPublisherColumnIndex() {
        return getColumnIndex("Publisher");
    }

    /**
     * Checks whether the current session is allowed to view a row.
     * Admins and guests can view all rows.
     * Publishers can only view rows matching their publisher name.
     */
    private boolean canViewRow(String[] row) {
        if (session.isAdmin() || session.isGuest()) return true;

        int pubIdx = getPublisherColumnIndex();
        return pubIdx != -1 && row[pubIdx].trim().equalsIgnoreCase(session.getPublisherName());
    }

    /**
     * Checks whether the current session is allowed to modify a row.
     */
    private boolean canModifyRow(String[] row) {
        if (session.isAdmin()) return true;
        if (session.isGuest()) return false;

        int pubIdx = getPublisherColumnIndex();
        return pubIdx != -1 && row[pubIdx].trim().equalsIgnoreCase(session.getPublisherName());
    }

    /**
     * Returns the selected visible table row converted back
     * into the real model row index.
     */
    private int getSelectedModelRowIndex() {
        int sel = view.table.getSelectedRow();
        if (sel == -1 || sel >= visibleRowIndexes.size()) return -1;
        return visibleRowIndexes.get(sel);
    }

    /**
     * Connects all buttons and row click behavior.
     */
    private void attachHandlers() {
        // Add a new row
        view.addBtn.addActionListener(e -> {
            String[] row = promptRow("Add Entry", null);
            if (row != null) {
                model.addRow(row);
                refreshVisibleTable();
                populateSearchControls();
            }
        });

        // Edit selected row
        view.editBtn.addActionListener(e -> {
            int idx = getSelectedModelRowIndex();
            if (idx == -1) return;
            if (!canModifyRow(model.getRow(idx))) return;

            String[] updated = promptRow("Edit Entry", model.getRow(idx));
            if (updated != null) {
                model.updateRow(idx, updated);
                refreshVisibleTable();
                populateSearchControls();
            }
        });

        // Delete selected row
        view.deleteBtn.addActionListener(e -> {
            int idx = getSelectedModelRowIndex();
            if (idx == -1) return;
            if (!canModifyRow(model.getRow(idx))) return;

            if (JOptionPane.showConfirmDialog(view, "Delete?") == JOptionPane.YES_OPTION) {
                model.removeRow(idx);
                refreshVisibleTable();
                populateSearchControls();
            }
        });

        // Save current data back into CSV
        view.saveBtn.addActionListener(e -> {
            try {
                model.saveToFile();
                JOptionPane.showMessageDialog(view, "Saved");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(view, "Error");
            }
        });

        // Log out and return to startup screen
        view.logoutBtn.addActionListener(e -> {
            view.dispose();
            SwingUtilities.invokeLater(StartUp::new);
        });

        // Toggle between compact and expanded table view
        view.toggleColumnsBtn.addActionListener(e -> {
            showingAllColumns = !showingAllColumns;
            refreshVisibleTable();
        });

        // Switch between light and dark theme
        view.toggleThemeBtn.addActionListener(e -> view.toggleTheme());

        // Clicking a row once shows full details
        view.table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = view.table.rowAtPoint(e.getPoint());
                if (row >= 0 && e.getClickCount() == 1) showDetails(row);
            }
        });
    }

    /**
     * Shows full information for one selected row.
     */
    private void showDetails(int visibleIdx) {
        int modelIdx = visibleRowIndexes.get(visibleIdx);
        String[] fullCols = model.getColumns();
        String[] fullRow = model.getRow(modelIdx);
        view.showRowDetails(fullCols, fullRow);
    }

    /**
     * Opens a form for adding or editing a row.
     * Publishers cannot change the Publisher field away from their own name.
     */
    private String[] promptRow(String title, String[] defaults) {
        JPanel p = new JPanel(new GridLayout(0, 2, 5, 5));
        JTextField[] f = new JTextField[model.getColumns().length];
        int pubIdx = getPublisherColumnIndex();

        for (int i = 0; i < f.length; i++) {
            p.add(new JLabel(model.getColumns()[i]));
            f[i] = new JTextField(defaults == null ? "" : defaults[i]);

            // Lock publisher field for publisher accounts
            if (session.isPublisher() && i == pubIdx) {
                f[i].setText(session.getPublisherName());
                f[i].setEditable(false);
            }

            p.add(f[i]);
        }

        if (JOptionPane.showConfirmDialog(view, p, title, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String[] row = new String[f.length];
            for (int i = 0; i < f.length; i++) row[i] = f[i].getText();
            return row;
        }

        return null;
    }

    /**
     * Application entry point.
     * Opens the startup window first.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(StartUp::new);
    }
}