package com.nocountry.webapp.controller;

import com.nocountry.webapp.dto.DigestGenerationResponseDTO;
import com.nocountry.webapp.dto.WeeklyDigestResponseDTO;
import com.nocountry.webapp.dto.WeeklyDigestUpdateRequestDTO;
import com.nocountry.webapp.dto.ChannelDraftResponseDTO;
import com.nocountry.webapp.entity.ChannelDraft;
import com.nocountry.webapp.entity.WeeklyDigest;
import com.nocountry.webapp.service.WeeklyDigestService;
import com.nocountry.webapp.service.dto.WeeklyDigestGenerationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/weekly-digests")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Weekly Digest", description = "Endpoints para gestión de resúmenes semanales")
public class WeeklyDigestController {

    private final WeeklyDigestService weeklyDigestService;

    /**
     * Convierte entidad a DTO
     */
    private WeeklyDigestResponseDTO toResponse(WeeklyDigest digest) {
        return WeeklyDigestResponseDTO.builder()
                .id(digest.getId())
                .communityId(digest.getCommunity().getId())
                .communityName(digest.getCommunity().getName())
                .weekStart(digest.getWeekStart())
                .weekEnd(digest.getWeekEnd())
                .summary(digest.getSummary())
                .status(digest.getStatus())
                .createdAt(digest.getCreatedAt())
                .build();
    }

