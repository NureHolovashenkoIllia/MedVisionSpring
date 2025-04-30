package ua.nure.holovashenko.medvisionspring.dto;

import lombok.Builder;
import lombok.Data;
import ua.nure.holovashenko.medvisionspring.enums.UserRole;

import java.time.LocalDate;

@Data
@Builder
public class PatientProfileResponse implements UserProfileResponse {
    private Long id;
    private String name;
    private String email;
    private UserRole role;

    private LocalDate birthDate;
    private String gender;
}