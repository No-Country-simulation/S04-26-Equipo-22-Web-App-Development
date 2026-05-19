package com.nocountry.webapp.dto;

import com.nocountry.webapp.entity.enums.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponseDTO {
    
    private Long id;
    
    private String email;
    
    private Role role;
}