package com.nocountry.webapp.dto;

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
@Schema(description = "Estadísticas semanales para el editor")
public class WeeklyStatisticsResponseDTO {
    
    @Schema(description = "Total de posts en la semana", example = "150")
    private Integer totalPosts;
    
    @Schema(description = "Total de preguntas", example = "80")
    private Long totalQuestions;
    
    @Schema(description = "Total de recursos", example = "30")
    private Long totalResources;
    
    @Schema(description = "Total de sesiones", example = "20")
    private Long totalSessions;
    
    @Schema(description = "Total de discusiones", example = "20")
    private Long totalDiscussions;
    
    @Schema(description = "Total de reacciones", example = "450")
    private Integer totalReactions;
    
    @Schema(description = "Total de comentarios", example = "320")
    private Integer totalComments;
    
    @Schema(description = "Inicio de la semana", example = "2024-01-15T00:00:00")
    private LocalDateTime weekStart;
    
    @Schema(description = "Fin de la semana", example = "2024-01-21T23:59:59")
    private LocalDateTime weekEnd;
}