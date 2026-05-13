package com.nocountry.webapp.dto;

import com.nocountry.webapp.entity.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRoleUpdateDTO {
    
    @NotNull(message = "El rol es obligatorio")
    private Role role;
}