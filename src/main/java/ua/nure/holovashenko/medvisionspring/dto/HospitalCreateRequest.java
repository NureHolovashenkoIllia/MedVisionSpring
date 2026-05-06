package ua.nure.holovashenko.medvisionspring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class HospitalCreateRequest {
    @NotBlank(message = "Назва лікарні не може бути порожньою")
    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String legalName;

    @Size(max = 64)
    private String code;

    @Size(max = 500)
    private String address;

    @Size(max = 50)
    private String phone;

    @Size(max = 100)
    private String email;
}
