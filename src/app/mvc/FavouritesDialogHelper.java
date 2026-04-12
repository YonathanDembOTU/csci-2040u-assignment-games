package app.mvc;

import app.auth.AuthManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Encapsulates the user-only favourites sub-catalogue.
 * <p>
 * This helper is responsible for creating the favourites CSV file when needed,
 * adding and removing selected catalogue rows, injecting the favourites-related
 * action buttons into the main right-side action bar, and rendering a themed
 * favourites browser with search, filters, show-more / show-less behaviour,
 * and double-click details support.
 */
public class FavouritesDialogHelper {
    private static final String FAVOURITES_FILE_PATH = "data/favs.csv";
    private static final String MULTI_VALUE_SEPARATOR = "|";

    private final DataView owner;
    private final AuthManager.UserSession session;
    private final JButton toggleFavouriteButton;
    private final JButton openFavouritesButton;


    /**
     * Creates the helper bound to the main data view and the active session.
     *
     * @param owner owning data view
     * @param session authenticated session used for access checks
     */
    public FavouritesDialogHelper(DataView owner, AuthManager.UserSession session) {
        this.owner = owner;
        this.session = session;
        this.toggleFavouriteButton = new JButton("Add Favourite");
        this.openFavouritesButton = new JButton("Favourites");
    }

    /**
     * Returns whether the current session should see and use the favourites
     * feature.
     * <p>
     * The current authentication model exposes admin, publisher, and guest.
     * This project treats the guest account as the requested user-level role.
     *
     * @return {@code true} when the current session is the end-user role
     */
    public boolean isAvailableToCurrentSession() {
        return session != null && session.isGuest();
    }

    /**
     * Adds the favourites buttons to the main right-side action bar and keeps
     * their colours in sync with the current view theme.
     */
    public void installMainViewControls() {
        owner.buttonPanel.add(toggleFavouriteButton);
        owner.buttonPanel.add(openFavouritesButton);
        owner.registerSideBarButton(toggleFavouriteButton);
        owner.registerSideBarButton(openFavouritesButton);
        applyAccessRules();
        owner.buttonPanel.revalidate();
        owner.buttonPanel.repaint();
    }

    /**
     * Applies the access visibility rules to the favourites buttons.
     */
    public void applyAccessRules() {
        boolean enabled = isAvailableToCurrentSession();
        toggleFavouriteButton.setVisible(enabled);
        toggleFavouriteButton.setEnabled(enabled);
        openFavouritesButton.setVisible(enabled);
        openFavouritesButton.setEnabled(enabled);
        owner.buttonPanel.revalidate();
        owner.buttonPanel.repaint();
    }

    /**
     * Returns the main-view button used to add or remove the selected row from
     * favourites.
     *
     * @return the button instance
     */
    public JButton getToggleFavouriteButton() {
        return toggleFavouriteButton;
    }

    /**
     * Returns the main-view button used to open the favourites sub-catalogue.
     *
     * @return the button instance
     */
    public JButton getOpenFavouritesButton() {
        return openFavouritesButton;
    }

