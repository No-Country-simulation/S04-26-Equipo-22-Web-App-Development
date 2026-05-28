package com.nocountry.webapp.unit.service;

import com.nocountry.webapp.entity.ChannelDraft;
import com.nocountry.webapp.entity.User;
import com.nocountry.webapp.entity.WeeklyDigest;
import com.nocountry.webapp.entity.enums.ChannelDraftStatus;
import com.nocountry.webapp.entity.enums.Role;
import com.nocountry.webapp.entity.enums.TargetPlatform;
import com.nocountry.webapp.exception.base.BusinessException;
import com.nocountry.webapp.exception.base.InvalidStateException;
import com.nocountry.webapp.exception.base.NotFoundException;
import com.nocountry.webapp.repository.ChannelDraftRepository;
import com.nocountry.webapp.repository.UserRepository;
import com.nocountry.webapp.repository.WeeklyDigestRepository;
import com.nocountry.webapp.service.ChannelDraftService;
import com.nocountry.webapp.unit.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Unit Test para ChannelDraftService.
 *
 * Prueba la logica de negocio del servicio de borradores de canal,
 * incluyendo el flujo de estados (GENERATED -> IN_REVIEW -> APPROVED -> PUBLISHED)
 * y las validaciones de rol de usuario.
 */
class ChannelDraftServiceUnitTest extends BaseUnitTest {

    @Mock
    private ChannelDraftRepository channelDraftRepository;

    @Mock
    private WeeklyDigestRepository weeklyDigestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private ChannelDraftService channelDraftService;

    private User editorUser;
    private User adminUser;
    private WeeklyDigest weeklyDigest;
    private ChannelDraft draft;

    private static final Long DIGEST_ID = 1L;
    private static final String EDITOR_EMAIL = "editor@test.com";
    private static final String ADMIN_EMAIL = "admin@test.com";
    private static final Long DRAFT_ID = 100L;
    private static final String CONTENT = "Contenido del borrador de prueba";
    private static final String NEWSLETTER_CONTENT = "Contenido newsletter";
    private static final String LINKEDIN_CONTENT = "Contenido LinkedIn";
    private static final String TWITTER_CONTENT = "Contenido X/Twitter";

    @BeforeEach
    void setUp() throws Exception {
        // Configurar Clock fijo para pruebas deterministas
        Clock fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        lenient().when(clock.instant()).thenReturn(fixedClock.instant());
        lenient().when(clock.getZone()).thenReturn(fixedClock.getZone());

        // Configurar defaultPageSize via reflexion (campo inyectado por @Value)
        Field pageSizeField = ChannelDraftService.class.getDeclaredField("defaultPageSize");
        pageSizeField.setAccessible(true);
        pageSizeField.setInt(channelDraftService, 20);

        // Crear usuario editor con rol USER
        editorUser = User.builder()
                .id(10L)
                .email("editor@test.com")
                .password("password")
                .role(Role.USER)
                .build();

        // Crear usuario admin con rol ADMIN
        adminUser = User.builder()
                .id(20L)
                .email("admin@test.com")
                .password("password")
                .role(Role.ADMIN)
                .build();

        // Crear digest semanal de prueba
        weeklyDigest = WeeklyDigest.builder()
                .id(DIGEST_ID)
                .build();

        // Crear borrador de prueba en estado GENERATED
        draft = ChannelDraft.builder()
                .id(DRAFT_ID)
                .content(CONTENT)
                .targetPlatform(TargetPlatform.NEWSLETTER)
                .status(ChannelDraftStatus.GENERATED)
                .editor(editorUser)
                .weeklyDigest(weeklyDigest)
                .build();
    }

    // ==================== TESTS DE createDraft ====================

    @Nested
    @DisplayName("createDraft - Crear borrador individual")
    class CreateDraftTests {

