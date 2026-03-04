import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.awt.GridLayout;

public class DataController {
    private final DataModel model;
    private final DataView view;

    public DataController(DataModel model, DataView view) {
        this.model = model;
        this.view = view;

        loadFile();
        attachHandlers();
    }

    private void loadFile() {
		File dataFile = new File("data.csv");
		
		if (dataFile.exists())
		{
			try {
				model.loadFromFile(dataFile);
				view.setTableData(model.getData(), model.getColumns());
			} catch (IOException e) {
				JOptionPane.showMessageDialog(view, "Failed to load file");
			}
		}
		
        // JFileChooser chooser = new JFileChooser();
        // if (chooser.showOpenDialog(view) == JFileChooser.APPROVE_OPTION) {
            // try {
                // model.loadFromFile(chooser.getSelectedFile());
                // view.setTableData(model.getData(), model.getColumns());
            // } catch (IOException e) {
                // JOptionPane.showMessageDialog(view, "Failed to load file");
            // }
        // }
    }

    private void attachHandlers() {
        view.addBtn.addActionListener(e -> {
            String[] row = promptRow("Add Entry", null);
            if (row != null) {
                model.addRow(row);
                view.setTableData(model.getData(), model.getColumns());
            }
        });

        view.editBtn.addActionListener(e -> {
            int selected = view.table.getSelectedRow();
            if (selected == -1) {
                JOptionPane.showMessageDialog(view, "Select a row first");
                return;
            }

            String[] current = new String[model.getColumns().length];
            for (int i = 0; i < current.length; i++) {
                current[i] = view.table.getValueAt(selected, i).toString();
            }

            String[] updated = promptRow("Edit Entry", current);
            if (updated != null) {
                model.updateRow(selected, updated);
                view.setTableData(model.getData(), model.getColumns());
            }
        });

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
                view.setTableData(model.getData(), model.getColumns());
            }
        });

        view.saveBtn.addActionListener(e -> {
            try {
                model.saveToFile();
                JOptionPane.showMessageDialog(view, "Saved successfully");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(view, "Save failed");
            }
        });
    }

    private String[] promptRow(String title, String[] defaults) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        JTextField[] fields = new JTextField[model.getColumns().length];

        for (int i = 0; i < fields.length; i++) {
            panel.add(new JLabel(model.getColumns()[i]));
            fields[i] = new JTextField(defaults == null ? "" : defaults[i]);
            panel.add(fields[i]);
        }

        int result = JOptionPane.showConfirmDialog(
                view, panel, title, JOptionPane.OK_CANCEL_OPTION
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

    public static void main(String[] args) {
        new DataController(new DataModel(), new DataView());
    }
}
