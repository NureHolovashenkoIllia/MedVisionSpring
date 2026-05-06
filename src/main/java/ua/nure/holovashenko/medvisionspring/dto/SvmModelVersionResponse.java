package ua.nure.holovashenko.medvisionspring.dto;

import lombok.Builder;
import lombok.Data;
import ua.nure.holovashenko.medvisionspring.enums.*;

import java.time.LocalDateTime;

@Data
@Builder
public class SvmModelVersionResponse {
    private Long modelVersionId;
    private String name;
    private String version;
    private SvmModelType modelType;
    private SvmKernelType kernelType;
    private SvmMulticlassStrategy multiclassStrategy;
    private String featureSet;
    private Integer imageWidth;
    private Integer imageHeight;
    private Integer patchSize;
    private Integer stepSize;
    private String datasetName;
    private String storageUri;
    private String metricsUri;
    private SvmModelStatus status;
    private LocalDateTime trainedAt;
    private LocalDateTime activatedAt;
}
