package com.nocountry.webapp.dto;

import com.nocountry.webapp.entity.enums.CommunityPostType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para filtrado de posts de comunidad")
public class CommunityPostFilterDTO {
    
    @Schema(description = "ID de la comunidad (opcional)", example = "5")
    private Long communityId;
    
    @Schema(description = "Tipo de post (opcional)", example = "QUESTION")
    private CommunityPostType type;
    
    @Schema(description = "Fecha de inicio del rango (YYYY-MM-DDTHH:MM:SS)", example = "2024-01-15T00:00:00")
    private LocalDateTime startDate;
    
    @Schema(description = "Fecha de fin del rango (YYYY-MM-DDTHH:MM:SS)", example = "2024-01-21T23:59:59")
    private LocalDateTime endDate;
    
    @Schema(description = "Límite de resultados (opcional, default 10)", example = "20")
    private Integer limit;
}