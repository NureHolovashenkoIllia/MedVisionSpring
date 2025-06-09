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

    public static final Map<Integer, DiagnosisInfo> CLASS_LABELS = Map.of(
            0, new DiagnosisInfo(
                    "Структура легень у межах норми, відхилень не виявлено.",
                    "Healthy — ознак патологій не виявлено, легені мають нормальну структуру.",
                    "Повторна діагностика не потрібна. Рекомендується щорічне профілактичне обстеження."
            ),
            1, new DiagnosisInfo(
                    "Виявлені неоднозначні ознаки, які не класифікуються як конкретне захворювання.",
                    "Other — виявлені ознаки, які не відповідають жодному з основних класів патологій.",
                    "Рекомендується повторне обстеження або консультація з фахівцем."
            ),
            2, new DiagnosisInfo(
                    "Виявлені інфільтрати, типові для пневмонії, особливо в нижніх долях легень.",
                    "Pneumonia — запалення легень, часто викликане інфекцією, що супроводжується інфільтратами.",
                    "Рекомендується антибіотикотерапія, контрольна рентгенографія через 7–10 днів."
            ),
            3, new DiagnosisInfo(
                    "Спостерігається розширення альвеол, порушення структури легеневої тканини.",
                    "Emphysema — хронічне захворювання легень, що викликає задишку через ушкодження альвеол.",
                    "Необхідна консультація пульмонолога, підтримувальна терапія, припинення куріння."
            ),
            4, new DiagnosisInfo(
                    "Виявлено ділянки рубцювання та ущільнення тканин легень.",
                    "Fibrosis — рубцювання тканин легень, що ускладнює дихання та знижує об'єм легень.",
                    "Рекомендується звернення до пульмонолога, можливе призначення антифібротичної терапії."
            )
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
