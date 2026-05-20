package com.bestreviewer;

import java.io.IOException;
import java.util.List;

/**
 * Facade: loads CSV, imputes missing values, computes BMI, and exposes statistics and queries.
 */
public class SHealth {

    static final int[] AGE_DECADES = AgeDecade.VALUES;

    private final CsvHealthRecordLoader loader = new CsvHealthRecordLoader();
    private final WeightImputer weightImputer = new WeightImputer();
    private final HeightImputer heightImputer = new HeightImputer();
    private final AgeDecadeStatistics statistics = new AgeDecadeStatistics();
    private final UserQueryService userQueryService = new UserQueryService();

    private List<UserRecord> records = List.of();

    /**
     * Loads data, imputes weights and heights, computes BMI and age-decade ratio statistics.
     *
     * @param filename path to CSV file
     * @return number of records loaded
     * @throws IOException if the file cannot be read
     */
    public int calculateBmi(String filename) throws IOException {
        records = loader.load(filename);
        weightImputer.impute(records);
        heightImputer.impute(records);
        computeAllBmis(records);
        statistics.aggregate(records);
        return records.size();
    }

    /**
     * @param ageDecade decade start age (20, 30, …, 70)
     * @param type      legacy category code (100–400)
     * @return percentage for that decade and category, or 0.0 if unknown
     */
    public double getBmiRatio(int ageDecade, int type) {
        BmiCategory category = BmiCategory.fromLegacyType(type);
        if (category == null) {
            return 0.0;
        }
        return getRatio(ageDecade, category);
    }

    /**
     * @param ageDecade decade start age (20, 30, …, 70)
     * @param category  BMI category
     * @return percentage for that decade and category, or 0.0 if not computed
     */
    public double getRatio(int ageDecade, BmiCategory category) {
        return statistics.getRatio(ageDecade, category);
    }

    /**
     * @return ids of users with normal BMI (18.5 &lt; BMI &lt; 23); empty if not yet loaded
     */
    public List<Integer> getNormalBmiUserIds() {
        return userQueryService.findNormalBmiUserIds(records);
    }

    /**
     * @param category BMI category
     * @return percentage of all loaded users in that category, or 0.0 if not loaded
     */
    public double getOverallRatio(BmiCategory category) {
        return userQueryService.getOverallRatio(records, category);
    }

    private void computeAllBmis(List<UserRecord> records) {
        for (UserRecord record : records) {
            double bmi = BmiClassifier.computeBmi(record.getWeight(), record.getHeight());
            record.setBmi(bmi);
        }
    }
}
