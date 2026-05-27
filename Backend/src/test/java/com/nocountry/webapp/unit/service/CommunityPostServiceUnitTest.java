package com.nocountry.webapp.unit.service;

import com.nocountry.webapp.analytics.WeeklyStatistics;
import com.nocountry.webapp.entity.CommunityPost;
import com.nocountry.webapp.entity.enums.CommunityPostType;
import com.nocountry.webapp.exception.base.BusinessException;
import com.nocountry.webapp.repository.CommunityPostRepository;
import com.nocountry.webapp.service.CommunityPostService;
import com.nocountry.webapp.unit.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit Test para CommunityPostService.
 *
 * Se prueban los metodos helpers de calculo de semana, las consultas
 * al repositorio (mockeado) y las validaciones de negocio.
 */
class CommunityPostServiceUnitTest extends BaseUnitTest {

    @Mock
    private CommunityPostRepository communityPostRepository;

    @InjectMocks
    private CommunityPostService communityPostService;

    private CommunityPost questionPost;
    private CommunityPost resourcePost;
    private CommunityPost sessionPost;
    private CommunityPost discussionPost;

    @BeforeEach
    void setUp() {
        questionPost = new CommunityPost();
        questionPost.setId(1L);
        questionPost.setContent("Como configurar Spring Security?");
        questionPost.setAuthorName("Ana Garcia");
        questionPost.setType(CommunityPostType.QUESTION);
        questionPost.setReactionsCount(15);
        questionPost.setCommentsCount(8);
        questionPost.setCollectedAt(LocalDateTime.now());

        resourcePost = new CommunityPost();
        resourcePost.setId(2L);
        resourcePost.setContent("Tutorial de Docker para principiantes");
        resourcePost.setAuthorName("Carlos Lopez");
        resourcePost.setType(CommunityPostType.RESOURCE);
        resourcePost.setReactionsCount(25);
        resourcePost.setCommentsCount(3);
        resourcePost.setCollectedAt(LocalDateTime.now());

        sessionPost = new CommunityPost();
        sessionPost.setId(3L);
        sessionPost.setContent("Sesion de pair programming");
        sessionPost.setAuthorName("Maria Torres");
        sessionPost.setType(CommunityPostType.SESSION);
        sessionPost.setReactionsCount(10);
        sessionPost.setCommentsCount(2);
        sessionPost.setCollectedAt(LocalDateTime.now());

        discussionPost = new CommunityPost();
        discussionPost.setId(4L);
        discussionPost.setContent("Debate sobre microservicios vs monolito");
        discussionPost.setAuthorName("Pedro Martinez");
        discussionPost.setType(CommunityPostType.DISCUSSION);
        discussionPost.setReactionsCount(20);
        discussionPost.setCommentsCount(12);
        discussionPost.setCollectedAt(LocalDateTime.now());
    }

    // ==================== METODOS HELPER DE SEMANA ====================

    @Nested
    @DisplayName("Calculos de inicio y fin de semana")
    class CalculosSemana {

