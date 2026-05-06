package ua.nure.holovashenko.medvisionspring.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TreatmentReportResponse {
    private TreatmentResponse treatment;
    private List<ImageAnalysisResponse> analyses;
    private List<AnalysisComparisonResponse> comparisons;
    private TreatmentConclusionResponse conclusion;
}
