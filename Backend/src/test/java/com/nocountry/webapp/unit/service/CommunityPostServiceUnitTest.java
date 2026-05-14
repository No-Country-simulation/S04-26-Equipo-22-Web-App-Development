package com.nocountry.webapp.unit.service;

import com.nocountry.webapp.analytics.WeeklyDigestData;
import com.nocountry.webapp.analytics.WeeklyStatistics;
import com.nocountry.webapp.entity.Community;
import com.nocountry.webapp.entity.CommunityPost;
import com.nocountry.webapp.entity.enums.CommunityPostType;
import com.nocountry.webapp.exception.base.BusinessException;
import com.nocountry.webapp.repository.CommunityPostRepository;
import com.nocountry.webapp.service.CommunityPostService;
import com.nocountry.webapp.unit.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit Test para CommunityPostService.
 * 
 * Por qué unit test:
 * - Probamos la LÓGICA del servicio, no la base de datos
 * - Los mocks aislan completamente el servicio de sus dependencias
 * - Son más rápidos (no levantan Spring Context)
 * - Si falla, sabemos que el error está en el servicio, no en la DB
 */
class CommunityPostServiceUnitTest extends BaseUnitTest {

    // @Mock: Crea un objeto falso que simula el comportamiento del repositorio
    // NO usa base de datos real. Mockito crea este mock y luego lo inyecta en el servicio
    @Mock
    private CommunityPostRepository communityPostRepository;

    // @InjectMocks: Inyecta los mocks (@Mock) dentro del servicio real
    // CommunityPostService REAL recibe CommunityPostRepository MOCKEADO
    @InjectMocks
    private CommunityPostService communityPostService;

    // Objeto de prueba reusable
    private Community community;
    private CommunityPost post1;
    private CommunityPost post2;
    private CommunityPost post3;
    private LocalDateTime mondayMorning;


    // @BeforeEach: Se ejecuta ANTES de cada test
    @BeforeEach
    void setUp() {
        // Fechas de la semana actual para pruebas
        mondayMorning = LocalDateTime.now().with(java.time.DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
        // Crear comunidad de prueba
        community = new Community();
        community.setId(1L);
        community.setName("Backend Developers");
        community.setPlatform("DISCORD");
        community.setActive(true);

        // Crear posts de prueba con diferentes métricas
        post1 = new CommunityPost();
        post1.setId(1L);
        post1.setContent("¿Cómo optimizar consultas SQL?");
        post1.setAuthorName("Maria Garcia");
        post1.setType(CommunityPostType.QUESTION);
        post1.setReactionsCount(45);
        post1.setCommentsCount(12);
        post1.setCollectedAt(mondayMorning.plusDays(1));
        post1.setCommunity(community);

        post2 = new CommunityPost();
        post2.setId(2L);
        post2.setContent("Guía completa de Spring Boot 3");
        post2.setAuthorName("Juan Perez");
        post2.setType(CommunityPostType.RESOURCE);
        post2.setReactionsCount(89);
        post2.setCommentsCount(5);
        post2.setCollectedAt(mondayMorning.plusDays(2));
        post2.setCommunity(community);

        post3 = new CommunityPost();
        post3.setId(3L);
        post3.setContent("Sesión: Microservicios con Kafka");
        post3.setAuthorName("Laura Martinez");
        post3.setType(CommunityPostType.SESSION);
        post3.setReactionsCount(67);
        post3.setCommentsCount(8);
        post3.setCollectedAt(mondayMorning.plusDays(3));
        post3.setCommunity(community);
    }

    // ==================== TESTS DE FECHAS ====================

    @Test
    @DisplayName("getStartOfCurrentWeek - Debe devolver el lunes 00:00:00")
    void getStartOfCurrentWeek_ShouldReturnMondayMidnight() {
        // ACT
        LocalDateTime result = communityPostService.getStartOfCurrentWeek();

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getDayOfWeek()).isEqualTo(java.time.DayOfWeek.MONDAY);
        assertThat(result.getHour()).isEqualTo(0);
        assertThat(result.getMinute()).isEqualTo(0);
        assertThat(result.getSecond()).isEqualTo(0);
    }

