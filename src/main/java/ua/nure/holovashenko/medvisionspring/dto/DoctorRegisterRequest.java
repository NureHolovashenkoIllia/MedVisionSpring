package ua.nure.holovashenko.medvisionspring.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DoctorRegisterRequest {

    @NotBlank(message = "Ім'я не може бути порожнім")
    private String name;

    @NotBlank(message = "Email не може бути порожнім")
    @Email(message = "Невірний формат email")
    private String email;

    @NotBlank(message = "Пароль не може бути порожнім")
    @Size(min = 8, message = "Пароль повинен містити щонайменше 8 символів")
    private String password;

    private String position;
    private String department;
    private String licenseNumber;
    private String medicalInstitution;
}