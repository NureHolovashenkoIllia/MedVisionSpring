package ua.nure.holovashenko.medvisionspring.service;

import org.springframework.stereotype.Service;
import ua.nure.holovashenko.medvisionspring.svm.Dataset;
import ua.nure.holovashenko.medvisionspring.svm.DatasetSplitResult;

import java.io.File;
import java.util.*;

@Service
public class DatasetSplitService {

    public DatasetSplitResult stratifiedSplit(
            Dataset dataset,
            double trainRatio,
            double validationRatio,
            double testRatio,
            long seed
    ) {
        validateRatios(trainRatio, validationRatio, testRatio);

        Map<Integer, List<Integer>> indexesByLabel = new HashMap<>();
        for (int i = 0; i < dataset.labels().size(); i++) {
            indexesByLabel.computeIfAbsent(dataset.labels().get(i), ignored -> new ArrayList<>()).add(i);
        }

        List<File> trainImages = new ArrayList<>();
        List<Integer> trainLabels = new ArrayList<>();
        List<File> validationImages = new ArrayList<>();
        List<Integer> validationLabels = new ArrayList<>();
        List<File> testImages = new ArrayList<>();
        List<Integer> testLabels = new ArrayList<>();

        Random random = new Random(seed);
        for (Map.Entry<Integer, List<Integer>> entry : indexesByLabel.entrySet()) {
            List<Integer> indexes = new ArrayList<>(entry.getValue());
            Collections.shuffle(indexes, random);

            int trainCount = (int) Math.floor(indexes.size() * trainRatio);
            int validationCount = (int) Math.floor(indexes.size() * validationRatio);

            for (int i = 0; i < indexes.size(); i++) {
                int sourceIndex = indexes.get(i);
                if (i < trainCount) {
                    trainImages.add(dataset.images().get(sourceIndex));
                    trainLabels.add(dataset.labels().get(sourceIndex));
                } else if (i < trainCount + validationCount) {
                    validationImages.add(dataset.images().get(sourceIndex));
                    validationLabels.add(dataset.labels().get(sourceIndex));
                } else {
                    testImages.add(dataset.images().get(sourceIndex));
                    testLabels.add(dataset.labels().get(sourceIndex));
                }
            }
        }

        return new DatasetSplitResult(
                trainImages,
                trainLabels,
                validationImages,
                validationLabels,
                testImages,
                testLabels,
                dataset.labelMap()
        );
    }

    public Map<Integer, Integer> countByClass(List<Integer> labels) {
        Map<Integer, Integer> counts = new TreeMap<>();
        for (Integer label : labels) {
            counts.put(label, counts.getOrDefault(label, 0) + 1);
        }
        return counts;
    }

    private void validateRatios(double trainRatio, double validationRatio, double testRatio) {
        double sum = trainRatio + validationRatio + testRatio;
        if (Math.abs(sum - 1.0) > 0.000001) {
            throw new IllegalArgumentException("Train, validation and test ratios must sum to 1.0");
        }
    }
}
