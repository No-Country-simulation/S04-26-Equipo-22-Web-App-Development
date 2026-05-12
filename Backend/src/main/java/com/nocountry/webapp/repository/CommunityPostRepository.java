package com.nocountry.webapp.repository;

import com.nocountry.webapp.entity.CommunityPost;
import com.nocountry.webapp.entity.enums.CommunityPostType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

    // 1. Posts de una comunidad en rango de fechas (usando ID, no la entidad)
    List<CommunityPost> findByCommunityIdAndCollectedAtBetween(
            Long communityId,
            LocalDateTime start,
            LocalDateTime end
    );


    // 4. Filtrar por tipo (QUESTION, RESOURCE, SESSION, DISCUSSION)
    List<CommunityPost> findByTypeAndCollectedAtBetween(
            CommunityPostType type,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    // Posts en un rango de fechas (semana)
    List<CommunityPost> findByCollectedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );


    // 2. Posts más reaccionados de la semana (con límite dinámico vía Pageable)
    List<CommunityPost> findByCollectedAtBetweenOrderByReactionsCountDesc(
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    // 3. Posts más comentados de la semana
    List<CommunityPost> findByCollectedAtBetweenOrderByCommentsCountDesc(
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );


    // 5. Top posts por comunidad (usando ID)
    List<CommunityPost> findByCommunityIdAndCollectedAtBetweenOrderByReactionsCountDesc(
            Long communityId,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    // 6. ⭐ NUEVO para MVP: preguntas más respondidas (las que tienen más comentarios)
    List<CommunityPost> findByTypeAndCollectedAtBetweenOrderByCommentsCountDesc(
            CommunityPostType type,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );
}