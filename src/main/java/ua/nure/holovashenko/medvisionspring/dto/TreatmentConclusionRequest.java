package ua.nure.holovashenko.medvisionspring.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TreatmentConclusionRequest {
    @NotBlank(message = "Висновок лікування не може бути порожнім")
    private String conclusionText;

    private String recommendations;
}
