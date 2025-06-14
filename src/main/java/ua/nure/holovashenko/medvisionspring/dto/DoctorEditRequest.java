package ua.nure.holovashenko.medvisionspring.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DoctorEditRequest {

    @NotBlank(message = "Ім'я не може бути порожнім")
    private String name;

    @NotBlank(message = "Посада не може бути порожньою")
    private String position;

    @NotBlank(message = "Відділення не може бути порожнім")
    private String department;

    @NotBlank(message = "Номер ліцензії не може бути порожнім")
    private String licenseNumber;

    @NotBlank(message = "Освіта не може бути порожньою")
    private String education;

    @NotBlank(message = "Досягнення не можуть бути порожніми")
    private String achievements;

    @NotBlank(message = "Місце роботи не може бути порожнім")
    private String medicalInstitution;
}
