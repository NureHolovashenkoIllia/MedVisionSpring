package ua.nure.holovashenko.medvisionspring.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnalysisComparisonCreateRequest {
    @NotNull
    private Long fromAnalysisId;

    @NotNull
    private Long toAnalysisId;

    private String doctorNotes;
}