        @Test
        @DisplayName("Debe crear borrador exitosamente con datos validos")
        void createDraft_conDatosValidos_debeCrearBorrador() {
            // Arrange
            when(weeklyDigestRepository.findById(DIGEST_ID)).thenReturn(Optional.of(weeklyDigest));
            when(userRepository.findByEmail(EDITOR_EMAIL)).thenReturn(Optional.of(editorUser));
            when(channelDraftRepository.findByWeeklyDigestIdAndTargetPlatform(DIGEST_ID, TargetPlatform.NEWSLETTER))
                    .thenReturn(Optional.empty());
            when(channelDraftRepository.save(any(ChannelDraft.class))).thenAnswer(invocation -> {
                ChannelDraft saved = invocation.getArgument(0);
                saved.setId(DRAFT_ID);
                return saved;
            });

            // Act
            ChannelDraft result = channelDraftService.createDraft(DIGEST_ID, CONTENT, TargetPlatform.NEWSLETTER, EDITOR_EMAIL);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEqualTo(CONTENT);
            assertThat(result.getTargetPlatform()).isEqualTo(TargetPlatform.NEWSLETTER);
            assertThat(result.getStatus()).isEqualTo(ChannelDraftStatus.GENERATED);
            assertThat(result.getEditor()).isEqualTo(editorUser);
            assertThat(result.getWeeklyDigest()).isEqualTo(weeklyDigest);
            verify(channelDraftRepository, times(1)).save(any(ChannelDraft.class));
        }

