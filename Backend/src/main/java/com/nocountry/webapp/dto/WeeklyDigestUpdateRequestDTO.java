package com.nocountry.webapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyDigestUpdateRequestDTO {

    @NotNull(message = "El summary no puede ser nulo")
    @Size(min = 10, max = 5000, message = "El summary debe tener entre 10 y 5000 caracteres")
    private String summary;
}
