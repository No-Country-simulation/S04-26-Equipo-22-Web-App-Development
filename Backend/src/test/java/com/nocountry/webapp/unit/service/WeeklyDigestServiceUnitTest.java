package com.nocountry.webapp.unit.service;

import com.nocountry.webapp.entity.Community;
import com.nocountry.webapp.entity.WeeklyDigest;
import com.nocountry.webapp.entity.enums.WeeklyDigestStatus;
import com.nocountry.webapp.exception.base.BusinessException;
import com.nocountry.webapp.exception.base.ConflictException;
import com.nocountry.webapp.exception.base.InvalidStateException;
import com.nocountry.webapp.exception.base.NotFoundException;
import com.nocountry.webapp.repository.CommunityRepository;
import com.nocountry.webapp.repository.WeeklyDigestRepository;
import com.nocountry.webapp.service.WeeklyDigestService;
import com.nocountry.webapp.service.dto.WeeklyDigestGenerationRequest;
import com.nocountry.webapp.unit.BaseUnitTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit Test para WeeklyDigestService.
 *
 * Cubre las operaciones CRUD y el ciclo de vida del digest semanal.
 * NO se prueban los metodos dependientes de IA (generateCompleteDigestWithAI,
 * generateDraftsForDigest).
 */
class WeeklyDigestServiceUnitTest extends BaseUnitTest {

    @Mock
    private WeeklyDigestRepository weeklyDigestRepository;

    @Mock
    private CommunityRepository communityRepository;

    // Clock fijo en miercoles 27 de mayo de 2026
    // -> calculateWeekStart() = lunes 25 de mayo
    // -> calculateWeekEnd()   = domingo 31 de mayo
    private final Clock fixedClock = Clock.fixed(
            LocalDate.of(2026, 5, 27)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant(),
            ZoneId.systemDefault()
    );

    @InjectMocks
    private WeeklyDigestService weeklyDigestService;

    // Fechas esperadas derivadas del reloj fijo
    private static final LocalDate EXPECTED_WEEK_START = LocalDate.of(2026, 5, 25); // lunes
    private static final LocalDate EXPECTED_WEEK_END = LocalDate.of(2026, 5, 31);   // domingo

    private Community community;
    private WeeklyDigest pendingDigest;
    private WeeklyDigest processedDigest;

    @BeforeEach
    void setUp() throws Exception {
        // Inyectar el Clock fijo ya que @InjectMocks no inyecta instancias no-mock
        Field clockField = WeeklyDigestService.class.getDeclaredField("clock");
        clockField.setAccessible(true);
        clockField.set(weeklyDigestService, fixedClock);

        // Inyectar defaultPageSize via reflexion (@Value no se procesa sin Spring)
        Field pageSizeField = WeeklyDigestService.class.getDeclaredField("defaultPageSize");
        pageSizeField.setAccessible(true);
        pageSizeField.setInt(weeklyDigestService, 20);

        // Comunidad de prueba
        community = new Community();
        community.setId(1L);
        community.setName("Backend Developers");
        community.setPlatform("DISCORD");
        community.setActive(true);

        // Digest pendiente reutilizable
        pendingDigest = WeeklyDigest.builder()
                .id(10L)
                .community(community)
                .weekStart(EXPECTED_WEEK_START)
                .weekEnd(EXPECTED_WEEK_END)
                .status(WeeklyDigestStatus.PENDING)
                .summary(null)
                .build();

        // Digest ya procesado reutilizable
        processedDigest = WeeklyDigest.builder()
                .id(20L)
                .community(community)
                .weekStart(EXPECTED_WEEK_START)
                .weekEnd(EXPECTED_WEEK_END)
                .status(WeeklyDigestStatus.PROCESSED)
                .summary("Resumen generado")
                .build();
    }

    // =========================================================================
    // generateWeeklyDigest
    // =========================================================================

    @Nested
    @DisplayName("Generar digest semanal (generateWeeklyDigest)")
    class GenerarDigestSemanal {

