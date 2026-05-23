package com.nocountry.webapp.repository;

import com.nocountry.webapp.entity.WeeklyDigest;
import com.nocountry.webapp.entity.enums.WeeklyDigestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WeeklyDigestRepository
        extends JpaRepository<WeeklyDigest, Long> {

    // Buscar digest exacto de una comunidad en una semana
    Optional<WeeklyDigest> findByCommunityIdAndWeekStartAndWeekEnd(
            Long communityId,
            LocalDate weekStart,
            LocalDate weekEnd
    );

    // Verificar si ya existe digest para evitar duplicados
        boolean existsByCommunityIdAndWeekStart(
                Long communityId, 
                LocalDate weekStart
        );


    // Listar pendientes para revisión editorial
    List<WeeklyDigest> findByStatusOrderByCreatedAtDesc(
            WeeklyDigestStatus status,
            Pageable pageable
    );

    // Historial de digests por comunidad
    List<WeeklyDigest> findByCommunityIdOrderByWeekStartDesc(
            Long communityId,
            Pageable pageable
    );

    // Últimos digests generados
    List<WeeklyDigest> findAllByOrderByCreatedAtDesc(
            Pageable pageable
    );
    
    /**
     * Digests por rango de fechas (para reportes)
     */
    List<WeeklyDigest> findByWeekStartBetweenOrderByWeekStartDesc(
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );
    
    /**
     * Contar digests pendientes para notificaciones al editor
     */
    long countByStatus(WeeklyDigestStatus status);
    
    /**
     * Buscar digests que NO han sido procesados y son de semanas anteriores
     * (útil para recovery si falló el pipeline)
     */
    @Query("SELECT w FROM WeeklyDigest w WHERE w.status = :status AND w.weekEnd < :currentDate")
    List<WeeklyDigest> findUnprocessedDigestsOlderThan(
            @Param("status") WeeklyDigestStatus status,
            @Param("currentDate") LocalDate currentDate,
            Pageable pageable
    );
    
}