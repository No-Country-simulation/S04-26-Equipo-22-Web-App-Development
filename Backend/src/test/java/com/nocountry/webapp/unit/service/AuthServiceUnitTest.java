package com.nocountry.webapp.unit.service;

import com.nocountry.webapp.dto.AuthResponseDTO;
import com.nocountry.webapp.entity.RefreshToken;
import com.nocountry.webapp.entity.User;
import com.nocountry.webapp.entity.enums.Role;
import com.nocountry.webapp.exception.base.ConflictException;
import com.nocountry.webapp.exception.base.UnauthorizedException;
import com.nocountry.webapp.repository.RefreshTokenRepository;
import com.nocountry.webapp.repository.UserRepository;
import com.nocountry.webapp.security.JwtUtil;
import com.nocountry.webapp.service.AuthService;
import com.nocountry.webapp.unit.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit Test para AuthService.
 *
 * Cubre los flujos de registro, login, refresh token y logout,
 * validando tanto los caminos exitosos como los casos de error.
 */
class AuthServiceUnitTest extends BaseUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    // Datos de prueba reutilizables
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "SecurePass123!";
    private static final String ENCODED_PASSWORD = "$2a$10$encodedPassword";
    private static final String ACCESS_TOKEN = "eyJ.access.token";
    private static final String REFRESH_TOKEN_STRING = "eyJ.refresh.token";
    private static final String NEW_ACCESS_TOKEN = "eyJ.new.access.token";
    private static final String NEW_REFRESH_TOKEN_STRING = "eyJ.new.refresh.token";
    private static final long REFRESH_EXPIRATION_MS = 604800000L;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email(TEST_EMAIL)
                .password(ENCODED_PASSWORD)
                .role(Role.USER)
                .build();
    }

    // ==================== TESTS DE REGISTRO ====================

    @Nested
    @DisplayName("Registro de usuario")
    class RegistroTests {

        @Test
        @DisplayName("Debe registrar usuario exitosamente con rol USER, codificar password y generar tokens")
        void register_conDatosValidos_debeCrearUsuarioYRetornarTokens() {
            // Arrange
            when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User saved = invocation.getArgument(0);
                saved.setId(1L);
                return saved;
            });
            when(jwtUtil.generateToken(any())).thenReturn(ACCESS_TOKEN);
            when(jwtUtil.generateRefreshToken(any())).thenReturn(REFRESH_TOKEN_STRING);
            when(jwtUtil.getRefreshExpirationMs()).thenReturn(REFRESH_EXPIRATION_MS);
            when(refreshTokenRepository.save(any(RefreshToken.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            AuthResponseDTO result = authService.register(TEST_EMAIL, TEST_PASSWORD);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(result.getRefreshToken()).isEqualTo(REFRESH_TOKEN_STRING);

            // Verificar que se codifica la password
            verify(passwordEncoder, times(1)).encode(TEST_PASSWORD);

            // Verificar que el usuario se guarda con rol USER
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository, times(1)).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getEmail()).isEqualTo(TEST_EMAIL);
            assertThat(savedUser.getPassword()).isEqualTo(ENCODED_PASSWORD);
            assertThat(savedUser.getRole()).isEqualTo(Role.USER);

            // Verificar que se genera y guarda el refresh token
            verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("Debe lanzar ConflictException cuando el email ya esta registrado")
        void register_conEmailDuplicado_debeLanzarConflictException() {
            // Arrange
            when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> authService.register(TEST_EMAIL, TEST_PASSWORD))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("El email ya está registrado");

            // Verificar que nunca se intenta guardar un usuario
            verify(userRepository, never()).save(any());
            verify(passwordEncoder, never()).encode(anyString());
            verify(refreshTokenRepository, never()).save(any());
        }
    }

    // ==================== TESTS DE LOGIN ====================

    @Nested
    @DisplayName("Login de usuario")
    class LoginTests {

        @Test
        @DisplayName("Debe autenticar usuario, revocar tokens anteriores y generar nuevos tokens")
        void login_conCredencialesValidas_debeRetornarTokens() {
            // Arrange
            when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
            when(refreshTokenRepository.findByUser(testUser)).thenReturn(List.of());
            when(jwtUtil.generateToken(any())).thenReturn(ACCESS_TOKEN);
            when(jwtUtil.generateRefreshToken(any())).thenReturn(REFRESH_TOKEN_STRING);
            when(jwtUtil.getRefreshExpirationMs()).thenReturn(REFRESH_EXPIRATION_MS);
            when(refreshTokenRepository.save(any(RefreshToken.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            AuthResponseDTO result = authService.login(TEST_EMAIL, TEST_PASSWORD);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(result.getRefreshToken()).isEqualTo(REFRESH_TOKEN_STRING);

            // Verificar que se autentico con AuthenticationManager
            verify(authenticationManager, times(1)).authenticate(
                    any(UsernamePasswordAuthenticationToken.class)
            );

            // Verificar que se revocaron tokens anteriores
            verify(refreshTokenRepository, times(1)).findByUser(testUser);

            // Verificar que se guardo el nuevo refresh token
            verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("Debe revocar tokens activos existentes antes de generar nuevos")
        void login_conTokensExistentes_debeRevocarTokensAnteriores() {
            // Arrange
            RefreshToken existingToken = RefreshToken.builder()
                    .id(10L)
                    .token("old-token")
                    .expiryDate(LocalDateTime.now().plusDays(7))
                    .revoked(false)
                    .user(testUser)
                    .build();

            when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
            when(refreshTokenRepository.findByUser(testUser)).thenReturn(List.of(existingToken));
            when(jwtUtil.generateToken(any())).thenReturn(ACCESS_TOKEN);
            when(jwtUtil.generateRefreshToken(any())).thenReturn(REFRESH_TOKEN_STRING);
            when(jwtUtil.getRefreshExpirationMs()).thenReturn(REFRESH_EXPIRATION_MS);
            when(refreshTokenRepository.save(any(RefreshToken.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            authService.login(TEST_EMAIL, TEST_PASSWORD);

            // Assert - El token existente debe haber sido revocado
            assertThat(existingToken.isRevoked()).isTrue();

            // Verificar que se guardaron los tokens revocados con saveAll
            verify(refreshTokenRepository, times(1)).saveAll(anyList());
        }

        @Test
        @DisplayName("Debe lanzar UnauthorizedException cuando las credenciales son incorrectas")
        void login_conCredencialesInvalidas_debeLanzarUnauthorizedException() {
            // Arrange
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            // Act & Assert
            assertThatThrownBy(() -> authService.login(TEST_EMAIL, "wrongPassword"))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Email o contraseña incorrectos");

            // Verificar que no se busca al usuario ni se generan tokens
            verify(userRepository, never()).findByEmail(anyString());
            verify(jwtUtil, never()).generateToken(any());
            verify(refreshTokenRepository, never()).save(any());
        }
    }

    // ==================== TESTS DE REFRESH TOKEN ====================

    @Nested
    @DisplayName("Renovacion de tokens")
    class RefreshTokenTests {

        private RefreshToken storedToken;

        @BeforeEach
        void setUpRefreshToken() {
            storedToken = RefreshToken.builder()
                    .id(5L)
                    .token(REFRESH_TOKEN_STRING)
                    .expiryDate(LocalDateTime.now().plusDays(7))
                    .revoked(false)
                    .user(testUser)
                    .build();
        }

        @Test
        @DisplayName("Debe revocar token anterior y generar nuevo par de tokens")
        void refreshToken_conTokenValido_debeGenerarNuevosTokens() {
            // Arrange
            when(refreshTokenRepository.findByToken(REFRESH_TOKEN_STRING))
                    .thenReturn(Optional.of(storedToken));
            when(jwtUtil.generateToken(any())).thenReturn(NEW_ACCESS_TOKEN);
            when(jwtUtil.generateRefreshToken(any())).thenReturn(NEW_REFRESH_TOKEN_STRING);
            when(jwtUtil.getRefreshExpirationMs()).thenReturn(REFRESH_EXPIRATION_MS);
            when(refreshTokenRepository.save(any(RefreshToken.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            AuthResponseDTO result = authService.refreshToken(REFRESH_TOKEN_STRING);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo(NEW_ACCESS_TOKEN);
            assertThat(result.getRefreshToken()).isEqualTo(NEW_REFRESH_TOKEN_STRING);

            // El token anterior debe haber sido revocado
            assertThat(storedToken.isRevoked()).isTrue();

            // Se debe guardar dos veces: una para revocar el viejo, otra para el nuevo
            verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("Debe lanzar UnauthorizedException cuando el token no existe")
        void refreshToken_conTokenInvalido_debeLanzarUnauthorizedException() {
            // Arrange
            when(refreshTokenRepository.findByToken("token-inexistente"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authService.refreshToken("token-inexistente"))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Refresh token inválido");

            verify(jwtUtil, never()).generateToken(any());
            verify(jwtUtil, never()).generateRefreshToken(any());
        }

        @Test
        @DisplayName("Debe lanzar UnauthorizedException cuando el token esta revocado")
        void refreshToken_conTokenRevocado_debeLanzarUnauthorizedException() {
            // Arrange
            storedToken.setRevoked(true);
            when(refreshTokenRepository.findByToken(REFRESH_TOKEN_STRING))
                    .thenReturn(Optional.of(storedToken));

            // Act & Assert
            assertThatThrownBy(() -> authService.refreshToken(REFRESH_TOKEN_STRING))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Refresh token revocado");

            verify(jwtUtil, never()).generateToken(any());
        }

        @Test
        @DisplayName("Debe lanzar UnauthorizedException cuando el token esta expirado")
        void refreshToken_conTokenExpirado_debeLanzarUnauthorizedException() {
            // Arrange
            storedToken.setExpiryDate(LocalDateTime.now().minusDays(1));
            when(refreshTokenRepository.findByToken(REFRESH_TOKEN_STRING))
                    .thenReturn(Optional.of(storedToken));

            // Act & Assert
            assertThatThrownBy(() -> authService.refreshToken(REFRESH_TOKEN_STRING))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Refresh token expirado");

            verify(jwtUtil, never()).generateToken(any());
        }
    }

    // ==================== TESTS DE LOGOUT ====================

    @Nested
    @DisplayName("Cierre de sesion")
    class LogoutTests {

        @Test
        @DisplayName("Debe marcar el refresh token como revocado")
        void logout_conTokenValido_debeRevocarToken() {
            // Arrange
            RefreshToken activeToken = RefreshToken.builder()
                    .id(7L)
                    .token(REFRESH_TOKEN_STRING)
                    .expiryDate(LocalDateTime.now().plusDays(7))
                    .revoked(false)
                    .user(testUser)
                    .build();

            when(refreshTokenRepository.findByToken(REFRESH_TOKEN_STRING))
                    .thenReturn(Optional.of(activeToken));
            when(refreshTokenRepository.save(any(RefreshToken.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            authService.logout(REFRESH_TOKEN_STRING);

            // Assert
            assertThat(activeToken.isRevoked()).isTrue();
            verify(refreshTokenRepository, times(1)).save(activeToken);
        }

        @Test
        @DisplayName("Debe lanzar UnauthorizedException cuando el token no existe")
        void logout_conTokenInvalido_debeLanzarUnauthorizedException() {
            // Arrange
            when(refreshTokenRepository.findByToken("token-invalido"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authService.logout("token-invalido"))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Refresh token inválido");

            verify(refreshTokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe retornar sin error cuando el token ya esta revocado")
        void logout_conTokenYaRevocado_debeRetornarSinError() {
            // Arrange
            RefreshToken revokedToken = RefreshToken.builder()
                    .id(8L)
                    .token(REFRESH_TOKEN_STRING)
                    .expiryDate(LocalDateTime.now().plusDays(7))
                    .revoked(true)
                    .user(testUser)
                    .build();

            when(refreshTokenRepository.findByToken(REFRESH_TOKEN_STRING))
                    .thenReturn(Optional.of(revokedToken));

            // Act - No debe lanzar excepcion
            authService.logout(REFRESH_TOKEN_STRING);

            // Assert - No debe intentar guardar porque ya estaba revocado
            verify(refreshTokenRepository, never()).save(any());
        }
    }
}
