package ua.nure.holovashenko.medvisionspring.dto;

import lombok.Data;

@Data
public class ComparisonReport {
    private Long fromId;
    private Long toId;

    private String analysisDetailsFrom;
    private String analysisDetailsTo;

    private String diagnosisTextFrom;
    private String diagnosisTextTo;

    private String treatmentRecommendationsFrom;
    private String treatmentRecommendationsTo;

    private int diagnosisClassFrom;
    private int diagnosisClassTo;

    private String fromImageBase64;
    private String toImageBase64;
    private String diffHeatmap; // base64-encoded PNG

    private Float accuracyFrom;
    private Float accuracyTo;

    private Float precisionFrom;
    private Float precisionTo;

    private Float recallFrom;
    private Float recallTo;

    private String createdAtFrom;
    private String createdAtTo;
}
