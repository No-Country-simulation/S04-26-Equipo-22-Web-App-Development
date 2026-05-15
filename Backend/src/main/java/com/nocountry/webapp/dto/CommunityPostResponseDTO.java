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
@Schema(description = "DTO para respuesta de posts de comunidad")
public class CommunityPostResponseDTO {
    
    @Schema(description = "ID del post", example = "1")
    private Long id;
    
    @Schema(description = "Contenido del post", example = "¿Cómo puedo mejorar mis habilidades en Java?")
    private String content;
    
    @Schema(description = "Nombre del autor", example = "Juan Pérez")
    private String authorName;
    
    @Schema(description = "Tipo de post", example = "QUESTION")
    private CommunityPostType type;
    
    @Schema(description = "Cantidad de reacciones", example = "25")
    private Integer reactionsCount;
    
    @Schema(description = "Cantidad de comentarios", example = "10")
    private Integer commentsCount;
    
    @Schema(description = "ID externo del post", example = "reddit_post_12345")
    private String externalPostId;
    
    @Schema(description = "URL de la fuente original", example = "https://reddit.com/r/java/post/123")
    private String sourceUrl;
    
    @Schema(description = "Fecha de recolección", example = "2024-01-15T10:30:00")
    private LocalDateTime collectedAt;
    
    @Schema(description = "ID de la comunidad", example = "5")
    private Long communityId;
}