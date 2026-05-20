package com.bestreviewer;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class UserQueryServiceTest {

    private final UserQueryService service = new UserQueryService();

    @Test
    void should_returnZero_when_noRecords() {
        List<UserRecord> records = List.of();

        assertEquals(0.0, service.getOverallRatio(records, BmiCategory.UNDERWEIGHT), 0.01);
        assertEquals(0.0, service.getOverallRatio(records, BmiCategory.NORMAL), 0.01);
        assertEquals(0.0, service.getOverallRatio(records, BmiCategory.OVERWEIGHT), 0.01);
        assertEquals(0.0, service.getOverallRatio(records, BmiCategory.OBESITY), 0.01);
    }

    @Test
    void should_countUnderweightAt18_5_when_classifyingOverall() {
        List<UserRecord> records = List.of(
                recordWithBmi(1, 18.5),
                recordWithBmi(2, 18.51));

        assertEquals(50.0, service.getOverallRatio(records, BmiCategory.UNDERWEIGHT), 0.01);
        assertEquals(50.0, service.getOverallRatio(records, BmiCategory.NORMAL), 0.01);
    }

    @Test
    void should_return100PercentForOneCategory_when_allSame() {
        List<UserRecord> records = List.of(
                recordWithBmi(1, 26.0),
                recordWithBmi(2, 27.0),
                recordWithBmi(3, 28.0));

        assertEquals(100.0, service.getOverallRatio(records, BmiCategory.OBESITY), 0.01);
        assertEquals(0.0, service.getOverallRatio(records, BmiCategory.NORMAL), 0.01);
    }

    @Test
    void should_excludeBoundaryBmis_when_at18_5And23() {
        List<UserRecord> records = new ArrayList<>(List.of(
                recordWithBmi(10, 18.5),
                recordWithBmi(11, 18.51),
                recordWithBmi(12, 22.99),
                recordWithBmi(13, 23.0)));

        List<Integer> normalIds = service.findNormalBmiUserIds(records);

        assertIterableEquals(List.of(11, 12), normalIds);
    }

    private static UserRecord recordWithBmi(int id, double bmi) {
        UserRecord record = new UserRecord(id, 25, 70.0, 170.0);
        record.setBmi(bmi);
        return record;
    }
}
