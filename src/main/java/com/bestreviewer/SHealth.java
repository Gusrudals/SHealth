package com.bestreviewer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads Samsung Health CSV data, imputes missing weights, computes BMI, and age-decade statistics.
 */
public class SHealth {

    private static final double MISSING_WEIGHT = 0.0;
    private static final double PERCENT_SCALE = 100.0;
    private static final int DECADE_WIDTH = 10;
    private static final int MIN_AGE_DECADE = 20;
    private static final int MAX_AGE_DECADE = 70;
    private static final int DECADE_STEP = 10;
    private static final int MAX_RECORDS = 10000;
    static final int[] AGE_DECADES = {20, 30, 40, 50, 60, 70};

    private int count;
    private final int[] ages = new int[MAX_RECORDS];
    private final double[] heights = new double[MAX_RECORDS];
    private final double[] weights = new double[MAX_RECORDS];
    private final double[] bmis = new double[MAX_RECORDS];
    private final Map<Integer, EnumMap<BmiCategory, Double>> ratiosByDecade = new HashMap<>();

    /**
     * Loads data, imputes weights, computes BMI and age-decade ratio statistics.
     *
     * @param filename path to CSV file
     * @return number of records loaded
     * @throws IOException if the file cannot be read
     */
    public int calculateBmi(String filename) throws IOException {
        count = 0;
        loadRecordsFromCsv(filename);
        imputeZeroWeightsByAgeDecade();
        computeAllBmis();
        aggregateRatiosByAgeDecade();
        return count;
    }

    /**
     * @param ageDecade decade start age (20, 30, …, 70)
     * @param type      legacy category code (100–400)
     * @return percentage for that decade and category, or 0.0 if unknown
     */
    public double getBmiRatio(int ageDecade, int type) {
        BmiCategory category = BmiCategory.fromLegacyType(type);
        if (category == null) {
            return 0.0;
        }
        return getRatio(ageDecade, category);
    }

    /**
     * @param ageDecade decade start age (20, 30, …, 70)
     * @param category  BMI category
     * @return percentage for that decade and category, or 0.0 if not computed
     */
    public double getRatio(int ageDecade, BmiCategory category) {
        EnumMap<BmiCategory, Double> ratios = ratiosByDecade.get(ageDecade);
        if (ratios == null) {
            return 0.0;
        }
        Double ratio = ratios.get(category);
        return ratio != null ? ratio : 0.0;
    }

    private void loadRecordsFromCsv(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> tokens = parseCsvLine(line, ',');
                if (tokens.isEmpty()) {
                    break;
                }
                ages[count] = Integer.parseInt(tokens.get(1));
                weights[count] = Double.parseDouble(tokens.get(2));
                heights[count] = Double.parseDouble(tokens.get(3));
                count++;
            }
        }
    }

    private void imputeZeroWeightsByAgeDecade() {
        for (int ageDecade = MIN_AGE_DECADE; ageDecade <= MAX_AGE_DECADE; ageDecade += DECADE_STEP) {
            imputeDecade(ageDecade);
        }
    }

    private void imputeDecade(int ageDecade) {
        double average = averageValidWeightInDecade(ageDecade);
        for (int i = 0; i < count; i++) {
            if (isInAgeDecade(ages[i], ageDecade) && weights[i] == MISSING_WEIGHT) {
                weights[i] = average;
            }
        }
    }

    private double averageValidWeightInDecade(int ageDecade) {
        double sum = 0.0;
        int validCount = 0;
        for (int i = 0; i < count; i++) {
            if (isInAgeDecade(ages[i], ageDecade) && weights[i] != MISSING_WEIGHT) {
                sum += weights[i];
                validCount++;
            }
        }
        return sum / validCount;
    }

    private void computeAllBmis() {
        for (int i = 0; i < count; i++) {
            bmis[i] = BmiClassifier.computeBmi(weights[i], heights[i]);
        }
    }

    private void aggregateRatiosByAgeDecade() {
        ratiosByDecade.clear();
        for (int ageDecade = MIN_AGE_DECADE; ageDecade <= MAX_AGE_DECADE; ageDecade += DECADE_STEP) {
            storeDecadeRatios(ageDecade, computeDecadeRatios(ageDecade));
        }
    }

    private EnumMap<BmiCategory, Double> computeDecadeRatios(int ageDecade) {
        int[] counts = new int[BmiCategory.values().length];
        int total = 0;
        for (int i = 0; i < count; i++) {
            if (!isInAgeDecade(ages[i], ageDecade)) {
                continue;
            }
            total++;
            BmiCategory category = BmiClassifier.classify(bmis[i]);
            counts[category.ordinal()]++;
        }
        return toPercentRatios(counts, total);
    }

    private EnumMap<BmiCategory, Double> toPercentRatios(int[] counts, int total) {
        EnumMap<BmiCategory, Double> ratios = new EnumMap<>(BmiCategory.class);
        if (total == 0) {
            return ratios;
        }
        for (BmiCategory category : BmiCategory.values()) {
            double percent = (double) counts[category.ordinal()] * PERCENT_SCALE / total;
            ratios.put(category, percent);
        }
        return ratios;
    }

    private void storeDecadeRatios(int ageDecade, EnumMap<BmiCategory, Double> ratios) {
        ratiosByDecade.put(ageDecade, ratios);
    }

    private static boolean isInAgeDecade(int age, int ageDecade) {
        return age >= ageDecade && age < ageDecade + DECADE_WIDTH;
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
