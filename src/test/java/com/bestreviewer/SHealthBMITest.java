package com.bestreviewer;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
