package com.bestreviewer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 * Queries user records: normal-BMI ids and overall category ratios.
 */
public class UserQueryService {

    private static final double PERCENT_SCALE = 100.0;

    /**
     * @param records records with BMI already computed
     * @return ids of users with normal BMI (18.5 &lt; BMI &lt; 23)
     */
    public List<Integer> findNormalBmiUserIds(List<UserRecord> records) {
        List<Integer> ids = new ArrayList<>();
        for (UserRecord record : records) {
            if (isNormalBmi(record.getBmi())) {
                ids.add(record.getId());
            }
        }
        return ids;
    }

    /**
     * @param records  records with BMI already computed
     * @param category BMI category
     * @return percentage of all users in that category, or 0.0 if no users
     */
    public double getOverallRatio(List<UserRecord> records, BmiCategory category) {
        EnumMap<BmiCategory, Double> ratios = computeOverallRatios(records);
        Double ratio = ratios.get(category);
        return ratio != null ? ratio : 0.0;
    }

    private EnumMap<BmiCategory, Double> computeOverallRatios(List<UserRecord> records) {
        int[] counts = new int[BmiCategory.values().length];
        int total = records.size();
        for (UserRecord record : records) {
            BmiCategory category = BmiClassifier.classify(record.getBmi());
            counts[category.ordinal()]++;
        }
        return toPercentRatios(counts, total);
    }

    private static boolean isNormalBmi(double bmi) {
        return BmiClassifier.classify(bmi) == BmiCategory.NORMAL;
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
