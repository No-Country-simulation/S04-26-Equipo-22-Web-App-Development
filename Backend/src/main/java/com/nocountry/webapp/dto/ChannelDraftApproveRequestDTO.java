package com.nocountry.webapp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelDraftApproveRequestDTO {
    @NotNull(message = "El ID del editor es obligatorio")
    private Long editorId;
}