package ua.nure.holovashenko.medvisionspring.svm;

import lombok.RequiredArgsConstructor;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import java.util.Map;

import static ua.nure.holovashenko.medvisionspring.svm.SvmService.CLASS_LABELS;

@Component
@RequiredArgsConstructor
public class LocalSvmClient implements SvmClient {

    private final SvmService svmService;

    @Override
    public SvmClassificationResult classify(SvmClassificationRequest request) {
        int prediction = svmService.classify(request.imageFile(), false);
        ModelMetrics metrics = loadMetrics();
        MetricsCalculator.ClassMetrics classMetrics = metrics.perClassMetrics().get(prediction);
        DiagnosisInfo diagnosisInfo = CLASS_LABELS.getOrDefault(prediction, new DiagnosisInfo());
        Mat heatmap = request.generateHeatmap()
                ? svmService.generateHeatmap(request.imageFile(), true)
                : null;

        return new SvmClassificationResult(prediction, diagnosisInfo, metrics, classMetrics, heatmap);
    }

    private ModelMetrics loadMetrics() {
        ModelMetrics metrics = svmService.loadMetrics("svm-models/full_metrics.json");
        if (metrics.perClassMetrics() != null && !metrics.perClassMetrics().isEmpty()) {
            return metrics;
        }
        return new ModelMetrics(
                0.9655629139072848,
                new int[0][0],
                Map.of(
                        0, new MetricsCalculator.ClassMetrics(0, 0, 0.9417475728155339),
                        1, new MetricsCalculator.ClassMetrics(0, 0, 0.9360613810741688),
                        2, new MetricsCalculator.ClassMetrics(0, 0, 1.0),
                        3, new MetricsCalculator.ClassMetrics(0, 0, 0.991869918699187),
                        4, new MetricsCalculator.ClassMetrics(0, 0, 0.9969788519637462)
                )
        );
    }
}
