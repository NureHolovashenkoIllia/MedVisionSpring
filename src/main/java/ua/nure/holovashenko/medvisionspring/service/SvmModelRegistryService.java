package ua.nure.holovashenko.medvisionspring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ua.nure.holovashenko.medvisionspring.dto.SvmModelMetricResponse;
import ua.nure.holovashenko.medvisionspring.dto.SvmModelVersionResponse;
import ua.nure.holovashenko.medvisionspring.entity.SvmModelMetric;
import ua.nure.holovashenko.medvisionspring.entity.SvmModelVersion;
import ua.nure.holovashenko.medvisionspring.enums.DatasetSplit;
import ua.nure.holovashenko.medvisionspring.enums.SvmModelStatus;
import ua.nure.holovashenko.medvisionspring.enums.SvmModelType;
import ua.nure.holovashenko.medvisionspring.exception.ApiException;
import ua.nure.holovashenko.medvisionspring.repository.SvmModelMetricRepository;
import ua.nure.holovashenko.medvisionspring.repository.SvmModelVersionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SvmModelRegistryService {

    private final SvmModelVersionRepository modelVersionRepository;
    private final SvmModelMetricRepository metricRepository;

    public SvmModelVersion getActiveFullImageModel() {
        return modelVersionRepository.findFirstByModelTypeAndStatusOrderByActivatedAtDesc(
                        SvmModelType.FULL_IMAGE,
                        SvmModelStatus.ACTIVE
                )
                .orElseThrow(() -> new ApiException("Активну SVM-модель не знайдено", HttpStatus.INTERNAL_SERVER_ERROR));
    }

    public List<SvmModelVersionResponse> getModels() {
        return modelVersionRepository.findAll().stream()
                .map(this::mapModel)
                .toList();
    }

    public List<SvmModelMetricResponse> getMetrics(Long modelVersionId, DatasetSplit split) {
        SvmModelVersion modelVersion = modelVersionRepository.findById(modelVersionId)
                .orElseThrow(() -> new ApiException("Версію SVM-моделі не знайдено", HttpStatus.NOT_FOUND));

        List<SvmModelMetric> metrics = split == null
                ? metricRepository.findAllByModelVersion(modelVersion)
                : metricRepository.findAllByModelVersionAndDatasetSplit(modelVersion, split);

        return metrics.stream()
                .map(this::mapMetric)
                .toList();
    }

    private SvmModelVersionResponse mapModel(SvmModelVersion model) {
        return SvmModelVersionResponse.builder()
                .modelVersionId(model.getModelVersionId())
                .name(model.getName())
                .version(model.getVersion())
                .modelType(model.getModelType())
                .kernelType(model.getKernelType())
                .multiclassStrategy(model.getMulticlassStrategy())
                .featureSet(model.getFeatureSet())
                .imageWidth(model.getImageWidth())
                .imageHeight(model.getImageHeight())
                .patchSize(model.getPatchSize())
                .stepSize(model.getStepSize())
                .datasetName(model.getDatasetName())
                .storageUri(model.getStorageUri())
                .metricsUri(model.getMetricsUri())
                .status(model.getStatus())
                .trainedAt(model.getTrainedAt())
                .activatedAt(model.getActivatedAt())
                .build();
    }

    private SvmModelMetricResponse mapMetric(SvmModelMetric metric) {
        return SvmModelMetricResponse.builder()
                .metricId(metric.getMetricId())
                .modelVersionId(metric.getModelVersion().getModelVersionId())
                .datasetSplit(metric.getDatasetSplit())
                .classLabel(metric.getClassLabel())
                .metricName(metric.getMetricName())
                .metricValue(metric.getMetricValue())
                .build();
    }
}
