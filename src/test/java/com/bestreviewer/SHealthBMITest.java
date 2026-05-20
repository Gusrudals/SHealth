package com.bestreviewer;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SHealthBMITest {

    @Test
    void should_computeExpectedBmi_when_weightAndHeightGiven() {
        double bmi = BmiClassifier.computeBmi(70.0, 170.0);
        assertEquals(24.22, bmi, 0.01);
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
    void should_classifyAsOverweight_when_bmiIs23() {
        assertEquals(BmiCategory.OVERWEIGHT, BmiClassifier.classify(23.0));
    }

    @Test
    void should_classifyAsObesity_when_bmiIs25() {
        assertEquals(BmiCategory.OBESITY, BmiClassifier.classify(25.0));
    }

    @Test
    void should_imputeDecadeAverageWeight_when_weightIsZero() throws IOException {
        Path csv = resourcePath("impute-weight.csv");
        SHealth shealth = new SHealth();
        int count = shealth.calculateBmi(csv.toString());

        assertEquals(3, count);
        assertEquals(100.0, shealth.getRatio(20, BmiCategory.OVERWEIGHT), 0.01);
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
