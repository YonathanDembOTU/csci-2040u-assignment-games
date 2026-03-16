package src.app.mvc;

import app.auth.AuthManager;
import app.mvc.DataModel;
import app.mvc.DataView;
import app.StartUp;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
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

        setupSearchAndFilterUI();
        loadFile();
        attachHandlers();
        applyPermissionsToView();
        updateWindowTitle();
    }

    /**
     * Connects the basic search field, advanced filters,
     * clear button, and advanced search toggle button.
     */
    private void setupSearchAndFilterUI() {
        view.searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refreshVisibleTable(); }
            public void removeUpdate(DocumentEvent e) { refreshVisibleTable(); }
            public void changedUpdate(DocumentEvent e) { refreshVisibleTable(); }
        });

        view.searchColumnCombo.addActionListener(e -> refreshVisibleTable());
        view.genreFilterCombo.addActionListener(e -> refreshVisibleTable());
        view.ratingFilterCombo.addActionListener(e -> refreshVisibleTable());
        view.platformFilterCombo.addActionListener(e -> refreshVisibleTable());
        view.multiplayerFilterCombo.addActionListener(e -> refreshVisibleTable());
        view.singlePlayerFilterCombo.addActionListener(e -> refreshVisibleTable());

        view.searchClearBtn.addActionListener(e -> clearAllFilters());

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
     * Updates the window title depending on the current user role.
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
     */
    private void applyPermissionsToView() {
        view.toggleColumnsBtn.setEnabled(true);
        view.toggleThemeBtn.setEnabled(true);
        view.logoutBtn.setEnabled(true);
        view.passwordMenuBtn.setVisible(session.isAdmin());
        view.passwordMenuBtn.setEnabled(session.isAdmin());

        boolean canModify = session.canModify();
        view.addBtn.setEnabled(canModify);
        view.editBtn.setEnabled(canModify);
        view.deleteBtn.setEnabled(canModify);
        view.saveBtn.setEnabled(canModify);
        view.applyTheme();
    }

    /**
     * Loads the CSV file into the model and refreshes the UI.
     */
    private void loadFile() {
        File dataFile = new File("data/data.csv");
        if (!dataFile.exists()) {
            JOptionPane.showMessageDialog(view, "data.csv not found.");
            view.setInteractionEnabled(false);
            return;
        }

        try {
            model.loadFromFile(dataFile);
            populateSearchControls();
            refreshVisibleTable();
            view.setInteractionEnabled(true);
            applyPermissionsToView();
        } catch (IOException e) {
            view.setInteractionEnabled(false);
        }
    }

    /**
     * Fills the search and filter combo boxes using CSV data.
     */
    private void populateSearchControls() {
        String[] columns = model.getColumns();
        if (columns == null || columns.length == 0) {
            return;
        }

        view.searchColumnCombo.removeAllItems();
        for (String column : columns) {
            if (!isHiddenIdColumn(column)) {
                view.searchColumnCombo.addItem(column);
            }
        }

        selectComboValue(view.searchColumnCombo, "Title");
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

        TreeSet<String> values = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (int rowIndex = 0; rowIndex < model.getRowCount(); rowIndex++) {
            String[] row = model.getRow(rowIndex);
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
     * Rebuilds the visible table based on current filters and permissions.
     */
    private void refreshVisibleTable() {
        String[] allColumns = model.getColumns();
        if (allColumns == null || allColumns.length == 0) {
            view.setTableData(new Object[0][0], new String[0]);
            return;
        }

        visibleRowIndexes.clear();

        String filterText = view.searchField.getText().trim().toLowerCase();
        String selectedSearchColumn = (String) view.searchColumnCombo.getSelectedItem();
        int filterColIdx = getColumnIndex(selectedSearchColumn);

        int[] visibleIndexes = showingAllColumns
                ? getExpandedColumnIndexes(allColumns)
                : getDefaultColumnIndexes(allColumns);

        String[] visibleColumns = new String[visibleIndexes.length];
        List<Object[]> visibleRows = new ArrayList<>();

        for (int i = 0; i < visibleIndexes.length; i++) {
            visibleColumns[i] = allColumns[visibleIndexes[i]];
        }

        for (int rowIndex = 0; rowIndex < model.getRowCount(); rowIndex++) {
            String[] fullRow = model.getRow(rowIndex);

            if (!canViewRow(fullRow)) continue;
            if (!matchesTextFilter(fullRow, filterColIdx, filterText)) continue;
            if (!matchesComboFilter(fullRow, "Genre", view.genreFilterCombo)) continue;
            if (!matchesComboFilter(fullRow, "ESRBRating", view.ratingFilterCombo)) continue;
            if (!matchesComboFilter(fullRow, "Platform", view.platformFilterCombo)) continue;
            if (!matchesComboFilter(fullRow, "Multiplayer", view.multiplayerFilterCombo)) continue;
            if (!matchesComboFilter(fullRow, "SinglePlayer", view.singlePlayerFilterCombo)) continue;

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
     * Checks whether a row matches one selected advanced filter value.
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
     * Returns the selected visible row converted back into the model row index.
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
        view.addBtn.addActionListener(e -> {
            String[] row = promptRow("Add Entry", null);
            if (row != null) {
                model.addRow(row);
                refreshVisibleTable();
                populateSearchControls();
            }
        });

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

        view.saveBtn.addActionListener(e -> {
            try {
                model.saveToFile();
                JOptionPane.showMessageDialog(view, "Saved");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(view, "Error");
            }
        });

        view.passwordMenuBtn.addActionListener(e -> openPasswordHandlingMenu());

        view.logoutBtn.addActionListener(e -> {
            view.dispose();
            SwingUtilities.invokeLater(StartUp::new);
        });

        view.toggleColumnsBtn.addActionListener(e -> {
            showingAllColumns = !showingAllColumns;
            refreshVisibleTable();
        });

        view.toggleThemeBtn.addActionListener(e -> view.toggleTheme());

        view.table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = view.table.rowAtPoint(e.getPoint());
                if (row >= 0 && e.getClickCount() == 1) showDetails(row);
            }
        });
    }

    /**
     * Opens the admin-only password handling menu.
     */
    private void openPasswordHandlingMenu() {
        if (!session.isAdmin()) {
            return;
        }

        String[] options = {"View Publisher Passwords", "Change Publisher Password", "Add Publisher User"};
        String choice = (String) JOptionPane.showInputDialog(
                view,
                "Choose a password handling option:",
                "Password Handling",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == null) {
            return;
        }

        if (choice.equals(options[0])) {
            showPublisherPasswordTable();
        } else if (choice.equals(options[1])) {
            changePublisherPassword();
        } else if (choice.equals(options[2])) {
            addPublisherUser();
        }
    }

    /**
     * Shows all publisher usernames, publisher names, and passwords.
     */
    private void showPublisherPasswordTable() {
        Object[][] data = AuthManager.getPublisherAccountTableData();
        String[] columns = {"Username", "Publisher", "Password"};

        JTable passwordTable = new JTable(data, columns);
        passwordTable.setEnabled(false);
        JScrollPane pane = new JScrollPane(passwordTable);
        pane.setPreferredSize(new java.awt.Dimension(620, 280));

        JOptionPane.showMessageDialog(
                view,
                pane,
                "Publisher Passwords",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * Lets the admin change one publisher password.
     */
    private void changePublisherPassword() {
        String[] usernames = AuthManager.getPublisherUsernames();
        if (usernames.length == 0) {
            JOptionPane.showMessageDialog(view, "No publisher users found.");
            return;
        }

        JComboBox<String> userCombo = new JComboBox<>(usernames);
        JPasswordField passwordField = new JPasswordField();
        JCheckBox showBox = new JCheckBox("Show Password");
        showBox.addActionListener(e -> passwordField.setEchoChar(showBox.isSelected() ? (char) 0 : '•'));

        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.add(new JLabel("Select Publisher Username:"));
        panel.add(userCombo);
        panel.add(new JLabel("New Password:"));
        panel.add(passwordField);
        panel.add(showBox);

        int result = JOptionPane.showConfirmDialog(view, panel, "Change Publisher Password", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String username = (String) userCombo.getSelectedItem();
        String newPassword = new String(passwordField.getPassword());

        if (newPassword.trim().isEmpty()) {
            JOptionPane.showMessageDialog(view, "Password cannot be blank.");
            return;
        }

        if (AuthManager.updatePublisherPassword(username, newPassword)) {
            JOptionPane.showMessageDialog(view, "Publisher password updated.");
        } else {
            JOptionPane.showMessageDialog(view, "Could not update publisher password.");
        }
    }

    /**
     * Lets the admin add one new publisher user.
     */
    private void addPublisherUser() {
        JTextField usernameField = new JTextField();
        JTextField publisherField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JCheckBox showBox = new JCheckBox("Show Password");
        showBox.addActionListener(e -> passwordField.setEchoChar(showBox.isSelected() ? (char) 0 : '•'));

        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Publisher Name:"));
        panel.add(publisherField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(showBox);

        int result = JOptionPane.showConfirmDialog(view, panel, "Add Publisher User", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String username = usernameField.getText().trim();
        String publisherName = publisherField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (AuthManager.addPublisherUser(username, publisherName, password)) {
            JOptionPane.showMessageDialog(view, "Publisher user added.");
        } else {
            JOptionPane.showMessageDialog(view, "Could not add publisher user. Username may already exist.");
        }
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
     */
    private String[] promptRow(String title, String[] defaults) {
        JPanel p = new JPanel(new GridLayout(0, 2, 5, 5));
        JTextField[] f = new JTextField[model.getColumns().length];
        int pubIdx = getPublisherColumnIndex();

        for (int i = 0; i < f.length; i++) {
            p.add(new JLabel(model.getColumns()[i]));
            f[i] = new JTextField(defaults == null ? "" : defaults[i]);

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
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(StartUp::new);
    }
}
