package com.nocountry.webapp.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthResponseDTO {

    private String accessToken;

    private String refreshToken;
}
