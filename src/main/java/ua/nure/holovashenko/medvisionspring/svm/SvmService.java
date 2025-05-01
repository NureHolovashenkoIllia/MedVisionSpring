package ua.nure.holovashenko.medvisionspring.svm;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_ml;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_ml.SVM;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imencode;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;
import static ua.nure.holovashenko.medvisionspring.svm.ImageProcessor.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

@Service
public class SvmService {

    private static final int FULL_IMAGE_FEATURES = 128 * 128;
    private static final int PATCH_FEATURES = 32 * 32;

    private SVM svmFullImage;
    private SVM svmPatch;

    public SvmService() {
        initModels();
    }

    private void initModels() {
        svmFullImage = createSvmInstance();
        svmPatch = createSvmInstance();
    }

    private SVM createSvmInstance() {
        SVM model = SVM.create();
        model.setKernel(SVM.LINEAR);
        model.setType(SVM.C_SVC);
        model.setTermCriteria(new TermCriteria(TermCriteria.EPS + TermCriteria.MAX_ITER, 1000, 1e-6));
        return model;
    }

    @PostConstruct
    public void loadModelsOnStartup() {
        loadModel("model/svm-full.xml", false);
        loadModel("model/svm-patch.xml", true);
    }

    public void loadModel(String path, boolean isPatchModel) {
        File file = new File(path);
        if (!file.exists()) {
            System.err.println("Модель не знайдена: " + path);
            return;
        }
        if (isPatchModel) {
            svmPatch = SVM.load(path);
        } else {
            svmFullImage = SVM.load(path);
        }
    }

    public void train(List<File> imageFiles, List<Integer> labels) {
        trainModel(imageFiles, labels, false);
    }

    public void trainOnPatches(List<File> imageFiles, List<Integer> labels) {
        trainModel(imageFiles, labels, true);
    }

    private void trainModel(List<File> imageFiles, List<Integer> labels, boolean isPatchModel) {
        if (imageFiles.size() != labels.size()) {
            throw new IllegalArgumentException("Кількість зображень і міток повинна співпадати.");
        }

        List<Mat> featureList = new ArrayList<>();
        List<Integer> labelList = new ArrayList<>();

        int windowSize = isPatchModel ? 32 : 128;
        int stride = isPatchModel ? 16 : 128;
        SVM model = isPatchModel ? svmPatch : svmFullImage;
        int expectedFeatures = windowSize * windowSize;

        for (int i = 0; i < imageFiles.size(); i++) {
            Mat image = preprocessImage(imageFiles.get(i));
            int label = labels.get(i);

            for (int y = 0; y <= image.rows() - windowSize; y += stride) {
                for (int x = 0; x <= image.cols() - windowSize; x += stride) {
                    Rect roi = new Rect(x, y, windowSize, windowSize);
                    Mat patch = new Mat(image, roi);
                    Mat flat = flattenImage(patch);
                    flat.convertTo(flat, opencv_core.CV_32F);
                    featureList.add(flat);
                    labelList.add(label);

                    if (!isPatchModel) break;
                }
                if (!isPatchModel) break;
            }
        }

        int numSamples = featureList.size();
        Mat trainData = new Mat(numSamples, expectedFeatures, opencv_core.CV_32F);
        Mat labelsMat = new Mat(numSamples, 1, opencv_core.CV_32S);

        for (int i = 0; i < numSamples; i++) {
            featureList.get(i).copyTo(trainData.row(i));
            labelsMat.ptr(i).putInt(labelList.get(i));
        }

        boolean success = model.train(trainData, opencv_ml.ROW_SAMPLE, labelsMat);
        if (!success) throw new RuntimeException("Не вдалося навчити SVM.");

        float[] metrics = computeMetrics(model, featureList, labelList);
        System.out.printf("Метрики %s-моделі: Accuracy=%.3f, Precision=%.3f, Recall=%.3f%n",
                isPatchModel ? "patch" : "full",
                metrics[0], metrics[1], metrics[2]);

        saveMetrics(isPatchModel ? "model/metrics-patch.json" : "model/metrics.json",
                new ModelMetrics(metrics[0], metrics[1], metrics[2]));
    }

