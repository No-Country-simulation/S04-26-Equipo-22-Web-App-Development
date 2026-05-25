package com.nocountry.webapp.service;

import com.nocountry.webapp.entity.Community;
import com.nocountry.webapp.exception.base.ConflictException;
import com.nocountry.webapp.exception.base.NotFoundException;
import com.nocountry.webapp.repository.CommunityRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {

    private final CommunityRepository communityRepository;

    /**
     * Obtener todas las comunidades activas
     */
    public List<Community> getAllActiveCommunities() {
        return communityRepository.findByActiveTrue();
    }

    /**
     * Obtener todas las comunidades (solo para administración)
     */
    public List<Community> getAllCommunities() {
        return communityRepository.findAll();
    }

    /**
     * Obtener comunidad por ID
     */
    public Community getCommunityById(Long id) {
        return communityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Comunidad no encontrada con ID: " + id));
    }

    /**
     * Crear nueva comunidad
     */
   @Transactional
    // 1. CAMBIÁ ESTA LÍNEA (agregando el String creatorEmail al final):
    public Community createCommunity(Community community) { 
        // Validar nombre
        if (community.getName() == null || community.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la comunidad es obligatorio");
        }

        String normalizedName = community.getName().trim();
        
        // Verificar duplicado
        if (communityRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new ConflictException("Ya existe una comunidad con el nombre: " + normalizedName);
        }

        community.setName(normalizedName);

        // Si platform es null, guardar como null (no obligatorio)
        if (community.getPlatform() != null) {
            community.setPlatform(community.getPlatform().trim());
        }
        
        Community saved = communityRepository.save(community);
        log.info("Comunidad creada: {} (ID: {})", saved.getName(), saved.getId());
        
        return saved;
    }

    /**
     * Actualizar comunidad existente
     */
    @Transactional
    public Community updateCommunity(Long id, Community updatedData) {
        Community existing = getCommunityById(id);
        
        // Actualizar nombre si viene y es diferente
        if (updatedData.getName() != null && !updatedData.getName().trim().isEmpty()) {
            String newName = updatedData.getName().trim();
            
            // Si el nombre cambió, verificar que no exista otra comunidad con ese nombre
            if (!existing.getName().equalsIgnoreCase(newName) 
                    && communityRepository.existsByNameIgnoreCase(newName)) {
                throw new ConflictException("Ya existe una comunidad con el nombre: " + newName);
            }
            existing.setName(newName);
        }
        
        // Actualizar plataforma si viene
        if (updatedData.getPlatform() != null) {
            existing.setPlatform(updatedData.getPlatform().trim());
        }
        
        Community saved = communityRepository.save(existing);
        log.info("Comunidad actualizada: {} (ID: {})", saved.getName(), saved.getId());
        
        return saved;
    }

    /**
     * Desactivar comunidad (borrado lógico)
     */
    @Transactional
    public void deactivateCommunity(Long id) {
        Community community = getCommunityById(id);
        community.setActive(false);
        communityRepository.save(community);
        log.info("Comunidad desactivada: {} (ID: {})", community.getName(), community.getId());
    }

    /**
     * Activar comunidad
     */
    @Transactional
    public void activateCommunity(Long id) {
        Community community = getCommunityById(id);
        community.setActive(true);
        communityRepository.save(community);
        log.info("Comunidad activada: {} (ID: {})", community.getName(), community.getId());
    }

    /**
     * Eliminar comunidad físicamente (usar con cuidado)
     */
    @Transactional
    public void deleteCommunityPermanently(Long id) {
        Community community = getCommunityById(id);
        communityRepository.delete(community);
        log.warn("Comunidad eliminada permanentemente: {} (ID: {})", community.getName(), community.getId());
    }
}