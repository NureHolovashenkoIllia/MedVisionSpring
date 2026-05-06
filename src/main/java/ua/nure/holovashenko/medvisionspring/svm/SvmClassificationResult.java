package ua.nure.holovashenko.medvisionspring.svm;

import org.bytedeco.opencv.opencv_core.Mat;

public record SvmClassificationResult(
        int diagnosisClass,
        DiagnosisInfo diagnosisInfo,
        ModelMetrics metrics,
        MetricsCalculator.ClassMetrics classMetrics,
        Mat heatmap
) {}
