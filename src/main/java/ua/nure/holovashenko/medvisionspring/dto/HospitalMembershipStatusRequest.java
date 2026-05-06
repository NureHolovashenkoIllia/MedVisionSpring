package ua.nure.holovashenko.medvisionspring.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ua.nure.holovashenko.medvisionspring.enums.MembershipStatus;

@Data
public class HospitalMembershipStatusRequest {
    @NotNull
    private MembershipStatus status;
}
