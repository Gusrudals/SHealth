package com.bestreviewer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SHealth {
    private int count;
    private int[] ages = new int[10000];
    private double[] heights = new double[10000];
    private double[] weights = new double[10000];
    private double[] bmis = new double[10000];

    private double underweight20;
    private double underweight30;
    private double underweight40;
    private double underweight50;
    private double underweight60;
    private double underweight70;
    private double normalweight20;
    private double normalweight30;
    private double normalweight40;
    private double normalweight50;
    private double normalweight60;
    private double normalweight70;
    private double overweight20;
    private double overweight30;
    private double overweight40;
    private double overweight50;
    private double overweight60;
    private double overweight70;
    private double obesity20;
    private double obesity30;
    private double obesity40;
    private double obesity50;
    private double obesity60;
    private double obesity70;

    public int calculateBmi(String filename) {
        count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            br.readLine(); //첫번째 줄 읽기
            while ((line = br.readLine()) != null) {
                List<String> tokens = split(line, ',');
                if (tokens.size() == 0) {
                    break;
                }
                ages[count] = Integer.parseInt(tokens.get(1));
                weights[count] = Double.parseDouble(tokens.get(2));
                heights[count] = Double.parseDouble(tokens.get(3));
                count++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 데이터 수집 중 누락된 체중에 나이대(ex. 20대, 30대, 40대 등)의 평균 체중을 적용
        for (int a = 20; a <= 70; a += 10) {
            double sum = 0;
            int ageCount = 0;
            for (int i = 0; i < count; i++) {
                if (ages[i] >= a && ages[i] < a + 10) {
                    if (weights[i] == 0.0) {
                        continue;
                    }
                    sum += weights[i];
                    ageCount++;
                }
            }
            for (int i = 0; i < count; i++) {
                if (ages[i] >= a && ages[i] < a + 10) {
                    if (weights[i] == 0.0) {
                        weights[i] = sum / ageCount;
                    }
                }
            }
        }

        // BMI 계산하기
        for (int i = 0; i < count; i++) {
            bmis[i] = weights[i] / ((heights[i] / 100.0) * (heights[i] / 100.0));
        }

        // 나이대(ex. 20대, 30대, 40대 등)의 BMI기준 저체중, 정상체중, 과체중, 비만 비율 계산
        for (int a = 20; a <= 70; a += 10) {
            int underweight = 0;
            int normalweight = 0;
            int overweight = 0;
            int obesity = 0;
            int sum = 0;
            for (int i = 0; i < count; i++) {
                if (ages[i] >= a && ages[i] < a + 10) {
                    sum++;
                    if (bmis[i] <= 18.5) {
                        underweight++;
                    } else if (bmis[i] > 18.5 && bmis[i] < 23) {
                        normalweight++;
                    } else if (bmis[i] >= 23 && bmis[i] < 25) {
                        overweight++;
                    } else if (bmis[i] > 25) {
                        obesity++;
                    }
                }
            }
            if (a == 20) {
                underweight20 = (double) underweight * 100 / sum;
                normalweight20 = (double) normalweight * 100 / sum;
                overweight20 = (double) overweight * 100 / sum;
                obesity20 = (double) obesity * 100 / sum;
            } else if (a == 30) {
                underweight30 = (double) underweight * 100 / sum;
                normalweight30 = (double) normalweight * 100 / sum;
                overweight30 = (double) overweight * 100 / sum;
                obesity30 = (double) obesity * 100 / sum;
            } else if (a == 40) {
                underweight40 = (double) underweight * 100 / sum;
                normalweight40 = (double) normalweight * 100 / sum;
                overweight40 = (double) overweight * 100 / sum;
                obesity40 = (double) obesity * 100 / sum;
            } else if (a == 50) {
                underweight50 = (double) underweight * 100 / sum;
                normalweight50 = (double) normalweight * 100 / sum;
                overweight50 = (double) overweight * 100 / sum;
                obesity50 = (double) obesity * 100 / sum;
            } else if (a == 60) {
                underweight60 = (double) underweight * 100 / sum;
                normalweight60 = (double) normalweight * 100 / sum;
                overweight60 = (double) overweight * 100 / sum;
                obesity60 = (double) obesity * 100 / sum;
            } else if (a == 70) {
                underweight70 = (double) underweight * 100 / sum;
                normalweight70 = (double) normalweight * 100 / sum;
                overweight70 = (double) overweight * 100 / sum;
                obesity70 = (double) obesity * 100 / sum;
            }
        }
        return count;
    }

    public double getBmiRatio(int ageClass, int type) {
        if (ageClass == 20 && type == 100) {
            return underweight20;
        } else if (ageClass == 20 && type == 200) {
            return normalweight20;
        } else if (ageClass == 20 && type == 300) {
            return overweight20;
        } else if (ageClass == 20 && type == 400) {
            return obesity20;
        } else if (ageClass == 30 && type == 100) {
            return underweight30;
        } else if (ageClass == 30 && type == 200) {
            return normalweight30;
        } else if (ageClass == 30 && type == 300) {
            return overweight30;
        } else if (ageClass == 30 && type == 400) {
            return obesity30;
        } else if (ageClass == 40 && type == 100) {
            return underweight40;
        } else if (ageClass == 40 && type == 200) {
            return normalweight40;
        } else if (ageClass == 40 && type == 300) {
            return overweight40;
        } else if (ageClass == 40 && type == 400) {
            return obesity40;
        } else if (ageClass == 50 && type == 100) {
            return underweight50;
        } else if (ageClass == 50 && type == 200) {
            return normalweight50;
        } else if (ageClass == 50 && type == 300) {
            return overweight50;
        } else if (ageClass == 50 && type == 400) {
            return obesity50;
        } else if (ageClass == 60 && type == 100) {
            return underweight60;
        } else if (ageClass == 60 && type == 200) {
            return normalweight60;
        } else if (ageClass == 60 && type == 300) {
            return overweight60;
        } else if (ageClass == 60 && type == 400) {
            return obesity60;
        } else if (ageClass == 70 && type == 100) {
            return underweight70;
        } else if (ageClass == 70 && type == 200) {
            return normalweight70;
        } else if (ageClass == 70 && type == 300) {
            return overweight70;
        } else if (ageClass == 70 && type == 400) {
            return obesity70;
        }
        return 0.0;
    }

    private List<String> split(String line, char delimiter) {
        List<String> tokens = new ArrayList<>();
        int start = 0;
        int end = line.indexOf(delimiter);
        while (end != -1) {
            tokens.add(line.substring(start, end));
            start = end + 1;
            end = line.indexOf(delimiter, start);
        }
        tokens.add(line.substring(start));
        return tokens;
    }
}