    private float[] computeMetrics(SVM model, List<Mat> features, List<Integer> labels) {
        int tp = 0, tn = 0, fp = 0, fn = 0;
        for (int i = 0; i < features.size(); i++) {
            int trueLabel = labels.get(i);
            int predicted = (int) model.predict(features.get(i));
            if (predicted == 1 && trueLabel == 1) tp++;
            else if (predicted == 0 && trueLabel == 0) tn++;
            else if (predicted == 1 && trueLabel == 0) fp++;
            else if (predicted == 0 && trueLabel == 1) fn++;
        }
        float accuracy = (tp + tn) / (float) (tp + tn + fp + fn);
        float precision = tp + fp == 0 ? 0f : tp / (float) (tp + fp);
        float recall = tp + fn == 0 ? 0f : tp / (float) (tp + fn);
        return new float[]{accuracy, precision, recall};
    }

    public int classify(File imageFile, boolean isPatchModel) {
        SVM model = isPatchModel ? svmPatch : svmFullImage;
        ensureModelReady(model);
        Mat feature = extractFeatureVector(imageFile, isPatchModel ? PATCH_FEATURES : FULL_IMAGE_FEATURES);
        return (int) model.predict(feature);
    }

    public float evaluate(List<File> imageFiles, List<Integer> labels, boolean isPatchModel) {
        if (isPatchModel) return evaluatePatchModel(imageFiles, labels);
        SVM model = svmFullImage;
        ensureModelReady(model);

        int correct = 0;
        for (int i = 0; i < imageFiles.size(); i++) {
            int predicted = classify(imageFiles.get(i), false);
            if (predicted == labels.get(i)) correct++;
        }
        return correct / (float) imageFiles.size();
    }

    public float evaluatePatchModel(List<File> imageFiles, List<Integer> labels) {
        ensureModelReady(svmPatch);
        int correct = 0;
        for (int i = 0; i < imageFiles.size(); i++) {
            int predicted = classifyByMajorityVoting(imageFiles.get(i));
            if (predicted == labels.get(i)) correct++;
        }
        return correct / (float) imageFiles.size();
    }

    private int classifyByMajorityVoting(File imageFile) {
        Mat image = preprocessImage(imageFile);
        int positiveVotes = 0, totalVotes = 0;

        for (int y = 0; y <= image.rows() - 32; y += 16) {
            for (int x = 0; x <= image.cols() - 32; x += 16) {
                Rect roi = new Rect(x, y, 32, 32);
                Mat patch = new Mat(image, roi);
                Mat feature = flattenImage(patch);
                feature.convertTo(feature, opencv_core.CV_32F);
                if (feature.cols() != PATCH_FEATURES) continue;
                if (svmPatch.predict(feature) == 1.0f) positiveVotes++;
                totalVotes++;
            }
        }
        if (totalVotes == 0) return 0;
        return (positiveVotes / (float) totalVotes) > 0.5f ? 1 : 0;
    }

