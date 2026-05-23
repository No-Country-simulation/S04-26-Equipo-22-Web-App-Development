package com.nocountry.webapp.dto;

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
public class ChannelDraftCompleteRequestDTO {

    @NotNull(message = "El ID del digest es obligatorio")
    private Long weeklyDigestId;

    @NotBlank(message = "El contenido para NEWSLETTER es obligatorio")
    private String newsletterContent;

    @NotBlank(message = "El contenido para LINKEDIN es obligatorio")
    private String linkedinContent;

    @NotBlank(message = "El contenido para X es obligatorio")
    private String twitterContent;

    @NotNull(message = "El ID del editor es obligatorio")
    private Long editorId;
}