        @Test
        @DisplayName("Debe crear un digest cuando la comunidad existe y no hay duplicado")
        void debeCrearDigestCuandoComunidadExisteYNoDuplicado() {
            // Arrange
            WeeklyDigestGenerationRequest request = WeeklyDigestGenerationRequest.builder()
                    .communityId(1L)
                    .build();

            when(communityRepository.findById(1L)).thenReturn(Optional.of(community));
            when(weeklyDigestRepository.existsByCommunityIdAndWeekStart(1L, EXPECTED_WEEK_START))
                    .thenReturn(false);
            when(weeklyDigestRepository.save(any(WeeklyDigest.class)))
                    .thenAnswer(invocation -> {
                        WeeklyDigest saved = invocation.getArgument(0);
                        saved.setId(10L);
                        return saved;
                    });

            // Act
            WeeklyDigest result = weeklyDigestService.generateWeeklyDigest(request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getCommunity()).isEqualTo(community);
            assertThat(result.getWeekStart()).isEqualTo(EXPECTED_WEEK_START);
            assertThat(result.getWeekEnd()).isEqualTo(EXPECTED_WEEK_END);
            assertThat(result.getStatus()).isEqualTo(WeeklyDigestStatus.PENDING);

            verify(weeklyDigestRepository, times(1)).save(any(WeeklyDigest.class));
        }

