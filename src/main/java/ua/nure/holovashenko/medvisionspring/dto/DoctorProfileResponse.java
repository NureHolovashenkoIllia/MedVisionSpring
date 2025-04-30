package ua.nure.holovashenko.medvisionspring.dto;

import lombok.Builder;
import lombok.Data;
import ua.nure.holovashenko.medvisionspring.enums.UserRole;

@Data
@Builder
public class DoctorProfileResponse implements UserProfileResponse {
    private Long id;
    private String name;
    private String email;
    private UserRole role;

    private String position;
    private String department;
    private String licenseNumber;
}
