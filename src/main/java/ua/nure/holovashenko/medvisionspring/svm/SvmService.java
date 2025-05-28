package ua.nure.holovashenko.medvisionspring.svm;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.opencv.opencv_core.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ua.nure.holovashenko.medvisionspring.exception.ApiException;

import java.io.File;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SvmService {

    private final SvmModelManager modelManager;
    private final DatasetLoader datasetLoader;
    private final ImagePatchExtractor patchExtractor;
    private final MetricsCalculator metricsCalculator;
    private final HeatmapGenerator heatmapGenerator;
    private final ImageUtils imageUtils;

    public static final Map<Integer, String> CLASS_LABELS = Map.of(
            0, "Healthy — ознак патологій не виявлено, легені мають нормальну структуру.",
            1, "Other — виявлені ознаки, які не відповідають жодному з основних класів патологій.",
            2, "Pneumonia — запалення легень, часто викликане інфекцією, що супроводжується інфільтратами.",
            3, "Emphysema — хронічне захворювання легень, що викликає задишку через ушкодження альвеол.",
            4, "Fibrosis — рубцювання тканин легень, що ускладнює дихання та знижує об'єм легень."
    );

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void preloadModelsInBackground() {
        log.info("Starting async preload of SVM models...");
        modelManager.loadModels();
        log.info("SVM models preloaded successfully.");
    }

    public void trainFromDirectory(String datasetPath, boolean isPatchModel) {
        Dataset dataset = datasetLoader.loadDataset(datasetPath);
        if (isPatchModel) {
            modelManager.trainPatchModel(dataset);
        } else {
            modelManager.trainFullImageModel(dataset);
        }
    }

    public int classify(File imageFile, boolean isPatchModel) {
        ensureModelsReady();
        return modelManager.classify(imageFile, isPatchModel);
    }

    public float evaluate(List<File> images, List<Integer> labels, boolean isPatchModel) {
        ensureModelsReady();
        return modelManager.evaluate(images, labels, isPatchModel);
    }

    public Map<Integer, Integer> detectPathologyCounts(File imageFile) {
        return modelManager.detectPatchPathologies(imageFile);
    }

    public Mat generateHeatmap(File imageFile, boolean isPatchModel) {
        ensureModelsReady();
        Mat inputImage = ImageUtils.loadAndResizeImage(imageFile.getAbsolutePath());
        double[][] heatmapData = modelManager.getHeatmapData(imageFile, isPatchModel);
        return heatmapGenerator.generateHeatmap(inputImage, heatmapData);
    }

    public void saveModel(String path, boolean isPatchModel) {
        ensureModelsReady();
        modelManager.saveModel(path, isPatchModel);
    }

    public byte[] matToBytes(Mat mat, String format) {
        return imageUtils.matToBytes(mat, format);
    }

    public void saveMatToFile(Mat mat, File file) {
        imageUtils.saveMatToFile(mat, file);
    }

    public ModelMetrics loadMetrics(String path) {
        return metricsCalculator.loadMetricsFromAzure(path);
    }

    private void ensureModelsReady() {
        if (!modelManager.areModelsReady()) {
            throw new ApiException("SVM models are still loading. Please try again later.", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
