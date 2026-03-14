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

public class DataController {
    private final DataModel model;
    private final DataView view;
    private final AuthManager.UserSession session;

    // Search UI components
    private JTextField searchField;
    private JButton filterTypeBtn;
    private String currentFilterColumn = "Title"; // Default filter

    private boolean showingAllColumns = false;
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

    private void setupSearchAndFilterUI() {
        filterTypeBtn = new JButton("Search By: Title");
        filterTypeBtn.addActionListener(e -> {
            String[] columns = model.getColumns();
            if (columns == null || columns.length == 0) return;

            // Show a popup to select which column to filter by
            String selection = (String) JOptionPane.showInputDialog(
                    view,
                    "Select column to filter by:",
                    "Filter Settings",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    columns,
                    currentFilterColumn
            );

            if (selection != null) {
                currentFilterColumn = selection;
                filterTypeBtn.setText("Search By: " + selection);
                refreshVisibleTable(); // Re-apply filter with new column context
            }
        });

        searchField = new JTextField(15);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refreshVisibleTable(); }
            public void removeUpdate(DocumentEvent e) { refreshVisibleTable(); }
            public void changedUpdate(DocumentEvent e) { refreshVisibleTable(); }
        });

        view.topBar.add(filterTypeBtn);
        view.topBar.add(searchField);
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
        if (session.isAdmin()) title += " [Admin]";
        else if (session.isPublisher()) title += " [Publisher: " + session.getPublisherName() + "]";
        else title += " [Guest]";
        view.setTitle(title);
    }

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

    private void loadFile() {
        File dataFile = new File("data.csv");
        if (!dataFile.exists()) {
            JOptionPane.showMessageDialog(view, "data.csv not found.");
            view.setInteractionEnabled(false);
            return;
        }
        try {
            model.loadFromFile(dataFile);
            refreshVisibleTable();
            view.setInteractionEnabled(true);
            applyPermissionsToView();
        } catch (IOException e) {
            view.setInteractionEnabled(false);
        }
    }

    private void refreshVisibleTable() {
        String[] allColumns = model.getColumns();
        visibleRowIndexes.clear();

        String filterText = searchField.getText().toLowerCase();
        int filterColIdx = -1;
        for (int i = 0; i < allColumns.length; i++) {
            if (allColumns[i].equalsIgnoreCase(currentFilterColumn)) {
                filterColIdx = i;
                break;
            }
        }

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

            // Apply Universal Filter
            if (filterColIdx != -1 && !filterText.isEmpty()) {
                String cellValue = fullRow[filterColIdx].toLowerCase();
                if (!cellValue.contains(filterText)) continue;
            }

            Object[] visibleRow = new Object[visibleIndexes.length];
            for (int col = 0; col < visibleIndexes.length; col++) {
                visibleRow[col] = fullRow[visibleIndexes[col]];
            }
            visibleRows.add(visibleRow);
            visibleRowIndexes.add(rowIndex);
        }

        Object[][] visibleData = new Object[visibleRows.size()][visibleIndexes.length];
        for (int i = 0; i < visibleRows.size(); i++) visibleData[i] = visibleRows.get(i);

        view.setTableData(visibleData, visibleColumns);
        view.resizeColumnsToFitContent();
        view.fitWindowToTable(showingAllColumns);
        view.toggleColumnsBtn.setText(showingAllColumns ? "Show Less" : "Show More");
    }

    private int[] getDefaultColumnIndexes(String[] columns) {
        String[] preferred = {"Title", "Developer", "Publisher", "Rating", "Platform", "Genre"};
        List<Integer> indexes = new ArrayList<>();
        for (String wanted : preferred) {
            for (int i = 0; i < columns.length; i++) {
                if (columns[i].equalsIgnoreCase(wanted)) { indexes.add(i); break; }
            }
        }
        return indexes.isEmpty() ? getExpandedColumnIndexes(columns) : toIntArray(indexes);
    }

    private int[] getExpandedColumnIndexes(String[] columns) {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < columns.length; i++) {
            if (!isHiddenIdColumn(columns[i])) indexes.add(i);
        }
        return toIntArray(indexes);
    }

    private boolean isHiddenIdColumn(String name) {
        String n = name.toLowerCase();
        return n.equals("id") || n.contains("id");
    }

    private int[] toIntArray(List<Integer> indexes) {
        int[] res = new int[indexes.size()];
        for (int i = 0; i < indexes.size(); i++) res[i] = indexes.get(i);
        return res;
    }

    private int getPublisherColumnIndex() {
        String[] cols = model.getColumns();
        if (cols == null) return -1;
        for (int i = 0; i < cols.length; i++) {
            if (cols[i].equalsIgnoreCase("Publisher")) return i;
        }
        return -1;
    }

    private boolean canViewRow(String[] row) {
        if (session.isAdmin() || session.isGuest()) return true;
        int pubIdx = getPublisherColumnIndex();
        return pubIdx != -1 && row[pubIdx].trim().equalsIgnoreCase(session.getPublisherName());
    }

    private boolean canModifyRow(String[] row) {
        if (session.isAdmin()) return true;
        if (session.isGuest()) return false;
        int pubIdx = getPublisherColumnIndex();
        return pubIdx != -1 && row[pubIdx].trim().equalsIgnoreCase(session.getPublisherName());
    }

    private int getSelectedModelRowIndex() {
        int sel = view.table.getSelectedRow();
        if (sel == -1 || sel >= visibleRowIndexes.size()) return -1;
        return visibleRowIndexes.get(sel);
    }

    private void attachHandlers() {
        view.addBtn.addActionListener(e -> {
            String[] row = promptRow("Add Entry", null);
            if (row != null) { model.addRow(row); refreshVisibleTable(); }
        });

        view.editBtn.addActionListener(e -> {
            int idx = getSelectedModelRowIndex();
            if (idx == -1) return;
            if (!canModifyRow(model.getRow(idx))) return;
            String[] updated = promptRow("Edit Entry", model.getRow(idx));
            if (updated != null) { model.updateRow(idx, updated); refreshVisibleTable(); }
        });

        view.deleteBtn.addActionListener(e -> {
            int idx = getSelectedModelRowIndex();
            if (idx == -1) return;
            if (!canModifyRow(model.getRow(idx))) return;
            if (JOptionPane.showConfirmDialog(view, "Delete?") == JOptionPane.YES_OPTION) {
                model.removeRow(idx); refreshVisibleTable();
            }
        });

        view.saveBtn.addActionListener(e -> {
            try { model.saveToFile(); JOptionPane.showMessageDialog(view, "Saved"); }
            catch (IOException ex) { JOptionPane.showMessageDialog(view, "Error"); }
        });

        view.logoutBtn.addActionListener(e -> {
            view.dispose();
            SwingUtilities.invokeLater(StartUp::new);
        });

        view.toggleColumnsBtn.addActionListener(e -> { showingAllColumns = !showingAllColumns; refreshVisibleTable(); });
        view.toggleThemeBtn.addActionListener(e -> view.toggleTheme());

        view.table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = view.table.rowAtPoint(e.getPoint());
                if (row >= 0 && e.getClickCount() == 1) showDetails(row);
            }
        });
    }

    private void showDetails(int visibleIdx) {
        int modelIdx = visibleRowIndexes.get(visibleIdx);
        String[] fullCols = model.getColumns();
        String[] fullRow = model.getRow(modelIdx);
        view.showRowDetails(fullCols, fullRow);
    }

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

    public static void main(String[] args) { SwingUtilities.invokeLater(StartUp::new); }
}