package com.nocountry.webapp.service;

import com.nocountry.webapp.analytics.WeeklyStatistics;
import com.nocountry.webapp.entity.ChannelDraft;
import com.nocountry.webapp.entity.Community;
import com.nocountry.webapp.entity.CommunityPost;
import com.nocountry.webapp.entity.WeeklyDigest;
import com.nocountry.webapp.entity.enums.CommunityPostType;
import com.nocountry.webapp.entity.enums.WeeklyDigestStatus;
import com.nocountry.webapp.exception.base.BusinessException;
import com.nocountry.webapp.exception.base.ConflictException;
import com.nocountry.webapp.exception.base.InvalidStateException;
import com.nocountry.webapp.exception.base.NotFoundException;
import com.nocountry.webapp.repository.CommunityPostRepository;
import com.nocountry.webapp.repository.CommunityRepository;
import com.nocountry.webapp.repository.WeeklyDigestRepository;
import com.nocountry.webapp.service.ai.GeminiService;
import com.nocountry.webapp.service.ai.dto.GeneratedContentDTO;
import com.nocountry.webapp.service.ai.dto.WeeklyDigestContextDTO;
import com.nocountry.webapp.service.ai.dto.WeeklyDigestContextDTO.PostSummary;
import com.nocountry.webapp.service.dto.WeeklyDigestGenerationRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeeklyDigestService {

    private final WeeklyDigestRepository weeklyDigestRepository;
    private final CommunityRepository communityRepository;
    private final CommunityPostRepository communityPostRepository;

    private final GeminiService geminiService;
    private final ChannelDraftService channelDraftService;

    @Value("${weekly-digest.page-size:20}")
    private int defaultPageSize;

    private final Clock clock;

    /**
     * Genera digest completo con IA + drafts
     */
    @Transactional
    public WeeklyDigest generateCompleteDigestWithAI(
            Long communityId,
            Long editorId
    ) {

        log.info(
                "Generando digest completo IA para comunidad {}",
                communityId
        );

        WeeklyDigestGenerationRequest request =
                WeeklyDigestGenerationRequest.builder()
                        .communityId(communityId)
                        .build();

        WeeklyDigest digest =
                generateWeeklyDigest(request);

        WeeklyDigestContextDTO context =
                buildAIContext(digest);

        GeneratedContentDTO generatedContent =
                geminiService.generateWeeklyDigestContent(
                        context
                );

        digest.setSummary(
                generatedContent.getWeeklySummary()
        );

        channelDraftService.createCompleteDrafts(
                digest.getId(),
                generatedContent.getWeeklySummary(),
                generatedContent.getLinkedinPost(),
                generatedContent.getTwitterPost(),
                editorId
        );

        digest.setStatus(
                WeeklyDigestStatus.PROCESSED
        );

        WeeklyDigest saved =
                weeklyDigestRepository.save(digest);

        log.info(
                "Digest IA generado correctamente {}",
                saved.getId()
        );

        return saved;
    }

    /**
     * Regenera drafts IA para un digest existente
     */
    @Transactional
    public List<ChannelDraft> generateDraftsForDigest(
            Long digestId,
            Long editorId
    ) {

        WeeklyDigest digest =
                getDigestById(digestId);

        WeeklyDigestContextDTO context =
                buildAIContext(digest);

        GeneratedContentDTO generatedContent =
                geminiService.generateWeeklyDigestContent(
                        context
                );

        return channelDraftService.createCompleteDrafts(
                digest.getId(),
                generatedContent.getWeeklySummary(),
                generatedContent.getLinkedinPost(),
                generatedContent.getTwitterPost(),
                editorId
        );
    }

    /**
     * Construye el contexto para IA
     */
    private WeeklyDigestContextDTO buildAIContext(
            WeeklyDigest digest
    ) {

        LocalDateTime start =
                digest.getWeekStart().atStartOfDay();

        LocalDateTime end =
                digest.getWeekEnd().atTime(
                        23,
                        59,
                        59
                );

        List<CommunityPost> posts =
                communityPostRepository
                        .findByCommunityIdAndCollectedAtBetween(
                                digest.getCommunity().getId(),
                                start,
                                end
                        );

        WeeklyStatistics statistics =
                calculateStatistics(posts);

        List<PostSummary> topReacted =
                posts.stream()
                        .sorted((a, b) ->
                                Integer.compare(
                                        b.getReactionsCount(),
                                        a.getReactionsCount()
                                )
                        )
                        .limit(5)
                        .map(this::toPostSummary)
                        .toList();

        List<PostSummary> topCommented =
                posts.stream()
                        .filter(post ->
                                post.getType() ==
                                        CommunityPostType.QUESTION
                        )
                        .sorted((a, b) ->
                                Integer.compare(
                                        b.getCommentsCount(),
                                        a.getCommentsCount()
                                )
                        )
                        .limit(3)
                        .map(this::toPostSummary)
                        .toList();

        List<PostSummary> topResources =
                posts.stream()
                        .filter(post ->
                                post.getType() ==
                                        CommunityPostType.RESOURCE
                        )
                        .limit(5)
                        .map(this::toPostSummary)
                        .toList();

        List<PostSummary> topDiscussions =
                posts.stream()
                        .filter(post ->
                                post.getType() ==
                                        CommunityPostType.DISCUSSION
                        )
                        .sorted((a, b) ->
                                Integer.compare(
                                        b.getCommentsCount(),
                                        a.getCommentsCount()
                                )
                        )
                        .limit(3)
                        .map(this::toPostSummary)
                        .toList();

        return WeeklyDigestContextDTO.builder()
                .communityName(
                        digest.getCommunity().getName()
                )
                .weekStart(digest.getWeekStart())
                .weekEnd(digest.getWeekEnd())
                .topReacted(topReacted)
                .topCommented(topCommented)
                .topResources(topResources)
                .topDiscussions(topDiscussions)
                .statistics(statistics)
                .build();
    }

    /**
     * Mapper CommunityPost -> PostSummary
     */
    private PostSummary toPostSummary(
            CommunityPost post
    ) {

        return PostSummary.builder()
                .title(
                        extractTitle(post.getContent())
                )
                .author(post.getAuthorName())
                .content(post.getContent())
                .reactions(post.getReactionsCount())
                .comments(post.getCommentsCount())
                .type(post.getType().name())
                .build();
    }

    /**
     * Calcula estadísticas semanales
     */
    private WeeklyStatistics calculateStatistics(
            List<CommunityPost> posts
    ) {

        return WeeklyStatistics.builder()
                .totalPosts(
                        posts.size()
                )
                .totalReactions(
                        posts.stream()
                                .mapToInt(
                                        CommunityPost::getReactionsCount
                                )
                                .sum()
                )
                .totalComments(
                        posts.stream()
                                .mapToInt(
                                        CommunityPost::getCommentsCount
                                )
                                .sum()
                )
                .totalQuestions(
                        posts.stream()
                                .filter(post ->
                                        post.getType() ==
                                                CommunityPostType.QUESTION
                                )
                                .count()
                )
                .totalResources(
                        posts.stream()
                                .filter(post ->
                                        post.getType() ==
                                                CommunityPostType.RESOURCE
                                )
                                .count()
                )
                .totalDiscussions(
                        posts.stream()
                                .filter(post ->
                                        post.getType() ==
                                                CommunityPostType.DISCUSSION
                                )
                                .count()
                )
                .build();
    }

    /**
     * Extrae título desde contenido
     */
    private String extractTitle(
            String content
    ) {

        if (content == null || content.isBlank()) {
            return "Sin título";
        }

        return content.length() > 100
                ? content.substring(0, 100) + "..."
                : content;
    }

    /**
     * Genera un nuevo digest semanal para una comunidad
     */
    @Transactional
    public WeeklyDigest generateWeeklyDigest(
            WeeklyDigestGenerationRequest request
    ) {

        log.info(
                "Generando digest semanal para comunidad ID: {}",
                request.getCommunityId()
        );

        Community community =
                communityRepository.findById(
                                request.getCommunityId()
                        )
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Comunidad no encontrada con ID: "
                                                + request.getCommunityId()
                                )
                        );

        LocalDate weekStart =
                request.getWeekStart() != null
                        ? request.getWeekStart()
                        : calculateWeekStart();

        LocalDate weekEnd =
                request.getWeekEnd() != null
                        ? request.getWeekEnd()
                        : calculateWeekEnd(weekStart);

        if (weekEnd.isBefore(weekStart)) {
            throw new BusinessException(
                    "La fecha de fin de semana no puede ser anterior a la fecha de inicio"
            );
        }

        if (weekStart.getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new BusinessException(
                    "weekStart debe ser lunes"
            );
        }

        if (weekEnd.getDayOfWeek() != DayOfWeek.SUNDAY) {
            throw new BusinessException(
                    "weekEnd debe ser domingo"
            );
        }

        if (weeklyDigestRepository.existsByCommunityIdAndWeekStart(
                request.getCommunityId(),
                weekStart
        )) {

            throw new ConflictException(
                    String.format(
                            "Ya existe un digest para la comunidad '%s' en la semana que inició el %s",
                            community.getName(),
                            weekStart
                    )
            );
        }

        try {

            WeeklyDigest digest =
                    WeeklyDigest.builder()
                            .community(community)
                            .weekStart(weekStart)
                            .weekEnd(weekEnd)
                            .status(WeeklyDigestStatus.PENDING)
                            .build();

            WeeklyDigest saved =
                    weeklyDigestRepository.save(digest);

            log.info(
                    "Digest generado exitosamente con ID: {}",
                    saved.getId()
            );

            return saved;

        } catch (DataIntegrityViolationException e) {

            throw new ConflictException(
                    "Ya existe un digest para esa comunidad y semana"
            );
        }
    }

    /**
     * Genera digests para todas las comunidades activas
     */
    @Transactional
    public List<WeeklyDigest> generateDigestsForAllActiveCommunities() {

        log.info(
                "Iniciando generación masiva de digests"
        );

        List<Community> activeCommunities =
                communityRepository.findByActiveTrue();

        LocalDate weekStart =
                calculateWeekStart();

        LocalDate weekEnd =
                calculateWeekEnd(weekStart);

        List<WeeklyDigest> generated =
                activeCommunities.stream()
                        .map(community -> {

                            try {

                                if (weeklyDigestRepository
                                        .existsByCommunityIdAndWeekStart(
                                                community.getId(),
                                                weekStart
                                        )) {

                                    log.warn(
                                            "Comunidad {} ya tiene digest para la semana {}",
                                            community.getName(),
                                            weekStart
                                    );

                                    return null;
                                }

                                WeeklyDigest digest =
                                        WeeklyDigest.builder()
                                                .community(community)
                                                .weekStart(weekStart)
                                                .weekEnd(weekEnd)
                                                .status(
                                                        WeeklyDigestStatus.PENDING
                                                )
                                                .build();

                                return weeklyDigestRepository
                                        .save(digest);

                            } catch (Exception e) {

                                log.error(
                                        "Error generando digest para comunidad {}: {}",
                                        community.getId(),
                                        e.getMessage()
                                );

                                return null;
                            }
                        })
                        .filter(d -> d != null)
                        .toList();

        log.info(
                "Generados {} digests de {} comunidades activas",
                generated.size(),
                activeCommunities.size()
        );

        return generated;
    }

    /**
     * Marca un digest como procesado
     */
    @Transactional
    public WeeklyDigest processDigest(
            Long digestId
    ) {

        log.info(
                "Procesando digest ID: {}",
                digestId
        );

        WeeklyDigest digest =
                weeklyDigestRepository.findById(digestId)
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Digest no encontrado con ID: "
                                                + digestId
                                )
                        );

        if (digest.getStatus() ==
                WeeklyDigestStatus.PROCESSED) {

            throw new InvalidStateException(
                    "El digest ya fue procesado anteriormente"
            );
        }

        digest.setStatus(
                WeeklyDigestStatus.PROCESSED
        );

        WeeklyDigest saved =
                weeklyDigestRepository.save(digest);

        log.info(
                "Digest {} procesado exitosamente",
                digestId
        );

        return saved;
    }

    /**
     * Actualiza resumen
     */
    @Transactional
    public WeeklyDigest updateSummary(
            Long digestId,
            String summary
    ) {

        log.info(
                "Actualizando resumen del digest ID: {}",
                digestId
        );

        WeeklyDigest digest =
                weeklyDigestRepository.findById(digestId)
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Digest no encontrado con ID: "
                                                + digestId
                                )
                        );

        if (digest.getStatus() ==
                WeeklyDigestStatus.PROCESSED) {

            throw new InvalidStateException(
                    "No se puede editar un digest ya procesado"
            );
        }

        digest.setSummary(summary);

        return weeklyDigestRepository.save(digest);
    }

    /**
     * Elimina digest
     */
    @Transactional
    public void deleteDigest(
            Long digestId
    ) {

        log.info(
                "Eliminando digest ID: {}",
                digestId
        );

        WeeklyDigest digest =
                weeklyDigestRepository.findById(digestId)
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Digest no encontrado con ID: "
                                                + digestId
                                )
                        );

        if (digest.getStatus() ==
                WeeklyDigestStatus.PROCESSED) {

            throw new InvalidStateException(
                    "No se puede eliminar un digest ya procesado"
            );
        }

        weeklyDigestRepository.delete(digest);

        log.info(
                "Digest {} eliminado exitosamente",
                digestId
        );
    }

    /**
     * Obtiene digest por ID
     */
    public WeeklyDigest getDigestById(
            Long digestId
    ) {

        return weeklyDigestRepository.findById(digestId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Digest no encontrado con ID: "
                                        + digestId
                        )
                );
    }

    /**
     * Obtiene digest por comunidad y semana
     */
    public WeeklyDigest getDigestByCommunityAndWeek(
            Long communityId,
            LocalDate weekStart,
            LocalDate weekEnd
    ) {

        return weeklyDigestRepository
                .findByCommunityIdAndWeekStartAndWeekEnd(
                        communityId,
                        weekStart,
                        weekEnd
                )
                .orElseThrow(() ->
                        new NotFoundException(
                                String.format(
                                        "No existe digest para comunidad %s en la semana del %s al %s",
                                        communityId,
                                        weekStart,
                                        weekEnd
                                )
                        )
                );
    }

    /**
     * Lista pendientes
     */
    public List<WeeklyDigest> getPendingDigests(
            int page,
            int size
    ) {

        Pageable pageable =
                PageRequest.of(
                        page,
                        size > 0 ? size : defaultPageSize
                );

        return weeklyDigestRepository
                .findByStatusOrderByCreatedAtDesc(
                        WeeklyDigestStatus.PENDING,
                        pageable
                );
    }

    /**
     * Historial por comunidad
     */
    public List<WeeklyDigest> getCommunityDigestHistory(
            Long communityId,
            int page,
            int size
    ) {

        if (!communityRepository.existsById(
                communityId
        )) {

            throw new NotFoundException(
                    "Comunidad no encontrada con ID: "
                            + communityId
            );
        }

        Pageable pageable =
                PageRequest.of(
                        page,
                        size > 0 ? size : defaultPageSize
                );

        return weeklyDigestRepository
                .findByCommunityIdOrderByWeekStartDesc(
                        communityId,
                        pageable
                );
    }

    /**
     * Últimos digests
     */
    public List<WeeklyDigest> getLatestDigests(
            int page,
            int size
    ) {

        Pageable pageable =
                PageRequest.of(
                        page,
                        size > 0 ? size : defaultPageSize
                );

        return weeklyDigestRepository
                .findAllByOrderByCreatedAtDesc(
                        pageable
                );
    }

    /**
     * Digests por rango
     */
    public List<WeeklyDigest> getDigestsByDateRange(
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size
    ) {

        Pageable pageable =
                PageRequest.of(
                        page,
                        size > 0 ? size : defaultPageSize
                );

        return weeklyDigestRepository
                .findByWeekStartBetweenOrderByWeekStartDesc(
                        startDate,
                        endDate,
                        pageable
                );
    }

    /**
     * Cuenta pendientes
     */
    public long countPendingDigests() {

        return weeklyDigestRepository.countByStatus(
                WeeklyDigestStatus.PENDING
        );
    }

    /**
     * Recupera digests pendientes viejos
     */
    public List<WeeklyDigest> recoverUnprocessedDigests(
            LocalDate currentDate,
            int limit
    ) {

        Pageable pageable =
                PageRequest.of(0, limit);

        return weeklyDigestRepository
                .findUnprocessedDigestsOlderThan(
                        WeeklyDigestStatus.PENDING,
                        currentDate,
                        pageable
                );
    }

    /**
     * Verifica digest actual
     */
    public boolean hasDigestForCurrentWeek(
            Long communityId
    ) {

        LocalDate weekStart =
                calculateWeekStart();

        return weeklyDigestRepository
                .existsByCommunityIdAndWeekStart(
                        communityId,
                        weekStart
                );
    }

    /**
     * Inicio de semana
     */
    private LocalDate calculateWeekStart() {

        return LocalDate.now(clock)
                .with(
                        TemporalAdjusters.previousOrSame(
                                DayOfWeek.MONDAY
                        )
                );
    }

    /**
     * Fin de semana
     */
    private LocalDate calculateWeekEnd(
            LocalDate weekStart
    ) {

        return weekStart.with(
                TemporalAdjusters.nextOrSame(
                        DayOfWeek.SUNDAY
                )
        );
    }
}