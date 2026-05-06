package ua.nure.holovashenko.medvisionspring.dto;

import lombok.Builder;
import lombok.Data;
import ua.nure.holovashenko.medvisionspring.enums.DatasetSplit;

@Data
@Builder
public class SvmModelMetricResponse {
    private Long metricId;
    private Long modelVersionId;
    private DatasetSplit datasetSplit;
    private String classLabel;
    private String metricName;
    private Double metricValue;
}
