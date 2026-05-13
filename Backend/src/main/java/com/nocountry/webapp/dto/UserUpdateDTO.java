package com.nocountry.webapp.dto;

import com.nocountry.webapp.entity.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateDTO {
    
    @Email(message = "Debe proporcionar un email válido")
    private String email;
    
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;
    
    private Role role;
}