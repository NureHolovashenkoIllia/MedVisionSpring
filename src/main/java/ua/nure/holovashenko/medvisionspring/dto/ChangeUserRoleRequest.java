package ua.nure.holovashenko.medvisionspring.dto;

import lombok.Data;
import ua.nure.holovashenko.medvisionspring.enums.UserRole;

@Data
public class ChangeUserRoleRequest {
    private UserRole newRole;
}
