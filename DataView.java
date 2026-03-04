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

        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

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

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void setTableData(Object[][] data, String[] columns) {
        tableModel.setDataVector(data, columns);
    }
}
