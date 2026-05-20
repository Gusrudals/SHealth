package com.bestreviewer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads user records from Samsung Health CSV (id, age, weight, height).
 */
public class CsvHealthRecordLoader {

    private static final int CSV_COLUMN_COUNT = 4;

    /**
     * @param filename path to CSV file
     * @return loaded records (header skipped)
     * @throws IOException if the file cannot be read or columns are invalid
     */
    public List<UserRecord> load(String filename) throws IOException {
        List<UserRecord> records = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                appendRecordFromLine(line, records);
            }
        }
        return records;
    }

    private void appendRecordFromLine(String line, List<UserRecord> records) throws IOException {
        List<String> tokens = parseCsvLine(line, ',');
        if (tokens.isEmpty()) {
            return;
        }
        if (tokens.size() < CSV_COLUMN_COUNT) {
            throw new IOException("Invalid CSV: expected at least " + CSV_COLUMN_COUNT + " columns");
        }
        int id = Integer.parseInt(tokens.get(0));
        int age = Integer.parseInt(tokens.get(1));
        double weight = Double.parseDouble(tokens.get(2));
        double height = Double.parseDouble(tokens.get(3));
        records.add(new UserRecord(id, age, weight, height));
    }

    private List<String> parseCsvLine(String line, char delimiter) {
        List<String> tokens = new ArrayList<>();
        int start = 0;
        int end = line.indexOf(delimiter);
        while (end != -1) {
            tokens.add(line.substring(start, end));
            start = end + 1;
            end = line.indexOf(delimiter, start);
        }
        tokens.add(line.substring(start));
        return tokens;
    }
}
