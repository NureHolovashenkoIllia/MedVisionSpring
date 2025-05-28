package ua.nure.holovashenko.medvisionspring.svm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_ml;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_ml.SVM;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ua.nure.holovashenko.medvisionspring.exception.ApiException;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SvmModelManager {

    private final ImagePatchExtractor patchExtractor;
    private final MetricsCalculator metricsCalculator;
    private volatile boolean modelsReady = false;

    private SVM fullImageModel;
    private SVM patchModel;

    public boolean areModelsReady() {
        return modelsReady;
    }

    public void loadModels() {
        try {
            patchModel = SVM.load("svm-models/svm_patch_model.xml");
            log.info("Patch-based SVM model successfully loaded");
        } catch (Exception e) {
            patchModel = SVM.create();
            log.warn("Patch-based SVM model could not be loaded. New model created.");
        }

        try {
            fullImageModel = SVM.load("svm-models/svm_full_model.xml");
            log.info("Full image SVM model successfully loaded");
        } catch (Exception e) {
            fullImageModel = SVM.create();
            log.warn("Full image SVM model could not be loaded. New model created.");
        }

        modelsReady = true;
        log.info("All SVM models are ready.");
    }

    public void trainFullImageModel(Dataset dataset) {
        List<Mat> featureList = new ArrayList<>();
        Mat labels = new Mat(dataset.labels().size(), 1, opencv_core.CV_32S);

        for (int i = 0; i < dataset.images().size(); i++) {
            Mat image = ImageUtils.loadAndResizeImage(dataset.images().get(i).getAbsolutePath());

            if (image.empty()) {
                throw new IllegalArgumentException("Failed to load image: " + dataset.images().get(i).getAbsolutePath());
            }

            if (!image.isContinuous()) {
                image = image.clone();
            }

            Mat feature = image.reshape(1, 1);
            feature.convertTo(feature, opencv_core.CV_32F);
            featureList.add(feature);
            labels.ptr(i, 0).putInt(dataset.labels().get(i));
        }

        MatVector vector = new MatVector(featureList.toArray(new Mat[0]));
        Mat trainingData = new Mat();
        opencv_core.vconcat(vector, trainingData);

        fullImageModel = SVM.create();
        fullImageModel.setKernel(SVM.LINEAR);
        fullImageModel.setType(SVM.C_SVC);
        fullImageModel.setC(2.67);
        fullImageModel.setGamma(5.383);

        fullImageModel.train(trainingData, opencv_ml.ROW_SAMPLE, labels);

        ModelMetrics metrics = metricsCalculator.calculate(fullImageModel, featureList, dataset.labels());
        metricsCalculator.save(metrics, "models/full_metrics.json");
    }

    public void trainPatchModel(Dataset dataset) {
        List<Mat> featureList = new ArrayList<>();
        List<Integer> labelList = new ArrayList<>();

        for (int i = 0; i < dataset.images().size(); i++) {
            File imageFile = dataset.images().get(i);
            int label = dataset.labels().get(i);

            List<FeatureLabelPair> patches = patchExtractor.extract(imageFile, label, true);
            for (FeatureLabelPair pair : patches) {
                featureList.add(pair.features());
                labelList.add(pair.label());
            }
        }

        Mat labels = new Mat(labelList.size(), 1, opencv_core.CV_32S);
        for (int i = 0; i < labelList.size(); i++) {
            labels.ptr(i, 0).putInt(labelList.get(i));
        }

        MatVector vector = new MatVector(featureList.toArray(new Mat[0]));
        Mat trainingData = new Mat();
        opencv_core.vconcat(vector, trainingData);

        patchModel = SVM.create();
        patchModel.setKernel(SVM.LINEAR);
        patchModel.setType(SVM.C_SVC);
        patchModel.setC(2.67);
        patchModel.setGamma(5.383);

        patchModel.train(trainingData, opencv_ml.ROW_SAMPLE, labels);

        ModelMetrics metrics = metricsCalculator.calculate(patchModel, featureList, labelList);
        metricsCalculator.save(metrics, "models/patch_metrics.json");
    }

    public int classify(File imageFile, boolean isPatch) {
        Mat image = ImageUtils.loadAndResizeImage(imageFile.getAbsolutePath());

        if (isPatch) {
            List<FeatureLabelPair> patches = patchExtractor.extract(imageFile, -1, true);
            Map<Integer, Integer> counts = new HashMap<>();

            for (FeatureLabelPair pair : patches) {
                float prediction = patchModel.predict(pair.features());
                counts.put((int) prediction, counts.getOrDefault((int) prediction, 0) + 1);
            }

            return counts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(-1);

        } else {
            Mat reshaped = image.reshape(1, 1);
            reshaped.convertTo(reshaped, opencv_core.CV_32F);
            return (int) fullImageModel.predict(reshaped);
        }
    }

    public float evaluate(List<File> images, List<Integer> labels, boolean isPatch) {
        List<Integer> predictions = new ArrayList<>();

        for (File image : images) {
            int pred = classify(image, isPatch);
            predictions.add(pred);
        }

        int correct = 0;
        for (int i = 0; i < labels.size(); i++) {
            if (Objects.equals(labels.get(i), predictions.get(i))) {
                correct++;
            }
        }

        return (float) correct / labels.size();
    }

    public Map<Integer, Integer> detectPatchPathologies(File imageFile) {
        List<FeatureLabelPair> patches = patchExtractor.extract(imageFile, -1, true);
        Map<Integer, Integer> pathologyCounts = new HashMap<>();

        for (FeatureLabelPair patch : patches) {
            float prediction = patchModel.predict(patch.features());
            pathologyCounts.put((int) prediction, pathologyCounts.getOrDefault((int) prediction, 0) + 1);
        }

        return pathologyCounts;
    }

    public void saveModel(String path, boolean isPatchModel) {
        if (isPatchModel) {
            patchModel.save(path);
        } else {
            fullImageModel.save(path);
        }
    }

    public double[][] getHeatmapData(String imageUrl, boolean fullImage) {
        if (!areModelsReady()) {
            throw new ApiException("SVM models are still loading. Please try again later.", HttpStatus.SERVICE_UNAVAILABLE);
        }

        try (InputStream in = URI.create(imageUrl).toURL().openStream()) {
            File tempFile = File.createTempFile("image_download_", ".png");
            java.nio.file.Files.copy(in, tempFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            double[][] result = getHeatmapData(tempFile, fullImage);
            tempFile.delete();
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Помилка при завантаженні зображення: " + imageUrl, e);
        }
    }

    public double[][] getHeatmapData(File imageFile, boolean isPatchModel) {
        Mat image = ImageUtils.loadAndResizeImage(imageFile.getAbsolutePath());
        List<FeatureLabelPair> patches = patchExtractor.extract(imageFile, -1, true);

        int rows = image.rows();
        int cols = image.cols();
        double[][] heatmap = new double[rows][cols];

        int patchSize = 64;
        int stepSize = 32;

        int patchIndex = 0;

        for (int y = 0; y <= rows - patchSize; y += stepSize) {
            for (int x = 0; x <= cols - patchSize; x += stepSize) {
                if (patchIndex >= patches.size()) {
                    continue;
                }

                FeatureLabelPair patch = patches.get(patchIndex++);
                float prediction = isPatchModel ?
                        patchModel.predict(patch.features()) :
                        fullImageModel.predict(patch.features());

                for (int dy = 0; dy < patchSize; dy++) {
                    for (int dx = 0; dx < patchSize; dx++) {
                        int yy = y + dy;
                        int xx = x + dx;
                        if (yy < rows && xx < cols) {
                            heatmap[yy][xx] = prediction;
                        }
                    }
                }
            }
        }

        return heatmap;
    }

    public SVM getModel(boolean isPatchModel) {
        return isPatchModel ? patchModel : fullImageModel;
    }
}