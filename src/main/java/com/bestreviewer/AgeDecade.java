package com.bestreviewer;

/**
 * Age-decade boundaries used for imputation and statistics (20–29 → 20, etc.).
 */
public final class AgeDecade {

    public static final int MIN_DECADE = 20;
    public static final int MAX_DECADE = 70;
    public static final int STEP = 10;
    public static final int WIDTH = 10;
    public static final int[] VALUES = {20, 30, 40, 50, 60, 70};

    private AgeDecade() {
    }

    /**
     * @param age       person age
     * @param ageDecade decade start (20, 30, …)
     * @return true if age falls in that decade
     */
    public static boolean isInDecade(int age, int ageDecade) {
        return age >= ageDecade && age < ageDecade + WIDTH;
    }
}
