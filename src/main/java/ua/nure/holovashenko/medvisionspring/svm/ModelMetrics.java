package ua.nure.holovashenko.medvisionspring.svm;

import java.util.Map;

public record ModelMetrics(
        double accuracy,
        int[][] confusionMatrix,
        Map<Integer, MetricsCalculator.ClassMetrics> perClassMetrics
) {}
