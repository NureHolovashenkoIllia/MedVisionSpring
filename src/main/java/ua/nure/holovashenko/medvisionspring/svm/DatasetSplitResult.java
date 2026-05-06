package ua.nure.holovashenko.medvisionspring.svm;

import java.io.File;
import java.util.List;
import java.util.Map;

public record DatasetSplitResult(
        List<File> trainImages,
        List<Integer> trainLabels,
        List<File> validationImages,
        List<Integer> validationLabels,
        List<File> testImages,
        List<Integer> testLabels,
        Map<String, Integer> labelMap
) {}
