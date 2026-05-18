package com.nocountry.webapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos del digest semanal para IA")
public class WeeklyDigestDataResponseDTO {
    
    @Schema(description = "Posts más reaccionados de la semana")
    private List<CommunityPostResponseDTO> topReactedPosts;
    
    @Schema(description = "Posts más comentados de la semana")
    private List<CommunityPostResponseDTO> topCommentedPosts;
    
    @Schema(description = "Preguntas más respondidas de la semana")
    private List<CommunityPostResponseDTO> mostAnsweredQuestions;
    
    @Schema(description = "Recursos más compartidos de la semana")
    private List<CommunityPostResponseDTO> topResources;
    
    @Schema(description = "Sesiones de la semana")
    private List<CommunityPostResponseDTO> weeklySessions;
    
    @Schema(description = "Discusiones de la semana")
    private List<CommunityPostResponseDTO> weeklyDiscussions;
    
    @Schema(description = "Total de posts en la semana", example = "150")
    private Integer totalPosts;
    
    @Schema(description = "Inicio de la semana", example = "2024-01-15T00:00:00")
    private LocalDateTime weekStart;
    
    @Schema(description = "Fin de la semana", example = "2024-01-21T23:59:59")
    private LocalDateTime weekEnd;
}