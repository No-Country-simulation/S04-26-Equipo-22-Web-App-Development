package com.nocountry.webapp.dto;

import com.nocountry.webapp.entity.enums.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequestDTO {
    
    private String email;

    private String password;

    private Role role;
    
}
