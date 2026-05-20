package com.bestreviewer;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HeightImputerTest {

    @Test
    void should_setDecadeAverageHeight_when_heightIsZero() {
        List<UserRecord> records = new ArrayList<>(List.of(
                new UserRecord(1, 25, 70.0, 170.0),
                new UserRecord(2, 27, 70.0, 0.0),
                new UserRecord(3, 28, 70.0, 180.0)));

        new HeightImputer().impute(records);

        assertEquals(170.0, records.get(0).getHeight(), 0.01);
        assertEquals(175.0, records.get(1).getHeight(), 0.01);
        assertEquals(180.0, records.get(2).getHeight(), 0.01);
    }

    @Test
    void should_keepZeroHeight_when_noValidHeightInDecade() {
        List<UserRecord> records = new ArrayList<>(List.of(
                new UserRecord(1, 32, 70.0, 0.0),
                new UserRecord(2, 35, 70.0, 0.0)));

        new HeightImputer().impute(records);

        assertEquals(0.0, records.get(0).getHeight(), 0.01);
        assertEquals(0.0, records.get(1).getHeight(), 0.01);
    }
}
