package com.bestreviewer;

/**
 * One user row from Samsung Health CSV with mutable weight, height, and computed BMI.
 */
public class UserRecord {

    private final int id;
    private final int age;
    private double weight;
    private double height;
    private double bmi;

    /**
     * @param id     user id from CSV
     * @param age    age in years
     * @param weight weight in kg (0 means missing)
     * @param height height in cm (0 means missing)
     */
    public UserRecord(int id, int age, double weight, double height) {
        this.id = id;
        this.age = age;
        this.weight = weight;
        this.height = height;
    }

    public int getId() {
        return id;
    }

    public int getAge() {
        return age;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getBmi() {
        return bmi;
    }

    public void setBmi(double bmi) {
        this.bmi = bmi;
    }
}
