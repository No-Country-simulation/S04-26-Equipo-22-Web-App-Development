package com.nocountry.webapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para responder con datos de comunidad")
public class CommunityResponseDTO {

    @Schema(description = "ID de la comunidad", example = "1")
    private Long id;

    @Schema(description = "Nombre de la comunidad", example = "Java Developers Argentina")
    private String name;
    
    @Schema(description = "Plataforma de la comunidad", example = "Discord")
    private String platform;

    @Schema(description = "Estado activo de la comunidad", example = "true")
    private boolean active;
}