    /**
     * Adds the supplied row to favourites when it is not already present, or
     * removes it when it already exists.
     *
     * @param columns current main-catalogue column headers
     * @param row selected main-catalogue row
     */
    public void toggleFavourite(String[] columns, String[] row) {
        if (!isAvailableToCurrentSession()) {
            return;
        }
        if (columns == null || columns.length == 0 || row == null) {
            AppDialogThemeHelper.showMessageDialog(
                    owner,
                    "Favourites",
                    "The selected game could not be added to favourites because the catalogue data is unavailable.",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            DataModel favouritesModel = loadFavouritesModel(columns);
            int existingIndex = findMatchingRowIndex(favouritesModel, columns, row);

            if (existingIndex >= 0) {
                favouritesModel.removeRow(existingIndex);
                favouritesModel.saveToFile();
                AppDialogThemeHelper.showMessageDialog(
                        owner,
                        "Favourites",
                        "The selected game was removed from favourites.",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            favouritesModel.addRow(copyRow(columns, row));
            favouritesModel.saveToFile();
            AppDialogThemeHelper.showMessageDialog(
                    owner,
                    "Favourites",
                    "The selected game was added to favourites.",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            AppDialogThemeHelper.showMessageDialog(
                    owner,
                    "Favourites",
                    "The favourites file could not be updated.\n" + ex.getMessage(),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Opens the themed favourites browser dialog.
     *
     * @param mainColumns current main-catalogue column headers used when the
     *                    favourites file needs to be created
     * @param startExpanded whether the favourites browser should initially show
     *                      all non-ID columns
     */
    public void showFavouritesDialog(String[] mainColumns, boolean startExpanded) {
        if (!isAvailableToCurrentSession()) {
            return;
        }

        if (mainColumns == null || mainColumns.length == 0) {
            AppDialogThemeHelper.showMessageDialog(
                    owner,
                    "Favourites",
                    "Open the main catalogue first so the favourites file can inherit the catalogue columns.",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            DataModel favouritesModel = loadFavouritesModel(mainColumns);
            FavouritesBrowserDialog dialog = new FavouritesBrowserDialog(owner, favouritesModel, mainColumns, startExpanded);
            dialog.open();
        } catch (IOException ex) {
            AppDialogThemeHelper.showMessageDialog(
                    owner,
                    "Favourites",
                    "The favourites browser could not be opened.\n" + ex.getMessage(),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private DataModel loadFavouritesModel(String[] columns) throws IOException {
        DataModel favouritesModel = new DataModel();
        favouritesModel.loadOrCreateFile(new File(FAVOURITES_FILE_PATH), columns);
        return favouritesModel;
    }

    private String[] copyRow(String[] columns, String[] row) {
        String[] copy = new String[columns.length];
        for (int i = 0; i < columns.length; i++) {
            copy[i] = i < row.length && row[i] != null ? row[i].trim() : "";
        }
        return copy;
    }

    private int findMatchingRowIndex(DataModel model, String[] columns, String[] candidateRow) {
        int idIndex = getIdColumnIndex(columns);
        String candidateId = idIndex >= 0 && idIndex < candidateRow.length ? safe(candidateRow[idIndex]) : "";

        for (int rowIndex = 0; rowIndex < model.getRowCount(); rowIndex++) {
            String[] existing = model.getRow(rowIndex);
            if (rowsMatch(columns, existing, candidateRow, idIndex, candidateId)) {
                return rowIndex;
            }
        }
        return -1;
    }

    private boolean rowsMatch(String[] columns, String[] left, String[] right, int idIndex, String candidateId) {
        if (idIndex >= 0 && !candidateId.isBlank()) {
            String existingId = idIndex < left.length ? safe(left[idIndex]) : "";
            if (!existingId.isBlank() && existingId.equalsIgnoreCase(candidateId)) {
                return true;
            }
        }

        return valueFor(columns, left, "Title").equalsIgnoreCase(valueFor(columns, right, "Title"))
                && valueFor(columns, left, "Publisher").equalsIgnoreCase(valueFor(columns, right, "Publisher"))
                && valueFor(columns, left, "Developer").equalsIgnoreCase(valueFor(columns, right, "Developer"));
    }

    private String valueFor(String[] columns, String[] row, String wantedColumn) {
        int index = getColumnIndex(columns, wantedColumn);
        if (index == -1 || row == null || index >= row.length) {
            return "";
        }
        return safe(row[index]);
    }

    private int getIdColumnIndex(String[] columns) {
        int index = getColumnIndex(columns, "GameID");
        if (index == -1) {
            index = getColumnIndex(columns, "ID");
        }
        return index;
    }

    private int getColumnIndex(String[] columns, String columnName) {
        if (columns == null || columnName == null) {
            return -1;
        }
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Stateful controller for the themed favourites browser dialog.
     */
    private static final class FavouritesBrowserDialog {
        private final Component parent;
        private final DataModel model;
        private final String[] columns;
        private boolean showingAllColumns;
        private final List<Integer> visibleRowIndexes = new ArrayList<>();

        private final JTextField searchField = new JTextField(20);
        private final JComboBox<String> searchColumnCombo = new JComboBox<>();
        private final JComboBox<String> genreFilterCombo = new JComboBox<>();
        private final JComboBox<String> ratingFilterCombo = new JComboBox<>();
        private final JComboBox<String> platformFilterCombo = new JComboBox<>();
        private final JComboBox<String> multiplayerFilterCombo = new JComboBox<>();
        private final JComboBox<String> singlePlayerFilterCombo = new JComboBox<>();
        private final JLabel statusLabel = new JLabel();
        private final JButton toggleColumnsButton = new JButton();
        private final JButton clearButton = new JButton("Clear Filters");
        private final JButton refreshButton = new JButton("Refresh");
        private final JButton removeButton = new JButton("Remove Selected");
        private final DefaultTableModel tableModel;
        private final JTable table;

        private FavouritesBrowserDialog(Component parent, DataModel model, String[] columns, boolean startExpanded) {
            this.parent = parent;
            this.model = model;
            this.columns = model.getColumns() != null && model.getColumns().length > 0 ? model.getColumns() : columns;
            this.showingAllColumns = startExpanded;

            boolean dark = AppDialogThemeHelper.isDark(parent);
            this.tableModel = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            this.table = new JTable(tableModel) {
                @Override
                public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                    Component component = super.prepareRenderer(renderer, row, column);
                    if (!isRowSelected(row)) {
                        component.setBackground(dark ? new Color(60, 60, 60) : new Color(245, 245, 245));
                        component.setForeground(dark ? new Color(235, 235, 235) : new Color(30, 30, 30));
                    }
                    return component;
                }
            };
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table.setFillsViewportHeight(true);

            styleInputs(dark);
            wireHandlers();
            populateSearchControls();
            refreshTable();
        }

        private void open() {
            boolean dark = AppDialogThemeHelper.isDark(parent);
            JPanel root = AppDialogThemeHelper.createSurfacePanel(new BorderLayout(0, 12), dark);
            root.setPreferredSize(new Dimension(1040, 640));
            root.setOpaque(false);

            root.add(buildSearchPanel(dark), BorderLayout.NORTH);
            root.add(buildTablePanel(dark), BorderLayout.CENTER);
            root.add(buildActionPanel(dark), BorderLayout.SOUTH);

            AppDialogThemeHelper.showContentDialog(parent, "Favourites", root);
        }

        private JPanel buildSearchPanel(boolean dark) {
            JPanel wrapper = AppDialogThemeHelper.createSurfacePanel(new BorderLayout(0, 10), dark);
            wrapper.setOpaque(false);

            JPanel topRow = AppDialogThemeHelper.createCardPanel(new GridBagLayout(), dark);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(4, 6, 4, 6);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 0;

            addLabeledField(topRow, gbc, 0, "Search", searchField);
            addLabeledField(topRow, gbc, 1, "Column", searchColumnCombo);
            addLabeledField(topRow, gbc, 2, "Genre", genreFilterCombo);
            addLabeledField(topRow, gbc, 3, "Rating", ratingFilterCombo);
            addLabeledField(topRow, gbc, 4, "Platform", platformFilterCombo);
            addLabeledField(topRow, gbc, 5, "Multiplayer", multiplayerFilterCombo);
            addLabeledField(topRow, gbc, 6, "Single Player", singlePlayerFilterCombo);

            wrapper.add(topRow, BorderLayout.CENTER);

            JPanel statusPanel = AppDialogThemeHelper.createSurfacePanel(new FlowLayout(FlowLayout.LEFT, 0, 0), dark);
            statusPanel.setOpaque(false);
            AppDialogThemeHelper.styleLabel(statusLabel, true, dark);
            statusPanel.add(statusLabel);
            wrapper.add(statusPanel, BorderLayout.SOUTH);
            return wrapper;
        }

        private JPanel buildTablePanel(boolean dark) {
            JPanel tablePanel = AppDialogThemeHelper.createCardPanel(new BorderLayout(), dark);
            AppDialogThemeHelper.styleTable(table, dark);

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setPreferredSize(new Dimension(980, 420));
            AppDialogThemeHelper.styleScrollPane(scrollPane, dark);
            tablePanel.add(scrollPane, BorderLayout.CENTER);
            return tablePanel;
        }

        private JPanel buildActionPanel(boolean dark) {
            JPanel actions = AppDialogThemeHelper.createSurfacePanel(new FlowLayout(FlowLayout.RIGHT, 10, 0), dark);
            actions.setOpaque(false);

            toggleColumnsButton.setText(showingAllColumns ? "Show Less" : "Show More");
            AppDialogThemeHelper.styleButton(toggleColumnsButton, false, dark);
            AppDialogThemeHelper.styleButton(clearButton, false, dark);
            AppDialogThemeHelper.styleButton(refreshButton, false, dark);
            AppDialogThemeHelper.styleButton(removeButton, true, dark);

            actions.add(toggleColumnsButton);
            actions.add(clearButton);
            actions.add(refreshButton);
            actions.add(removeButton);
            return actions;
        }

        private void addLabeledField(JPanel panel, GridBagConstraints gbc, int gridx, String labelText, JComponent field) {
            boolean dark = AppDialogThemeHelper.isDark(parent);
            gbc.gridx = gridx;
            gbc.gridy = 0;

            JPanel cell = AppDialogThemeHelper.createSurfacePanel(new BorderLayout(0, 4), dark);
            cell.setOpaque(false);

            JLabel label = new JLabel(labelText);
            AppDialogThemeHelper.styleLabel(label, false, dark);
            cell.add(label, BorderLayout.NORTH);
            cell.add(field, BorderLayout.CENTER);
            panel.add(cell, gbc);
        }

        private void styleInputs(boolean dark) {
            AppDialogThemeHelper.styleTextField(searchField, dark);
            AppDialogThemeHelper.styleComboBox(searchColumnCombo, dark);
            AppDialogThemeHelper.styleComboBox(genreFilterCombo, dark);
            AppDialogThemeHelper.styleComboBox(ratingFilterCombo, dark);
            AppDialogThemeHelper.styleComboBox(platformFilterCombo, dark);
            AppDialogThemeHelper.styleComboBox(multiplayerFilterCombo, dark);
            AppDialogThemeHelper.styleComboBox(singlePlayerFilterCombo, dark);
        }

        private void wireHandlers() {
            searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    refreshTable();
                }

                @Override
                public void removeUpdate(javax.swing.event.DocumentEvent e) {
                    refreshTable();
                }

                @Override
                public void changedUpdate(javax.swing.event.DocumentEvent e) {
                    refreshTable();
                }
            });

            searchColumnCombo.addActionListener(e -> refreshTable());
            genreFilterCombo.addActionListener(e -> refreshTable());
            ratingFilterCombo.addActionListener(e -> refreshTable());
            platformFilterCombo.addActionListener(e -> refreshTable());
            multiplayerFilterCombo.addActionListener(e -> refreshTable());
            singlePlayerFilterCombo.addActionListener(e -> refreshTable());

            toggleColumnsButton.addActionListener(e -> {
                showingAllColumns = !showingAllColumns;
                toggleColumnsButton.setText(showingAllColumns ? "Show Less" : "Show More");
                refreshTable();
            });

            clearButton.addActionListener(e -> clearFilters());
            refreshButton.addActionListener(e -> {
                populateSearchControls();
                refreshTable();
            });
            removeButton.addActionListener(e -> removeSelectedFavourite());

            GameEntryDetailsDialogHelper.installDoubleClickPreview(table, this::showDetails);
        }

        private void clearFilters() {
            searchField.setText("");
            selectComboValue(searchColumnCombo, "Title");
            resetCombo(genreFilterCombo);
            resetCombo(ratingFilterCombo);
            resetCombo(platformFilterCombo);
            resetCombo(multiplayerFilterCombo);
            resetCombo(singlePlayerFilterCombo);
            refreshTable();
        }

        private void resetCombo(JComboBox<String> combo) {
            if (combo.getItemCount() > 0) {
                combo.setSelectedIndex(0);
            }
        }

        private void populateSearchControls() {
            searchColumnCombo.removeAllItems();
            for (String column : columns) {
                if (!isHiddenIdColumn(column)) {
                    searchColumnCombo.addItem(column);
                }
            }
            selectComboValue(searchColumnCombo, "Title");

            populateFilterCombo(genreFilterCombo, "Genre");
            populateFilterCombo(ratingFilterCombo, "ESRBRating");
            populateFilterCombo(platformFilterCombo, "Platform");
            populateFilterCombo(multiplayerFilterCombo, "Multiplayer");
            populateFilterCombo(singlePlayerFilterCombo, "SinglePlayer");
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

        private void refreshTable() {
            visibleRowIndexes.clear();
            String filterText = searchField.getText().trim().toLowerCase();
            String selectedSearchColumn = (String) searchColumnCombo.getSelectedItem();
            int filterColIdx = getColumnIndex(selectedSearchColumn);

            int[] visibleIndexes = showingAllColumns
                    ? getExpandedColumnIndexes(columns)
                    : getDefaultColumnIndexes(columns);

            String[] visibleColumns = new String[visibleIndexes.length];
            for (int i = 0; i < visibleIndexes.length; i++) {
                visibleColumns[i] = columns[visibleIndexes[i]];
            }

            List<Object[]> visibleRows = new ArrayList<>();
            for (int rowIndex = 0; rowIndex < model.getRowCount(); rowIndex++) {
                String[] row = model.getRow(rowIndex);
                if (!matchesTextFilter(row, filterColIdx, filterText)) {
                    continue;
                }
                if (!matchesComboFilter(row, "Genre", genreFilterCombo)) {
                    continue;
                }
                if (!matchesComboFilter(row, "ESRBRating", ratingFilterCombo)) {
                    continue;
                }
                if (!matchesComboFilter(row, "Platform", platformFilterCombo)) {
                    continue;
                }
                if (!matchesComboFilter(row, "Multiplayer", multiplayerFilterCombo)) {
                    continue;
                }
                if (!matchesComboFilter(row, "SinglePlayer", singlePlayerFilterCombo)) {
                    continue;
                }

                Object[] visibleRow = new Object[visibleIndexes.length];
                for (int col = 0; col < visibleIndexes.length; col++) {
                    visibleRow[col] = formatDisplayValue(visibleColumns[col], row[visibleIndexes[col]]);
                }
                visibleRows.add(visibleRow);
                visibleRowIndexes.add(rowIndex);
            }

            tableModel.setDataVector(visibleRows.toArray(new Object[0][]), visibleColumns);
            tableModel.fireTableDataChanged();
            resizeColumnsToFitContent();
            statusLabel.setText("Showing " + visibleRows.size() + " of " + model.getRowCount() + " favourite game(s)");
        }

        private void resizeColumnsToFitContent() {
            for (int col = 0; col < table.getColumnModel().getColumnCount(); col++) {
                int preferredWidth = 110;
                for (int row = 0; row < table.getRowCount(); row++) {
                    TableCellRenderer renderer = table.getCellRenderer(row, col);
                    Component comp = table.prepareRenderer(renderer, row, col);
                    preferredWidth = Math.max(preferredWidth, comp.getPreferredSize().width + 18);
                }
                table.getColumnModel().getColumn(col).setPreferredWidth(Math.min(preferredWidth, 320));
            }
        }

        private boolean matchesTextFilter(String[] row, int filterColIdx, String filterText) {
            if (filterText.isEmpty() || filterColIdx == -1) {
                return true;
            }

            String rawValue = filterColIdx < row.length && row[filterColIdx] != null
                    ? row[filterColIdx].trim().toLowerCase()
                    : "";
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
            if (columnIndex == -1 || columnIndex >= row.length) {
                return true;
            }

            for (String value : splitMultiValueCell(row[columnIndex])) {
                if (value.equalsIgnoreCase(selectedValue)) {
                    return true;
                }
            }
            return false;
        }

        private Object formatDisplayValue(String columnName, String rawValue) {
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

        private int[] getDefaultColumnIndexes(String[] columns) {
            String[] preferred = {"Title", "Developer", "Publisher", "ESRBRating", "Platform", "Genre", "Description"};
            List<Integer> indexes = new ArrayList<>();
            for (String wanted : preferred) {
                int index = getColumnIndex(wanted);
                if (index != -1) {
                    indexes.add(index);
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

        private int[] toIntArray(List<Integer> indexes) {
            int[] result = new int[indexes.size()];
            for (int i = 0; i < indexes.size(); i++) {
                result[i] = indexes.get(i);
            }
            return result;
        }

        private boolean isHiddenIdColumn(String name) {
            String normalized = name == null ? "" : name.trim().toLowerCase();
            return normalized.equals("id") || normalized.equals("gameid");
        }

        private int getColumnIndex(String columnName) {
            if (columnName == null) {
                return -1;
            }
            for (int i = 0; i < columns.length; i++) {
                if (columns[i].equalsIgnoreCase(columnName)) {
                    return i;
                }
            }
            return -1;
        }

        private String[] splitMultiValueCell(String rawValue) {
            if (rawValue == null || rawValue.trim().isEmpty()) {
                return new String[0];
            }
            String[] pieces = rawValue.split("\\Q" + MULTI_VALUE_SEPARATOR + "\\E");
            List<String> cleaned = new ArrayList<>();
            for (String piece : pieces) {
                String value = piece == null ? "" : piece.trim();
                if (!value.isEmpty()) {
                    cleaned.add(value);
                }
            }
            return cleaned.toArray(new String[0]);
        }

        private void removeSelectedFavourite() {
            int selectedRow = table.getSelectedRow();
            if (selectedRow < 0 || selectedRow >= visibleRowIndexes.size()) {
                AppDialogThemeHelper.showMessageDialog(
                        parent,
                        "Favourites",
                        "Select a favourite entry before trying to remove it.",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            int modelIndex = visibleRowIndexes.get(selectedRow);
            model.removeRow(modelIndex);
            try {
                model.saveToFile();
                populateSearchControls();
                refreshTable();
            } catch (IOException ex) {
                AppDialogThemeHelper.showMessageDialog(
                        parent,
                        "Favourites",
                        "The selected favourite could not be removed.\n" + ex.getMessage(),
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        private void showDetails(int visibleRowIndex) {
            if (visibleRowIndex < 0 || visibleRowIndex >= visibleRowIndexes.size()) {
                return;
            }
            int modelIndex = visibleRowIndexes.get(visibleRowIndex);
            GameEntryDetailsDialogHelper.showDialog(parent, columns, model.getRow(modelIndex));
        }
    }
}
