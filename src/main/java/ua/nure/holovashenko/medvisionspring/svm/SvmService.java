package ua.nure.holovashenko.medvisionspring.svm;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.bytedeco.opencv.opencv_core.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;

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

    @PostConstruct
    public void init() {
        modelManager.loadModels();
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
        return modelManager.classify(imageFile, isPatchModel);
    }

    public float evaluate(List<File> images, List<Integer> labels, boolean isPatchModel) {
        return modelManager.evaluate(images, labels, isPatchModel);
    }

    public Map<Integer, Integer> detectPathologyCounts(File imageFile) {
        return modelManager.detectPatchPathologies(imageFile);
    }

    public Mat generateHeatmap(File imageFile, boolean isPatchModel) {
        Mat inputImage = ImageUtils.loadAndResizeImage(imageFile.getAbsolutePath());
        double[][] heatmapData = modelManager.getHeatmapData(imageFile, isPatchModel);
        return heatmapGenerator.generateHeatmap(inputImage, heatmapData);
    }

    public void saveModel(String path, boolean isPatchModel) {
        modelManager.saveModel(path, isPatchModel);
    }

    public byte[] matToBytes(Mat mat, String format) {
        return imageUtils.matToBytes(mat, format);
    }

    public void saveMatToFile(Mat mat, File file) {
        imageUtils.saveMatToFile(mat, file);
    }

    public ModelMetrics loadMetrics(String path) {
        return metricsCalculator.loadMetricsFromGcs(path);
    }
}
