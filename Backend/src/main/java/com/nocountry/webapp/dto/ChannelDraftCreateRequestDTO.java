package com.nocountry.webapp.dto;

import com.nocountry.webapp.entity.enums.TargetPlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelDraftCreateRequestDTO {
    @NotNull(message = "El ID del digest es obligatorio")
    private Long weeklyDigestId;

    @NotBlank(message = "El contenido no puede estar vacío")
    private String content;

    @NotNull(message = "La plataforma destino es obligatoria")
    private TargetPlatform platform;

    @NotNull(message = "El ID del editor es obligatorio")
    private Long editorId;
}