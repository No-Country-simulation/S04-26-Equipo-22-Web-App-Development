package com.nocountry.webapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelDraftUpdateContentRequestDTO {
    @NotBlank(message = "El contenido no puede estar vacío")
    private String content;
}
