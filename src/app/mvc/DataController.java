package app.mvc;

import app.auth.AuthManager;
import app.StartUp;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class DataController {
    // Multi-platform values are stored inside one CSV cell using | as a separator.
    private static final String MULTI_VALUE_SEPARATOR = "|";

    private final DataModel model;
    private final DataView view;
    private final AuthManager.UserSession session;

    private boolean showingAllColumns = false;
    private final List<Integer> visibleRowIndexes = new ArrayList<>();

    /**
     * Wires the model, view, and session together for the main UI.
     */
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
     * Sets up live search, platform filtering, counters, and filter visibility.
     */
    private void setupSearchAndFilterUI() {
        view.searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                refreshVisibleTable();
            }

            public void removeUpdate(DocumentEvent e) {
                refreshVisibleTable();
            }

            public void changedUpdate(DocumentEvent e) {
                refreshVisibleTable();
            }
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

    public static void launchMainUI(AuthManager.UserSession session) {
        SwingUtilities.invokeLater(() -> {
            DataView view = new DataView();
            DataModel model = new DataModel();
            new DataController(model, view, session);
        });
    }

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
     * Loads the CSV file used by the application on startup.
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
            JOptionPane.showMessageDialog(view, "Could not load data.csv.");
        }
    }

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

    private void populateFilterCombo(JComboBox<String> combo, String columnName) {
        String previous = (String) combo.getSelectedItem();

        combo.removeAllItems();
        combo.addItem("All");

        int columnIndex = getColumnIndex(columnName);
        if (columnIndex == -1) {
            return;
        }

        TreeSet<String> values = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (int rowIndex = 0; rowIndex < model.getRowCount(); rowIndex++) {
            String[] row = model.getRow(rowIndex);
            if (!canViewRow(row)) {
                continue;
            }

            for (String value : splitMultiValueCell(row[columnIndex])) {
                if (!value.isEmpty()) {
                    values.add(value);
                }
            }
        }

        for (String value : values) {
            combo.addItem(value);
        }

        if (previous != null) {
            selectComboValue(combo, previous);
        } else {
            combo.setSelectedIndex(0);
        }
    }

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

    private void refreshVisibleTable() {
        String[] allColumns = model.getColumns();
        if (allColumns == null || allColumns.length == 0) {
            view.setTableData(new Object[0][0], new String[0]);
            view.setSearchStatus(0, 0, view.searchField.getText().trim());
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
        int accessibleTotal = 0;

        for (int i = 0; i < visibleIndexes.length; i++) {
            visibleColumns[i] = allColumns[visibleIndexes[i]];
        }

        for (int rowIndex = 0; rowIndex < model.getRowCount(); rowIndex++) {
            String[] fullRow = model.getRow(rowIndex);

            if (!canViewRow(fullRow)) {
                continue;
            }

            accessibleTotal++;

            if (!matchesTextFilter(fullRow, filterColIdx, filterText)) {
                continue;
            }
            if (!matchesComboFilter(fullRow, "Genre", view.genreFilterCombo)) {
                continue;
            }
            if (!matchesComboFilter(fullRow, "ESRBRating", view.ratingFilterCombo)) {
                continue;
            }
            if (!matchesComboFilter(fullRow, "Platform", view.platformFilterCombo)) {
                continue;
            }
            if (!matchesComboFilter(fullRow, "Multiplayer", view.multiplayerFilterCombo)) {
                continue;
            }
            if (!matchesComboFilter(fullRow, "SinglePlayer", view.singlePlayerFilterCombo)) {
                continue;
            }

            Object[] visibleRow = new Object[visibleIndexes.length];
            for (int col = 0; col < visibleIndexes.length; col++) {
                String columnName = visibleColumns[col];
                String rawValue = fullRow[visibleIndexes[col]];
                visibleRow[col] = formatDisplayValue(columnName, rawValue);
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
        view.toggleColumnsBtn.setText(showingAllColumns ? "▥ Show Less" : "▤ Show More");
        view.setSearchStatus(visibleRows.size(), accessibleTotal, view.searchField.getText().trim());
        updateWindowTitle();
    }


    private String formatDisplayValue(String columnName, String rawValue) {
        if (rawValue == null) {
            return "";
        }

        if ("Platform".equalsIgnoreCase(columnName)) {
            String[] parts = splitMultiValueCell(rawValue);
            if (parts.length <= 1) {
                return rawValue;
            }

            StringBuilder html = new StringBuilder("<html>");
            for (int i = 0; i < parts.length; i++) {
                if (i > 0) {
                    html.append("<br>");
                }
                html.append(parts[i]);
            }
            html.append("</html>");
            return html.toString();
        }

        return rawValue;
    }

    private boolean matchesTextFilter(String[] row, int filterColIdx, String filterText) {
        if (filterText.isEmpty() || filterColIdx == -1) {
            return true;
        }

        String rawValue = row[filterColIdx] == null ? "" : row[filterColIdx].trim().toLowerCase();
        if (rawValue.contains(filterText)) {
            return true;
        }

        for (String part : splitMultiValueCell(rawValue)) {
            if (part.toLowerCase().contains(filterText)) {
                return true;
            }
        }

        return false;
    }

    private boolean matchesComboFilter(String[] row, String columnName, JComboBox<String> combo) {
        String selectedValue = (String) combo.getSelectedItem();
        if (selectedValue == null || selectedValue.equalsIgnoreCase("All")) {
            return true;
        }

        int columnIndex = getColumnIndex(columnName);
        if (columnIndex == -1) {
            return true;
        }

        for (String value : splitMultiValueCell(row[columnIndex])) {
            if (value.equalsIgnoreCase(selectedValue)) {
                return true;
            }
        }

        return false;
    }

    private int[] getDefaultColumnIndexes(String[] columns) {
        String[] preferred = { "Title", "Developer", "Publisher", "ESRBRating", "Platform", "Genre", "Description" };
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

    private int[] getExpandedColumnIndexes(String[] columns) {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < columns.length; i++) {
            if (!isHiddenIdColumn(columns[i])) {
                indexes.add(i);
            }
        }
        return toIntArray(indexes);
    }

    private boolean isHiddenIdColumn(String name) {
        String n = name.toLowerCase();
        return n.equals("id") || n.equals("gameid");
    }

    private int[] toIntArray(List<Integer> indexes) {
        int[] res = new int[indexes.size()];
        for (int i = 0; i < indexes.size(); i++) {
            res[i] = indexes.get(i);
        }
        return res;
    }

    private int getColumnIndex(String columnName) {
        if (columnName == null) {
            return -1;
        }

        String[] cols = model.getColumns();
        if (cols == null) {
            return -1;
        }

        for (int i = 0; i < cols.length; i++) {
            if (cols[i].equalsIgnoreCase(columnName)) {
                return i;
            }
        }

        return -1;
    }

    private int getPublisherColumnIndex() {
        return getColumnIndex("Publisher");
    }

    private int getIdColumnIndex() {
        int idx = getColumnIndex("GameID");
        if (idx == -1) {
            idx = getColumnIndex("ID");
        }
        return idx;
    }

    /**
     * Finds the lowest missing positive integer and uses it as the next game ID.
     */
    private String generateNextGameId() {
        int idIdx = getIdColumnIndex();
        if (idIdx == -1) {
            return "";
        }

        TreeSet<Integer> usedIds = new TreeSet<>();
        for (int rowIndex = 0; rowIndex < model.getRowCount(); rowIndex++) {
            String[] row = model.getRow(rowIndex);
            if (idIdx < row.length) {
                try {
                    int value = Integer.parseInt(row[idIdx].trim());
                    if (value > 0) {
                        usedIds.add(value);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        int nextId = 1;
        for (int value : usedIds) {
            if (value == nextId) {
                nextId++;
            } else if (value > nextId) {
                break;
            }
        }
        return String.valueOf(nextId);
    }

    private boolean canViewRow(String[] row) {
        // Publishers can browse all games, but can only modify games owned by their publisher.
        return true;
    }

    private boolean canModifyRow(String[] row) {
        if (session.isAdmin()) {
            return true;
        }
        if (session.isGuest()) {
            return false;
        }

        int pubIdx = getPublisherColumnIndex();
        return pubIdx != -1 && row[pubIdx].trim().equalsIgnoreCase(session.getPublisherName());
    }

    private int getSelectedModelRowIndex() {
        int sel = view.table.getSelectedRow();
        if (sel == -1 || sel >= visibleRowIndexes.size()) {
            return -1;
        }
        return visibleRowIndexes.get(sel);
    }

    /**
     * Attaches button, table, and mouse listeners for the interface.
     */
    private void attachHandlers() {
        view.addBtn.addActionListener(e -> {
            String[] row = requestRowInput("Add Entry", null);
            if (row != null) {
                model.addRow(row);
                populateSearchControls();
                refreshVisibleTable();
            }
        });

        view.editBtn.addActionListener(e -> {
            int idx = getSelectedModelRowIndex();
            if (idx == -1) {
                return;
            }
            if (!canModifyRow(model.getRow(idx))) {
                return;
            }

            String[] updated = requestRowInput("Edit Entry", model.getRow(idx));
            if (updated != null) {
                model.updateRow(idx, updated);
                populateSearchControls();
                refreshVisibleTable();
            }
        });

        view.deleteBtn.addActionListener(e -> {
            int idx = getSelectedModelRowIndex();
            if (idx == -1) {
                return;
            }
            if (!canModifyRow(model.getRow(idx))) {
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    view,
                    "Are you sure you want to delete this game entry?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                model.removeRow(idx);
                populateSearchControls();
                refreshVisibleTable();
            }
        });

        view.saveBtn.addActionListener(e -> {
            try {
                model.saveToFile();
                JOptionPane.showMessageDialog(view, "Changes saved successfully.");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(view, "Failed to save changes.");
            }
        });

        view.passwordMenuBtn.addActionListener(e -> {
            if (session.isAdmin()) {
                view.showPasswordManagementDialog();
            }
        });

        view.logoutBtn.addActionListener(e -> {
            view.dispose();
            SwingUtilities.invokeLater(StartUp::new);
        });

        view.toggleColumnsBtn.addActionListener(e -> {
            showingAllColumns = !showingAllColumns;
            refreshVisibleTable();
        });

        view.toggleThemeBtn.addActionListener(e -> view.toggleTheme());

        // Double-clicking a game row opens the extracted details helper dialog.
        view.table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = view.table.rowAtPoint(e.getPoint());
                if (row >= 0 && e.getClickCount() == 2) {
                    showDetails(row);
                }
            }
        });
    }

    /**
     * Opens the extracted data entry helper and returns a completed row.
     */
    private String[] requestRowInput(String title, String[] defaults) {
        String[] columns = model.getColumns();
        if (columns == null || columns.length == 0) {
            return null;
        }

        int idIdx = getIdColumnIndex();
        String generatedId = idIdx == -1 ? null : (defaults == null ? generateNextGameId() : defaults[idIdx]);

        return view.showDataEntryDialog(
                title,
                columns,
                defaults,
                session.isPublisher(),
                session.getPublisherName(),
                generatedId);
    }

    private void showDetails(int visibleIdx) {
        int modelIdx = visibleRowIndexes.get(visibleIdx);
        String[] fullCols = model.getColumns();
        String[] fullRow = model.getRow(modelIdx);
        view.showRowDetails(fullCols, fullRow);
    }



    private String[] splitMultiValueCell(String value) {
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

    private String normalizeMultiValueCell(String value) {
        String[] parts = splitMultiValueCell(value);
        return String.join(" " + MULTI_VALUE_SEPARATOR + " ", parts);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StartUp::new);
    }
}