    public Mat generateHeatmap(File imageFile, boolean isPatchModel) {
        SVM model = isPatchModel ? svmPatch : svmFullImage;
        ensureModelReady(model);

        Mat gray = preprocessImage(imageFile); // grayscale CV_32F normalized
        Mat heatmap = new Mat(gray.size(), opencv_core.CV_32F, new Scalar(0));

        int windowSize = model.getVarCount() == PATCH_FEATURES ? 32 : 128;
        int stride = windowSize == 32 ? 8 : 128;

        for (int y = 0; y <= gray.rows() - windowSize; y += stride) {
            for (int x = 0; x <= gray.cols() - windowSize; x += stride) {
                Rect roi = new Rect(x, y, windowSize, windowSize);
                Mat patch = new Mat(gray, roi);
                Mat feature = flattenImage(patch);
                feature.convertTo(feature, opencv_core.CV_32F);

                if (feature.cols() != model.getVarCount()) continue;

                float prediction = model.predict(feature, new Mat(), SVM.RAW_OUTPUT);
                float score = Math.min(Math.abs(prediction) * 10f, 100f);

                for (int dy = 0; dy < windowSize; dy++) {
                    for (int dx = 0; dx < windowSize; dx++) {
                        if (y + dy < heatmap.rows() && x + dx < heatmap.cols()) {
                            float current = heatmap.ptr(y + dy, x + dx).getFloat();
                            heatmap.ptr(y + dy, x + dx).putFloat(current + score);
                        }
                    }
                }
            }
        }
        DoublePointer minVal = new DoublePointer(1);
        DoublePointer maxVal = new DoublePointer(1);
        opencv_core.minMaxLoc(heatmap, minVal, maxVal, null, null, new Mat());
        System.out.println("Heatmap min/max: " + minVal.get() + " / " + maxVal.get());

        // Нормалізуємо до [0, 255] (CV_8U)
        Mat heatmap8U = new Mat();
        opencv_core.normalize(heatmap, heatmap8U, 0.0, 255.0, opencv_core.NORM_MINMAX, opencv_core.CV_8U, null);

        // Перетворення в кольорову карту
        Mat colorMap = new Mat();
        applyColorMap(heatmap8U, colorMap, COLORMAP_JET);

        // Перетворюємо grayscale -> BGR
        Mat gray8U = new Mat();
        gray.convertTo(gray8U, opencv_core.CV_8U, 255.0, 0.0);
        Mat originalBGR = new Mat();
        cvtColor(gray8U, originalBGR, COLOR_GRAY2BGR);

        // Перевіримо типи
        if (colorMap.type() != originalBGR.type())
            colorMap.convertTo(colorMap, originalBGR.type());

        // Накладення: більше 0.7 для оригіналу, менше для теплової карти
        Mat blended = new Mat();
        opencv_core.addWeighted(originalBGR, 0.5, colorMap, 0.5, 0.0, blended);

        return blended;
    }

    public byte[] matToBytes(Mat mat) {
        BytePointer buf = new BytePointer();
        imencode(".png", mat, buf);
        byte[] bytes = new byte[(int) buf.limit()];
        buf.get(bytes);
        return bytes;
    }

    public void saveMatToFile(Mat mat, File file) {
        if (!imwrite(file.getAbsolutePath(), mat)) {
            throw new RuntimeException("Не вдалося зберегти зображення: " + file.getAbsolutePath());
        }
    }

    public void saveModel(String path, boolean isPatchModel) {
        (isPatchModel ? svmPatch : svmFullImage).save(path);
    }

    private void saveMetrics(String path, ModelMetrics metrics) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(new File(path), metrics);
        } catch (IOException e) {
            throw new RuntimeException("Не вдалося зберегти метрики моделі", e);
        }
    }

    public ModelMetrics loadMetrics(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) throw new IOException("Файл не знайдено: " + path);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(file, ModelMetrics.class);
        } catch (IOException e) {
            throw new RuntimeException("Не вдалося завантажити метрики моделі: " + e.getMessage(), e);
        }
    }

    private Mat extractFeatureVector(File imageFile, int expectedFeatureCount) {
        Mat processed = preprocessImage(imageFile);
        Mat flat = flattenImage(processed);
        flat.convertTo(flat, opencv_core.CV_32F);
        if (flat.cols() != expectedFeatureCount) {
            throw new IllegalArgumentException("Неправильна кількість ознак: " + flat.cols());
        }
        return flat;
    }

    private void ensureModelReady(SVM model) {
        if (model == null || model.getVarCount() == 0) {
            throw new IllegalStateException("SVM модель не готова.");
        }
    }
}
