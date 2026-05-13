package com.nocountry.webapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserEmailUpdateDTO {
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe proporcionar un email válido")
    private String email;
}
