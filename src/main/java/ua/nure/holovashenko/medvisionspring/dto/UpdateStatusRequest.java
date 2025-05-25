package ua.nure.holovashenko.medvisionspring.dto;

import lombok.Data;
import ua.nure.holovashenko.medvisionspring.enums.AnalysisStatus;

@Data
public class UpdateStatusRequest {
    private AnalysisStatus status;
}