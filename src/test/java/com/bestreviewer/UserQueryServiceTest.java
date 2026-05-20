package com.bestreviewer;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class UserQueryServiceTest {

    @Test
    void should_excludeBoundaryBmis_when_at18_5And23() {
        List<UserRecord> records = new ArrayList<>(List.of(
                recordWithBmi(10, 18.5),
                recordWithBmi(11, 18.51),
                recordWithBmi(12, 22.99),
                recordWithBmi(13, 23.0)));

        List<Integer> normalIds = new UserQueryService().findNormalBmiUserIds(records);

        assertIterableEquals(List.of(11, 12), normalIds);
    }

    private static UserRecord recordWithBmi(int id, double bmi) {
        UserRecord record = new UserRecord(id, 25, 70.0, 170.0);
        record.setBmi(bmi);
        return record;
    }
}
