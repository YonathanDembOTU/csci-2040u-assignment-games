import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataModel {
    private String[] columns;
    private List<String[]> rows = new ArrayList<>();
    private File currentFile;

    public void loadFromFile(File file) throws IOException {
        currentFile = file;
        rows.clear();

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;

        line = reader.readLine();
        if (line != null) {
            columns = line.split(",");
        }

        while ((line = reader.readLine()) != null) {
            rows.add(line.split(","));
        }

        reader.close();
    }

    public void saveToFile() throws IOException {
        if (currentFile == null) return;

        PrintWriter writer = new PrintWriter(new FileWriter(currentFile));
        writer.println(String.join(",", columns));

        for (String[] row : rows) {
            writer.println(String.join(",", row));
        }

        writer.close();
    }

    public String[] getColumns() {
        return columns;
    }

    public Object[][] getData() {
        Object[][] data = new Object[rows.size()][columns.length];
        for (int i = 0; i < rows.size(); i++) {
            data[i] = rows.get(i);
        }
        return data;
    }

    public void addRow(String[] row) {
        rows.add(row);
    }

    public void updateRow(int index, String[] row) {
        rows.set(index, row);
    }

    public void removeRow(int index) {
        rows.remove(index);
    }
}