        @Test
        @DisplayName("getStartOfWeek debe retornar el lunes 00:00:00 de esa semana")
        void getStartOfWeek_DebeRetornarLunesMedianoche() {
            // ARRANGE - Miercoles 21 de mayo de 2025 a las 14:30
            LocalDateTime miercoles = LocalDateTime.of(2025, 5, 21, 14, 30, 0);

            // ACT
            LocalDateTime resultado = communityPostService.getStartOfWeek(miercoles);

            // ASSERT
            assertThat(resultado.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
            assertThat(resultado.toLocalDate()).isEqualTo(miercoles.toLocalDate().with(DayOfWeek.MONDAY));
            assertThat(resultado.toLocalTime()).isEqualTo(LocalTime.MIDNIGHT);
        }

        @Test
        @DisplayName("getStartOfWeek con un lunes debe retornar ese mismo lunes a las 00:00")
        void getStartOfWeek_ConLunes_DebeRetornarMismoDia() {
            // ARRANGE - Lunes 19 de mayo de 2025 a las 10:00
            LocalDateTime lunes = LocalDateTime.of(2025, 5, 19, 10, 0, 0);

            // ACT
            LocalDateTime resultado = communityPostService.getStartOfWeek(lunes);

            // ASSERT
            assertThat(resultado.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
            assertThat(resultado.toLocalDate()).isEqualTo(lunes.toLocalDate());
            assertThat(resultado.getHour()).isZero();
            assertThat(resultado.getMinute()).isZero();
            assertThat(resultado.getSecond()).isZero();
        }

        @Test
        @DisplayName("getEndOfWeek debe retornar el domingo 23:59:59.999... de esa semana")
        void getEndOfWeek_DebeRetornarDomingoFinalDia() {
            // ARRANGE - Miercoles 21 de mayo de 2025
            LocalDateTime miercoles = LocalDateTime.of(2025, 5, 21, 14, 30, 0);

            // ACT
            LocalDateTime resultado = communityPostService.getEndOfWeek(miercoles);

            // ASSERT
            assertThat(resultado.getDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY);
            assertThat(resultado.toLocalTime()).isEqualTo(LocalTime.MAX);
        }

        @Test
        @DisplayName("getEndOfWeek con un domingo debe retornar ese mismo domingo a las 23:59:59")
        void getEndOfWeek_ConDomingo_DebeRetornarMismoDia() {
            // ARRANGE - Domingo 25 de mayo de 2025
            LocalDateTime domingo = LocalDateTime.of(2025, 5, 25, 8, 0, 0);

            // ACT
            LocalDateTime resultado = communityPostService.getEndOfWeek(domingo);

            // ASSERT
            assertThat(resultado.getDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY);
            assertThat(resultado.toLocalDate()).isEqualTo(domingo.toLocalDate());
            assertThat(resultado.toLocalTime()).isEqualTo(LocalTime.MAX);
        }
    }

    // ==================== CONSULTAS SEMANALES ====================

    @Nested
    @DisplayName("Consultas de posts semanales")
    class ConsultasSemanales {

        @Test
        @DisplayName("getWeeklyPosts debe llamar al repositorio con rango de la semana actual")
        void getWeeklyPosts_DebeLlamarRepositorioConRangoSemanal() {
            // ARRANGE
            List<CommunityPost> postsEsperados = List.of(questionPost, resourcePost);
            when(communityPostRepository.findByCollectedAtBetween(
                    any(LocalDateTime.class), any(LocalDateTime.class))
            ).thenReturn(postsEsperados);

            // ACT
            List<CommunityPost> resultado = communityPostService.getWeeklyPosts();

            // ASSERT
            assertThat(resultado).hasSize(2);
            verify(communityPostRepository, times(1))
                    .findByCollectedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("getTopReactedPostsOfWeek debe retornar posts ordenados por reacciones")
        void getTopReactedPostsOfWeek_DebeRetornarPostsOrdenadosPorReacciones() {
            // ARRANGE
            List<CommunityPost> postsEsperados = List.of(resourcePost, discussionPost);
            when(communityPostRepository.findByCollectedAtBetweenOrderByReactionsCountDesc(
                    any(LocalDateTime.class), any(LocalDateTime.class), any())
            ).thenReturn(postsEsperados);

            // ACT
            List<CommunityPost> resultado = communityPostService.getTopReactedPostsOfWeek(2);

            // ASSERT
            assertThat(resultado).hasSize(2);
            assertThat(resultado.get(0).getReactionsCount())
                    .isGreaterThanOrEqualTo(resultado.get(1).getReactionsCount());
            verify(communityPostRepository, times(1))
                    .findByCollectedAtBetweenOrderByReactionsCountDesc(
                            any(LocalDateTime.class), any(LocalDateTime.class), any());
        }

        @Test
        @DisplayName("getTopCommentedPostsOfWeek debe retornar posts ordenados por comentarios")
        void getTopCommentedPostsOfWeek_DebeRetornarPostsOrdenadosPorComentarios() {
            // ARRANGE
            List<CommunityPost> postsEsperados = List.of(discussionPost, questionPost);
            when(communityPostRepository.findByCollectedAtBetweenOrderByCommentsCountDesc(
                    any(LocalDateTime.class), any(LocalDateTime.class), any())
            ).thenReturn(postsEsperados);

            // ACT
            List<CommunityPost> resultado = communityPostService.getTopCommentedPostsOfWeek(2);

            // ASSERT
            assertThat(resultado).hasSize(2);
            assertThat(resultado.get(0).getCommentsCount())
                    .isGreaterThanOrEqualTo(resultado.get(1).getCommentsCount());
            verify(communityPostRepository, times(1))
                    .findByCollectedAtBetweenOrderByCommentsCountDesc(
                            any(LocalDateTime.class), any(LocalDateTime.class), any());
        }

        @Test
        @DisplayName("getMostAnsweredQuestionsOfWeek debe filtrar solo QUESTION")
        void getMostAnsweredQuestionsOfWeek_DebeUsarTipoQuestion() {
            // ARRANGE
            List<CommunityPost> preguntasEsperadas = List.of(questionPost);
            when(communityPostRepository.findByTypeAndCollectedAtBetweenOrderByCommentsCountDesc(
                    eq(CommunityPostType.QUESTION),
                    any(LocalDateTime.class), any(LocalDateTime.class), any())
            ).thenReturn(preguntasEsperadas);

            // ACT
            List<CommunityPost> resultado = communityPostService.getMostAnsweredQuestionsOfWeek(5);

            // ASSERT
            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getType()).isEqualTo(CommunityPostType.QUESTION);
            verify(communityPostRepository, times(1))
                    .findByTypeAndCollectedAtBetweenOrderByCommentsCountDesc(
                            eq(CommunityPostType.QUESTION),
                            any(LocalDateTime.class), any(LocalDateTime.class), any());
        }

        @Test
        @DisplayName("getTopResourcesOfWeek debe filtrar solo RESOURCE")
        void getTopResourcesOfWeek_DebeUsarTipoResource() {
            // ARRANGE
            List<CommunityPost> recursosEsperados = List.of(resourcePost);
            when(communityPostRepository.findByTypeAndCollectedAtBetweenOrderByReactionsCountDesc(
                    eq(CommunityPostType.RESOURCE),
                    any(LocalDateTime.class), any(LocalDateTime.class), any())
            ).thenReturn(recursosEsperados);

            // ACT
            List<CommunityPost> resultado = communityPostService.getTopResourcesOfWeek(5);

            // ASSERT
            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getType()).isEqualTo(CommunityPostType.RESOURCE);
            verify(communityPostRepository, times(1))
                    .findByTypeAndCollectedAtBetweenOrderByReactionsCountDesc(
                            eq(CommunityPostType.RESOURCE),
                            any(LocalDateTime.class), any(LocalDateTime.class), any());
        }

        @Test
        @DisplayName("getTopPostsByCommunity debe llamar al repositorio con communityId")
        void getTopPostsByCommunity_DebeLlamarConCommunityId() {
            // ARRANGE
            Long communityId = 1L;
            List<CommunityPost> postsEsperados = List.of(resourcePost, questionPost);
            when(communityPostRepository.findByCommunityIdAndCollectedAtBetweenOrderByReactionsCountDesc(
                    eq(communityId), any(LocalDateTime.class), any(LocalDateTime.class), any())
            ).thenReturn(postsEsperados);

            // ACT
            List<CommunityPost> resultado = communityPostService.getTopPostsByCommunity(communityId, 5);

            // ASSERT
            assertThat(resultado).hasSize(2);
            verify(communityPostRepository, times(1))
                    .findByCommunityIdAndCollectedAtBetweenOrderByReactionsCountDesc(
                            eq(communityId),
                            any(LocalDateTime.class), any(LocalDateTime.class), any());
        }
    }

