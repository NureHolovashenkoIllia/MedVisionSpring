package ua.nure.holovashenko.medvisionspring.svm;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_ml.SVM;
import org.springframework.stereotype.Service;
import ua.nure.holovashenko.medvisionspring.service.AzureBlobStorageService;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
public class MetricsCalculator {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AzureBlobStorageService azureStorageService;

    public MetricsCalculator(AzureBlobStorageService azureStorageService) {
        this.azureStorageService = azureStorageService;
    }

    public ModelMetrics calculate(SVM model, List<Mat> features, List<Integer> labels) {
        if (features.size() != labels.size()) {
            throw new IllegalArgumentException("Розмір features і labels повинен бути однаковим.");
        }

        List<Integer> predictions = new ArrayList<>();
        for (Mat feature : features) {
            float prediction = model.predict(feature);
            predictions.add((int) prediction);
        }

        double accuracy = computeAccuracy(labels, predictions);
        int numClasses = getNumClasses(labels);
        int[][] confusionMatrix = computeConfusionMatrix(labels, predictions, numClasses);
        Map<Integer, ClassMetrics> perClassMetrics = computePerClassMetrics(confusionMatrix);

        return new ModelMetrics(accuracy, confusionMatrix, perClassMetrics);
    }

    public void save(ModelMetrics metrics, String path) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(path), metrics);
        } catch (Exception e) {
            log.error("Не вдалося зберегти метрики в файл: {}", path, e);
        }
    }

    public ModelMetrics loadMetricsFromAzure(String blobName) {
        try {
            byte[] data = azureStorageService.downloadFileByName(blobName);
            return objectMapper.readValue(data, ModelMetrics.class);
        } catch (IOException e) {
            System.err.println("Не вдалося завантажити метрики з Azure: " + blobName);
            e.printStackTrace();
            return new ModelMetrics(0.0, new int[0][0], Map.of());
        }
    }

    public double computeAccuracy(List<Integer> groundTruth, List<Integer> predictions) {
        if (groundTruth.size() != predictions.size()) {
            throw new IllegalArgumentException("Розміри списків groundTruth і predictions мають збігатися.");
        }

        int correct = 0;
        for (int i = 0; i < groundTruth.size(); i++) {
            if (groundTruth.get(i).equals(predictions.get(i))) {
                correct++;
            }
        }
        return (double) correct / groundTruth.size();
    }

    public int[][] computeConfusionMatrix(List<Integer> groundTruth, List<Integer> predictions, int numClasses) {
        int[][] matrix = new int[numClasses][numClasses];

        for (int i = 0; i < groundTruth.size(); i++) {
            int actual = groundTruth.get(i);
            int predicted = predictions.get(i);
            matrix[actual][predicted]++;
        }

        return matrix;
    }

    public Map<Integer, ClassMetrics> computePerClassMetrics(int[][] confusionMatrix) {
        Map<Integer, ClassMetrics> metricsMap = new HashMap<>();
        int numClasses = confusionMatrix.length;

        for (int classIdx = 0; classIdx < numClasses; classIdx++) {
            int tp = confusionMatrix[classIdx][classIdx];
            int fp = 0, fn = 0;

            for (int i = 0; i < numClasses; i++) {
                if (i != classIdx) {
                    fp += confusionMatrix[i][classIdx];
                    fn += confusionMatrix[classIdx][i];
                }
            }

            double precision = tp + fp == 0 ? 0 : (double) tp / (tp + fp);
            double recall = tp + fn == 0 ? 0 : (double) tp / (tp + fn);
            double f1 = precision + recall == 0 ? 0 : 2 * (precision * recall) / (precision + recall);

            metricsMap.put(classIdx, new ClassMetrics(precision, recall, f1));
        }

        return metricsMap;
    }

    private int getNumClasses(List<Integer> labels) {
        return labels.stream().mapToInt(i -> i).max().orElse(0) + 1;
    }

    public record ClassMetrics(double precision, double recall, double f1) {}
}
