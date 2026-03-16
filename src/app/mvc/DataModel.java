package app.mvc;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataModel {
    // Stores the CSV header row
    private String[] columns;

    // Stores all data rows from the CSV
    private final List<String[]> rows = new ArrayList<>();

    // Stores the file currently being edited
    private File currentFile;

    /**
     * Loads CSV data from a file.
     * The first line is treated as the header row.
     *
     * @param file the CSV file to read
     * @throws IOException if the file cannot be read
     */
    public void loadFromFile(File file) throws IOException {
        currentFile = file;
        rows.clear();

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;

        // Read the first line as column headers
        line = reader.readLine();
        if (line != null) {
            columns = line.split(",");
        } else {
            columns = new String[0];
        }

        // Read remaining lines as data rows
        while ((line = reader.readLine()) != null) {
            rows.add(line.split(","));
        }

        reader.close();
    }

    /**
     * Saves the current in-memory data back to the current CSV file.
     *
     * @throws IOException if the file cannot be written
     */
    public void saveToFile() throws IOException {
        if (currentFile == null || columns == null) {
            return;
        }

        PrintWriter writer = new PrintWriter(new FileWriter(currentFile));

        // Write the header row first
        writer.println(String.join(",", columns));

        // Write each row, padding missing cells with empty strings
        for (String[] row : rows) {
            String[] outputRow = new String[columns.length];

            for (int i = 0; i < columns.length; i++) {
                outputRow[i] = (i < row.length) ? row[i] : "";
            }

            writer.println(String.join(",", outputRow));
        }

        writer.close();
    }

    /**
     * Returns the column header names.
     *
     * @return array of column names
     */
    public String[] getColumns() {
        return columns;
    }

    /**
     * Returns all data as a 2D Object array for JTable use.
     * Missing values in short rows are padded with empty strings.
     *
     * @return all table data
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
     * Returns a single row, padded to the full number of columns.
     *
     * @param index row index
     * @return padded row data
     */
    public String[] getRow(int index) {
        String[] original = rows.get(index);
        String[] padded = new String[columns.length];

        for (int i = 0; i < columns.length; i++) {
            padded[i] = (i < original.length) ? original[i] : "";
        }

        return padded;
    }

    /**
     * Returns the number of rows in the dataset.
     *
     * @return row count
     */
    public int getRowCount() {
        return rows.size();
    }

    /**
     * Adds a new row to the dataset.
     *
     * @param row new row data
     */
    public void addRow(String[] row) {
    if (row == null || row.length != columns.length) {
        throw new IllegalArgumentException("Invalid row data");
    }

    for (int i = 0; i < row.length; i++) {
        if (row[i] == null || row[i].trim().isEmpty()) {
            row[i] = "N/A";
        }
    }

    rows.add(row);

    }

    /**
     * Updates an existing row.
     *
     * @param index row index to update
     * @param row new row contents
     */
    public void updateRow(int index, String[] row) {
        rows.set(index, row);
    }

    /**
     * Removes a row from the dataset.
     *
     * @param index row index to remove
     */
    public void removeRow(int index) {
        rows.remove(index);
    }

    /**
     * Checks whether valid column headers were loaded.
     *
     * @return true if valid header data exists
     */
    public boolean hasValidData() {
        return columns != null && columns.length > 0;
    }
}