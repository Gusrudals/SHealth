package com.bestreviewer;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SHealthBMITest {

    @Test
    void should_computeExpectedBmi_when_weightAndHeightGiven() {
        double bmi = BmiClassifier.computeBmi(70.0, 170.0);
        assertEquals(24.22, bmi, 0.01);
    }

    @Test
    void should_computeExpectedBmi_when_anotherWeightAndHeightGiven() {
        double bmi = BmiClassifier.computeBmi(80.0, 180.0);
        assertEquals(24.69, bmi, 0.01);
    }

    @Test
    void should_computeExpectedBmi_when_sampleDataWeightAndHeightGiven() {
        double bmi = BmiClassifier.computeBmi(53.5, 150.2);
        assertEquals(23.71, bmi, 0.01);
    }

    @Test
    void should_classifyAsUnderweight_when_bmiIs18_5() {
        assertEquals(BmiCategory.UNDERWEIGHT, BmiClassifier.classify(18.5));
    }

    @Test
    void should_classifyAsNormal_when_bmiIsJustAbove18_5() {
        assertEquals(BmiCategory.NORMAL, BmiClassifier.classify(18.51));
    }

    @Test
    void should_classifyAsNormal_when_bmiIs22_99() {
        assertEquals(BmiCategory.NORMAL, BmiClassifier.classify(22.99));
    }

    @Test
    void should_classifyAsOverweight_when_bmiIs23() {
        assertEquals(BmiCategory.OVERWEIGHT, BmiClassifier.classify(23.0));
    }

    @Test
    void should_classifyAsOverweight_when_bmiIs24_99() {
        assertEquals(BmiCategory.OVERWEIGHT, BmiClassifier.classify(24.99));
    }

    @Test
    void should_classifyAsObesity_when_bmiIs25() {
        assertEquals(BmiCategory.OBESITY, BmiClassifier.classify(25.0));
    }

    @Test
    void should_classifyAsObesity_when_bmiIs25_01() {
        assertEquals(BmiCategory.OBESITY, BmiClassifier.classify(25.01));
    }

    @Test
    void should_imputeDecadeAverageWeight_when_weightIsZero() throws IOException {
        Path csv = resourcePath("impute-weight.csv");
        SHealth shealth = new SHealth();
        int count = shealth.calculateBmi(csv.toString());

        assertEquals(3, count);
        assertEquals(100.0, shealth.getRatio(20, BmiCategory.OVERWEIGHT), 0.01);
    }

    @Test
    void should_imputeOnlySameDecade_when_multipleDecadesPresent() throws IOException {
        Path csv = resourcePath("impute-cross-decade.csv");
        SHealth shealth = new SHealth();
        shealth.calculateBmi(csv.toString());

        assertEquals(33.33, shealth.getRatio(20, BmiCategory.OVERWEIGHT), 0.01);
        assertEquals(66.67, shealth.getRatio(20, BmiCategory.OBESITY), 0.01);
        assertEquals(100.0, shealth.getRatio(30, BmiCategory.UNDERWEIGHT), 0.01);
    }

    @Test
    void should_keepZeroWeight_when_noValidWeightInDecade() throws IOException {
        Path csv = resourcePath("impute-no-valid-weight.csv");
        SHealth shealth = new SHealth();
        shealth.calculateBmi(csv.toString());

        assertEquals(100.0, shealth.getRatio(30, BmiCategory.UNDERWEIGHT), 0.01);
        assertEquals(0.0, shealth.getRatio(30, BmiCategory.NORMAL), 0.01);
    }

    @Test
    void should_throwIOException_when_fileDoesNotExist() {
        SHealth shealth = new SHealth();
        assertThrows(IOException.class, () -> shealth.calculateBmi("no-such-shealth-file.dat"));
    }

    @Test
    void should_returnZeroRecords_when_onlyHeaderPresent() throws IOException {
        Path csv = resourcePath("empty-data.csv");
        SHealth shealth = new SHealth();
        int count = shealth.calculateBmi(csv.toString());
        assertEquals(0, count);
    }

    @Test
    void should_throwIOException_when_csvHasTooFewColumns() throws IOException {
        Path csv = resourcePath("bad-columns.csv");
        SHealth shealth = new SHealth();
        assertThrows(IOException.class, () -> shealth.calculateBmi(csv.toString()));
    }

    @Test
    void should_throwNumberFormatException_when_ageIsNotNumeric() throws IOException {
        Path csv = resourcePath("bad-number.csv");
        SHealth shealth = new SHealth();
        assertThrows(NumberFormatException.class, () -> shealth.calculateBmi(csv.toString()));
    }

    @Test
    void should_imputeDecadeAverageHeight_when_heightIsZero() throws IOException {
        Path csv = resourcePath("impute-height.csv");
        SHealth shealth = new SHealth();
        shealth.calculateBmi(csv.toString());

        assertEquals(33.33, shealth.getRatio(20, BmiCategory.OVERWEIGHT), 0.01);
        assertEquals(66.67, shealth.getRatio(20, BmiCategory.NORMAL), 0.01);
    }

    @Test
    void should_imputeOnlySameDecadeHeight_when_multipleDecadesPresent() throws IOException {
        Path csv = resourcePath("impute-cross-decade-height.csv");
        SHealth shealth = new SHealth();
        shealth.calculateBmi(csv.toString());

        assertEquals(33.33, shealth.getRatio(20, BmiCategory.OBESITY), 0.01);
        assertEquals(33.33, shealth.getRatio(20, BmiCategory.OVERWEIGHT), 0.01);
        assertEquals(33.33, shealth.getRatio(20, BmiCategory.NORMAL), 0.01);
        assertEquals(100.0, shealth.getRatio(30, BmiCategory.OVERWEIGHT), 0.01);
    }

    @Test
    void should_keepZeroHeight_when_noValidHeightInDecade() throws IOException {
        Path csv = resourcePath("impute-no-valid-height.csv");
        SHealth shealth = new SHealth();
        shealth.calculateBmi(csv.toString());

        assertEquals(100.0, shealth.getRatio(30, BmiCategory.OBESITY), 0.01);
        assertEquals(0.0, shealth.getRatio(30, BmiCategory.NORMAL), 0.01);
    }

    @Test
    void should_returnNormalBmiUserIds_when_mixedCategoriesPresent() throws IOException {
        Path csv = resourcePath("normal-users.csv");
        SHealth shealth = new SHealth();
        shealth.calculateBmi(csv.toString());

        List<Integer> normalIds = shealth.getNormalBmiUserIds();
        assertIterableEquals(List.of(2, 4), normalIds);
    }

    @Test
    void should_returnEmptyList_when_dataNotLoaded() {
        SHealth shealth = new SHealth();
        assertIterableEquals(List.of(), shealth.getNormalBmiUserIds());
    }

    @Test
    void should_returnEmptyList_when_noNormalUsers() throws IOException {
        Path csv = resourcePath("normal-none.csv");
        SHealth shealth = new SHealth();
        shealth.calculateBmi(csv.toString());

        assertIterableEquals(List.of(), shealth.getNormalBmiUserIds());
    }

    @Test
    void should_includeUser_when_weightImputedToNormalBmi() throws IOException {
        Path csv = resourcePath("normal-after-weight-impute.csv");
        SHealth shealth = new SHealth();
        shealth.calculateBmi(csv.toString());

        assertIterableEquals(List.of(1, 2), shealth.getNormalBmiUserIds());
    }

    @Test
    void should_returnOverallRatios_when_allCategoriesPresent() throws IOException {
        Path csv = resourcePath("overall-ratio.csv");
        SHealth shealth = new SHealth();
        shealth.calculateBmi(csv.toString());

        assertEquals(25.0, shealth.getOverallRatio(BmiCategory.UNDERWEIGHT), 0.01);
        assertEquals(25.0, shealth.getOverallRatio(BmiCategory.NORMAL), 0.01);
        assertEquals(25.0, shealth.getOverallRatio(BmiCategory.OVERWEIGHT), 0.01);
        assertEquals(25.0, shealth.getOverallRatio(BmiCategory.OBESITY), 0.01);
    }

    @Test
    void should_returnOverallRatios_when_categoriesUneven() throws IOException {
        Path csv = resourcePath("overall-uneven.csv");
        SHealth shealth = new SHealth();
        shealth.calculateBmi(csv.toString());

        assertEquals(33.33, shealth.getOverallRatio(BmiCategory.UNDERWEIGHT), 0.01);
        assertEquals(66.67, shealth.getOverallRatio(BmiCategory.NORMAL), 0.01);
        assertEquals(0.0, shealth.getOverallRatio(BmiCategory.OVERWEIGHT), 0.01);
        assertEquals(0.0, shealth.getOverallRatio(BmiCategory.OBESITY), 0.01);
    }

    @Test
    void should_sumTo100Percent_when_allUsersLoaded() throws IOException {
        Path csv = resourcePath("overall-ratio.csv");
        SHealth shealth = new SHealth();
        shealth.calculateBmi(csv.toString());

        double sum = shealth.getOverallRatio(BmiCategory.UNDERWEIGHT)
                + shealth.getOverallRatio(BmiCategory.NORMAL)
                + shealth.getOverallRatio(BmiCategory.OVERWEIGHT)
                + shealth.getOverallRatio(BmiCategory.OBESITY);
        assertEquals(100.0, sum, 0.01);
    }

    @Test
    void should_returnZeroOverallRatios_when_notLoaded() {
        SHealth shealth = new SHealth();

        assertEquals(0.0, shealth.getOverallRatio(BmiCategory.UNDERWEIGHT), 0.01);
        assertEquals(0.0, shealth.getOverallRatio(BmiCategory.NORMAL), 0.01);
        assertEquals(0.0, shealth.getOverallRatio(BmiCategory.OVERWEIGHT), 0.01);
        assertEquals(0.0, shealth.getOverallRatio(BmiCategory.OBESITY), 0.01);
    }

    @Test
    void should_returnZeroOverallRatios_when_noUsers() throws IOException {
        Path csv = resourcePath("empty-data.csv");
        SHealth shealth = new SHealth();
        shealth.calculateBmi(csv.toString());

        assertEquals(0.0, shealth.getOverallRatio(BmiCategory.UNDERWEIGHT), 0.01);
        assertEquals(0.0, shealth.getOverallRatio(BmiCategory.NORMAL), 0.01);
        assertEquals(0.0, shealth.getOverallRatio(BmiCategory.OVERWEIGHT), 0.01);
        assertEquals(0.0, shealth.getOverallRatio(BmiCategory.OBESITY), 0.01);
    }

    @Test
    void should_differFromDecadeRatio_when_agesSpanDecades() throws IOException {
        Path csv = resourcePath("overall-decade-contrast.csv");
        SHealth shealth = new SHealth();
        shealth.calculateBmi(csv.toString());

        assertEquals(100.0, shealth.getRatio(20, BmiCategory.UNDERWEIGHT), 0.01);
        assertEquals(50.0, shealth.getOverallRatio(BmiCategory.UNDERWEIGHT), 0.01);
        assertEquals(50.0, shealth.getOverallRatio(BmiCategory.NORMAL), 0.01);
    }

    @Test
    void should_reflectImputedBmi_when_weightOrHeightZero() throws IOException {
        Path csv = resourcePath("overall-after-weight-impute.csv");
        SHealth shealth = new SHealth();
        shealth.calculateBmi(csv.toString());

        assertEquals(100.0, shealth.getOverallRatio(BmiCategory.NORMAL), 0.01);
        assertEquals(0.0, shealth.getOverallRatio(BmiCategory.UNDERWEIGHT), 0.01);
    }

    @Test
    void should_returnEqualRatios_when_twentiesHaveOnePerCategory() throws IOException {
        Path csv = resourcePath("decade-20-four-categories.csv");
        SHealth shealth = new SHealth();
        shealth.calculateBmi(csv.toString());

        assertEquals(25.0, shealth.getRatio(20, BmiCategory.UNDERWEIGHT), 0.01);
        assertEquals(25.0, shealth.getRatio(20, BmiCategory.NORMAL), 0.01);
        assertEquals(25.0, shealth.getRatio(20, BmiCategory.OVERWEIGHT), 0.01);
        assertEquals(25.0, shealth.getRatio(20, BmiCategory.OBESITY), 0.01);
    }

    @Test
    void should_returnDecadeRatios_when_thirtiesMixedCategories() throws IOException {
        Path csv = resourcePath("decade-30-mixed.csv");
        SHealth shealth = new SHealth();
        shealth.calculateBmi(csv.toString());

        assertEquals(33.33, shealth.getRatio(30, BmiCategory.UNDERWEIGHT), 0.01);
        assertEquals(33.33, shealth.getRatio(30, BmiCategory.NORMAL), 0.01);
        assertEquals(0.0, shealth.getRatio(30, BmiCategory.OVERWEIGHT), 0.01);
        assertEquals(33.33, shealth.getRatio(30, BmiCategory.OBESITY), 0.01);
    }

    @Test
    void should_returnZeroRatios_when_decadeHasNoUsers() throws IOException {
        Path csv = resourcePath("decade-empty-40s.csv");
        SHealth shealth = new SHealth();
        shealth.calculateBmi(csv.toString());

        assertEquals(0.0, shealth.getRatio(40, BmiCategory.UNDERWEIGHT), 0.01);
        assertEquals(0.0, shealth.getRatio(40, BmiCategory.NORMAL), 0.01);
        assertEquals(0.0, shealth.getRatio(40, BmiCategory.OVERWEIGHT), 0.01);
        assertEquals(0.0, shealth.getRatio(40, BmiCategory.OBESITY), 0.01);
    }

    @Test
    void should_sumTo100Percent_when_decadeHasUsers() throws IOException {
        Path csv = resourcePath("decade-20-four-categories.csv");
        SHealth shealth = new SHealth();
        shealth.calculateBmi(csv.toString());

        double sum = shealth.getRatio(20, BmiCategory.UNDERWEIGHT)
                + shealth.getRatio(20, BmiCategory.NORMAL)
                + shealth.getRatio(20, BmiCategory.OVERWEIGHT)
                + shealth.getRatio(20, BmiCategory.OBESITY);
        assertEquals(100.0, sum, 0.01);
    }

    private Path resourcePath(String name) throws IOException {
        URL url = getClass().getClassLoader().getResource(name);
        if (url == null) {
            throw new IOException("Resource not found: " + name);
        }
        try {
            return Path.of(url.toURI());
        } catch (java.net.URISyntaxException e) {
            throw new IOException("Invalid resource URI: " + name, e);
        }
    }
}
