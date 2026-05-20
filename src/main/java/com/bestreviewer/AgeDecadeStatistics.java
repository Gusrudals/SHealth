package com.bestreviewer;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregates BMI category percentages per age decade.
 */
public class AgeDecadeStatistics {

    private static final double PERCENT_SCALE = 100.0;

    private final Map<Integer, EnumMap<BmiCategory, Double>> ratiosByDecade = new HashMap<>();

    /**
     * @param records records with BMI already computed
     */
    public void aggregate(List<UserRecord> records) {
        ratiosByDecade.clear();
        for (int ageDecade = AgeDecade.MIN_DECADE; ageDecade <= AgeDecade.MAX_DECADE; ageDecade += AgeDecade.STEP) {
            ratiosByDecade.put(ageDecade, computeDecadeRatios(records, ageDecade));
        }
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

    private EnumMap<BmiCategory, Double> computeDecadeRatios(List<UserRecord> records, int ageDecade) {
        int[] counts = new int[BmiCategory.values().length];
        int total = 0;
        for (UserRecord record : records) {
            if (!AgeDecade.isInDecade(record.getAge(), ageDecade)) {
                continue;
            }
            total++;
            BmiCategory category = BmiClassifier.classify(record.getBmi());
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
}
