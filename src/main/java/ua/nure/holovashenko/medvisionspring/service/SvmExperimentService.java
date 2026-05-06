package ua.nure.holovashenko.medvisionspring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.nure.holovashenko.medvisionspring.dto.SvmExperimentRequest;
import ua.nure.holovashenko.medvisionspring.dto.SvmExperimentResponse;
import ua.nure.holovashenko.medvisionspring.svm.Dataset;
import ua.nure.holovashenko.medvisionspring.svm.DatasetLoader;
import ua.nure.holovashenko.medvisionspring.svm.DatasetSplitResult;

@Service
@RequiredArgsConstructor
public class SvmExperimentService {

    private final DatasetLoader datasetLoader;
    private final DatasetSplitService datasetSplitService;

    public SvmExperimentResponse previewSplit(SvmExperimentRequest request) {
        Dataset dataset = datasetLoader.loadDataset(request.getDatasetPath());
        DatasetSplitResult split = datasetSplitService.stratifiedSplit(
                dataset,
                request.getTrainRatio(),
                request.getValidationRatio(),
                request.getTestRatio(),
                request.getSeed()
        );

        return SvmExperimentResponse.builder()
                .status("SPLIT_PREVIEW")
                .trainCount(split.trainImages().size())
                .validationCount(split.validationImages().size())
                .testCount(split.testImages().size())
                .labelMap(split.labelMap())
                .trainClassCounts(datasetSplitService.countByClass(split.trainLabels()))
                .validationClassCounts(datasetSplitService.countByClass(split.validationLabels()))
                .testClassCounts(datasetSplitService.countByClass(split.testLabels()))
                .build();
    }
}
