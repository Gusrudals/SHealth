package com.bestreviewer;

import java.util.List;

/**
 * Replaces missing heights ({@code height == 0}) with the same age-decade average.
 */
public class HeightImputer {

    private static final double MISSING_HEIGHT = 0.0;
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
        double average = averageValidHeightInDecade(records, ageDecade);
        if (average == NO_DECADE_AVERAGE) {
            return;
        }
        for (UserRecord record : records) {
            if (AgeDecade.isInDecade(record.getAge(), ageDecade) && record.getHeight() == MISSING_HEIGHT) {
                record.setHeight(average);
            }
        }
    }

    private double averageValidHeightInDecade(List<UserRecord> records, int ageDecade) {
        double sum = 0.0;
        int validCount = 0;
        for (UserRecord record : records) {
            if (AgeDecade.isInDecade(record.getAge(), ageDecade) && record.getHeight() != MISSING_HEIGHT) {
                sum += record.getHeight();
                validCount++;
            }
        }
        if (validCount == 0) {
            return NO_DECADE_AVERAGE;
        }
        return sum / validCount;
    }
}
