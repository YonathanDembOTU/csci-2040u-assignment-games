package app.mvc;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores the loaded game catalogue data in memory and provides CSV load and save operations.
 */
public class DataModel {
    // Stores the CSV column headers currently loaded from file.
    private String[] columns;
    /**
     * Loads CSV data from the supplied file into memory.
     *
     * @param file the file to load from
     *
     * @throws IOException if the operation cannot be completed successfully
     */
    private final List<String[]> rows = new ArrayList<>();
    private File currentFile;

    public void loadFromFile(File file) throws IOException {
        currentFile = file;
        rows.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            columns = line != null ? parseCsvLine(line) : new String[0];

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parsed = parseCsvLine(line);
                String[] padded = new String[columns.length];
                for (int i = 0; i < columns.length; i++) {
                    padded[i] = i < parsed.length ? parsed[i].trim() : "";
                }
                rows.add(padded);
            }
        }
    }

    /**
     * Writes the in-memory catalogue back to the currently loaded file.
     *
     * @throws IOException if the operation cannot be completed successfully
     */
    public void saveToFile() throws IOException {
        if (currentFile == null || columns == null) {
            return;
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(currentFile))) {
            writer.println(toCsvLine(columns));

            for (String[] row : rows) {
                String[] outputRow = new String[columns.length];
                for (int i = 0; i < columns.length; i++) {
                    outputRow[i] = i < row.length && row[i] != null ? row[i] : "";
                }
                writer.println(toCsvLine(outputRow));
            }
        }
    }

    /**
     * Returns the currently loaded column headers.
     *
     * @return the resulting array
     */
    public String[] getColumns() {
        return columns;
    }

    /**
     * Returns the loaded data as a two-dimensional object array.
     *
     * @return the resulting array
     */
    public Object[][] getData() {
        if (columns == null) {
            return new Object[0][0];
        }

        Object[][] data = new Object[rows.size()][columns.length];
        for (int i = 0; i < rows.size(); i++) {
            String[] row = rows.get(i);
            for (int j = 0; j < columns.length; j++) {
                data[i][j] = j < row.length ? row[j] : "";
            }
        }
        return data;
    }

    /**
     * Returns a padded copy of the requested row.
     *
     * @param index the index involved in the operation
     *
     * @return the resulting array
     */
    public String[] getRow(int index) {
        String[] original = rows.get(index);
        String[] padded = new String[columns.length];
        for (int i = 0; i < columns.length; i++) {
            padded[i] = i < original.length ? original[i] : "";
        }
        return padded;
    }

    /**
     * Returns the number of rows currently loaded in memory.
     *
     * @return the resulting numeric value
     */
    public int getRowCount() {
        return rows.size();
    }

    /**
     * Adds a new row to the in-memory data set.
     *
     * @param row the row values involved in the operation
     */
    public void addRow(String[] row) {
        if (row == null || columns == null || row.length != columns.length) {
            throw new IllegalArgumentException("Invalid row data");
        }

        String[] normalized = new String[row.length];
        for (int i = 0; i < row.length; i++) {
            normalized[i] = row[i] == null || row[i].trim().isEmpty() ? "N/A" : row[i].trim();
        }

        rows.add(normalized);
    }

    /**
     * Replaces the row at the supplied index.
     *
     * @param index the index involved in the operation
     * @param row the row values involved in the operation
     */
    public void updateRow(int index, String[] row) {
        if (row == null || columns == null || row.length != columns.length) {
            throw new IllegalArgumentException("Invalid row data");
        }

        String[] normalized = new String[row.length];
        for (int i = 0; i < row.length; i++) {
            normalized[i] = row[i] == null ? "" : row[i].trim();
        }

        rows.set(index, normalized);
    }

    /**
     * Removes the row at the supplied index.
     *
     * @param index the index involved in the operation
     */
    public void removeRow(int index) {
        rows.remove(index);
    }

    /**
     * Returns whether valid data.
     *
     * @return {@code true} when the requested condition is met; otherwise {@code false}
     */
    public boolean hasValidData() {
        return columns != null && columns.length > 0;
    }

    /**
     * Parses a single CSV line into its individual cell values.
     *
     * @param line the CSV line to parse
     *
     * @return the resulting array
     */
    private String[] parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }

        values.add(current.toString());
        return values.toArray(new String[0]);
    }

    /**
     * Converts a row of values into a CSV-formatted line.
     *
     * @param values the values to join
     *
     * @return the resulting string value
     */
    private String toCsvLine(String[] values) {
        String[] escaped = new String[values.length];

        for (int i = 0; i < values.length; i++) {
            String value = values[i] == null ? "" : values[i];
            boolean needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n");
            if (value.contains("\"")) {
                value = value.replace("\"", "\"\"");
            }
            escaped[i] = needsQuotes ? "\"" + value + "\"" : value;
        }

        return String.join(",", escaped);
    }
}
