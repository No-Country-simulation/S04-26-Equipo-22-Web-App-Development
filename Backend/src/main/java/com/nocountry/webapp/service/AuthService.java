package com.nocountry.webapp.service;

import com.nocountry.webapp.dto.AuthResponseDTO;
import com.nocountry.webapp.entity.RefreshToken;
import com.nocountry.webapp.entity.User;
import com.nocountry.webapp.entity.enums.Role;
import com.nocountry.webapp.exception.base.ConflictException;
import com.nocountry.webapp.exception.base.UnauthorizedException;
import com.nocountry.webapp.repository.RefreshTokenRepository;
import com.nocountry.webapp.repository.UserRepository;
import com.nocountry.webapp.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final RefreshTokenRepository refreshTokenRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    private final AuthenticationManager authenticationManager;

    /**
     * Registro de usuario
     */
    public AuthResponseDTO register(
            String email,
            String password,
            Role role
    ) {

        if (userRepository.existsByEmail(email)) {

            throw new ConflictException(
                    "El email ya está registrado"
            );
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .build();

        userRepository.save(user);

        UserDetails userDetails =
                org.springframework.security.core.userdetails.User
                        .builder()
                        .username(user.getEmail())
                        .password(user.getPassword())
                        .authorities(
                                "ROLE_" + user.getRole().name()
                        )
                        .build();

        String accessToken =
                jwtUtil.generateToken(userDetails);

        String refreshToken =
                jwtUtil.generateRefreshToken(userDetails);

        RefreshToken refreshTokenEntity =
                RefreshToken.builder()
                        .token(refreshToken)
                        .expiryDate(
                                LocalDateTime.now().plusDays(7)
                        )
                        .revoked(false)
                        .user(user)
                        .build();

        refreshTokenRepository.save(refreshTokenEntity);

        return AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * Login de usuario
     */
    public AuthResponseDTO login(
            String email,
            String password
    ) {

        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email,
                            password
                    )
            );

        } catch (BadCredentialsException e) {

            throw new UnauthorizedException(
                    "Email o contraseña incorrectos"
            );
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UnauthorizedException(
                                "Usuario no encontrado"
                        ));
        
        revokeAllUserTokens(user);

        UserDetails userDetails =
                org.springframework.security.core.userdetails.User
                        .builder()
                        .username(user.getEmail())
                        .password(user.getPassword())
                        .authorities(
                                "ROLE_" + user.getRole().name()
                        )
                        .build();

        String accessToken =
                jwtUtil.generateToken(userDetails);

        String refreshToken =
                jwtUtil.generateRefreshToken(userDetails);

        RefreshToken refreshTokenEntity =
                RefreshToken.builder()
                        .token(refreshToken)
                        .expiryDate(
                                LocalDateTime.now().plusDays(7)
                        )
                        .revoked(false)
                        .user(user)
                        .build();

        refreshTokenRepository.save(refreshTokenEntity);

        return AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

        /**
         * Refresh token
         */
        public AuthResponseDTO refreshToken(String refreshToken) {

                RefreshToken storedToken =
                        refreshTokenRepository
                                .findByToken(refreshToken)
                                .orElseThrow(() ->
                                        new UnauthorizedException(
                                                "Refresh token inválido"
                                        ));

                if (storedToken.isRevoked()) {

                        throw new UnauthorizedException(
                                "Refresh token revocado"
                        );
                }

                if (storedToken.getExpiryDate().isBefore(LocalDateTime.now())) {

                        throw new UnauthorizedException(
                                "Refresh token expirado"
                        );
                }

                User user = storedToken.getUser();

                UserDetails userDetails =
                        org.springframework.security.core.userdetails.User
                                .builder()
                                .username(user.getEmail())
                                .password(user.getPassword())
                                .authorities(
                                        "ROLE_" + user.getRole().name()
                                )
                                .build();

                storedToken.setRevoked(true);

                refreshTokenRepository.save(storedToken);

                String newAccessToken =
                        jwtUtil.generateToken(userDetails);

                String newRefreshToken =
                        jwtUtil.generateRefreshToken(userDetails);

                RefreshToken refreshTokenEntity =
                        RefreshToken.builder()
                                .token(newRefreshToken)
                                .expiryDate(
                                        LocalDateTime.now().plusDays(7)
                                )
                                .revoked(false)
                                .user(user)
                                .build();

                refreshTokenRepository.save(refreshTokenEntity);

                return AuthResponseDTO.builder()
                        .accessToken(newAccessToken)
                        .refreshToken(newRefreshToken)
                        .build();
                }

        /**
         * Logout
         */

        public void logout(String refreshToken) {


        RefreshToken token = refreshTokenRepository
                .findByToken(refreshToken)
                .orElseThrow(() ->
                        new UnauthorizedException(
                                "Refresh token inválido"
                        ));

        token.setRevoked(true);

        refreshTokenRepository.save(token);
        }

        private void revokeAllUserTokens(User user) {

                var validTokens =
                        refreshTokenRepository.findByUser(user);

                validTokens.forEach(token ->
                        token.setRevoked(true)
                );

                refreshTokenRepository.saveAll(validTokens);
        }
}