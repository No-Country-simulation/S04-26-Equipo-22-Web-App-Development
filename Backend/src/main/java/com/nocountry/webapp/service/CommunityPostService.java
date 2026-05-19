package com.nocountry.webapp.service;

import com.nocountry.webapp.entity.CommunityPost;
import com.nocountry.webapp.entity.enums.CommunityPostType;
import com.nocountry.webapp.exception.base.BusinessException;
import com.nocountry.webapp.repository.CommunityPostRepository;
import com.nocountry.webapp.analytics.WeeklyDigestData;
import com.nocountry.webapp.analytics.WeeklyStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityPostService {

    private final CommunityPostRepository communityPostRepository;

    /**
     * Obtener el inicio de la semana actual (lunes 00:00:00)
     */
    public LocalDateTime getStartOfCurrentWeek() {
        return LocalDateTime.now().with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
    }

    /**
     * Obtener el fin de la semana actual (domingo 23:59:59)
     */
    public LocalDateTime getEndOfCurrentWeek() {
        return LocalDateTime.now().with(DayOfWeek.SUNDAY).toLocalDate().atTime(LocalTime.MAX);
    }

    /**
     * Obtener el inicio de una semana específica (lunes 00:00:00)
     */
    public LocalDateTime getStartOfWeek(LocalDateTime date) {
        return date.with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
    }

    /**
     * Obtener el fin de una semana específica (domingo 23:59:59)
     */
    public LocalDateTime getEndOfWeek(LocalDateTime date) {
        return date.with(DayOfWeek.SUNDAY).toLocalDate().atTime(LocalTime.MAX);
    }

    /**
     * 1. Todos los posts de la semana
     */
    public List<CommunityPost> getWeeklyPosts() {
        LocalDateTime start = getStartOfCurrentWeek();
        LocalDateTime end = getEndOfCurrentWeek();
        return communityPostRepository.findByCollectedAtBetween(start, end);
    }

    /**
     * 2. Posts más reaccionados de la semana (para identificar trending topics)
     */
    public List<CommunityPost> getTopReactedPostsOfWeek(int limit) {

        validateLimit(limit);

        LocalDateTime start = getStartOfCurrentWeek();
        LocalDateTime end = getEndOfCurrentWeek();

        log.info("Obteniendo top {} posts más reaccionados de la semana ({})", limit, start);
        
        return communityPostRepository.findByCollectedAtBetweenOrderByReactionsCountDesc(
                start, end, PageRequest.of(0, limit)
        );
    }

    /**
     * 3. Posts más comentados de la semana
     */
    public List<CommunityPost> getTopCommentedPostsOfWeek(int limit) {

         validateLimit(limit);

        LocalDateTime start = getStartOfCurrentWeek();
        LocalDateTime end = getEndOfCurrentWeek();

        log.info("Obteniendo top {} posts más comentados de la semana", limit);
        
        return communityPostRepository.findByCollectedAtBetweenOrderByCommentsCountDesc(
                start, end, PageRequest.of(0, limit)
        );
    }

    /**
     * 4. Preguntas más respondidas (crítico para el MVP)
     */
    public List<CommunityPost> getMostAnsweredQuestionsOfWeek(int limit) {

        validateLimit(limit);

        LocalDateTime start = getStartOfCurrentWeek();
        LocalDateTime end = getEndOfCurrentWeek();

        log.info("Obteniendo top {} preguntas más respondidas de la semana", limit);
        
        return communityPostRepository.findByTypeAndCollectedAtBetweenOrderByCommentsCountDesc(
                CommunityPostType.QUESTION, start, end, PageRequest.of(0, limit)
        );
    }

    /**
     * 5. Recursos más compartidos (para el newsletter)
     */
    public List<CommunityPost> getTopResourcesOfWeek(int limit) {

        validateLimit(limit);  

        LocalDateTime start = getStartOfCurrentWeek();
        LocalDateTime end = getEndOfCurrentWeek();

        log.info("Obteniendo top {} recursos compartidos de la semana", limit);
        
        return communityPostRepository.findByTypeAndCollectedAtBetweenOrderByReactionsCountDesc(
                CommunityPostType.RESOURCE, start, end, PageRequest.of(0, limit)
        );
    }

    /**
     * 6. Sesiones realizadas en la semana
     */
    public List<CommunityPost> getWeeklySessions() {
        LocalDateTime start = getStartOfCurrentWeek();
        LocalDateTime end = getEndOfCurrentWeek();
        return communityPostRepository.findByTypeAndCollectedAtBetween(
                CommunityPostType.SESSION, start, end, PageRequest.of(0, 100)
        );
    }

    /**
     * 7. Discusiones activas de la semana
     */
    public List<CommunityPost> getWeeklyDiscussions() {
        LocalDateTime start = getStartOfCurrentWeek();
        LocalDateTime end = getEndOfCurrentWeek();
        return communityPostRepository.findByTypeAndCollectedAtBetween(
                CommunityPostType.DISCUSSION, start, end, PageRequest.of(0, 100)
        );
    }

    /**
     * 8. Top posts por comunidad específica
     */
    public List<CommunityPost> getTopPostsByCommunity(Long communityId, int limit) {
        
        validateCommunityId(communityId);
        validateLimit(limit);
        
        LocalDateTime start = getStartOfCurrentWeek();
        LocalDateTime end = getEndOfCurrentWeek();

        log.info("Obteniendo top {} posts de la comunidad ID: {}", limit, communityId);
        
        return communityPostRepository.findByCommunityIdAndCollectedAtBetweenOrderByReactionsCountDesc(
                communityId, start, end, PageRequest.of(0, limit)
        );
    }

    /**
     * 9. Posts por comunidad en rango de fechas
     */
    public List<CommunityPost> getPostsByCommunityAndDateRange(Long communityId, LocalDateTime start, LocalDateTime end, int limit) {
        
        validateCommunityId(communityId);
        validateDateRange(start, end);
        validateLimit(limit);
        
        log.info("Obteniendo posts de la comunidad ID: {} en rango de fechas: {} - {}", communityId, start, end);
        
        return communityPostRepository
        .findByCommunityIdAndCollectedAtBetweenOrderByCollectedAtDesc(
                communityId,
                start,
                end,
                PageRequest.of(0, limit)
        );
    }

    /**
     * 10. POST CLAVE PARA IA: Obtener datos agregados semanales para alimentar al LLM
     * Esto devuelve un mapa con las contribuciones más relevantes
     */
    public WeeklyDigestData getWeeklyDigestData(int topLimit) {

            validateLimit(topLimit);

            LocalDateTime weekStart = getStartOfCurrentWeek();
            LocalDateTime weekEnd = getEndOfCurrentWeek();

            log.info(
                "Generando WeeklyDigestData para alimentar al LLM (top {})",
                topLimit
            );

            List<CommunityPost> weeklyPosts =
                    communityPostRepository.findByCollectedAtBetween(
                            weekStart,
                            weekEnd
                    );

            return WeeklyDigestData.builder()
                    .topReactedPosts(
                            communityPostRepository
                                    .findByCollectedAtBetweenOrderByReactionsCountDesc(
                                            weekStart,
                                            weekEnd,
                                            PageRequest.of(0, topLimit)
                                    )
                    )
                    .topCommentedPosts(
                            communityPostRepository
                                    .findByCollectedAtBetweenOrderByCommentsCountDesc(
                                            weekStart,
                                            weekEnd,
                                            PageRequest.of(0, topLimit)
                                    )
                    )
                    .mostAnsweredQuestions(
                            communityPostRepository
                                    .findByTypeAndCollectedAtBetweenOrderByCommentsCountDesc(
                                            CommunityPostType.QUESTION,
                                            weekStart,
                                            weekEnd,
                                            PageRequest.of(0, topLimit)
                                    )
                    )
                    .topResources(
                            communityPostRepository
                                    .findByTypeAndCollectedAtBetweenOrderByReactionsCountDesc(
                                            CommunityPostType.RESOURCE,
                                            weekStart,
                                            weekEnd,
                                            PageRequest.of(0, topLimit)
                                    )
                    )
                    .weeklySessions(
                            communityPostRepository.findByTypeAndCollectedAtBetween(
                                    CommunityPostType.SESSION,
                                    weekStart,
                                    weekEnd,
                                    PageRequest.of(0, 100)
                            )
                    )
                    .weeklyDiscussions(
                            communityPostRepository.findByTypeAndCollectedAtBetween(
                                    CommunityPostType.DISCUSSION,
                                    weekStart,
                                    weekEnd,
                                    PageRequest.of(0, 100)
                            )
                    )
                    .totalPosts(weeklyPosts.size())
                    .weekStart(weekStart)
                    .weekEnd(weekEnd)
                    .build();
        }

    /**
     * 11. Filtrar posts por tipo y fecha (genérico)
     */
    public List<CommunityPost> getPostsByTypeAndDateRange(CommunityPostType type, LocalDateTime start, LocalDateTime end, int limit) {
        
        validatePostType(type);
        validateLimit(limit);
        validateDateRange(start, end);
        

        log.info("Obteniendo posts de tipo {} en rango de fechas: {} - {}", type, start, end);

        return communityPostRepository.findByTypeAndCollectedAtBetween(type, start, end, PageRequest.of(0, limit));
    }

     /**
      * 12. Obtener posts por rango de fechas
      */
        public List<CommunityPost> getPostsByDateRange(
                LocalDateTime start,
                LocalDateTime end,
                int limit
        ) {

        validateDateRange(start, end);
        validateLimit(limit);

        log.info(
                "Obteniendo posts en rango de fechas: {} - {}",
                start,
                end
        );

        return communityPostRepository
            .findByCollectedAtBetweenOrderByCollectedAtDesc(
                    start,
                    end,
                    PageRequest.of(0, limit)
            );
        }

    /**
     * 13. Verificar si hay actividad en la semana
     */
    public boolean hasWeeklyActivity() {
        return !getWeeklyPosts().isEmpty();
    }

    /**
     * 14. Obtener estadísticas resumidas para el editor
     */
    public WeeklyStatistics getWeeklyStatistics() {

            LocalDateTime weekStart = getStartOfCurrentWeek();
            LocalDateTime weekEnd = getEndOfCurrentWeek();

            List<CommunityPost> allPosts =
                    communityPostRepository.findByCollectedAtBetween(
                            weekStart,
                            weekEnd
                    );

            long totalQuestions = allPosts.stream()
                    .filter(p -> p.getType() == CommunityPostType.QUESTION)
                    .count();

            long totalResources = allPosts.stream()
                    .filter(p -> p.getType() == CommunityPostType.RESOURCE)
                    .count();

            long totalSessions = allPosts.stream()
                    .filter(p -> p.getType() == CommunityPostType.SESSION)
                    .count();

            long totalDiscussions = allPosts.stream()
                    .filter(p -> p.getType() == CommunityPostType.DISCUSSION)
                    .count();

            int totalReactions = allPosts.stream()
                    .mapToInt(CommunityPost::getReactionsCount)
                    .sum();

            int totalComments = allPosts.stream()
                    .mapToInt(CommunityPost::getCommentsCount)
                    .sum();

            return WeeklyStatistics.builder()
                    .totalPosts(allPosts.size())
                    .totalQuestions(totalQuestions)
                    .totalResources(totalResources)
                    .totalSessions(totalSessions)
                    .totalDiscussions(totalDiscussions)
                    .totalReactions(totalReactions)
                    .totalComments(totalComments)
                    .weekStart(weekStart)
                    .weekEnd(weekEnd)
                    .build();
        }

    
    // ==================== VALIDATIONS ====================

    /**
     * Valida que el límite sea válido
     */
    private void validateLimit(int limit) {
        if (limit <= 0) {
            throw new BusinessException(
                    "El límite debe ser mayor a cero"
            );
        }
    }

    /**
    * Valida que el ID de comunidad sea válido
    */
    private void validateCommunityId(Long communityId) {
        if (communityId == null || communityId <= 0) {
            throw new BusinessException(
                    "El ID de comunidad es inválido"
            );
        }
    }

    /**
    * Valida que el rango de fechas sea válido
    */
    private void validateDateRange(
                LocalDateTime start,
                LocalDateTime end
        ) {

            if (start == null || end == null) {
                throw new BusinessException(
                        "Las fechas no pueden ser nulas"
                );
            }

            if (start.isAfter(end)) {
                throw new BusinessException(
                        "La fecha de inicio no puede ser posterior a la fecha fin"
                );
            }
        }
    
    /**
     * Valida que el tipo de post sea válido
     */
    private void validatePostType(CommunityPostType type) {

        if (type == null) {
            throw new BusinessException(
                    "El tipo de post es obligatorio"
            );
        }
    }

   
        
}