        @Test
        @DisplayName("Debe usar weekStart y weekEnd del request cuando se proporcionan")
        void debeUsarFechasExplicitasDelRequest() {
            // Arrange
            LocalDate customStart = LocalDate.of(2026, 5, 18); // lunes
            LocalDate customEnd = LocalDate.of(2026, 5, 24);   // domingo

            WeeklyDigestGenerationRequest request = WeeklyDigestGenerationRequest.builder()
                    .communityId(1L)
                    .weekStart(customStart)
                    .weekEnd(customEnd)
                    .build();

            when(communityRepository.findById(1L)).thenReturn(Optional.of(community));
            when(weeklyDigestRepository.existsByCommunityIdAndWeekStart(1L, customStart))
                    .thenReturn(false);
            when(weeklyDigestRepository.save(any(WeeklyDigest.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            WeeklyDigest result = weeklyDigestService.generateWeeklyDigest(request);

            // Assert
            assertThat(result.getWeekStart()).isEqualTo(customStart);
            assertThat(result.getWeekEnd()).isEqualTo(customEnd);
        }

        @Test
        @DisplayName("Debe lanzar NotFoundException cuando la comunidad no existe")
        void debeLanzarNotFoundCuandoComunidadNoExiste() {
            // Arrange
            WeeklyDigestGenerationRequest request = WeeklyDigestGenerationRequest.builder()
                    .communityId(99L)
                    .build();

            when(communityRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> weeklyDigestService.generateWeeklyDigest(request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Comunidad no encontrada con ID: 99");

            verify(weeklyDigestRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar ConflictException cuando ya existe digest para esa semana")
        void debeLanzarConflictCuandoYaExisteDigest() {
            // Arrange
            WeeklyDigestGenerationRequest request = WeeklyDigestGenerationRequest.builder()
                    .communityId(1L)
                    .build();

            when(communityRepository.findById(1L)).thenReturn(Optional.of(community));
            when(weeklyDigestRepository.existsByCommunityIdAndWeekStart(1L, EXPECTED_WEEK_START))
                    .thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> weeklyDigestService.generateWeeklyDigest(request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Ya existe un digest");

            verify(weeklyDigestRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar BusinessException si weekStart no es lunes")
        void debeLanzarBusinessSiWeekStartNoEsLunes() {
            // Arrange - martes 26 de mayo
            WeeklyDigestGenerationRequest request = WeeklyDigestGenerationRequest.builder()
                    .communityId(1L)
                    .weekStart(LocalDate.of(2026, 5, 26))
                    .weekEnd(LocalDate.of(2026, 5, 31))
                    .build();

            when(communityRepository.findById(1L)).thenReturn(Optional.of(community));

            // Act & Assert
            assertThatThrownBy(() -> weeklyDigestService.generateWeeklyDigest(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("weekStart debe ser lunes");

            verify(weeklyDigestRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar BusinessException si weekEnd no es domingo")
        void debeLanzarBusinessSiWeekEndNoEsDomingo() {
            // Arrange - lunes 25 y sabado 30
            WeeklyDigestGenerationRequest request = WeeklyDigestGenerationRequest.builder()
                    .communityId(1L)
                    .weekStart(LocalDate.of(2026, 5, 25))
                    .weekEnd(LocalDate.of(2026, 5, 30))
                    .build();

            when(communityRepository.findById(1L)).thenReturn(Optional.of(community));

            // Act & Assert
            assertThatThrownBy(() -> weeklyDigestService.generateWeeklyDigest(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("weekEnd debe ser domingo");
        }

        @Test
        @DisplayName("Debe lanzar BusinessException si weekEnd es anterior a weekStart")
        void debeLanzarBusinessSiFechaFinAnteriorAInicio() {
            // Arrange
            WeeklyDigestGenerationRequest request = WeeklyDigestGenerationRequest.builder()
                    .communityId(1L)
                    .weekStart(LocalDate.of(2026, 5, 25))
                    .weekEnd(LocalDate.of(2026, 5, 20))
                    .build();

            when(communityRepository.findById(1L)).thenReturn(Optional.of(community));

            // Act & Assert
            assertThatThrownBy(() -> weeklyDigestService.generateWeeklyDigest(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("fecha de fin de semana no puede ser anterior");
        }
    }

    // =========================================================================
    // generateDigestsForAllActiveCommunities
    // =========================================================================

    @Nested
    @DisplayName("Generar digests para todas las comunidades activas")
    class GenerarDigestsParaTodasLasComunidades {

        @Test
        @DisplayName("Debe crear digests para todas las comunidades activas sin duplicados")
        void debeCrearDigestsParaComunidadesActivasSinDuplicados() {
            // Arrange
            Community community2 = new Community();
            community2.setId(2L);
            community2.setName("Frontend Guild");
            community2.setPlatform("SLACK");
            community2.setActive(true);

            when(communityRepository.findByActiveTrue())
                    .thenReturn(List.of(community, community2));
            when(weeklyDigestRepository.existsByCommunityIdAndWeekStart(eq(1L), any()))
                    .thenReturn(false);
            when(weeklyDigestRepository.existsByCommunityIdAndWeekStart(eq(2L), any()))
                    .thenReturn(false);
            when(weeklyDigestRepository.save(any(WeeklyDigest.class)))
                    .thenAnswer(invocation -> {
                        WeeklyDigest saved = invocation.getArgument(0);
                        saved.setId(saved.getCommunity().getId() * 10);
                        return saved;
                    });

            // Act
            List<WeeklyDigest> result = weeklyDigestService.generateDigestsForAllActiveCommunities();

            // Assert
            assertThat(result).hasSize(2);
            verify(weeklyDigestRepository, times(2)).save(any(WeeklyDigest.class));
        }

        @Test
        @DisplayName("Debe omitir comunidades que ya tienen digest para la semana actual")
        void debeOmitirComunidadesConDigestExistente() {
            // Arrange
            Community community2 = new Community();
            community2.setId(2L);
            community2.setName("Frontend Guild");
            community2.setPlatform("SLACK");
            community2.setActive(true);

            when(communityRepository.findByActiveTrue())
                    .thenReturn(List.of(community, community2));
            // Comunidad 1 ya tiene digest, comunidad 2 no
            when(weeklyDigestRepository.existsByCommunityIdAndWeekStart(eq(1L), any()))
                    .thenReturn(true);
            when(weeklyDigestRepository.existsByCommunityIdAndWeekStart(eq(2L), any()))
                    .thenReturn(false);
            when(weeklyDigestRepository.save(any(WeeklyDigest.class)))
                    .thenAnswer(invocation -> {
                        WeeklyDigest saved = invocation.getArgument(0);
                        saved.setId(20L);
                        return saved;
                    });

            // Act
            List<WeeklyDigest> result = weeklyDigestService.generateDigestsForAllActiveCommunities();

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCommunity().getId()).isEqualTo(2L);
            verify(weeklyDigestRepository, times(1)).save(any(WeeklyDigest.class));
        }
    }

    // =========================================================================
    // processDigest
    // =========================================================================

    @Nested
    @DisplayName("Procesar digest (processDigest)")
    class ProcesarDigest {

        @Test
        @DisplayName("Debe marcar un digest PENDING como PROCESSED")
        void debeMarcarPendingComoProcesado() {
            // Arrange
            when(weeklyDigestRepository.findById(10L))
                    .thenReturn(Optional.of(pendingDigest));
            when(weeklyDigestRepository.save(any(WeeklyDigest.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            WeeklyDigest result = weeklyDigestService.processDigest(10L);

            // Assert
            assertThat(result.getStatus()).isEqualTo(WeeklyDigestStatus.PROCESSED);
            verify(weeklyDigestRepository, times(1)).save(pendingDigest);
        }

        @Test
        @DisplayName("Debe lanzar InvalidStateException si el digest ya fue procesado")
        void debeLanzarInvalidStateSiYaProcesado() {
            // Arrange
            when(weeklyDigestRepository.findById(20L))
                    .thenReturn(Optional.of(processedDigest));

            // Act & Assert
            assertThatThrownBy(() -> weeklyDigestService.processDigest(20L))
                    .isInstanceOf(InvalidStateException.class)
                    .hasMessageContaining("ya fue procesado");

            verify(weeklyDigestRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar NotFoundException si el digest no existe")
        void debeLanzarNotFoundSiDigestNoExiste() {
            // Arrange
            when(weeklyDigestRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> weeklyDigestService.processDigest(999L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Digest no encontrado con ID: 999");
        }
    }

    // =========================================================================
    // updateSummary
    // =========================================================================

    @Nested
    @DisplayName("Actualizar resumen (updateSummary)")
    class ActualizarResumen {

        @Test
        @DisplayName("Debe actualizar el resumen de un digest PENDING")
        void debeActualizarResumenDeDigestPending() {
            // Arrange
            String nuevoResumen = "Resumen actualizado de la semana";
            when(weeklyDigestRepository.findById(10L))
                    .thenReturn(Optional.of(pendingDigest));
            when(weeklyDigestRepository.save(any(WeeklyDigest.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            WeeklyDigest result = weeklyDigestService.updateSummary(10L, nuevoResumen);

            // Assert
            assertThat(result.getSummary()).isEqualTo(nuevoResumen);
            verify(weeklyDigestRepository, times(1)).save(pendingDigest);
        }

        @Test
        @DisplayName("Debe lanzar InvalidStateException si el digest ya esta procesado")
        void debeLanzarInvalidStateSiDigestProcesado() {
            // Arrange
            when(weeklyDigestRepository.findById(20L))
                    .thenReturn(Optional.of(processedDigest));

            // Act & Assert
            assertThatThrownBy(() ->
                    weeklyDigestService.updateSummary(20L, "Nuevo resumen"))
                    .isInstanceOf(InvalidStateException.class)
                    .hasMessageContaining("No se puede editar un digest ya procesado");

            verify(weeklyDigestRepository, never()).save(any());
        }
    }

    // =========================================================================
    // deleteDigest
    // =========================================================================

    @Nested
    @DisplayName("Eliminar digest (deleteDigest)")
    class EliminarDigest {

        @Test
        @DisplayName("Debe eliminar un digest PENDING correctamente")
        void debeEliminarDigestPending() {
            // Arrange
            when(weeklyDigestRepository.findById(10L))
                    .thenReturn(Optional.of(pendingDigest));
            doNothing().when(weeklyDigestRepository).delete(pendingDigest);

            // Act
            weeklyDigestService.deleteDigest(10L);

            // Assert
            verify(weeklyDigestRepository, times(1)).delete(pendingDigest);
        }

        @Test
        @DisplayName("Debe lanzar InvalidStateException si el digest ya fue procesado")
        void debeLanzarInvalidStateSiDigestProcesado() {
            // Arrange
            when(weeklyDigestRepository.findById(20L))
                    .thenReturn(Optional.of(processedDigest));

            // Act & Assert
            assertThatThrownBy(() -> weeklyDigestService.deleteDigest(20L))
                    .isInstanceOf(InvalidStateException.class)
                    .hasMessageContaining("No se puede eliminar un digest ya procesado");

            verify(weeklyDigestRepository, never()).delete(any(WeeklyDigest.class));
        }
    }

    // =========================================================================
    // getDigestById
    // =========================================================================

    @Nested
    @DisplayName("Obtener digest por ID (getDigestById)")
    class ObtenerDigestPorId {

        @Test
        @DisplayName("Debe retornar el digest cuando existe")
        void debeRetornarDigestCuandoExiste() {
            // Arrange
            when(weeklyDigestRepository.findById(10L))
                    .thenReturn(Optional.of(pendingDigest));

            // Act
            WeeklyDigest result = weeklyDigestService.getDigestById(10L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getCommunity().getName()).isEqualTo("Backend Developers");
        }

        @Test
        @DisplayName("Debe lanzar NotFoundException cuando no existe")
        void debeLanzarNotFoundCuandoNoExiste() {
            // Arrange
            when(weeklyDigestRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> weeklyDigestService.getDigestById(999L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Digest no encontrado con ID: 999");
        }
    }

    // =========================================================================
    // hasDigestForCurrentWeek
    // =========================================================================

    @Nested
    @DisplayName("Verificar digest de la semana actual (hasDigestForCurrentWeek)")
    class VerificarDigestSemanaActual {

        @Test
        @DisplayName("Debe retornar true cuando existe digest para la semana actual")
        void debeRetornarTrueCuandoExisteDigest() {
            // Arrange
            when(weeklyDigestRepository.existsByCommunityIdAndWeekStart(1L, EXPECTED_WEEK_START))
                    .thenReturn(true);

            // Act
            boolean result = weeklyDigestService.hasDigestForCurrentWeek(1L);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Debe retornar false cuando no existe digest para la semana actual")
        void debeRetornarFalseCuandoNoExisteDigest() {
            // Arrange
            when(weeklyDigestRepository.existsByCommunityIdAndWeekStart(1L, EXPECTED_WEEK_START))
                    .thenReturn(false);

            // Act
            boolean result = weeklyDigestService.hasDigestForCurrentWeek(1L);

            // Assert
            assertThat(result).isFalse();
        }
    }

    // =========================================================================
    // getPendingDigests
    // =========================================================================

    @Nested
    @DisplayName("Listar digests pendientes (getPendingDigests)")
    class ListarDigestsPendientes {

        @Test
        @DisplayName("Debe retornar lista paginada de digests pendientes")
        void debeRetornarListaPaginadaDePendientes() {
            // Arrange
            when(weeklyDigestRepository.findByStatusOrderByCreatedAtDesc(
                    eq(WeeklyDigestStatus.PENDING), any()))
                    .thenReturn(List.of(pendingDigest));

            // Act
            List<WeeklyDigest> result = weeklyDigestService.getPendingDigests(0, 10);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(WeeklyDigestStatus.PENDING);
            verify(weeklyDigestRepository, times(1))
                    .findByStatusOrderByCreatedAtDesc(eq(WeeklyDigestStatus.PENDING), any());
        }

        @Test
        @DisplayName("Debe usar defaultPageSize cuando size es 0 o negativo")
        void debeUsarDefaultPageSizeCuandoSizeInvalido() {
            // Arrange
            when(weeklyDigestRepository.findByStatusOrderByCreatedAtDesc(
                    eq(WeeklyDigestStatus.PENDING), any()))
                    .thenReturn(List.of());

            // Act
            weeklyDigestService.getPendingDigests(0, 0);

            // Assert - verifica que se llamo al repo (el defaultPageSize=20 se usa internamente)
            verify(weeklyDigestRepository, times(1))
                    .findByStatusOrderByCreatedAtDesc(eq(WeeklyDigestStatus.PENDING), any());
        }
    }

    // =========================================================================
    // getCommunityDigestHistory
    // =========================================================================

    @Nested
    @DisplayName("Historial de digests por comunidad (getCommunityDigestHistory)")
    class HistorialDigestsPorComunidad {

        @Test
        @DisplayName("Debe retornar historial paginado cuando la comunidad existe")
        void debeRetornarHistorialCuandoComunidadExiste() {
            // Arrange
            when(communityRepository.existsById(1L)).thenReturn(true);
            when(weeklyDigestRepository.findByCommunityIdOrderByWeekStartDesc(eq(1L), any()))
                    .thenReturn(List.of(pendingDigest, processedDigest));

            // Act
            List<WeeklyDigest> result = weeklyDigestService.getCommunityDigestHistory(1L, 0, 10);

            // Assert
            assertThat(result).hasSize(2);
            verify(communityRepository, times(1)).existsById(1L);
            verify(weeklyDigestRepository, times(1))
                    .findByCommunityIdOrderByWeekStartDesc(eq(1L), any());
        }

        @Test
        @DisplayName("Debe lanzar NotFoundException cuando la comunidad no existe")
        void debeLanzarNotFoundCuandoComunidadNoExiste() {
            // Arrange
            when(communityRepository.existsById(99L)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() ->
                    weeklyDigestService.getCommunityDigestHistory(99L, 0, 10))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Comunidad no encontrada con ID: 99");

            verify(weeklyDigestRepository, never())
                    .findByCommunityIdOrderByWeekStartDesc(any(), any());
        }
    }
}