        @Test
        @DisplayName("Debe lanzar NotFoundException cuando el digest no existe")
        void createDraft_digestNoExiste_debeLanzarNotFoundException() {
            // Arrange
            when(weeklyDigestRepository.findById(DIGEST_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> channelDraftService.createDraft(DIGEST_ID, CONTENT, TargetPlatform.NEWSLETTER, EDITOR_EMAIL))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Digest no encontrado con ID: " + DIGEST_ID);

            verify(channelDraftRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar NotFoundException cuando el editor no existe")
        void createDraft_editorNoExiste_debeLanzarNotFoundException() {
            // Arrange
            when(weeklyDigestRepository.findById(DIGEST_ID)).thenReturn(Optional.of(weeklyDigest));
            when(userRepository.findByEmail(EDITOR_EMAIL)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> channelDraftService.createDraft(DIGEST_ID, CONTENT, TargetPlatform.NEWSLETTER, EDITOR_EMAIL))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Editor no encontrado con email: " + EDITOR_EMAIL);

            verify(channelDraftRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar BusinessException cuando el editor tiene rol ADMIN")
        void createDraft_editorConRolAdmin_debeLanzarBusinessException() {
            // Arrange
            when(weeklyDigestRepository.findById(DIGEST_ID)).thenReturn(Optional.of(weeklyDigest));
            when(userRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(adminUser));

            // Act & Assert
            assertThatThrownBy(() -> channelDraftService.createDraft(DIGEST_ID, CONTENT, TargetPlatform.NEWSLETTER, ADMIN_EMAIL))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("El usuario asignado no tiene rol USER");

            verify(channelDraftRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar BusinessException cuando ya existe borrador para la plataforma")
        void createDraft_duplicadoPlataforma_debeLanzarBusinessException() {
            // Arrange
            when(weeklyDigestRepository.findById(DIGEST_ID)).thenReturn(Optional.of(weeklyDigest));
            when(userRepository.findByEmail(EDITOR_EMAIL)).thenReturn(Optional.of(editorUser));
            when(channelDraftRepository.findByWeeklyDigestIdAndTargetPlatform(DIGEST_ID, TargetPlatform.NEWSLETTER))
                    .thenReturn(Optional.of(draft));

            // Act & Assert
            assertThatThrownBy(() -> channelDraftService.createDraft(DIGEST_ID, CONTENT, TargetPlatform.NEWSLETTER, EDITOR_EMAIL))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Ya existe un borrador para el digest");

            verify(channelDraftRepository, never()).save(any());
        }
    }

    // ==================== TESTS DE createCompleteDrafts ====================

    @Nested
    @DisplayName("createCompleteDrafts - Crear borradores completos (3 plataformas)")
    class CreateCompleteDraftsTests {

        @Test
        @DisplayName("Debe crear 3 borradores exitosamente para todas las plataformas")
        void createCompleteDrafts_conDatosValidos_debeCrearTresBorradores() {
            // Arrange
            when(weeklyDigestRepository.findById(DIGEST_ID)).thenReturn(Optional.of(weeklyDigest));
            when(userRepository.findByEmail(EDITOR_EMAIL)).thenReturn(Optional.of(editorUser));
            for (TargetPlatform platform : TargetPlatform.values()) {
                when(channelDraftRepository.findByWeeklyDigestIdAndTargetPlatform(DIGEST_ID, platform))
                        .thenReturn(Optional.empty());
            }
            when(channelDraftRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            List<ChannelDraft> result = channelDraftService.createCompleteDrafts(
                    DIGEST_ID, NEWSLETTER_CONTENT, LINKEDIN_CONTENT, TWITTER_CONTENT, EDITOR_EMAIL);

            // Assert
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getTargetPlatform()).isEqualTo(TargetPlatform.NEWSLETTER);
            assertThat(result.get(0).getContent()).isEqualTo(NEWSLETTER_CONTENT);
            assertThat(result.get(1).getTargetPlatform()).isEqualTo(TargetPlatform.LINKEDIN);
            assertThat(result.get(1).getContent()).isEqualTo(LINKEDIN_CONTENT);
            assertThat(result.get(2).getTargetPlatform()).isEqualTo(TargetPlatform.X);
            assertThat(result.get(2).getContent()).isEqualTo(TWITTER_CONTENT);
            verify(channelDraftRepository, times(1)).saveAll(anyList());
        }

        @Test
        @DisplayName("Debe lanzar BusinessException si ya existe borrador en alguna plataforma")
        void createCompleteDrafts_plataformaDuplicada_debeLanzarBusinessException() {
            // Arrange
            when(weeklyDigestRepository.findById(DIGEST_ID)).thenReturn(Optional.of(weeklyDigest));
            when(userRepository.findByEmail(EDITOR_EMAIL)).thenReturn(Optional.of(editorUser));
            // NEWSLETTER ya existe
            when(channelDraftRepository.findByWeeklyDigestIdAndTargetPlatform(DIGEST_ID, TargetPlatform.NEWSLETTER))
                    .thenReturn(Optional.of(draft));

            // Act & Assert
            assertThatThrownBy(() -> channelDraftService.createCompleteDrafts(
                    DIGEST_ID, NEWSLETTER_CONTENT, LINKEDIN_CONTENT, TWITTER_CONTENT, EDITOR_EMAIL))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Ya existe un borrador para el digest");

            verify(channelDraftRepository, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("Debe lanzar BusinessException cuando el editor tiene rol ADMIN")
        void createCompleteDrafts_editorAdmin_debeLanzarBusinessException() {
            // Arrange
            when(weeklyDigestRepository.findById(DIGEST_ID)).thenReturn(Optional.of(weeklyDigest));
            when(userRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(adminUser));

            // Act & Assert
            assertThatThrownBy(() -> channelDraftService.createCompleteDrafts(
                    DIGEST_ID, NEWSLETTER_CONTENT, LINKEDIN_CONTENT, TWITTER_CONTENT, ADMIN_EMAIL))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("El usuario asignado no tiene rol USER");

            verify(channelDraftRepository, never()).saveAll(anyList());
        }
    }

    // ==================== TESTS DE getDraftById ====================

    @Nested
    @DisplayName("getDraftById - Obtener borrador por ID")
    class GetDraftByIdTests {

        @Test
        @DisplayName("Debe retornar el borrador cuando existe")
        void getDraftById_existente_debeRetornarBorrador() {
            // Arrange
            when(channelDraftRepository.findById(DRAFT_ID)).thenReturn(Optional.of(draft));

            // Act
            ChannelDraft result = channelDraftService.getDraftById(DRAFT_ID);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(DRAFT_ID);
            assertThat(result.getContent()).isEqualTo(CONTENT);
            verify(channelDraftRepository, times(1)).findById(DRAFT_ID);
        }

        @Test
        @DisplayName("Debe lanzar NotFoundException cuando el borrador no existe")
        void getDraftById_noExistente_debeLanzarNotFoundException() {
            // Arrange
            when(channelDraftRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> channelDraftService.getDraftById(999L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Borrador no encontrado con ID: 999");
        }
    }

    // ==================== TESTS DE updateContent ====================

    @Nested
    @DisplayName("updateContent - Actualizar contenido del borrador")
    class UpdateContentTests {

        @Test
        @DisplayName("Debe actualizar contenido de borrador en estado GENERATED")
        void updateContent_estadoGenerated_debeActualizar() {
            // Arrange
            String nuevoContenido = "Contenido actualizado";
            when(channelDraftRepository.findById(DRAFT_ID)).thenReturn(Optional.of(draft));
            when(channelDraftRepository.save(any(ChannelDraft.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            ChannelDraft result = channelDraftService.updateContent(DRAFT_ID, nuevoContenido);

            // Assert
            assertThat(result.getContent()).isEqualTo(nuevoContenido);
            verify(channelDraftRepository, times(1)).save(any(ChannelDraft.class));
        }

        @Test
        @DisplayName("Debe actualizar contenido de borrador en estado IN_REVIEW")
        void updateContent_estadoInReview_debeActualizar() {
            // Arrange
            draft.setStatus(ChannelDraftStatus.IN_REVIEW);
            String nuevoContenido = "Contenido revisado";
            when(channelDraftRepository.findById(DRAFT_ID)).thenReturn(Optional.of(draft));
            when(channelDraftRepository.save(any(ChannelDraft.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            ChannelDraft result = channelDraftService.updateContent(DRAFT_ID, nuevoContenido);

            // Assert
            assertThat(result.getContent()).isEqualTo(nuevoContenido);
        }

        @Test
        @DisplayName("Debe lanzar InvalidStateException al editar borrador PUBLISHED")
        void updateContent_estadoPublished_debeLanzarInvalidStateException() {
            // Arrange
            draft.setStatus(ChannelDraftStatus.PUBLISHED);
            when(channelDraftRepository.findById(DRAFT_ID)).thenReturn(Optional.of(draft));

            // Act & Assert
            assertThatThrownBy(() -> channelDraftService.updateContent(DRAFT_ID, "Nuevo contenido"))
                    .isInstanceOf(InvalidStateException.class)
                    .hasMessageContaining("No se puede editar un borrador ya publicado");

            verify(channelDraftRepository, never()).save(any());
        }
    }

    // ==================== TESTS DE approveDraft ====================

    @Nested
    @DisplayName("approveDraft - Aprobar borrador")
    class ApproveDraftTests {

        @Test
        @DisplayName("Debe aprobar borrador exitosamente con editor valido")
        void approveDraft_editorValido_debeAprobar() {
            // Arrange
            when(channelDraftRepository.findById(DRAFT_ID)).thenReturn(Optional.of(draft));
            when(userRepository.findByEmail(EDITOR_EMAIL)).thenReturn(Optional.of(editorUser));
            when(channelDraftRepository.save(any(ChannelDraft.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            ChannelDraft result = channelDraftService.approveDraft(DRAFT_ID, EDITOR_EMAIL);

            // Assert
            assertThat(result.getStatus()).isEqualTo(ChannelDraftStatus.APPROVED);
            assertThat(result.getApprovedAt()).isNotNull();
            assertThat(result.getEditor()).isEqualTo(editorUser);
            verify(channelDraftRepository, times(1)).save(any(ChannelDraft.class));
        }

        @Test
        @DisplayName("Debe lanzar InvalidStateException al aprobar borrador PUBLISHED")
        void approveDraft_estadoPublished_debeLanzarInvalidStateException() {
            // Arrange
            draft.setStatus(ChannelDraftStatus.PUBLISHED);
            when(channelDraftRepository.findById(DRAFT_ID)).thenReturn(Optional.of(draft));
            when(userRepository.findByEmail(EDITOR_EMAIL)).thenReturn(Optional.of(editorUser));

            // Act & Assert
            assertThatThrownBy(() -> channelDraftService.approveDraft(DRAFT_ID, EDITOR_EMAIL))
                    .isInstanceOf(InvalidStateException.class)
                    .hasMessageContaining("No se puede aprobar un borrador ya publicado");

            verify(channelDraftRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar BusinessException cuando el editor tiene rol ADMIN")
        void approveDraft_editorAdmin_debeLanzarBusinessException() {
            // Arrange
            when(channelDraftRepository.findById(DRAFT_ID)).thenReturn(Optional.of(draft));
            when(userRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(adminUser));

            // Act & Assert
            assertThatThrownBy(() -> channelDraftService.approveDraft(DRAFT_ID, ADMIN_EMAIL))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("El usuario asignado no tiene rol USER");

            verify(channelDraftRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar NotFoundException cuando el editor no existe")
        void approveDraft_editorNoExiste_debeLanzarNotFoundException() {
            // Arrange
            when(channelDraftRepository.findById(DRAFT_ID)).thenReturn(Optional.of(draft));
            when(userRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> channelDraftService.approveDraft(DRAFT_ID, "noexiste@test.com"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Editor no encontrado con email: noexiste@test.com");
        }
    }

    // ==================== TESTS DE rejectDraft ====================

    @Nested
    @DisplayName("rejectDraft - Rechazar borrador")
    class RejectDraftTests {

        @Test
        @DisplayName("Debe rechazar borrador y limpiar approvedAt")
        void rejectDraft_estadoValido_debeRechazar() {
            // Arrange
            draft.setStatus(ChannelDraftStatus.APPROVED);
            draft.setApprovedAt(LocalDateTime.now());
            when(channelDraftRepository.findById(DRAFT_ID)).thenReturn(Optional.of(draft));
            when(channelDraftRepository.save(any(ChannelDraft.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            ChannelDraft result = channelDraftService.rejectDraft(DRAFT_ID);

            // Assert
            assertThat(result.getStatus()).isEqualTo(ChannelDraftStatus.REJECTED);
            assertThat(result.getApprovedAt()).isNull();
            verify(channelDraftRepository, times(1)).save(any(ChannelDraft.class));
        }

        @Test
        @DisplayName("Debe lanzar InvalidStateException al rechazar borrador PUBLISHED")
        void rejectDraft_estadoPublished_debeLanzarInvalidStateException() {
            // Arrange
            draft.setStatus(ChannelDraftStatus.PUBLISHED);
            when(channelDraftRepository.findById(DRAFT_ID)).thenReturn(Optional.of(draft));

            // Act & Assert
            assertThatThrownBy(() -> channelDraftService.rejectDraft(DRAFT_ID))
                    .isInstanceOf(InvalidStateException.class)
                    .hasMessageContaining("No se puede rechazar un borrador ya publicado");

            verify(channelDraftRepository, never()).save(any());
        }
    }

    // ==================== TESTS DE startReview ====================

    @Nested
    @DisplayName("startReview - Iniciar revision del borrador")
    class StartReviewTests {

        @Test
        @DisplayName("Debe iniciar revision cuando el borrador esta en estado GENERATED")
        void startReview_estadoGenerated_debeIniciarRevision() {
            // Arrange
            when(channelDraftRepository.findById(DRAFT_ID)).thenReturn(Optional.of(draft));
            when(channelDraftRepository.save(any(ChannelDraft.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            ChannelDraft result = channelDraftService.startReview(DRAFT_ID);

            // Assert
            assertThat(result.getStatus()).isEqualTo(ChannelDraftStatus.IN_REVIEW);
            verify(channelDraftRepository, times(1)).save(any(ChannelDraft.class));
        }

        @Test
        @DisplayName("Debe lanzar InvalidStateException cuando el borrador no esta en GENERATED")
        void startReview_estadoApproved_debeLanzarInvalidStateException() {
            // Arrange
            draft.setStatus(ChannelDraftStatus.APPROVED);
            when(channelDraftRepository.findById(DRAFT_ID)).thenReturn(Optional.of(draft));

            // Act & Assert
            assertThatThrownBy(() -> channelDraftService.startReview(DRAFT_ID))
                    .isInstanceOf(InvalidStateException.class)
                    .hasMessageContaining("Solo se pueden revisar borradores en estado GENERATED");
        }

        @Test
        @DisplayName("Debe lanzar InvalidStateException cuando el borrador esta en IN_REVIEW")
        void startReview_estadoInReview_debeLanzarInvalidStateException() {
            // Arrange
            draft.setStatus(ChannelDraftStatus.IN_REVIEW);
            when(channelDraftRepository.findById(DRAFT_ID)).thenReturn(Optional.of(draft));

            // Act & Assert
            assertThatThrownBy(() -> channelDraftService.startReview(DRAFT_ID))
                    .isInstanceOf(InvalidStateException.class)
                    .hasMessageContaining("Solo se pueden revisar borradores en estado GENERATED");
        }
    }

    // ==================== TESTS DE markAsPublished ====================

    @Nested
    @DisplayName("markAsPublished - Marcar borrador como publicado")
    class MarkAsPublishedTests {

        @Test
        @DisplayName("Debe publicar borrador cuando esta en estado APPROVED")
        void markAsPublished_estadoApproved_debePublicar() {
            // Arrange
            draft.setStatus(ChannelDraftStatus.APPROVED);
            when(channelDraftRepository.findById(DRAFT_ID)).thenReturn(Optional.of(draft));
            when(channelDraftRepository.save(any(ChannelDraft.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            ChannelDraft result = channelDraftService.markAsPublished(DRAFT_ID);

            // Assert
            assertThat(result.getStatus()).isEqualTo(ChannelDraftStatus.PUBLISHED);
            verify(channelDraftRepository, times(1)).save(any(ChannelDraft.class));
        }

        @Test
        @DisplayName("Debe lanzar InvalidStateException cuando el borrador no esta APPROVED")
        void markAsPublished_estadoGenerated_debeLanzarInvalidStateException() {
            // Arrange - draft esta en GENERATED por defecto
            when(channelDraftRepository.findById(DRAFT_ID)).thenReturn(Optional.of(draft));

            // Act & Assert
            assertThatThrownBy(() -> channelDraftService.markAsPublished(DRAFT_ID))
                    .isInstanceOf(InvalidStateException.class)
                    .hasMessageContaining("Solo se pueden publicar borradores aprobados");
        }

        @Test
        @DisplayName("Debe lanzar InvalidStateException cuando el borrador esta REJECTED")
        void markAsPublished_estadoRejected_debeLanzarInvalidStateException() {
            // Arrange
            draft.setStatus(ChannelDraftStatus.REJECTED);
            when(channelDraftRepository.findById(DRAFT_ID)).thenReturn(Optional.of(draft));

            // Act & Assert
            assertThatThrownBy(() -> channelDraftService.markAsPublished(DRAFT_ID))
                    .isInstanceOf(InvalidStateException.class)
                    .hasMessageContaining("Solo se pueden publicar borradores aprobados");
        }
    }

    // ==================== TESTS DE deleteDraft ====================

    @Nested
    @DisplayName("deleteDraft - Eliminar borrador")
    class DeleteDraftTests {

        @Test
        @DisplayName("Debe eliminar borrador en estado GENERATED")
        void deleteDraft_estadoGenerated_debeEliminar() {
            // Arrange
            when(channelDraftRepository.findById(DRAFT_ID)).thenReturn(Optional.of(draft));
            doNothing().when(channelDraftRepository).delete(draft);

            // Act
            channelDraftService.deleteDraft(DRAFT_ID);

            // Assert
            verify(channelDraftRepository, times(1)).delete(draft);
        }

        @Test
        @DisplayName("Debe lanzar InvalidStateException al eliminar borrador PUBLISHED")
        void deleteDraft_estadoPublished_debeLanzarInvalidStateException() {
            // Arrange
            draft.setStatus(ChannelDraftStatus.PUBLISHED);
            when(channelDraftRepository.findById(DRAFT_ID)).thenReturn(Optional.of(draft));

            // Act & Assert
            assertThatThrownBy(() -> channelDraftService.deleteDraft(DRAFT_ID))
                    .isInstanceOf(InvalidStateException.class)
                    .hasMessageContaining("No se puede eliminar un borrador ya publicado");

            verify(channelDraftRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Debe eliminar borrador en estado REJECTED")
        void deleteDraft_estadoRejected_debeEliminar() {
            // Arrange
            draft.setStatus(ChannelDraftStatus.REJECTED);
            when(channelDraftRepository.findById(DRAFT_ID)).thenReturn(Optional.of(draft));
            doNothing().when(channelDraftRepository).delete(draft);

            // Act
            channelDraftService.deleteDraft(DRAFT_ID);

            // Assert
            verify(channelDraftRepository, times(1)).delete(draft);
        }
    }

    // ==================== TESTS DE areAllDraftsApproved ====================

    @Nested
    @DisplayName("areAllDraftsApproved - Verificar si todos los borradores estan aprobados")
    class AreAllDraftsApprovedTests {

        @Test
        @DisplayName("Debe retornar false cuando no hay borradores")
        void areAllDraftsApproved_sinBorradores_debeRetornarFalse() {
            // Arrange
            when(channelDraftRepository.findByWeeklyDigestId(DIGEST_ID))
                    .thenReturn(Collections.emptyList());

            // Act
            boolean result = channelDraftService.areAllDraftsApproved(DIGEST_ID);

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Debe retornar true cuando todos los borradores estan APPROVED")
        void areAllDraftsApproved_todosAprobados_debeRetornarTrue() {
            // Arrange
            ChannelDraft draft1 = ChannelDraft.builder().status(ChannelDraftStatus.APPROVED).build();
            ChannelDraft draft2 = ChannelDraft.builder().status(ChannelDraftStatus.APPROVED).build();
            ChannelDraft draft3 = ChannelDraft.builder().status(ChannelDraftStatus.APPROVED).build();
            when(channelDraftRepository.findByWeeklyDigestId(DIGEST_ID))
                    .thenReturn(List.of(draft1, draft2, draft3));

            // Act
            boolean result = channelDraftService.areAllDraftsApproved(DIGEST_ID);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Debe retornar true cuando los borradores estan APPROVED o PUBLISHED")
        void areAllDraftsApproved_aprobadosYPublicados_debeRetornarTrue() {
            // Arrange
            ChannelDraft draft1 = ChannelDraft.builder().status(ChannelDraftStatus.APPROVED).build();
            ChannelDraft draft2 = ChannelDraft.builder().status(ChannelDraftStatus.PUBLISHED).build();
            when(channelDraftRepository.findByWeeklyDigestId(DIGEST_ID))
                    .thenReturn(List.of(draft1, draft2));

            // Act
            boolean result = channelDraftService.areAllDraftsApproved(DIGEST_ID);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Debe retornar false cuando algun borrador esta en estado diferente")
        void areAllDraftsApproved_algunoNoAprobado_debeRetornarFalse() {
            // Arrange
            ChannelDraft draft1 = ChannelDraft.builder().status(ChannelDraftStatus.APPROVED).build();
            ChannelDraft draft2 = ChannelDraft.builder().status(ChannelDraftStatus.GENERATED).build();
            when(channelDraftRepository.findByWeeklyDigestId(DIGEST_ID))
                    .thenReturn(List.of(draft1, draft2));

            // Act
            boolean result = channelDraftService.areAllDraftsApproved(DIGEST_ID);

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Debe retornar false cuando algun borrador esta REJECTED")
        void areAllDraftsApproved_algunoRechazado_debeRetornarFalse() {
            // Arrange
            ChannelDraft draft1 = ChannelDraft.builder().status(ChannelDraftStatus.APPROVED).build();
            ChannelDraft draft2 = ChannelDraft.builder().status(ChannelDraftStatus.REJECTED).build();
            when(channelDraftRepository.findByWeeklyDigestId(DIGEST_ID))
                    .thenReturn(List.of(draft1, draft2));

            // Act
            boolean result = channelDraftService.areAllDraftsApproved(DIGEST_ID);

            // Assert
            assertThat(result).isFalse();
        }
    }
}
