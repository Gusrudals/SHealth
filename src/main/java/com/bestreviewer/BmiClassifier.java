package com.bestreviewer;

/**
 * BMI calculation and classification per README domain rules.
 */
public final class BmiClassifier {

    public static final double UNDERWEIGHT_MAX = 18.5;
    public static final double NORMAL_MAX = 23.0;
    public static final double OVERWEIGHT_MAX = 25.0;
    public static final double CM_PER_M = 100.0;

    private BmiClassifier() {
    }

    /**
     * @param weightKg weight in kilograms
     * @param heightCm height in centimeters
     * @return BMI value
     */
    public static double computeBmi(double weightKg, double heightCm) {
        double heightM = heightCm / CM_PER_M;
        return weightKg / (heightM * heightM);
    }

    /**
     * @param bmi BMI value
     * @return category per README boundaries
     */
    public static BmiCategory classify(double bmi) {
        if (bmi <= UNDERWEIGHT_MAX) {
            return BmiCategory.UNDERWEIGHT;
        }
        if (bmi < NORMAL_MAX) {
            return BmiCategory.NORMAL;
        }
        if (bmi < OVERWEIGHT_MAX) {
            return BmiCategory.OVERWEIGHT;
        }
        return BmiCategory.OBESITY;
    }
}
