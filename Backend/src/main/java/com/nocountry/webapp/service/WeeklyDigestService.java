package com.nocountry.webapp.service;

import com.nocountry.webapp.entity.Community;
import com.nocountry.webapp.entity.WeeklyDigest;
import com.nocountry.webapp.entity.enums.WeeklyDigestStatus;
import com.nocountry.webapp.exception.base.BusinessException;
import com.nocountry.webapp.exception.base.ConflictException;
import com.nocountry.webapp.exception.base.InvalidStateException;
import com.nocountry.webapp.exception.base.NotFoundException;
import com.nocountry.webapp.repository.CommunityRepository;
import com.nocountry.webapp.repository.WeeklyDigestRepository;
import com.nocountry.webapp.service.dto.WeeklyDigestGenerationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeeklyDigestService {

    private final WeeklyDigestRepository weeklyDigestRepository;
    private final CommunityRepository communityRepository;

    @Value("${weekly-digest.page-size:20}")
    private int defaultPageSize;

    /**
     * Genera un nuevo digest semanal para una comunidad
     */
    @Transactional
    public WeeklyDigest generateWeeklyDigest(WeeklyDigestGenerationRequest request) {
        log.info("Generando digest semanal para comunidad ID: {}", request.getCommunityId());

        Community community = communityRepository.findById(request.getCommunityId())
                .orElseThrow(() -> new NotFoundException("Comunidad no encontrada con ID: " + request.getCommunityId()));

        // Si no vienen fechas, calcular semana actual
        LocalDate weekStart = (request.getWeekStart() != null) ? request.getWeekStart() : calculateWeekStart();
        LocalDate weekEnd = (request.getWeekEnd() != null) ? request.getWeekEnd() : calculateWeekEnd(weekStart);

        // Validar que weekEnd sea después de weekStart
        if (weekEnd.isBefore(weekStart)) {
            throw new BusinessException("La fecha de fin de semana no puede ser anterior a la fecha de inicio");
        }

        // Verificar duplicado por comunidad y semana
        if (weeklyDigestRepository.existsByCommunityIdAndWeekStart(request.getCommunityId(), weekStart)) {
            throw new ConflictException(
                    String.format("Ya existe un digest para la comunidad '%s' en la semana que inició el %s",
                            community.getName(), weekStart));
        }

        try {
            // Crear el digest
            WeeklyDigest digest = WeeklyDigest.builder()
                    .community(community)
                    .weekStart(weekStart)
                    .weekEnd(weekEnd)
                    .status(WeeklyDigestStatus.PENDING)
                    .build();

            WeeklyDigest saved = weeklyDigestRepository.save(digest);
            log.info("Digest generado exitosamente con ID: {}", saved.getId());
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
        log.info("Iniciando generación masiva de digests");

        List<Community> activeCommunities = communityRepository.findByActiveTrue();
        LocalDate weekStart = calculateWeekStart();
        LocalDate weekEnd = calculateWeekEnd(weekStart);

        List<WeeklyDigest> generated = activeCommunities.stream()
                .map(community -> {
                    try {
                        // Verificar si ya existe
                        if (weeklyDigestRepository.existsByCommunityIdAndWeekStart(community.getId(), weekStart)) {
                            log.warn("Comunidad {} ya tiene digest para la semana {}", community.getName(), weekStart);
                            return null;
                        }

                        WeeklyDigest digest = WeeklyDigest.builder()
                                .community(community)
                                .weekStart(weekStart)
                                .weekEnd(weekEnd)
                                .status(WeeklyDigestStatus.PENDING)
                                .build();

                        return weeklyDigestRepository.save(digest);
                    } catch (Exception e) {
                        log.error("Error generando digest para comunidad {}: {}", community.getId(), e.getMessage());
                        return null;
                    }
                })
                .filter(d -> d != null)
                .toList();

        log.info("Generados {} digests de {} comunidades activas", generated.size(), activeCommunities.size());
        return generated;
    }

    /**
     * Aprueba un digest (cambia estado a PROCESSED)
     */
    @Transactional
    public WeeklyDigest approveDigest(Long digestId) {
        log.info("Aprobando digest ID: {}", digestId);

        WeeklyDigest digest = weeklyDigestRepository.findById(digestId)
                .orElseThrow(() -> new NotFoundException("Digest no encontrado con ID: " + digestId));

        if (digest.getStatus() == WeeklyDigestStatus.PROCESSED) {
            throw new InvalidStateException("El digest ya fue procesado anteriormente");
        }

        digest.setStatus(WeeklyDigestStatus.PROCESSED);
        WeeklyDigest saved = weeklyDigestRepository.save(digest);
        
        log.info("Digest {} aprobado exitosamente", digestId);
        return saved;
    }

    /**
     * Actualiza el resumen (summary) de un digest
     */
    @Transactional
    public WeeklyDigest updateSummary(Long digestId, String summary) {
        log.info("Actualizando resumen del digest ID: {}", digestId);

        WeeklyDigest digest = weeklyDigestRepository.findById(digestId)
                .orElseThrow(() -> new NotFoundException("Digest no encontrado con ID: " + digestId));

        if (digest.getStatus() == WeeklyDigestStatus.PROCESSED) {
            throw new InvalidStateException("No se puede editar un digest ya procesado");
        }

        digest.setSummary(summary);
        return weeklyDigestRepository.save(digest);
    }

    /**
     * Elimina un digest (solo si está PENDING)
     */
    @Transactional
    public void deleteDigest(Long digestId) {
        log.info("Eliminando digest ID: {}", digestId);

        WeeklyDigest digest = weeklyDigestRepository.findById(digestId)
                .orElseThrow(() -> new NotFoundException("Digest no encontrado con ID: " + digestId));

        if (digest.getStatus() == WeeklyDigestStatus.PROCESSED) {
            throw new InvalidStateException("No se puede eliminar un digest ya procesado");
        }

        weeklyDigestRepository.delete(digest);
        log.info("Digest {} eliminado exitosamente", digestId);
    }

    /**
     * Obtiene un digest por ID
     */
    public WeeklyDigest getDigestById(Long digestId) {
        return weeklyDigestRepository.findById(digestId)
                .orElseThrow(() -> new NotFoundException("Digest no encontrado con ID: " + digestId));
    }

    /**
     * Obtiene digest por comunidad y semana
     */
    public WeeklyDigest getDigestByCommunityAndWeek(Long communityId, LocalDate weekStart, LocalDate weekEnd) {
        return weeklyDigestRepository.findByCommunityIdAndWeekStartAndWeekEnd(communityId, weekStart, weekEnd)
                .orElseThrow(() -> new NotFoundException(
                        String.format("No existe digest para comunidad %s en la semana del %s al %s",
                                communityId, weekStart, weekEnd)));
    }

    /**
     * Lista digests pendientes (PENDING)
     */
    public List<WeeklyDigest> getPendingDigests(int page, int size) {
        Pageable pageable = PageRequest.of(page, size > 0 ? size : defaultPageSize);
        return weeklyDigestRepository.findByStatusOrderByCreatedAtDesc(WeeklyDigestStatus.PENDING, pageable);
    }

    /**
     * Historial de digests por comunidad
     */
    public List<WeeklyDigest> getCommunityDigestHistory(Long communityId, int page, int size) {
        if (!communityRepository.existsById(communityId)) {
            throw new NotFoundException("Comunidad no encontrada con ID: " + communityId);
        }

        Pageable pageable = PageRequest.of(page, size > 0 ? size : defaultPageSize);
        return weeklyDigestRepository.findByCommunityIdOrderByWeekStartDesc(communityId, pageable);
    }

    /**
     * Últimos digests generados (todos)
     */
    public List<WeeklyDigest> getLatestDigests(int page, int size) {
        Pageable pageable = PageRequest.of(page, size > 0 ? size : defaultPageSize);
        return weeklyDigestRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    /**
     * Digests por rango de fechas
     */
    public List<WeeklyDigest> getDigestsByDateRange(LocalDate startDate, LocalDate endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size > 0 ? size : defaultPageSize);
        return weeklyDigestRepository.findByWeekStartBetweenOrderByWeekStartDesc(startDate, endDate, pageable);
    }

    /**
     * Cuenta digests pendientes
     */
    public long countPendingDigests() {
        return weeklyDigestRepository.countByStatus(WeeklyDigestStatus.PENDING);
    }

    /**
     * Recupera digests no procesados de semanas anteriores
     */
    public List<WeeklyDigest> recoverUnprocessedDigests(LocalDate currentDate, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return weeklyDigestRepository.findUnprocessedDigestsOlderThan(
                WeeklyDigestStatus.PENDING, currentDate, pageable);
    }

    /**
     * Verifica si una comunidad ya tiene digest para la semana actual
     */
    public boolean hasDigestForCurrentWeek(Long communityId) {
        LocalDate weekStart = calculateWeekStart();
        return weeklyDigestRepository.existsByCommunityIdAndWeekStart(communityId, weekStart);
    }

    /**
     * Calcula inicio de semana (lunes)
     */
    private LocalDate calculateWeekStart() {
        return LocalDate.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
    }

    /**
     * Calcula fin de semana (domingo)
     */
    private LocalDate calculateWeekEnd(LocalDate weekStart) {
        return weekStart.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));
    }
}