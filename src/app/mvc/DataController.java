package app.mvc;

import app.auth.AuthManager;
import app.StartUp;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
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
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

public class DataController {
    // Multi-platform values are stored inside one CSV cell using | as a separator.
    private static final String MULTI_VALUE_SEPARATOR = "|";
    private static final String RAWG_API_BASE_URL = "https://api.rawg.io/api";
    private static final String RAWG_ATTRIBUTION_URL = "https://rawg.io/";
    private static final String RAWG_API_KEY_ENV = "RAWG_API_KEY";
    private static final String RAWG_API_KEY_PROPERTY = "rawg.api.key";
    private static final int RAWG_LIST_PAGE_SIZE = 25;

    private final DataModel model;
    private final DataView view;
    private final AuthManager.UserSession session;
    private final String rawgApiKey;
    private final HttpClient httpClient;

    private boolean showingAllColumns = false;
    private final List<Integer> visibleRowIndexes = new ArrayList<>();

    /**
     * Wires the model, view, and session together for the main UI.
     */
    public DataController(DataModel model, DataView view, AuthManager.UserSession session) {
        this.model = model;
        this.view = view;
        this.session = session;
        this.rawgApiKey = resolveRawgApiKey();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .build();

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
        view.attributionBtn.setVisible(true);
        view.attributionBtn.setEnabled(true);

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
            String[] row = requestAddRowInput();
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

        view.attributionBtn.addActionListener(e -> openRawgAttributionPage());

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

    private String[] requestAddRowInput() {
        if (!session.isAdmin()) {
            return requestRowInput("Add Entry", null);
        }

        int choice = RawgImportDialogHelper.showAdminAddChoice(view);
        if (choice == RawgImportDialogHelper.CANCEL) {
            return null;
        }

        if (choice == RawgImportDialogHelper.IMPORT_FROM_RAWG) {
            String[] importedDefaults = requestRawgDefaultsForAdminAdd();
            if (importedDefaults == null) {
                return null;
            }
            return requestRowInput("Add Entry", importedDefaults);
        }

        return requestRowInput("Add Entry", null);
    }

    private String[] requestRawgDefaultsForAdminAdd() {
        if (!hasRawgApiKey()) {
            RawgImportDialogHelper.showMissingApiKey(view);
            return null;
        }

        String gameName = RawgImportDialogHelper.promptForGameName(view);
        if (gameName == null) {
            return null;
        }

        String[] columns = model.getColumns();
        if (columns == null || columns.length == 0) {
            return null;
        }

        Cursor previousCursor = view.getCursor();
        view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            List<RawgImportDialogHelper.SearchCandidate> similarMatches =
                    fetchRawgSearchCandidates(gameName, RawgImportDialogHelper.MAX_SIMILAR_CHOICES);
            RawgImportDialogHelper.SearchCandidate selected =
                    RawgImportDialogHelper.promptToChooseSimilarMatch(view, gameName, similarMatches);
            if (selected == null) {
                return null;
            }

            RawgGameData rawgData = fetchRawgGameDataById(selected.getRawgId());
            if (rawgData == null || rawgData.title == null || rawgData.title.isBlank()) {
                RawgImportDialogHelper.showNoMatch(view, gameName);
                return null;
            }

            int nextGameId = 1;
            String generatedId = generateNextGameId();
            try {
                nextGameId = Integer.parseInt(generatedId);
            } catch (NumberFormatException ignored) {
            }

            return buildCsvRowFromRawgData(columns, nextGameId, rawgData);
        } catch (Exception ex) {
            RawgImportDialogHelper.showImportFailed(view, ex.getMessage());
            return null;
        } finally {
            view.setCursor(previousCursor);
        }
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

        Cursor previousCursor = view.getCursor();
        view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            view.showRowDetails(fullCols, enrichRowWithRawgData(fullCols, fullRow));
        } finally {
            view.setCursor(previousCursor);
        }
    }


    private String resolveRawgApiKey() {
        String propertyKey = System.getProperty(RAWG_API_KEY_PROPERTY);
        if (propertyKey != null && !propertyKey.isBlank()) {
            return propertyKey.trim();
        }

        String envKey = System.getenv(RAWG_API_KEY_ENV);
        if (envKey != null && !envKey.isBlank()) {
            return envKey.trim();
        }

        return "5dcf55f1c08d4c0fba2a1cdc6c2b2852";
    }

    private boolean hasRawgApiKey() {
        return rawgApiKey != null && !rawgApiKey.isBlank();
    }

    private void openRawgAttributionPage() {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI.create(RAWG_ATTRIBUTION_URL));
                return;
            }
        } catch (Exception ignored) {
        }

        JOptionPane.showMessageDialog(
                view,
                "Open this link in your browser: " + RAWG_ATTRIBUTION_URL,
                "RAWG Attribution",
                JOptionPane.INFORMATION_MESSAGE);
    }


    private RawgGameData fetchRawgGameDataById(String rawgId) throws IOException, InterruptedException {
        if (rawgId == null || rawgId.isBlank()) {
            return null;
        }

        String detailsUrl = RAWG_API_BASE_URL + "/games/" + rawgId + "?key=" + rawgApiKey;
        String detailsJson = sendRawgGet(detailsUrl);
        if (detailsJson.isBlank()) {
            return null;
        }

        JsonObject game = parseJsonObject(detailsJson);
        if (game == null) {
            return null;
        }

        return buildRawgGameDataFromDetailsObject(game);
    }

    private RawgGameData buildRawgGameDataFromDetailsObject(JsonObject game) {
        if (game == null) {
            return null;
        }

        RawgGameData data = new RawgGameData();
        data.title = cleanupText(game.getString("name"));
        data.description = cleanupText(game.getString("description_raw"));
        data.releaseYear = extractReleaseYear(game.getString("released"));
        data.developer = joinWithComma(extractNameList(game.getArray("developers")));
        data.publisher = joinWithComma(extractNameList(game.getArray("publishers")));
        data.genre = normalizeMultiValueCell(String.join(" | ", extractNameList(game.getArray("genres"))));
        data.platform = normalizeMultiValueCell(String.join(" | ", extractNestedNameList(game.getArray("platforms"), "platform")));

        JsonObject esrbRating = game.getObject("esrb_rating");
        data.esrbRating = normalizeEsrbRating(esrbRating == null ? "" : esrbRating.getString("name"));
        data.metacriticScore = game.getPrimitiveString("metacritic");
        data.multiplayer = containsTag(game.getArray("tags"), "multiplayer")
                || containsTag(game.getArray("tags"), "co-op")
                || containsTag(game.getArray("tags"), "massively multiplayer")
                ? "Yes" : "No";
        data.singlePlayer = containsTag(game.getArray("tags"), "singleplayer")
                || containsTag(game.getArray("tags"), "single-player")
                ? "Yes" : "No";
        data.priceUsd = "N/A";
        data.downloadSizeGb = "N/A";
        return data;
    }

    private String[] buildCsvRowFromRawgData(String[] columns, int gameId, RawgGameData rawgData) {
        String[] row = new String[columns.length];
        for (int i = 0; i < columns.length; i++) {
            String column = columns[i];
            if (column.equalsIgnoreCase("GameID")) {
                row[i] = String.valueOf(gameId);
            } else if (column.equalsIgnoreCase("Title")) {
                row[i] = fallback(rawgData.title, "Untitled");
            } else if (column.equalsIgnoreCase("Developer")) {
                row[i] = fallback(rawgData.developer, "N/A");
            } else if (column.equalsIgnoreCase("Publisher")) {
                row[i] = fallback(rawgData.publisher, "N/A");
            } else if (column.equalsIgnoreCase("ReleaseYear")) {
                row[i] = fallback(rawgData.releaseYear, "N/A");
            } else if (column.equalsIgnoreCase("Genre")) {
                row[i] = fallback(rawgData.genre, "N/A");
            } else if (column.equalsIgnoreCase("Platform")) {
                row[i] = fallback(rawgData.platform, "N/A");
            } else if (column.equalsIgnoreCase("ESRBRating")) {
                row[i] = fallback(rawgData.esrbRating, "N/A");
            } else if (column.equalsIgnoreCase("PriceUSD")) {
                row[i] = fallback(rawgData.priceUsd, "N/A");
            } else if (column.equalsIgnoreCase("MetacriticScore")) {
                row[i] = fallback(rawgData.metacriticScore, "N/A");
            } else if (column.equalsIgnoreCase("Multiplayer")) {
                row[i] = fallback(rawgData.multiplayer, "N/A");
            } else if (column.equalsIgnoreCase("SinglePlayer")) {
                row[i] = fallback(rawgData.singlePlayer, "N/A");
            } else if (column.equalsIgnoreCase("DownloadSizeGB")) {
                row[i] = fallback(rawgData.downloadSizeGb, "N/A");
            } else if (column.equalsIgnoreCase("Description")) {
                row[i] = fallback(rawgData.description, "RAWG game entry imported from the API.");
            } else {
                row[i] = "N/A";
            }
        }
        return row;
    }

    private String fallback(String value, String fallbackValue) {
        return value == null || value.isBlank() ? fallbackValue : value.trim();
    }

    private String[] enrichRowWithRawgData(String[] columns, String[] localRow) {
        if (columns == null || localRow == null || columns.length == 0) {
            return localRow;
        }

        int titleIndex = getColumnIndex(columns, "Title");
        if (titleIndex == -1 || titleIndex >= localRow.length) {
            return localRow;
        }

        String title = localRow[titleIndex] == null ? "" : localRow[titleIndex].trim();
        if (title.isEmpty() || !hasRawgApiKey()) {
            return localRow;
        }

        try {
            RawgGameData rawgData = fetchRawgGameData(title);
            if (rawgData == null) {
                return localRow;
            }
            return mergeRowWithRawgData(columns, localRow, rawgData);
        } catch (Exception ignored) {
            return localRow;
        }
    }

    private RawgGameData fetchDirectRawgGameData(String title) throws IOException, InterruptedException {
        JsonArray results = fetchRawgResults(title, RawgImportDialogHelper.MAX_SIMILAR_CHOICES, true);
        if (results == null) {
            return null;
        }

        String normalizedWanted = normalizeSearchText(title);
        for (JsonObject candidate : results.objectItems()) {
            String candidateTitle = candidate.getString("name");
            String candidateId = candidate.getPrimitiveString("id");
            if (candidateTitle.isBlank() || candidateId.isBlank()) {
                continue;
            }

            if (normalizeSearchText(candidateTitle).equals(normalizedWanted)) {
                return fetchRawgGameDataById(candidateId);
            }
        }

        return null;
    }

    private List<RawgImportDialogHelper.SearchCandidate> fetchRawgSearchCandidates(String title, int limit)
            throws IOException, InterruptedException {
        List<RawgImportDialogHelper.SearchCandidate> candidates = new ArrayList<>();
        if (title == null || title.isBlank() || limit <= 0) {
            return candidates;
        }

        JsonArray results = fetchRawgResults(title, limit, false);
        if (results == null) {
            return candidates;
        }

        LinkedHashSet<String> seenIds = new LinkedHashSet<>();
        for (JsonObject candidate : results.objectItems()) {
            String candidateId = candidate.getPrimitiveString("id");
            String candidateTitle = candidate.getString("name");
            if (candidateId.isBlank() || candidateTitle.isBlank() || seenIds.contains(candidateId)) {
                continue;
            }

            seenIds.add(candidateId);
            String releaseYear = extractReleaseYear(candidate.getString("released"));
            String platforms = normalizeMultiValueCell(
                    String.join(" | ", extractNestedNameList(candidate.getArray("platforms"), "platform")));

            candidates.add(new RawgImportDialogHelper.SearchCandidate(
                    candidateId,
                    candidateTitle,
                    releaseYear,
                    platforms));

            if (candidates.size() >= limit) {
                break;
            }
        }

        return candidates;
    }

    private RawgGameData fetchRawgGameData(String title) throws IOException, InterruptedException {
        JsonArray results = fetchRawgResults(title, 5, true);
        if (results == null) {
            return null;
        }

        String bestId = null;
        String normalizedWanted = normalizeSearchText(title);
        int bestScore = -1;

        for (JsonObject candidate : results.objectItems()) {
            String candidateTitle = candidate.getString("name");
            String candidateId = candidate.getPrimitiveString("id");
            if (candidateTitle.isBlank() || candidateId.isBlank()) {
                continue;
            }

            String normalizedCandidate = normalizeSearchText(candidateTitle);
            int score = 1;
            if (normalizedCandidate.equals(normalizedWanted)) {
                score = 3;
            } else if (normalizedCandidate.startsWith(normalizedWanted) || normalizedWanted.startsWith(normalizedCandidate)) {
                score = 2;
            }

            if (score > bestScore) {
                bestScore = score;
                bestId = candidateId;
            }
        }

        return fetchRawgGameDataById(bestId);
    }

    private JsonArray fetchRawgResults(String title, int limit, boolean searchPrecise)
            throws IOException, InterruptedException {
        if (title == null || title.isBlank() || limit <= 0) {
            return null;
        }

        String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);
        String searchUrl = RAWG_API_BASE_URL + "/games?key=" + rawgApiKey
                + "&search=" + encodedTitle
                + (searchPrecise ? "&search_precise=true" : "")
                + "&page_size=" + limit;

        String searchJson = sendRawgGet(searchUrl);
        if (searchJson.isBlank()) {
            return null;
        }

        JsonObject root = parseJsonObject(searchJson);
        return root == null ? null : root.getArray("results");
    }

    private String sendRawgGet(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        }
        return "";
    }

    private String[] mergeRowWithRawgData(String[] columns, String[] localRow, RawgGameData rawgData) {
        String[] merged = new String[localRow.length];
        System.arraycopy(localRow, 0, merged, 0, localRow.length);

        applyRawgValue(merged, columns, "Title", rawgData.title);
        applyRawgValue(merged, columns, "Developer", rawgData.developer);
        applyRawgValue(merged, columns, "Publisher", rawgData.publisher);
        applyRawgValue(merged, columns, "ReleaseYear", rawgData.releaseYear);
        applyRawgValue(merged, columns, "Genre", rawgData.genre);
        applyRawgValue(merged, columns, "Platform", rawgData.platform);
        applyRawgValue(merged, columns, "ESRBRating", rawgData.esrbRating);
        applyRawgValue(merged, columns, "MetacriticScore", rawgData.metacriticScore);
        applyRawgValue(merged, columns, "Description", rawgData.description);
        return merged;
    }

    private void applyRawgValue(String[] row, String[] columns, String columnName, String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        int index = getColumnIndex(columns, columnName);
        if (index == -1 || index >= row.length) {
            return;
        }

        row[index] = value.trim();
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

    private String normalizeSearchText(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim();
    }

    private String extractReleaseYear(String releasedDate) {
        if (releasedDate == null || releasedDate.length() < 4) {
            return "";
        }
        String year = releasedDate.substring(0, 4);
        return year.matches("\\d{4}") ? year : "";
    }

    private String normalizeEsrbRating(String ratingName) {
        if (ratingName == null) {
            return "";
        }

        String clean = ratingName.trim();
        if (clean.equalsIgnoreCase("Everyone")) {
            return "E";
        }
        if (clean.equalsIgnoreCase("Everyone 10+") || clean.equalsIgnoreCase("Everyone 10 Plus")) {
            return "E10+";
        }
        if (clean.equalsIgnoreCase("Teen")) {
            return "T";
        }
        if (clean.equalsIgnoreCase("Mature") || clean.equalsIgnoreCase("Mature 17+")) {
            return "M";
        }
        if (clean.equalsIgnoreCase("Adults Only 18+")) {
            return "AO";
        }
        if (clean.equalsIgnoreCase("Rating Pending")) {
            return "RP";
        }
        return clean;
    }

    private String joinWithComma(List<String> values) {
        return String.join(", ", values);
    }

    private List<String> extractNameList(JsonArray array) {
        List<String> names = new ArrayList<>();
        if (array == null) {
            return names;
        }

        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (JsonObject object : array.objectItems()) {
            String name = cleanupText(object.getString("name"));
            if (!name.isBlank()) {
                unique.add(name);
            }
        }

        names.addAll(unique);
        return names;
    }

    private List<String> extractNestedNameList(JsonArray array, String nestedObjectKey) {
        List<String> names = new ArrayList<>();
        if (array == null || nestedObjectKey == null || nestedObjectKey.isBlank()) {
            return names;
        }

        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (JsonObject object : array.objectItems()) {
            JsonObject nestedObject = object.getObject(nestedObjectKey);
            if (nestedObject == null) {
                continue;
            }

            String name = cleanupText(nestedObject.getString("name"));
            if (!name.isBlank()) {
                unique.add(name);
            }
        }

        names.addAll(unique);
        return names;
    }

    private boolean containsTag(JsonArray tags, String wantedTag) {
        if (tags == null || wantedTag == null || wantedTag.isBlank()) {
            return false;
        }

        for (JsonObject tagObject : tags.objectItems()) {
            String name = tagObject.getString("name");
            if (name.equalsIgnoreCase(wantedTag)) {
                return true;
            }
        }
        return false;
    }

    private JsonObject parseJsonObject(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }

        try {
            Object parsed = new JsonParser(json).parseValue();
            return parsed instanceof JsonObject ? (JsonObject) parsed : null;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String cleanupText(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replaceAll("<[^>]+>", " ")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&nbsp;", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static class JsonObject {
        private final java.util.Map<String, Object> values;

        private JsonObject(java.util.Map<String, Object> values) {
            this.values = values;
        }

        private String getString(String key) {
            Object value = values.get(key);
            if (value == null) {
                return "";
            }
            if (value instanceof String) {
                return (String) value;
            }
            if (value instanceof Number || value instanceof Boolean) {
                return String.valueOf(value);
            }
            return "";
        }

        private String getPrimitiveString(String key) {
            Object value = values.get(key);
            if (value == null) {
                return "";
            }
            if (value instanceof String || value instanceof Number || value instanceof Boolean) {
                return String.valueOf(value);
            }
            return "";
        }

        private JsonObject getObject(String key) {
            Object value = values.get(key);
            return value instanceof JsonObject ? (JsonObject) value : null;
        }

        private JsonArray getArray(String key) {
            Object value = values.get(key);
            return value instanceof JsonArray ? (JsonArray) value : null;
        }
    }

    private static class JsonArray {
        private final List<Object> items;

        private JsonArray(List<Object> items) {
            this.items = items;
        }

        private List<JsonObject> objectItems() {
            List<JsonObject> objects = new ArrayList<>();
            for (Object item : items) {
                if (item instanceof JsonObject) {
                    objects.add((JsonObject) item);
                }
            }
            return objects;
        }
    }

    private static class JsonParser {
        private final String text;
        private int index;

        private JsonParser(String text) {
            this.text = text == null ? "" : text;
            this.index = 0;
        }

        private Object parseValue() {
            skipWhitespace();
            if (index >= text.length()) {
                throw new IllegalArgumentException("Unexpected end of JSON.");
            }

            char ch = text.charAt(index);
            if (ch == '{') {
                return parseObject();
            }
            if (ch == '[') {
                return parseArray();
            }
            if (ch == '"') {
                return parseString();
            }
            if (ch == 't') {
                expectLiteral("true");
                return Boolean.TRUE;
            }
            if (ch == 'f') {
                expectLiteral("false");
                return Boolean.FALSE;
            }
            if (ch == 'n') {
                expectLiteral("null");
                return null;
            }
            if (ch == '-' || Character.isDigit(ch)) {
                return parseNumber();
            }

            throw new IllegalArgumentException("Unexpected JSON token at index " + index + ".");
        }

        private JsonObject parseObject() {
            expectChar('{');
            java.util.Map<String, Object> values = new java.util.LinkedHashMap<>();
            skipWhitespace();

            if (peek('}')) {
                index++;
                return new JsonObject(values);
            }

            while (true) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                expectChar(':');
                Object value = parseValue();
                values.put(key, value);

                skipWhitespace();
                if (peek('}')) {
                    index++;
                    return new JsonObject(values);
                }

                expectChar(',');
            }
        }

        private JsonArray parseArray() {
            expectChar('[');
            List<Object> values = new ArrayList<>();
            skipWhitespace();

            if (peek(']')) {
                index++;
                return new JsonArray(values);
            }

            while (true) {
                values.add(parseValue());
                skipWhitespace();

                if (peek(']')) {
                    index++;
                    return new JsonArray(values);
                }

                expectChar(',');
            }
        }

        private String parseString() {
            expectChar('"');
            StringBuilder builder = new StringBuilder();

            while (index < text.length()) {
                char ch = text.charAt(index++);
                if (ch == '"') {
                    return builder.toString();
                }

                if (ch == '\\') {
                    if (index >= text.length()) {
                        throw new IllegalArgumentException("Invalid JSON escape.");
                    }

                    char escaped = text.charAt(index++);
                    switch (escaped) {
                        case '"':
                        case '\\':
                        case '/':
                            builder.append(escaped);
                            break;
                        case 'b':
                            builder.append('\b');
                            break;
                        case 'f':
                            builder.append('\f');
                            break;
                        case 'n':
                            builder.append('\n');
                            break;
                        case 'r':
                            builder.append('\r');
                            break;
                        case 't':
                            builder.append('\t');
                            break;
                        case 'u':
                            builder.append(parseUnicodeEscape());
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid JSON escape sequence.");
                    }
                } else {
                    builder.append(ch);
                }
            }

            throw new IllegalArgumentException("Unterminated JSON string.");
        }

        private char parseUnicodeEscape() {
            if (index + 4 > text.length()) {
                throw new IllegalArgumentException("Invalid unicode escape.");
            }

            String hex = text.substring(index, index + 4);
            index += 4;
            try {
                return (char) Integer.parseInt(hex, 16);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Invalid unicode escape.", ex);
            }
        }

        private Number parseNumber() {
            int start = index;
            if (text.charAt(index) == '-') {
                index++;
            }

            while (index < text.length() && Character.isDigit(text.charAt(index))) {
                index++;
            }

            boolean isFractional = false;
            if (index < text.length() && text.charAt(index) == '.') {
                isFractional = true;
                index++;
                while (index < text.length() && Character.isDigit(text.charAt(index))) {
                    index++;
                }
            }

            if (index < text.length() && (text.charAt(index) == 'e' || text.charAt(index) == 'E')) {
                isFractional = true;
                index++;
                if (index < text.length() && (text.charAt(index) == '+' || text.charAt(index) == '-')) {
                    index++;
                }
                while (index < text.length() && Character.isDigit(text.charAt(index))) {
                    index++;
                }
            }

            String rawNumber = text.substring(start, index);
            try {
                if (isFractional) {
                    return Double.parseDouble(rawNumber);
                }
                return Long.parseLong(rawNumber);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Invalid number in JSON.", ex);
            }
        }

        private void expectLiteral(String literal) {
            if (!text.startsWith(literal, index)) {
                throw new IllegalArgumentException("Expected literal " + literal + ".");
            }
            index += literal.length();
        }

        private void expectChar(char expected) {
            skipWhitespace();
            if (index >= text.length() || text.charAt(index) != expected) {
                throw new IllegalArgumentException("Expected '" + expected + "' in JSON.");
            }
            index++;
        }

        private boolean peek(char expected) {
            return index < text.length() && text.charAt(index) == expected;
        }

        private void skipWhitespace() {
            while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
                index++;
            }
        }
    }

    private static class RawgGameData {
        private String title = "";
        private String developer = "";
        private String publisher = "";
        private String releaseYear = "";
        private String genre = "";
        private String platform = "";
        private String esrbRating = "";
        private String metacriticScore = "";
        private String multiplayer = "";
        private String singlePlayer = "";
        private String priceUsd = "";
        private String downloadSizeGb = "";
        private String description = "";
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