    // ==================== ACTIVIDAD Y ESTADISTICAS ====================

    @Nested
    @DisplayName("Actividad semanal y estadisticas")
    class ActividadYEstadisticas {

        @Test
        @DisplayName("hasWeeklyActivity debe retornar true cuando existen posts")
        void hasWeeklyActivity_ConPosts_DebeRetornarTrue() {
            // ARRANGE
            when(communityPostRepository.findByCollectedAtBetween(
                    any(LocalDateTime.class), any(LocalDateTime.class))
            ).thenReturn(List.of(questionPost));

            // ACT
            boolean resultado = communityPostService.hasWeeklyActivity();

            // ASSERT
            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("hasWeeklyActivity debe retornar false cuando no existen posts")
        void hasWeeklyActivity_SinPosts_DebeRetornarFalse() {
            // ARRANGE
            when(communityPostRepository.findByCollectedAtBetween(
                    any(LocalDateTime.class), any(LocalDateTime.class))
            ).thenReturn(Collections.emptyList());

            // ACT
            boolean resultado = communityPostService.hasWeeklyActivity();

            // ASSERT
            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("getWeeklyStatistics debe retornar conteos correctos por tipo")
        void getWeeklyStatistics_DebeRetornarConteosCorrectos() {
            // ARRANGE - Lista con 4 posts de diferentes tipos
            List<CommunityPost> todosPosts = List.of(
                    questionPost, resourcePost, sessionPost, discussionPost
            );
            when(communityPostRepository.findByCollectedAtBetween(
                    any(LocalDateTime.class), any(LocalDateTime.class))
            ).thenReturn(todosPosts);

            // ACT
            WeeklyStatistics stats = communityPostService.getWeeklyStatistics();

            // ASSERT
            assertThat(stats.getTotalPosts()).isEqualTo(4);
            assertThat(stats.getTotalQuestions()).isEqualTo(1);
            assertThat(stats.getTotalResources()).isEqualTo(1);
            assertThat(stats.getTotalSessions()).isEqualTo(1);
            assertThat(stats.getTotalDiscussions()).isEqualTo(1);
            // Reacciones: 15 + 25 + 10 + 20 = 70
            assertThat(stats.getTotalReactions()).isEqualTo(70);
            // Comentarios: 8 + 3 + 2 + 12 = 25
            assertThat(stats.getTotalComments()).isEqualTo(25);
            assertThat(stats.getWeekStart()).isNotNull();
            assertThat(stats.getWeekEnd()).isNotNull();
        }

        @Test
        @DisplayName("getWeeklyStatistics sin posts debe retornar todos los conteos en cero")
        void getWeeklyStatistics_SinPosts_DebeRetornarCeros() {
            // ARRANGE
            when(communityPostRepository.findByCollectedAtBetween(
                    any(LocalDateTime.class), any(LocalDateTime.class))
            ).thenReturn(Collections.emptyList());

            // ACT
            WeeklyStatistics stats = communityPostService.getWeeklyStatistics();

            // ASSERT
            assertThat(stats.getTotalPosts()).isZero();
            assertThat(stats.getTotalQuestions()).isZero();
            assertThat(stats.getTotalResources()).isZero();
            assertThat(stats.getTotalSessions()).isZero();
            assertThat(stats.getTotalDiscussions()).isZero();
            assertThat(stats.getTotalReactions()).isZero();
            assertThat(stats.getTotalComments()).isZero();
        }
    }

    // ==================== VALIDACIONES ====================

    @Nested
    @DisplayName("Validaciones de parametros")
    class Validaciones {

        @Test
        @DisplayName("Limite cero debe lanzar BusinessException")
        void limiteCero_DebeLanzarBusinessException() {
            // ACT & ASSERT
            assertThatThrownBy(() -> communityPostService.getTopReactedPostsOfWeek(0))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("mayor a cero");

            verify(communityPostRepository, never())
                    .findByCollectedAtBetweenOrderByReactionsCountDesc(any(), any(), any());
        }

        @Test
        @DisplayName("Limite negativo debe lanzar BusinessException")
        void limiteNegativo_DebeLanzarBusinessException() {
            // ACT & ASSERT
            assertThatThrownBy(() -> communityPostService.getTopCommentedPostsOfWeek(-5))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("mayor a cero");

            verify(communityPostRepository, never())
                    .findByCollectedAtBetweenOrderByCommentsCountDesc(any(), any(), any());
        }

        @Test
        @DisplayName("communityId null debe lanzar BusinessException")
        void communityIdNull_DebeLanzarBusinessException() {
            // ACT & ASSERT
            assertThatThrownBy(() -> communityPostService.getTopPostsByCommunity(null, 5))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("inválido");

            verify(communityPostRepository, never())
                    .findByCommunityIdAndCollectedAtBetweenOrderByReactionsCountDesc(
                            any(), any(), any(), any());
        }

        @Test
        @DisplayName("communityId cero o negativo debe lanzar BusinessException")
        void communityIdCeroONegativo_DebeLanzarBusinessException() {
            // ACT & ASSERT
            assertThatThrownBy(() -> communityPostService.getTopPostsByCommunity(0L, 5))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("inválido");

            assertThatThrownBy(() -> communityPostService.getTopPostsByCommunity(-1L, 5))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("inválido");
        }

        @Test
        @DisplayName("Rango de fechas invalido debe lanzar BusinessException")
        void rangoFechasInvalido_DebeLanzarBusinessException() {
            // ARRANGE
            LocalDateTime inicio = LocalDateTime.of(2025, 5, 25, 0, 0);
            LocalDateTime fin = LocalDateTime.of(2025, 5, 20, 0, 0); // fin antes que inicio

            // ACT & ASSERT
            assertThatThrownBy(() -> communityPostService.getPostsByDateRange(inicio, fin, 10))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("fecha de inicio no puede ser posterior");
        }

        @Test
        @DisplayName("Fechas nulas en rango deben lanzar BusinessException")
        void fechasNulas_DebeLanzarBusinessException() {
            // ACT & ASSERT
            assertThatThrownBy(() -> communityPostService.getPostsByDateRange(null, LocalDateTime.now(), 10))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("nulas");

            assertThatThrownBy(() -> communityPostService.getPostsByDateRange(LocalDateTime.now(), null, 10))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("nulas");
        }
    }
}
