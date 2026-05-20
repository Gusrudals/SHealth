package com.bestreviewer;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgeDecadeStatisticsTest {

    @Test
    void should_returnEqualRatios_when_twentiesHaveOnePerCategory() {
        List<UserRecord> records = List.of(
                recordWithBmi(1, 25, 15.57),
                recordWithBmi(2, 26, 20.76),
                recordWithBmi(3, 27, 24.22),
                recordWithBmi(4, 28, 27.68));

        AgeDecadeStatistics statistics = new AgeDecadeStatistics();
        statistics.aggregate(records);

        assertEquals(25.0, statistics.getRatio(20, BmiCategory.UNDERWEIGHT), 0.01);
        assertEquals(25.0, statistics.getRatio(20, BmiCategory.NORMAL), 0.01);
        assertEquals(25.0, statistics.getRatio(20, BmiCategory.OVERWEIGHT), 0.01);
        assertEquals(25.0, statistics.getRatio(20, BmiCategory.OBESITY), 0.01);
    }

    private static UserRecord recordWithBmi(int id, int age, double bmi) {
        UserRecord record = new UserRecord(id, age, 70.0, 170.0);
        record.setBmi(bmi);
        return record;
    }
}
