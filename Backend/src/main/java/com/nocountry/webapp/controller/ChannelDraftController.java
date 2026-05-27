package com.nocountry.webapp.controller;

import com.nocountry.webapp.dto.*;
import com.nocountry.webapp.entity.ChannelDraft;
import com.nocountry.webapp.entity.enums.ChannelDraftStatus;
import com.nocountry.webapp.entity.enums.TargetPlatform;
import com.nocountry.webapp.service.ChannelDraftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/channel-drafts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Channel Drafts", description = "Endpoints para gestión de borradores de canales")
public class ChannelDraftController {

    private final ChannelDraftService channelDraftService;

    /**
     * Convierte entidad a DTO
     */
    private ChannelDraftResponseDTO toResponse(ChannelDraft draft) {
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

    private List<ChannelDraftResponseDTO> toResponseList(List<ChannelDraft> drafts) {
        return drafts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo borrador para un digest")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Borrador creado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Digest o Editor no encontrado"),
            @ApiResponse(responseCode = "400", description = "Ya existe un borrador para esta plataforma o editor sin rol USER")
    })
    public ResponseEntity<ChannelDraftResponseDTO> createDraft(
            @Valid @RequestBody ChannelDraftCreateRequestDTO request, Authentication authentication) {
        log.info("POST /api/channel-drafts - Creando borrador para digest: {} en plataforma: {}",
                request.getWeeklyDigestId(), request.getPlatform());
        
        ChannelDraft draft = channelDraftService.createDraft(
                request.getWeeklyDigestId(),
                request.getContent(),
                request.getPlatform(),
                authentication.getName()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(draft));
    }

    @PostMapping("/complete")
    @Operation(summary = "Crear los 3 borradores completos (NEWSLETTER, LINKEDIN, X) para un digest")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Borradores creados exitosamente"),
            @ApiResponse(responseCode = "404", description = "Digest o Editor no encontrado"),
            @ApiResponse(responseCode = "400", description = "Editor sin rol USER")
    })
    public ResponseEntity<List<ChannelDraftResponseDTO>> createCompleteDrafts(
            @Valid @RequestBody ChannelDraftCompleteRequestDTO request, Authentication authentication) {
        log.info("POST /api/channel-drafts/complete - Creando borradores completos para digest: {}",
                request.getWeeklyDigestId());
        String email = authentication.getName();
        List<ChannelDraft> drafts = channelDraftService.createCompleteDrafts(
                request.getWeeklyDigestId(),
                request.getNewsletterContent(),
                request.getLinkedinContent(),
                request.getTwitterContent(),
                email
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponseList(drafts));
    }

    @GetMapping("/{draftId}")
    @Operation(summary = "Obtener un borrador por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Borrador encontrado"),
            @ApiResponse(responseCode = "404", description = "Borrador no encontrado")
    })
    public ResponseEntity<ChannelDraftResponseDTO> getDraftById(
            @Parameter(description = "ID del borrador") @PathVariable Long draftId) {
        log.info("GET /api/channel-drafts/{}", draftId);
        ChannelDraft draft = channelDraftService.getDraftById(draftId);
        return ResponseEntity.ok(toResponse(draft));
    }

    @GetMapping("/by-digest/{weeklyDigestId}")
    @Operation(summary = "Obtener todos los borradores de un digest")
    @ApiResponse(responseCode = "200", description = "Lista de borradores")
    @ApiResponse(responseCode = "404", description = "Digest no encontrado")
    public ResponseEntity<List<ChannelDraftResponseDTO>> getDraftsByDigestId(
            @Parameter(description = "ID del digest semanal") @PathVariable Long weeklyDigestId) {
        log.info("GET /api/channel-drafts/by-digest/{}", weeklyDigestId);
        List<ChannelDraft> drafts = channelDraftService.getDraftsByDigestId(weeklyDigestId);
        return ResponseEntity.ok(toResponseList(drafts));
    }

    @GetMapping("/by-digest-platform")
    @Operation(summary = "Obtener un borrador específico por digest y plataforma")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Borrador encontrado"),
            @ApiResponse(responseCode = "404", description = "Borrador no encontrado")
    })
    public ResponseEntity<ChannelDraftResponseDTO> getDraftByDigestAndPlatform(
            @RequestParam Long weeklyDigestId,
            @RequestParam TargetPlatform platform) {
        log.info("GET /api/channel-drafts/by-digest-platform - digestId={}, platform={}", 
                weeklyDigestId, platform);
        ChannelDraft draft = channelDraftService.getDraftByDigestAndPlatform(weeklyDigestId, platform);
        return ResponseEntity.ok(toResponse(draft));
    }

    @PatchMapping("/{draftId}/content")
    @Operation(summary = "Actualizar el contenido de un borrador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contenido actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Borrador no encontrado"),
            @ApiResponse(responseCode = "400", description = "No se puede editar un borrador publicado")
    })
    public ResponseEntity<ChannelDraftResponseDTO> updateContent(
            @Parameter(description = "ID del borrador") @PathVariable Long draftId,
            @Valid @RequestBody ChannelDraftUpdateContentRequestDTO request) {
        log.info("PATCH /api/channel-drafts/{}/content", draftId);
        ChannelDraft draft = channelDraftService.updateContent(draftId, request.getContent());
        return ResponseEntity.ok(toResponse(draft));
    }

    @PatchMapping("/{draftId}/approve")
    @Operation(summary = "Aprobar un borrador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Borrador aprobado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Borrador o Editor no encontrado"),
            @ApiResponse(responseCode = "400", description = "No se puede aprobar un borrador ya publicado o editor sin rol USER")
    })
    public ResponseEntity<ChannelDraftResponseDTO> approveDraft(
            @Parameter(description = "ID del borrador") @PathVariable Long draftId,
            Authentication authentication) {
        log.info("PATCH /api/channel-drafts/{}/approve", draftId);
        String email = authentication.getName();
        ChannelDraft draft = channelDraftService.approveDraft(draftId, email);
        return ResponseEntity.ok(toResponse(draft));
    }

    @PatchMapping("/{draftId}/reject")
    @Operation(summary = "Rechazar un borrador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Borrador rechazado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Borrador no encontrado"),
            @ApiResponse(responseCode = "400", description = "No se puede rechazar un borrador publicado")
    })
    public ResponseEntity<ChannelDraftResponseDTO> rejectDraft(
            @Parameter(description = "ID del borrador") @PathVariable Long draftId) {
        log.info("PATCH /api/channel-drafts/{}/reject", draftId);
        ChannelDraft draft = channelDraftService.rejectDraft(draftId);
        return ResponseEntity.ok(toResponse(draft));
    }

    @PatchMapping("/{draftId}/start-review")
    @Operation(summary = "Poner un borrador en revisión")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Borrador en revisión"),
            @ApiResponse(responseCode = "404", description = "Borrador no encontrado"),
            @ApiResponse(responseCode = "400", description = "Solo se pueden revisar borradores en estado GENERATED")
    })
    public ResponseEntity<ChannelDraftResponseDTO> startReview(
            @Parameter(description = "ID del borrador") @PathVariable Long draftId) {
        log.info("PATCH /api/channel-drafts/{}/start-review", draftId);
        ChannelDraft draft = channelDraftService.startReview(draftId);
        return ResponseEntity.ok(toResponse(draft));
    }

    @PatchMapping("/{draftId}/publish")
    @Operation(summary = "Marcar un borrador como publicado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Borrador marcado como publicado"),
            @ApiResponse(responseCode = "404", description = "Borrador no encontrado"),
            @ApiResponse(responseCode = "400", description = "Solo se pueden publicar borradores aprobados")
    })
    public ResponseEntity<ChannelDraftResponseDTO> markAsPublished(
            @Parameter(description = "ID del borrador") @PathVariable Long draftId) {
        log.info("PATCH /api/channel-drafts/{}/publish", draftId);
        ChannelDraft draft = channelDraftService.markAsPublished(draftId);
        return ResponseEntity.ok(toResponse(draft));
    }

    @DeleteMapping("/{draftId}")
    @Operation(summary = "Eliminar un borrador (solo si no está publicado)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Borrador eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Borrador no encontrado"),
            @ApiResponse(responseCode = "400", description = "No se puede eliminar un borrador publicado")
    })
    public ResponseEntity<Void> deleteDraft(
            @Parameter(description = "ID del borrador") @PathVariable Long draftId) {
        log.info("DELETE /api/channel-drafts/{}", draftId);
        channelDraftService.deleteDraft(draftId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-status")
    @Operation(summary = "Listar borradores por estado")
    public ResponseEntity<List<ChannelDraftResponseDTO>> getDraftsByStatus(
            @RequestParam ChannelDraftStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("GET /api/channel-drafts/by-status - status={}, page={}, size={}", status, page, size);
        List<ChannelDraft> drafts = channelDraftService.getDraftsByStatus(status, page, size);
        return ResponseEntity.ok(toResponseList(drafts));
    }

    @GetMapping("/pending")
    @Operation(summary = "Listar borradores pendientes (GENERATED o IN_REVIEW)")
    public ResponseEntity<List<ChannelDraftResponseDTO>> getPendingDrafts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("GET /api/channel-drafts/pending - page={}, size={}", page, size);
        List<ChannelDraft> drafts = channelDraftService.getPendingDrafts(page, size);
        return ResponseEntity.ok(toResponseList(drafts));
    }

    @GetMapping("/count/{status}")
    @Operation(summary = "Contar borradores por estado")
    public ResponseEntity<Long> countByStatus(
            @Parameter(description = "Estado del borrador") @PathVariable ChannelDraftStatus status) {
        log.info("GET /api/channel-drafts/count/{}", status);
        long count = channelDraftService.countByStatus(status);
        return ResponseEntity.ok(count);
    }

    @PatchMapping("/approve-all/{weeklyDigestId}")
    @Operation(summary = "Aprobar todos los borradores de un digest")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Borradores aprobados exitosamente"),
            @ApiResponse(responseCode = "404", description = "Editor no encontrado"),
            @ApiResponse(responseCode = "400", description = "No hay borradores asociados o editor sin rol USER")
    })
    public ResponseEntity<Void> approveAllDraftsByDigestId(
            @Parameter(description = "ID del digest semanal") @PathVariable Long weeklyDigestId,
            Authentication authentication) {
        String email = authentication.getName();
        log.info("PATCH /api/channel-drafts/approve-all/{} - editorEmail={}", weeklyDigestId, email);
        channelDraftService.approveAllDraftsByDigestId(weeklyDigestId, email);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/are-all-approved/{weeklyDigestId}")
    @Operation(summary = "Verificar si un digest tiene todos sus borradores aprobados")
    public ResponseEntity<Boolean> areAllDraftsApproved(
            @Parameter(description = "ID del digest semanal") @PathVariable Long weeklyDigestId) {
        log.info("GET /api/channel-drafts/are-all-approved/{}", weeklyDigestId);
        boolean allApproved = channelDraftService.areAllDraftsApproved(weeklyDigestId);
        return ResponseEntity.ok(allApproved);
    }
}