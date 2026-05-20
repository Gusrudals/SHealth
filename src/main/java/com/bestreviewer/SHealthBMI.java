package com.bestreviewer;

import java.io.IOException;

/**
 * Entry point: loads {@code shealth.dat} and prints age-decade BMI distribution ratios.
 */
public class SHealthBMI {

    private static final String DEFAULT_DATA_FILE = "shealth.dat";

    public static void main(String[] args) throws IOException {
        String dataFile = args.length > 0 ? args[0] : DEFAULT_DATA_FILE;
        SHealth shealth = new SHealth();
        shealth.calculateBmi(dataFile);

        for (int ageDecade : SHealth.AGE_DECADES) {
            printDecadeRatios(shealth, ageDecade);
        }
    }

    private static void printDecadeRatios(SHealth shealth, int ageDecade) {
        System.out.printf(
                "%d - underweight = %f, normal = %f, overweight = %f, obesity = %f%n",
                ageDecade,
                shealth.getRatio(ageDecade, BmiCategory.UNDERWEIGHT),
                shealth.getRatio(ageDecade, BmiCategory.NORMAL),
                shealth.getRatio(ageDecade, BmiCategory.OVERWEIGHT),
                shealth.getRatio(ageDecade, BmiCategory.OBESITY));
    }
}