    @Test
    @DisplayName("getEndOfCurrentWeek - Debe devolver el domingo 23:59:59")
    void getEndOfCurrentWeek_ShouldReturnSundayMaxTime() {
        // ACT
        LocalDateTime result = communityPostService.getEndOfCurrentWeek();

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getDayOfWeek()).isEqualTo(java.time.DayOfWeek.SUNDAY);
        assertThat(result.getHour()).isEqualTo(23);
        assertThat(result.getMinute()).isEqualTo(59);
        assertThat(result.getSecond()).isEqualTo(59);
    }

    // ==================== TESTS DE OBTENCIÓN DE POSTS ====================

    @Test
    @DisplayName("getWeeklyPosts - Debe devolver todos los posts de la semana")
    void getWeeklyPosts_ShouldReturnAllWeeklyPosts() {
        // ARRANGE
        List<CommunityPost> expectedPosts = List.of(post1, post2, post3);
        when(communityPostRepository.findByCollectedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(expectedPosts);

        // ACT
        List<CommunityPost> result = communityPostService.getWeeklyPosts();

        // ASSERT
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly(post1, post2, post3);
        verify(communityPostRepository, times(1)).findByCollectedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("getWeeklyPosts - Cuando no hay posts, debe devolver lista vacía")
    void getWeeklyPosts_WhenNoPosts_ShouldReturnEmptyList() {
        // ARRANGE
        when(communityPostRepository.findByCollectedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        // ACT
        List<CommunityPost> result = communityPostService.getWeeklyPosts();

        // ASSERT
        assertThat(result).isEmpty();
        verify(communityPostRepository, times(1)).findByCollectedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    // ==================== TESTS TOP REACTED POSTS ====================

    @Test
    @DisplayName("getTopReactedPostsOfWeek - Debe devolver los posts más reaccionados")
    void getTopReactedPostsOfWeek_ShouldReturnTopReactedPosts() {
        // ARRANGE
        List<CommunityPost> expectedPosts = List.of(post2, post3, post1); // post2 tiene 89 reacciones
        when(communityPostRepository.findByCollectedAtBetweenOrderByReactionsCountDesc(
                any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(expectedPosts);

        // ACT
        List<CommunityPost> result = communityPostService.getTopReactedPostsOfWeek(3);

        // ASSERT
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getReactionsCount()).isEqualTo(89); // El primero es el más reaccionado
        verify(communityPostRepository, times(1))
                .findByCollectedAtBetweenOrderByReactionsCountDesc(any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class));
    }

    @Test
    @DisplayName("getTopReactedPostsOfWeek - Con límite inválido debe lanzar BusinessException")
    void getTopReactedPostsOfWeek_WithInvalidLimit_ShouldThrowBusinessException() {
        // ACT & ASSERT
        assertThatThrownBy(() -> communityPostService.getTopReactedPostsOfWeek(0))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("El límite debe ser mayor a cero");

        assertThatThrownBy(() -> communityPostService.getTopReactedPostsOfWeek(-5))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("El límite debe ser mayor a cero");

        // Verifica que NUNCA se llamó al repositorio
        verify(communityPostRepository, never())
                .findByCollectedAtBetweenOrderByReactionsCountDesc(any(), any(), any());
    }

    // ==================== TESTS TOP COMMENTED POSTS ====================

    @Test
    @DisplayName("getTopCommentedPostsOfWeek - Debe devolver los posts más comentados")
    void getTopCommentedPostsOfWeek_ShouldReturnTopCommentedPosts() {
        // ARRANGE
        List<CommunityPost> expectedPosts = List.of(post1, post3, post2); // post1 tiene 12 comentarios
        when(communityPostRepository.findByCollectedAtBetweenOrderByCommentsCountDesc(
                any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(expectedPosts);

        // ACT
        List<CommunityPost> result = communityPostService.getTopCommentedPostsOfWeek(3);

        // ASSERT
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getCommentsCount()).isEqualTo(12);
        verify(communityPostRepository, times(1))
                .findByCollectedAtBetweenOrderByCommentsCountDesc(any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class));
    }

    // ==================== TESTS MOST ANSWERED QUESTIONS ====================

    @Test
    @DisplayName("getMostAnsweredQuestionsOfWeek - Debe devolver preguntas con más comentarios")
    void getMostAnsweredQuestionsOfWeek_ShouldReturnMostAnsweredQuestions() {
        // ARRANGE
        List<CommunityPost> expectedQuestions = List.of(post1); // post1 es QUESTION con 12 comentarios
        when(communityPostRepository.findByTypeAndCollectedAtBetweenOrderByCommentsCountDesc(
                eq(CommunityPostType.QUESTION), any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(expectedQuestions);

        // ACT
        List<CommunityPost> result = communityPostService.getMostAnsweredQuestionsOfWeek(5);

        // ASSERT
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(CommunityPostType.QUESTION);
        assertThat(result.get(0).getCommentsCount()).isEqualTo(12);
        verify(communityPostRepository, times(1))
                .findByTypeAndCollectedAtBetweenOrderByCommentsCountDesc(
                        eq(CommunityPostType.QUESTION), any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class));
    }

    // ==================== TESTS TOP RESOURCES ====================

    @Test
    @DisplayName("getTopResourcesOfWeek - Debe devolver recursos compartidos")
    void getTopResourcesOfWeek_ShouldReturnTopResources() {
        // ARRANGE
        List<CommunityPost> expectedResources = List.of(post2);
        when(communityPostRepository.findByTypeAndCollectedAtBetweenOrderByReactionsCountDesc(
                eq(CommunityPostType.RESOURCE), any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(expectedResources);

        // ACT
        List<CommunityPost> result = communityPostService.getTopResourcesOfWeek(5);

        // ASSERT
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(CommunityPostType.RESOURCE);
        verify(communityPostRepository, times(1))
                .findByTypeAndCollectedAtBetweenOrderByReactionsCountDesc(
                        eq(CommunityPostType.RESOURCE), any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class));
    }

    // ==================== TESTS WEEKLY SESSIONS ====================

    @Test
    @DisplayName("getWeeklySessions - Debe devolver todas las sesiones de la semana")
    void getWeeklySessions_ShouldReturnAllWeeklySessions() {
        // ARRANGE
        List<CommunityPost> expectedSessions = List.of(post3);
        when(communityPostRepository.findByTypeAndCollectedAtBetween(
                eq(CommunityPostType.SESSION), any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(expectedSessions);

        // ACT
        List<CommunityPost> result = communityPostService.getWeeklySessions();

        // ASSERT
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(CommunityPostType.SESSION);
        verify(communityPostRepository, times(1))
                .findByTypeAndCollectedAtBetween(
                        eq(CommunityPostType.SESSION), any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class));
    }

    // ==================== TESTS WEEKLY DISCUSSIONS ====================

    @Test
    @DisplayName("getWeeklyDiscussions - Debe devolver todas las discusiones de la semana")
    void getWeeklyDiscussions_ShouldReturnAllWeeklyDiscussions() {
        // ARRANGE
        CommunityPost discussion = new CommunityPost();
        discussion.setId(4L);
        discussion.setType(CommunityPostType.DISCUSSION);
        
        List<CommunityPost> expectedDiscussions = List.of(discussion);
        when(communityPostRepository.findByTypeAndCollectedAtBetween(
                eq(CommunityPostType.DISCUSSION), any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(expectedDiscussions);

        // ACT
        List<CommunityPost> result = communityPostService.getWeeklyDiscussions();

        // ASSERT
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(CommunityPostType.DISCUSSION);
        verify(communityPostRepository, times(1))
                .findByTypeAndCollectedAtBetween(
                        eq(CommunityPostType.DISCUSSION), any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class));
    }

    // ==================== TESTS TOP POSTS BY COMMUNITY ====================

    @Test
    @DisplayName("getTopPostsByCommunity - Debe devolver top posts de una comunidad específica")
    void getTopPostsByCommunity_ShouldReturnTopPostsForCommunity() {
        // ARRANGE
        List<CommunityPost> expectedPosts = List.of(post2, post3, post1);
        when(communityPostRepository.findByCommunityIdAndCollectedAtBetweenOrderByReactionsCountDesc(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(expectedPosts);

        // ACT
        List<CommunityPost> result = communityPostService.getTopPostsByCommunity(1L, 3);

        // ASSERT
        assertThat(result).hasSize(3);
        verify(communityPostRepository, times(1))
                .findByCommunityIdAndCollectedAtBetweenOrderByReactionsCountDesc(
                        eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class));
    }

    @Test
    @DisplayName("getTopPostsByCommunity - Con communityId nulo debe lanzar BusinessException")
    void getTopPostsByCommunity_WithNullCommunityId_ShouldThrowBusinessException() {
        // ACT & ASSERT
        assertThatThrownBy(() -> communityPostService.getTopPostsByCommunity(null, 5))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("El ID de comunidad es inválido");
    }

    @Test
    @DisplayName("getTopPostsByCommunity - Con communityId inválido debe lanzar BusinessException")
    void getTopPostsByCommunity_WithInvalidCommunityId_ShouldThrowBusinessException() {
        // ACT & ASSERT
        assertThatThrownBy(() -> communityPostService.getTopPostsByCommunity(0L, 5))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("El ID de comunidad es inválido");
    }

    // ==================== TESTS POSTS BY COMMUNITY AND DATE RANGE ====================

    @Test
    @DisplayName("getPostsByCommunityAndDateRange - Debe devolver posts por comunidad y rango de fechas")
    void getPostsByCommunityAndDateRange_ShouldReturnPosts() {
        // ARRANGE
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        List<CommunityPost> expectedPosts = List.of(post1, post2);
        
        when(communityPostRepository.findByCommunityIdAndCollectedAtBetween(eq(1L), eq(start), eq(end)))
                .thenReturn(expectedPosts);

        // ACT
        List<CommunityPost> result = communityPostService.getPostsByCommunityAndDateRange(1L, start, end);

        // ASSERT
        assertThat(result).hasSize(2);
        verify(communityPostRepository, times(1))
                .findByCommunityIdAndCollectedAtBetween(eq(1L), eq(start), eq(end));
    }

    @Test
    @DisplayName("getPostsByCommunityAndDateRange - Con fechas nulas debe lanzar BusinessException")
    void getPostsByCommunityAndDateRange_WithNullDates_ShouldThrowBusinessException() {
        // ACT & ASSERT
        assertThatThrownBy(() -> communityPostService.getPostsByCommunityAndDateRange(1L, null, LocalDateTime.now()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Las fechas no pueden ser nulas");
    }

    @Test
    @DisplayName("getPostsByCommunityAndDateRange - Con start después de end debe lanzar BusinessException")
    void getPostsByCommunityAndDateRange_WithStartAfterEnd_ShouldThrowBusinessException() {
        // ARRANGE
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().minusDays(1);

        // ACT & ASSERT
        assertThatThrownBy(() -> communityPostService.getPostsByCommunityAndDateRange(1L, start, end))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("La fecha de inicio no puede ser posterior a la fecha fin");
    }

    // ==================== TESTS WEEKLY DIGEST DATA (PARA IA) ====================

    @Test
    @DisplayName("getWeeklyDigestData - Debe construir correctamente el objeto para el LLM")
    void getWeeklyDigestData_ShouldBuildCompleteDigestForLLM() {
        // ARRANGE - Mockear todos los métodos internos
        when(communityPostRepository.findByCollectedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(post1, post2, post3));
        
        when(communityPostRepository.findByCollectedAtBetweenOrderByReactionsCountDesc(
                any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(List.of(post2, post3, post1));
        
        when(communityPostRepository.findByCollectedAtBetweenOrderByCommentsCountDesc(
                any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(List.of(post1, post3, post2));
        
        when(communityPostRepository.findByTypeAndCollectedAtBetweenOrderByCommentsCountDesc(
                eq(CommunityPostType.QUESTION), any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(List.of(post1));
        
        when(communityPostRepository.findByTypeAndCollectedAtBetweenOrderByReactionsCountDesc(
                eq(CommunityPostType.RESOURCE), any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(List.of(post2));
        
        when(communityPostRepository.findByTypeAndCollectedAtBetween(
                eq(CommunityPostType.SESSION), any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(List.of(post3));

        // ACT
        WeeklyDigestData result = communityPostService.getWeeklyDigestData(10);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getTopReactedPosts()).hasSize(3);
        assertThat(result.getTopCommentedPosts()).hasSize(3);
        assertThat(result.getMostAnsweredQuestions()).hasSize(1);
        assertThat(result.getTopResources()).hasSize(1);
        assertThat(result.getWeeklySessions()).hasSize(1);
        assertThat(result.getTotalPosts()).isEqualTo(3);
        assertThat(result.getWeekStart()).isNotNull();
        assertThat(result.getWeekEnd()).isNotNull();
    }

    // ==================== TESTS HAS WEEKLY ACTIVITY ====================

    @Test
    @DisplayName("hasWeeklyActivity - Cuando hay posts debe devolver true")
    void hasWeeklyActivity_WhenPostsExist_ShouldReturnTrue() {
        // ARRANGE
        when(communityPostRepository.findByCollectedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(post1, post2));

        // ACT
        boolean result = communityPostService.hasWeeklyActivity();

        // ASSERT
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("hasWeeklyActivity - Cuando no hay posts debe devolver false")
    void hasWeeklyActivity_WhenNoPosts_ShouldReturnFalse() {
        // ARRANGE
        when(communityPostRepository.findByCollectedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        // ACT
        boolean result = communityPostService.hasWeeklyActivity();

        // ASSERT
        assertThat(result).isFalse();
    }

    // ==================== TESTS WEEKLY STATISTICS ====================

    @Test
    @DisplayName("getWeeklyStatistics - Debe calcular correctamente las estadísticas")
    void getWeeklyStatistics_ShouldCalculateCorrectStatistics() {
        // ARRANGE - Crear posts de diferentes tipos
        CommunityPost question = new CommunityPost();
        question.setType(CommunityPostType.QUESTION);
        question.setReactionsCount(10);
        question.setCommentsCount(5);

        CommunityPost resource = new CommunityPost();
        resource.setType(CommunityPostType.RESOURCE);
        resource.setReactionsCount(20);
        resource.setCommentsCount(3);

        CommunityPost session = new CommunityPost();
        session.setType(CommunityPostType.SESSION);
        session.setReactionsCount(15);
        session.setCommentsCount(7);

        CommunityPost discussion = new CommunityPost();
        discussion.setType(CommunityPostType.DISCUSSION);
        discussion.setReactionsCount(5);
        discussion.setCommentsCount(2);

        List<CommunityPost> allPosts = List.of(question, resource, session, discussion);
        
        when(communityPostRepository.findByCollectedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(allPosts);

        // ACT
        WeeklyStatistics result = communityPostService.getWeeklyStatistics();

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getTotalPosts()).isEqualTo(4);
        assertThat(result.getTotalQuestions()).isEqualTo(1);
        assertThat(result.getTotalResources()).isEqualTo(1);
        assertThat(result.getTotalSessions()).isEqualTo(1);
        assertThat(result.getTotalDiscussions()).isEqualTo(1);
        assertThat(result.getTotalReactions()).isEqualTo(50); // 10+20+15+5
        assertThat(result.getTotalComments()).isEqualTo(17);   // 5+3+7+2
    }

    @Test
    @DisplayName("getWeeklyStatistics - Cuando no hay posts debe devolver estadísticas en cero")
    void getWeeklyStatistics_WhenNoPosts_ShouldReturnZeroStatistics() {
        // ARRANGE
        when(communityPostRepository.findByCollectedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        // ACT
        WeeklyStatistics result = communityPostService.getWeeklyStatistics();

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getTotalPosts()).isZero();
        assertThat(result.getTotalQuestions()).isZero();
        assertThat(result.getTotalResources()).isZero();
        assertThat(result.getTotalSessions()).isZero();
        assertThat(result.getTotalDiscussions()).isZero();
        assertThat(result.getTotalReactions()).isZero();
        assertThat(result.getTotalComments()).isZero();
    }

    // ==================== TESTS POSTS BY TYPE AND DATE RANGE ====================

    @Test
    @DisplayName("getPostsByTypeAndDateRange - Debe filtrar correctamente por tipo y fecha")
    void getPostsByTypeAndDateRange_ShouldFilterByTypeAndDate() {
        // ARRANGE
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        List<CommunityPost> expectedPosts = List.of(post1);
        
        when(communityPostRepository.findByTypeAndCollectedAtBetween(
                eq(CommunityPostType.QUESTION), eq(start), eq(end), any(PageRequest.class)))
                .thenReturn(expectedPosts);

        // ACT
        List<CommunityPost> result = communityPostService.getPostsByTypeAndDateRange(
                CommunityPostType.QUESTION, start, end, 5);

        // ASSERT
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(CommunityPostType.QUESTION);
        verify(communityPostRepository, times(1))
                .findByTypeAndCollectedAtBetween(
                        eq(CommunityPostType.QUESTION), eq(start), eq(end), any(PageRequest.class));
    }

    @Test
    @DisplayName("getPostsByTypeAndDateRange - Con límite inválido debe lanzar BusinessException")
    void getPostsByTypeAndDateRange_WithInvalidLimit_ShouldThrowBusinessException() {
        // ARRANGE
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();

        // ACT & ASSERT
        assertThatThrownBy(() -> communityPostService.getPostsByTypeAndDateRange(
                CommunityPostType.QUESTION, start, end, 0))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("El límite debe ser mayor a cero");
    }
}