import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataModel {
    private String[] columns;
    private final List<String[]> rows = new ArrayList<>();
    private File currentFile;

    /**
     * Loads CSV data from the given file.
     * The first line is treated as the column header row.
     *
     * @param file the CSV file to load
     * @throws IOException if the file cannot be read
     */
    public void loadFromFile(File file) throws IOException {
        currentFile = file;
        rows.clear();

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;

        // Read the header row first to get the column names.
        line = reader.readLine();
        if (line != null) {
            columns = line.split(",");
        } else {
            // If the file is empty, set columns to an empty array
            // so the program does not crash later.
            columns = new String[0];
        }

        // Read each remaining line as a row of CSV data.
        while ((line = reader.readLine()) != null) {
            rows.add(line.split(","));
        }

        reader.close();
    }

    /**
     * Saves the current data back into the currently loaded CSV file.
     *
     * @throws IOException if the file cannot be written
     */
    public void saveToFile() throws IOException {
        if (currentFile == null || columns == null) {
            return;
        }

        PrintWriter writer = new PrintWriter(new FileWriter(currentFile));

        // Write the column headers first.
        writer.println(String.join(",", columns));

        // Write each row of data.
        for (String[] row : rows) {
            writer.println(String.join(",", row));
        }

        writer.close();
    }

    /**
     * Returns the column headers.
     *
     * @return the array of column names
     */
    public String[] getColumns() {
        return columns;
    }

    /**
     * Returns the table data in Object[][] format for JTable.
     * This version safely handles rows that may have fewer values
     * than the number of columns.
     *
     * @return 2D object array of row data
     */
    public Object[][] getData() {
        if (columns == null) {
            return new Object[0][0];
        }

        Object[][] data = new Object[rows.size()][columns.length];

        for (int i = 0; i < rows.size(); i++) {
            String[] row = rows.get(i);

            for (int j = 0; j < columns.length; j++) {
                data[i][j] = (j < row.length) ? row[j] : "";
            }
        }

        return data;
    }

    /**
     * Adds a new row to the data set.
     *
     * @param row the row to add
     */
    public void addRow(String[] row) {
        rows.add(row);
    }

    /**
     * Replaces an existing row with updated values.
     *
     * @param index the row index to update
     * @param row   the new row data
     */
    public void updateRow(int index, String[] row) {
        rows.set(index, row);
    }

    /**
     * Removes a row from the data set.
     *
     * @param index the row index to remove
     */
    public void removeRow(int index) {
        rows.remove(index);
    }

    /**
     * Returns true if column headers are loaded.
     *
     * @return true if columns exist, false otherwise
     */
    public boolean hasValidData() {
        return columns != null && columns.length > 0;
    }
}
