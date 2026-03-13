import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class DataView extends JFrame {
    JTable table;
    JButton addBtn, editBtn, deleteBtn, saveBtn;
    DefaultTableModel tableModel;

    public DataView() {
        setTitle("Database Editor");
        setSize(700, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Create a table model where cells cannot be edited directly.
        // All editing must go through the Add/Edit buttons.
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Create the table and place it inside a scroll pane.
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Create the side panel that holds the buttons.
        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        addBtn = new JButton("Add");
        editBtn = new JButton("Edit");
        deleteBtn = new JButton("Delete");
        saveBtn = new JButton("Save");

        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(saveBtn);

        add(buttonPanel, BorderLayout.EAST);

        // Center the window on screen and make it visible.
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Updates the table with new data and column headers.
     *
     * @param data    the row data to display
     * @param columns the column names to display
     */
    public void setTableData(Object[][] data, String[] columns) {
        tableModel.setDataVector(data, columns);
    }

    /**
     * Enables or disables all user interaction controls.
     * This is used when the CSV file fails to load or does not exist.
     *
     * @param enabled true to allow interaction, false to block it
     */
    public void setInteractionEnabled(boolean enabled) {
        table.setEnabled(enabled);
        addBtn.setEnabled(enabled);
        editBtn.setEnabled(enabled);
        deleteBtn.setEnabled(enabled);
        saveBtn.setEnabled(enabled);
    }
}
