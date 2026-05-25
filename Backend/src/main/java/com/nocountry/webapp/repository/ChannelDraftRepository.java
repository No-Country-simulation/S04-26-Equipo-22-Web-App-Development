package com.nocountry.webapp.repository;

import com.nocountry.webapp.entity.ChannelDraft;
import com.nocountry.webapp.entity.enums.ChannelDraftStatus;
import com.nocountry.webapp.entity.enums.TargetPlatform;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChannelDraftRepository extends JpaRepository<ChannelDraft, Long> {

    /**
     * Busca todos los borradores de un digest específico
     */
    List<ChannelDraft> findByWeeklyDigestId(Long weeklyDigestId);

    /**
     * Busca borradores por estado (con paginación)
     */
    List<ChannelDraft> findByStatus(ChannelDraftStatus status, Pageable pageable);

    /**
     * Busca borrador específico por digest y plataforma
     */
    Optional<ChannelDraft> findByWeeklyDigestIdAndTargetPlatform(Long weeklyDigestId, TargetPlatform platform);

    /**
     * Cuenta borradores por estado
     */
    long countByStatus(ChannelDraftStatus status);

    /**
     * Busca borradores pendientes de aprobación (GENERATED o IN_REVIEW)
     */
    @Query("SELECT c FROM ChannelDraft c WHERE c.status IN :statuses ORDER BY c.createdAt ASC")
    List<ChannelDraft> findPendingDrafts(@Param("statuses") List<ChannelDraftStatus> statuses, Pageable pageable);

    /**
     * Actualiza el estado de todos los borradores de un digest
     */
    @Modifying
    @Transactional
    @Query("UPDATE ChannelDraft c SET c.status = :newStatus WHERE c.weeklyDigest.id = :digestId")
    void updateStatusByDigestId(@Param("digestId") Long digestId, @Param("newStatus") ChannelDraftStatus newStatus);

}