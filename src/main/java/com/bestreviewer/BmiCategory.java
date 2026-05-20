package com.bestreviewer;

/**
 * BMI classification categories aligned with README domain rules.
 */
public enum BmiCategory {
    UNDERWEIGHT(100),
    NORMAL(200),
    OVERWEIGHT(300),
    OBESITY(400);

    private final int legacyType;

    BmiCategory(int legacyType) {
        this.legacyType = legacyType;
    }

    /**
     * @return legacy numeric type used by {@link SHealth#getBmiRatio(int, int)}.
     */
    public int getLegacyType() {
        return legacyType;
    }

    /**
     * @param type legacy type code (100, 200, 300, 400)
     * @return matching category, or {@code null} if unknown
     */
    public static BmiCategory fromLegacyType(int type) {
        for (BmiCategory category : values()) {
            if (category.legacyType == type) {
                return category;
            }
        }
        return null;
    }
}
