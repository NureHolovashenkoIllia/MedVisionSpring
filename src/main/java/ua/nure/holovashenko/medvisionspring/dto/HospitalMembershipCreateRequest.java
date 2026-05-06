package ua.nure.holovashenko.medvisionspring.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ua.nure.holovashenko.medvisionspring.enums.HospitalRole;

@Data
public class HospitalMembershipCreateRequest {
    @NotNull
    private Long userId;

    @NotNull
    private HospitalRole hospitalRole;
}
