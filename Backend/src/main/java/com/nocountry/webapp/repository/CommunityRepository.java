package com.nocountry.webapp.repository;

import com.nocountry.webapp.entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Long> {

    /**
     * Busca comunidades activas
     */
    List<Community> findByActiveTrue();

    /**
     * Busca comunidades por plataforma
     */
    List<Community> findByPlatformIgnoreCase(String platform);

    /**
     * Verifica si existe una comunidad con ese nombre (para evitar duplicados)
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Busca comunidad por nombre exacto (útil para sincronización)
     */
    Optional<Community> findByNameIgnoreCase(String name);
}