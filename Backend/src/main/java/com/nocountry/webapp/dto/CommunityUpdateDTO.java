package com.nocountry.webapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para actualizar una comunidad")
public class CommunityUpdateDTO{

    @Schema(description = "Nombre de la comunidad", example = "Java Developers Argentina", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El nombre de la comunidad es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
    private String name;

    @Schema(description = "Plataforma de la comunidad", example = "Discord", allowableValues = {"Discord", "Telegram", "Slack", "WhatsApp", "Otro"})
    @Size(max = 50, message = "La plataforma no puede superar los 50 caracteres")
    private String platform;
}
