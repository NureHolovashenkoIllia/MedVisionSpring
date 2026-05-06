package ua.nure.holovashenko.medvisionspring.svm;

import lombok.RequiredArgsConstructor;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import static ua.nure.holovashenko.medvisionspring.svm.SvmService.CLASS_LABELS;

@Component
@RequiredArgsConstructor
public class LocalSvmClient implements SvmClient {

    private final SvmService svmService;

    @Override
    public SvmClassificationResult classify(SvmClassificationRequest request) {
        int prediction = svmService.classify(request.imageFile(), false);
        ModelMetrics metrics = svmService.loadMetrics("svm-models/full_metrics.json");
        MetricsCalculator.ClassMetrics classMetrics = metrics.perClassMetrics().get(prediction);
        DiagnosisInfo diagnosisInfo = CLASS_LABELS.getOrDefault(prediction, new DiagnosisInfo());
        Mat heatmap = request.generateHeatmap()
                ? svmService.generateHeatmap(request.imageFile(), true)
                : null;

        return new SvmClassificationResult(prediction, diagnosisInfo, metrics, classMetrics, heatmap);
    }
}
