package ua.nure.holovashenko.medvisionspring.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActiveHospitalResponse {
    private Long hospitalId;
    private String name;
    private boolean selected;
}
