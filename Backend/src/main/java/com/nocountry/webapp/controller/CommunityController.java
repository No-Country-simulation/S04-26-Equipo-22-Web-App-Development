package com.nocountry.webapp.controller;

import com.nocountry.webapp.dto.CommunityRequestDTO;
import com.nocountry.webapp.dto.CommunityResponseDTO;
import com.nocountry.webapp.entity.Community;
import com.nocountry.webapp.service.CommunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.media.ArraySchema;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
@Tag(name = "Communities", description = "Endpoints para gestión de comunidades")
public class CommunityController {

    private final CommunityService communityService;

    @GetMapping("/active")
    @Operation(summary = "Obtener todas las comunidades activas", 
               description = "Retorna una lista de todas las comunidades que están activas")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Comunidades obtenidas exitosamente",
                     content = @Content(mediaType = "application/json", 
                     array = @ArraySchema(schema = @Schema(implementation = CommunityResponseDTO.class)))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    public ResponseEntity<List<CommunityResponseDTO>> getAllActiveCommunities() {
        List<Community> communities = communityService.getAllActiveCommunities();
        List<CommunityResponseDTO> response = communities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Obtener todas las comunidades", 
               description = "Retorna una lista de TODAS las comunidades (incluye activas e inactivas) - Solo para administración")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Comunidades obtenidas exitosamente",
                     content = @Content(mediaType = "application/json", 
                     array = @ArraySchema(schema = @Schema(implementation = CommunityResponseDTO.class)))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    public ResponseEntity<List<CommunityResponseDTO>> getAllCommunities() {
        List<Community> communities = communityService.getAllCommunities();
        List<CommunityResponseDTO> response = communities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener comunidad por ID", 
               description = "Retorna una comunidad específica según su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Comunidad encontrada",
                     content = @Content(mediaType = "application/json", 
                     schema = @Schema(implementation = CommunityResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Comunidad no encontrada", content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    public ResponseEntity<CommunityResponseDTO> getCommunityById(
            @Parameter(description = "ID de la comunidad", example = "1", required = true)
            @PathVariable Long id) {
        Community community = communityService.getCommunityById(id);
        return ResponseEntity.ok(convertToDTO(community));
    }

    @PostMapping
    @Operation(summary = "Crear nueva comunidad", 
               description = "Crea una nueva comunidad en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Comunidad creada exitosamente",
                     content = @Content(mediaType = "application/json", 
                     schema = @Schema(implementation = CommunityResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
        @ApiResponse(responseCode = "409", description = "Ya existe una comunidad con ese nombre", content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    public ResponseEntity<CommunityResponseDTO> createCommunity(
            @Valid @RequestBody CommunityRequestDTO requestDTO) {
        Community community = convertToEntity(requestDTO);
        Community created = communityService.createCommunity(community);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar comunidad", 
               description = "Actualiza los datos de una comunidad existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Comunidad actualizada exitosamente",
                     content = @Content(mediaType = "application/json", 
                     schema = @Schema(implementation = CommunityResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
        @ApiResponse(responseCode = "404", description = "Comunidad no encontrada", content = @Content),
        @ApiResponse(responseCode = "409", description = "Ya existe otra comunidad con ese nombre", content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    public ResponseEntity<CommunityResponseDTO> updateCommunity(
            @Parameter(description = "ID de la comunidad", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody CommunityRequestDTO requestDTO) {
        // Convertir DTO a Entity para la actualización
        Community updatedData = new Community();
        updatedData.setName(requestDTO.getName());
        updatedData.setPlatform(requestDTO.getPlatform());
        
        Community updated = communityService.updateCommunity(id, updatedData);
        return ResponseEntity.ok(convertToDTO(updated));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Desactivar comunidad", 
               description = "Realiza un borrado lógico de la comunidad (la marca como inactiva)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Comunidad desactivada exitosamente", content = @Content),
        @ApiResponse(responseCode = "404", description = "Comunidad no encontrada", content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    public ResponseEntity<Void> deactivateCommunity(
            @Parameter(description = "ID de la comunidad", example = "1", required = true)
            @PathVariable Long id) {
        communityService.deactivateCommunity(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activar comunidad", 
               description = "Activa una comunidad que estaba desactivada")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Comunidad activada exitosamente", content = @Content),
        @ApiResponse(responseCode = "404", description = "Comunidad no encontrada", content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    public ResponseEntity<Void> activateCommunity(
            @Parameter(description = "ID de la comunidad", example = "1", required = true)
            @PathVariable Long id) {
        communityService.activateCommunity(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar comunidad permanentemente", 
               description = "Elimina físicamente la comunidad de la base de datos - ¡USAR CON CUIDADO!")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Comunidad eliminada exitosamente", content = @Content),
        @ApiResponse(responseCode = "404", description = "Comunidad no encontrada", content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    public ResponseEntity<Void> deleteCommunityPermanently(
            @Parameter(description = "ID de la comunidad", example = "1", required = true)
            @PathVariable Long id) {
        communityService.deleteCommunityPermanently(id);
        return ResponseEntity.noContent().build();
    }

    // Métodos de conversión
    private CommunityResponseDTO convertToDTO(Community community) {
        CommunityResponseDTO dto = new CommunityResponseDTO();
        dto.setId(community.getId());
        dto.setName(community.getName());
        dto.setPlatform(community.getPlatform());
        dto.setActive(community.isActive());
        return dto;
    }

    private Community convertToEntity(CommunityRequestDTO dto) {
        Community community = new Community();
        community.setName(dto.getName());
        community.setPlatform(dto.getPlatform());
        community.setActive(dto.isActive());
        return community;
    }
}