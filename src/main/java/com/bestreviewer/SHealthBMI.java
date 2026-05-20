package com.bestreviewer;

import java.io.IOException;
import java.util.List;

/**
 * Entry point: loads {@code shealth.dat} and prints age-decade BMI distribution ratios.
 */
public class SHealthBMI {

    private static final String DEFAULT_DATA_FILE = "shealth.dat";

    public static void main(String[] args) throws IOException {
        String dataFile = args.length > 0 ? args[0] : DEFAULT_DATA_FILE;
        SHealth shealth = new SHealth();
        shealth.calculateBmi(dataFile);
        printDecadeRatios(shealth);
        printNormalUsers(shealth);
        printOverallRatios(shealth);
    }

    private static void printDecadeRatios(SHealth shealth) {
        for (int ageDecade : SHealth.AGE_DECADES) {
            System.out.printf(
                    "%d - underweight = %f, normal = %f, overweight = %f, obesity = %f%n",
                    ageDecade,
                    shealth.getRatio(ageDecade, BmiCategory.UNDERWEIGHT),
                    shealth.getRatio(ageDecade, BmiCategory.NORMAL),
                    shealth.getRatio(ageDecade, BmiCategory.OVERWEIGHT),
                    shealth.getRatio(ageDecade, BmiCategory.OBESITY));
        }
    }

    private static void printNormalUsers(SHealth shealth) {
        List<Integer> normalIds = shealth.getNormalBmiUserIds();
        System.out.printf("normal BMI user ids = %s%n", normalIds);
    }

    private static void printOverallRatios(SHealth shealth) {
        System.out.printf(
                "overall - underweight = %f, normal = %f, overweight = %f, obesity = %f%n",
                shealth.getOverallRatio(BmiCategory.UNDERWEIGHT),
                shealth.getOverallRatio(BmiCategory.NORMAL),
                shealth.getOverallRatio(BmiCategory.OVERWEIGHT),
                shealth.getOverallRatio(BmiCategory.OBESITY));
    }
}
