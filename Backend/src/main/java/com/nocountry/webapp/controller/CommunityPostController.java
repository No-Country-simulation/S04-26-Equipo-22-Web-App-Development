package com.nocountry.webapp.controller;

import com.nocountry.webapp.dto.CommunityPostFilterDTO;
import com.nocountry.webapp.dto.CommunityPostResponseDTO;
import com.nocountry.webapp.dto.WeeklyDigestDataResponseDTO;
import com.nocountry.webapp.dto.WeeklyStatisticsResponseDTO;
import com.nocountry.webapp.analytics.WeeklyDigestData;
import com.nocountry.webapp.analytics.WeeklyStatistics;
import com.nocountry.webapp.entity.CommunityPost;
import com.nocountry.webapp.entity.enums.CommunityPostType;
import com.nocountry.webapp.service.CommunityPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/community-posts")
@RequiredArgsConstructor
@Tag(name = "Community Posts", description = "Endpoints para consulta de posts de comunidad")
public class CommunityPostController {

    private final CommunityPostService communityPostService;

    // ==================== ENDPOINTS PRINCIPALES ====================

    @GetMapping("/weekly")
    @Operation(summary = "Obtener todos los posts de la semana actual", 
               description = "Retorna todos los posts recolectados durante la semana actual")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Posts obtenidos exitosamente",
                     content = @Content(mediaType = "application/json", 
                     array = @ArraySchema(schema = @Schema(implementation = CommunityPostResponseDTO.class)))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    public ResponseEntity<List<CommunityPostResponseDTO>> getWeeklyPosts() {
        List<CommunityPost> posts = communityPostService.getWeeklyPosts();
        List<CommunityPostResponseDTO> response = posts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/weekly/top-reacted")
    @Operation(summary = "Obtener posts más reaccionados de la semana", 
               description = "Retorna el top N de posts con más reacciones de la semana actual")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Posts obtenidos exitosamente",
                     content = @Content(mediaType = "application/json", 
                     array = @ArraySchema(schema = @Schema(implementation = CommunityPostResponseDTO.class)))),
        @ApiResponse(responseCode = "400", description = "Límite inválido", content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    public ResponseEntity<List<CommunityPostResponseDTO>> getTopReactedPosts(
            @Parameter(description = "Número máximo de posts a retornar", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        List<CommunityPost> posts = communityPostService.getTopReactedPostsOfWeek(limit);
        List<CommunityPostResponseDTO> response = posts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/weekly/top-commented")
    @Operation(summary = "Obtener posts más comentados de la semana", 
               description = "Retorna el top N de posts con más comentarios de la semana actual")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Posts obtenidos exitosamente",
                     content = @Content(mediaType = "application/json", 
                     array = @ArraySchema(schema = @Schema(implementation = CommunityPostResponseDTO.class)))),
        @ApiResponse(responseCode = "400", description = "Límite inválido", content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    public ResponseEntity<List<CommunityPostResponseDTO>> getTopCommentedPosts(
            @Parameter(description = "Número máximo de posts a retornar", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        List<CommunityPost> posts = communityPostService.getTopCommentedPostsOfWeek(limit);
        List<CommunityPostResponseDTO> response = posts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/weekly/most-answered-questions")
    @Operation(summary = "Obtener preguntas más respondidas de la semana", 
               description = "Retorna el top N de preguntas con más respuestas (comentarios) de la semana actual")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Preguntas obtenidas exitosamente",
                     content = @Content(mediaType = "application/json", 
                     array = @ArraySchema(schema = @Schema(implementation = CommunityPostResponseDTO.class)))),
        @ApiResponse(responseCode = "400", description = "Límite inválido", content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    public ResponseEntity<List<CommunityPostResponseDTO>> getMostAnsweredQuestions(
            @Parameter(description = "Número máximo de preguntas a retornar", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        List<CommunityPost> posts = communityPostService.getMostAnsweredQuestionsOfWeek(limit);
        List<CommunityPostResponseDTO> response = posts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/weekly/top-resources")
    @Operation(summary = "Obtener recursos más compartidos de la semana", 
               description = "Retorna el top N de recursos más compartidos de la semana actual")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recursos obtenidos exitosamente",
                     content = @Content(mediaType = "application/json", 
                     array = @ArraySchema(schema = @Schema(implementation = CommunityPostResponseDTO.class)))),
        @ApiResponse(responseCode = "400", description = "Límite inválido", content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    public ResponseEntity<List<CommunityPostResponseDTO>> getTopResources(
            @Parameter(description = "Número máximo de recursos a retornar", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        List<CommunityPost> posts = communityPostService.getTopResourcesOfWeek(limit);
        List<CommunityPostResponseDTO> response = posts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/weekly/sessions")
    @Operation(summary = "Obtener sesiones de la semana", 
               description = "Retorna todas las sesiones realizadas durante la semana actual")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sesiones obtenidas exitosamente",
                     content = @Content(mediaType = "application/json", 
                     array = @ArraySchema(schema = @Schema(implementation = CommunityPostResponseDTO.class)))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    public ResponseEntity<List<CommunityPostResponseDTO>> getWeeklySessions() {
        List<CommunityPost> posts = communityPostService.getWeeklySessions();
        List<CommunityPostResponseDTO> response = posts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/weekly/discussions")
    @Operation(summary = "Obtener discusiones activas de la semana", 
               description = "Retorna todas las discusiones activas durante la semana actual")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Discusiones obtenidas exitosamente",
                     content = @Content(mediaType = "application/json", 
                     array = @ArraySchema(schema = @Schema(implementation = CommunityPostResponseDTO.class)))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    public ResponseEntity<List<CommunityPostResponseDTO>> getWeeklyDiscussions() {
        List<CommunityPost> posts = communityPostService.getWeeklyDiscussions();
        List<CommunityPostResponseDTO> response = posts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/community/{communityId}/top")
    @Operation(summary = "Obtener top posts por comunidad", 
               description = "Retorna el top N de posts de una comunidad específica según reacciones")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Posts obtenidos exitosamente",
                     content = @Content(mediaType = "application/json", 
                     array = @ArraySchema(schema = @Schema(implementation = CommunityPostResponseDTO.class)))),
        @ApiResponse(responseCode = "400", description = "ID de comunidad o límite inválido", content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    public ResponseEntity<List<CommunityPostResponseDTO>> getTopPostsByCommunity(
            @Parameter(description = "ID de la comunidad", example = "1", required = true)
            @PathVariable Long communityId,
            @Parameter(description = "Número máximo de posts a retornar", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        List<CommunityPost> posts = communityPostService.getTopPostsByCommunity(communityId, limit);
        List<CommunityPostResponseDTO> response = posts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/weekly/digest")
    @Operation(summary = "Obtener digest semanal para IA", 
               description = "Retorna datos agregados semanales para alimentar al LLM")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Digest obtenido exitosamente",
                     content = @Content(mediaType = "application/json", 
                     schema = @Schema(implementation = WeeklyDigestDataResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Límite inválido", content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    public ResponseEntity<WeeklyDigestDataResponseDTO> getWeeklyDigest(
            @Parameter(description = "Número máximo de elementos por categoría", example = "10")
            @RequestParam(defaultValue = "10") int topLimit) {
        var digestData = communityPostService.getWeeklyDigestData(topLimit);
        WeeklyDigestDataResponseDTO response = convertToDigestDTO(digestData);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/weekly/statistics")
    @Operation(summary = "Obtener estadísticas semanales", 
               description = "Retorna estadísticas resumidas para el editor")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas exitosamente",
                     content = @Content(mediaType = "application/json", 
                     schema = @Schema(implementation = WeeklyStatisticsResponseDTO.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    public ResponseEntity<WeeklyStatisticsResponseDTO> getWeeklyStatistics() {
        var statistics = communityPostService.getWeeklyStatistics();
        WeeklyStatisticsResponseDTO response = convertToStatisticsDTO(statistics);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/weekly/has-activity")
    @Operation(summary = "Verificar actividad semanal", 
               description = "Verifica si hay posts en la semana actual")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Resultado de la verificación",
                     content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    public ResponseEntity<Boolean> hasWeeklyActivity() {
        return ResponseEntity.ok(communityPostService.hasWeeklyActivity());
    }

    // ==================== ENDPOINTS DE FILTRADO ====================

    @PostMapping("/filter")
    @Operation(summary = "Filtrar posts", 
               description = "Filtra posts por comunidad, tipo y rango de fechas")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Posts filtrados exitosamente",
                     content = @Content(mediaType = "application/json", 
                     array = @ArraySchema(schema = @Schema(implementation = CommunityPostResponseDTO.class)))),
        @ApiResponse(responseCode = "400", description = "Parámetros de filtro inválidos", content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    public ResponseEntity<List<CommunityPostResponseDTO>> filterPosts(
            @Valid @RequestBody CommunityPostFilterDTO filterDTO) {
        
        List<CommunityPost> posts;

        int limit = filterDTO.getLimit() != null
            ? filterDTO.getLimit()
            : 100;

        // Si hay comunidad específica
        if (filterDTO.getCommunityId() != null) {
            
            posts = communityPostService.getPostsByCommunityAndDateRange(
                    filterDTO.getCommunityId(),
                    filterDTO.getStartDate(),
                    filterDTO.getEndDate(),
                    limit
            );
        } 
        // Si hay tipo específico
        else if (filterDTO.getType() != null) {
            posts = communityPostService.getPostsByTypeAndDateRange(
                    filterDTO.getType(),
                    filterDTO.getStartDate(),
                    filterDTO.getEndDate(),
                    limit
            );
        }
        // Si solo hay rango de fechas
        else if (filterDTO.getStartDate() != null && filterDTO.getEndDate() != null) {

            posts = communityPostService.getPostsByDateRange(
                    filterDTO.getStartDate(),
                    filterDTO.getEndDate(),
                    limit
            );
        }
        // Default: todos los posts de la semana
        else {
            posts = communityPostService.getWeeklyPosts();
        }
        
        List<CommunityPostResponseDTO> response = posts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Obtener posts por tipo", 
               description = "Filtra posts por tipo y rango de fechas")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Posts obtenidos exitosamente",
                     content = @Content(mediaType = "application/json", 
                     array = @ArraySchema(schema = @Schema(implementation = CommunityPostResponseDTO.class)))),
        @ApiResponse(responseCode = "400", description = "Tipo de post inválido", content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    public ResponseEntity<List<CommunityPostResponseDTO>> getPostsByType(
            @Parameter(description = "Tipo de post", example = "QUESTION", required = true)
            @PathVariable CommunityPostType type,
            @Parameter(description = "Fecha de inicio (YYYY-MM-DDTHH:MM:SS)", example = "2024-01-15T00:00:00")
            @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "Fecha de fin (YYYY-MM-DDTHH:MM:SS)", example = "2024-01-21T23:59:59")
            @RequestParam(required = false) LocalDateTime endDate,
            @Parameter(description = "Número máximo de posts a retornar", example = "50")
            @RequestParam(defaultValue = "50") int limit) {
        
        // Si no se proporcionan fechas, usar semana actual
        LocalDateTime now = LocalDateTime.now();

        if (startDate == null) {
            startDate = communityPostService.getStartOfWeek(now);
        }

        if (endDate == null) {
            endDate = communityPostService.getEndOfWeek(now);
        }
        List<CommunityPost> posts = communityPostService.getPostsByTypeAndDateRange(
                type, startDate, endDate, limit);
        List<CommunityPostResponseDTO> response = posts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // ==================== MÉTODOS DE CONVERSIÓN ====================

    private CommunityPostResponseDTO convertToDTO(CommunityPost post) {
        return CommunityPostResponseDTO.builder()
                .id(post.getId())
                .content(post.getContent())
                .authorName(post.getAuthorName())
                .type(post.getType())
                .reactionsCount(post.getReactionsCount())
                .commentsCount(post.getCommentsCount())
                .externalPostId(post.getExternalPostId())
                .sourceUrl(post.getSourceUrl())
                .collectedAt(post.getCollectedAt())
                .communityId(post.getCommunity() != null ? post.getCommunity().getId() : null)
                .build();
    }

    private WeeklyDigestDataResponseDTO convertToDigestDTO(WeeklyDigestData digestData) {
        return WeeklyDigestDataResponseDTO.builder()
                .topReactedPosts(digestData.getTopReactedPosts().stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList()))
                .topCommentedPosts(digestData.getTopCommentedPosts().stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList()))
                .mostAnsweredQuestions(digestData.getMostAnsweredQuestions().stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList()))
                .topResources(digestData.getTopResources().stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList()))
                .weeklySessions(digestData.getWeeklySessions().stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList()))
                .weeklyDiscussions(digestData.getWeeklyDiscussions().stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList()))
                .totalPosts(digestData.getTotalPosts())
                .weekStart(digestData.getWeekStart())
                .weekEnd(digestData.getWeekEnd())
                .build();
    }

    private WeeklyStatisticsResponseDTO convertToStatisticsDTO(WeeklyStatistics statistics) {
        return WeeklyStatisticsResponseDTO.builder()
                .totalPosts(statistics.getTotalPosts())
                .totalQuestions(statistics.getTotalQuestions())
                .totalResources(statistics.getTotalResources())
                .totalSessions(statistics.getTotalSessions())
                .totalDiscussions(statistics.getTotalDiscussions())
                .totalReactions(statistics.getTotalReactions())
                .totalComments(statistics.getTotalComments())
                .weekStart(statistics.getWeekStart())
                .weekEnd(statistics.getWeekEnd())
                .build();
    }
}