    private List<WeeklyDigestResponseDTO> toResponseList(List<WeeklyDigest> digests) {
        return digests.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ==================== ENDPOINTS EXISTENTES ====================

    @PostMapping
    @Operation(summary = "Generar un nuevo digest semanal para una comunidad")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Digest generado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Comunidad no encontrada"),
            @ApiResponse(responseCode = "409", description = "Ya existe un digest para esa comunidad y semana")
    })
    public ResponseEntity<WeeklyDigestResponseDTO> generateWeeklyDigest(
            @Valid @RequestBody WeeklyDigestGenerationRequest request) {
        log.info("POST /api/weekly-digests - Generando digest para comunidad: {}", request.getCommunityId());
        WeeklyDigest digest = weeklyDigestService.generateWeeklyDigest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(digest));
    }

    @PostMapping("/bulk")
    @Operation(summary = "Generar digests para todas las comunidades activas")
    @ApiResponse(responseCode = "200", description = "Digests generados exitosamente")
    public ResponseEntity<DigestGenerationResponseDTO> generateDigestsForAllCommunities() {
        log.info("POST /api/weekly-digests/bulk - Generando digests para todas las comunidades");
        List<WeeklyDigest> digests = weeklyDigestService.generateDigestsForAllActiveCommunities();
        return ResponseEntity.ok(DigestGenerationResponseDTO.builder()
                .generatedCount(digests.size())
                .message(String.format("Se generaron %d digests", digests.size()))
                .build());
    }

    @PatchMapping("/{digestId}/process")
    @Operation(summary = "Marcar un digest como procesado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Digest procesado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Digest no encontrado"),
            @ApiResponse(responseCode = "400", description = "El digest ya fue procesado anteriormente")
    })
    public ResponseEntity<WeeklyDigestResponseDTO> processDigest(
            @Parameter(description = "ID del digest") @PathVariable Long digestId) {
        log.info("PATCH /api/weekly-digests/{}/process", digestId);
        WeeklyDigest digest = weeklyDigestService.processDigest(digestId);
        return ResponseEntity.ok(toResponse(digest));
    }

    @PatchMapping("/{digestId}/summary")
    @Operation(summary = "Actualizar el resumen de un digest")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resumen actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Digest no encontrado"),
            @ApiResponse(responseCode = "400", description = "No se puede editar un digest procesado")
    })
    public ResponseEntity<WeeklyDigestResponseDTO> updateSummary(
            @Parameter(description = "ID del digest") @PathVariable Long digestId,
            @Valid @RequestBody WeeklyDigestUpdateRequestDTO request) {
        log.info("PATCH /api/weekly-digests/{}/summary", digestId);
        WeeklyDigest digest = weeklyDigestService.updateSummary(digestId, request.getSummary());
        return ResponseEntity.ok(toResponse(digest));
    }

    @DeleteMapping("/{digestId}")
    @Operation(summary = "Eliminar un digest (solo si está PENDING)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Digest eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Digest no encontrado"),
            @ApiResponse(responseCode = "400", description = "No se puede eliminar un digest procesado")
    })
    public ResponseEntity<Void> deleteDigest(
            @Parameter(description = "ID del digest") @PathVariable Long digestId) {
        log.info("DELETE /api/weekly-digests/{}", digestId);
        weeklyDigestService.deleteDigest(digestId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{digestId}")
    @Operation(summary = "Obtener un digest por ID")
    @ApiResponse(responseCode = "200", description = "Digest encontrado")
    @ApiResponse(responseCode = "404", description = "Digest no encontrado")
    public ResponseEntity<WeeklyDigestResponseDTO> getDigestById(
            @Parameter(description = "ID del digest") @PathVariable Long digestId) {
        log.info("GET /api/weekly-digests/{}", digestId);
        WeeklyDigest digest = weeklyDigestService.getDigestById(digestId);
        return ResponseEntity.ok(toResponse(digest));
    }

    @GetMapping("/by-community-week")
    @Operation(summary = "Obtener digest por comunidad y semana")
    @ApiResponse(responseCode = "200", description = "Digest encontrado")
    @ApiResponse(responseCode = "404", description = "Digest no encontrado")
    public ResponseEntity<WeeklyDigestResponseDTO> getDigestByCommunityAndWeek(
            @RequestParam Long communityId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekEnd) {
        log.info("GET /api/weekly-digests/by-community-week - communityId={}, weekStart={}, weekEnd={}", 
                communityId, weekStart, weekEnd);
        WeeklyDigest digest = weeklyDigestService.getDigestByCommunityAndWeek(communityId, weekStart, weekEnd);
        return ResponseEntity.ok(toResponse(digest));
    }

    @GetMapping("/pending")
    @Operation(summary = "Listar digests pendientes")
    public ResponseEntity<List<WeeklyDigestResponseDTO>> getPendingDigests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("GET /api/weekly-digests/pending - page={}, size={}", page, size);
        List<WeeklyDigest> digests = weeklyDigestService.getPendingDigests(page, size);
        return ResponseEntity.ok(toResponseList(digests));
    }

    @GetMapping("/community/{communityId}/history")
    @Operation(summary = "Historial de digests por comunidad")
    @ApiResponse(responseCode = "404", description = "Comunidad no encontrada")
    public ResponseEntity<List<WeeklyDigestResponseDTO>> getCommunityDigestHistory(
            @Parameter(description = "ID de la comunidad") @PathVariable Long communityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("GET /api/weekly-digests/community/{}/history - page={}, size={}", communityId, page, size);
        List<WeeklyDigest> digests = weeklyDigestService.getCommunityDigestHistory(communityId, page, size);
        return ResponseEntity.ok(toResponseList(digests));
    }

    @GetMapping("/latest")
    @Operation(summary = "Últimos digests generados")
    public ResponseEntity<List<WeeklyDigestResponseDTO>> getLatestDigests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("GET /api/weekly-digests/latest - page={}, size={}", page, size);
        List<WeeklyDigest> digests = weeklyDigestService.getLatestDigests(page, size);
        return ResponseEntity.ok(toResponseList(digests));
    }

    @GetMapping("/by-date-range")
    @Operation(summary = "Digests por rango de fechas")
    public ResponseEntity<List<WeeklyDigestResponseDTO>> getDigestsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("GET /api/weekly-digests/by-date-range - start={}, end={}, page={}, size={}", 
                startDate, endDate, page, size);
        List<WeeklyDigest> digests = weeklyDigestService.getDigestsByDateRange(startDate, endDate, page, size);
        return ResponseEntity.ok(toResponseList(digests));
    }

    @GetMapping("/pending/count")
    @Operation(summary = "Contar digests pendientes")
    public ResponseEntity<Long> countPendingDigests() {
        log.info("GET /api/weekly-digests/pending/count");
        long count = weeklyDigestService.countPendingDigests();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/current-week/{communityId}/exists")
    @Operation(summary = "Verificar si una comunidad tiene digest para la semana actual")
    public ResponseEntity<Boolean> hasDigestForCurrentWeek(
            @Parameter(description = "ID de la comunidad") @PathVariable Long communityId) {
        log.info("GET /api/weekly-digests/current-week/{}/exists", communityId);
        boolean hasDigest = weeklyDigestService.hasDigestForCurrentWeek(communityId);
        return ResponseEntity.ok(hasDigest);
    }

    @GetMapping("/recover-unprocessed")
    @Operation(summary = "Recuperar digests no procesados de semanas anteriores")
    public ResponseEntity<List<WeeklyDigestResponseDTO>> recoverUnprocessedDigests(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate currentDate,
            @RequestParam(defaultValue = "100") int limit) {
        log.info("GET /api/weekly-digests/recover-unprocessed - currentDate={}, limit={}", currentDate, limit);
        List<WeeklyDigest> digests = weeklyDigestService.recoverUnprocessedDigests(currentDate, limit);
        return ResponseEntity.ok(toResponseList(digests));
    }

    // ==================== NUEVOS ENDPOINTS CON IA ====================

    @PostMapping("/generate-with-ai")
    @Operation(summary = "Generar digest completo usando IA (resumen + drafts para todos los canales)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Digest generado exitosamente con IA"),
            @ApiResponse(responseCode = "404", description = "Comunidad o Editor no encontrado"),
            @ApiResponse(responseCode = "409", description = "Ya existe un digest para esa comunidad y semana"),
            @ApiResponse(responseCode = "400", description = "Error al generar contenido con IA")
    })
    public ResponseEntity<WeeklyDigestResponseDTO> generateCompleteDigestWithAI(
            @Parameter(description = "ID de la comunidad") @RequestParam Long communityId,
            @Parameter(description = "ID del editor") Authentication authentication) {
        String email = authentication.getName();
        log.info("POST /api/weekly-digests/generate-with-ai - communityId={}, editorId={}", communityId, email);
        WeeklyDigest digest = weeklyDigestService.generateCompleteDigestWithAI(communityId, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(digest));
    }

    @PostMapping("/{digestId}/regenerate-drafts")
    @Operation(summary = "Regenerar solo los drafts IA para un digest existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Drafts regenerados exitosamente"),
            @ApiResponse(responseCode = "404", description = "Digest o Editor no encontrado"),
            @ApiResponse(responseCode = "400", description = "Error al generar contenido con IA")
    })
    public ResponseEntity<List<ChannelDraftResponseDTO>> regenerateDrafts(
            @Parameter(description = "ID del digest") @PathVariable Long digestId,
            @Parameter(description = "ID del editor") Authentication authentication) {
        String email = authentication.getName();
        log.info("POST /api/weekly-digests/{}/regenerate-drafts - editorId={}", digestId, email);
        List<ChannelDraft> drafts = weeklyDigestService.generateDraftsForDigest(digestId, email);
        
        List<ChannelDraftResponseDTO> response = drafts.stream()
                .map(this::toChannelDraftDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Convierte ChannelDraft a DTO
     */
    private ChannelDraftResponseDTO toChannelDraftDTO(ChannelDraft draft) {
        return ChannelDraftResponseDTO.builder()
                .id(draft.getId())
                .content(draft.getContent())
                .targetPlatform(draft.getTargetPlatform())
                .status(draft.getStatus())
                .createdAt(draft.getCreatedAt())
                .approvedAt(draft.getApprovedAt())
                .editorId(draft.getEditor() != null ? draft.getEditor().getId() : null)
                .editorEmail(draft.getEditor() != null ? draft.getEditor().getEmail() : null)
                .weeklyDigestId(draft.getWeeklyDigest().getId())
                .build();
    }
}