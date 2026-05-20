package com.bestreviewer;

import java.util.List;

/**
 * Replaces missing weights ({@code weight == 0}) with the same age-decade average.
 */
public class WeightImputer {

    private static final double MISSING_WEIGHT = 0.0;
    private static final double NO_DECADE_AVERAGE = -1.0;

    /**
     * @param records mutable user records
     */
    public void impute(List<UserRecord> records) {
        for (int ageDecade = AgeDecade.MIN_DECADE; ageDecade <= AgeDecade.MAX_DECADE; ageDecade += AgeDecade.STEP) {
            imputeDecade(records, ageDecade);
        }
    }

    private void imputeDecade(List<UserRecord> records, int ageDecade) {
        double average = averageValidWeightInDecade(records, ageDecade);
        if (average == NO_DECADE_AVERAGE) {
            return;
        }
        for (UserRecord record : records) {
            if (AgeDecade.isInDecade(record.getAge(), ageDecade) && record.getWeight() == MISSING_WEIGHT) {
                record.setWeight(average);
            }
        }
    }

    private double averageValidWeightInDecade(List<UserRecord> records, int ageDecade) {
        double sum = 0.0;
        int validCount = 0;
        for (UserRecord record : records) {
            if (AgeDecade.isInDecade(record.getAge(), ageDecade) && record.getWeight() != MISSING_WEIGHT) {
                sum += record.getWeight();
                validCount++;
            }
        }
        if (validCount == 0) {
            return NO_DECADE_AVERAGE;
        }
        return sum / validCount;
    }
}
