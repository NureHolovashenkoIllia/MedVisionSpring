package ua.nure.holovashenko.medvisionspring.svm;

import org.bytedeco.opencv.opencv_core.Mat;

public record FeatureLabelPair(Mat features, int label) {}