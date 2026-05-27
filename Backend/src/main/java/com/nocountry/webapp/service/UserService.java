package com.nocountry.webapp.service;

import com.nocountry.webapp.dto.UpdatePasswordRequestDTO;
import com.nocountry.webapp.dto.UserResponseDTO;
import com.nocountry.webapp.entity.User;
import com.nocountry.webapp.exception.base.BusinessException;
import com.nocountry.webapp.exception.base.NotFoundException;
import com.nocountry.webapp.repository.RefreshTokenRepository;
import com.nocountry.webapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDTO getByEmail(String email) {

        User user = findUserByEmail(email);

        return mapToDTO(user);
    }

    @Transactional
    public UserResponseDTO updatePassword(
            String email,
            UpdatePasswordRequestDTO request
    ) {

        User user = findUserByEmail(email);

        // Validar password actual
        if (
            !passwordEncoder.matches(
                    request.getCurrentPassword(),
                    user.getPassword()
            )
        ) {
            throw new BusinessException(
                    "La contraseña actual es incorrecta"
            );
        }

        // Evitar misma contraseña
        if (
            passwordEncoder.matches(
                    request.getNewPassword(),
                    user.getPassword()
            )
        ) {
            throw new BusinessException(
                    "La nueva contraseña no puede ser igual a la actual"
            );
        }

        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        userRepository.save(user);

        // Eliminar refresh tokens existentes

        refreshTokenRepository.deleteByUser(user);

        return mapToDTO(user);
    }

    @Transactional
    public void deleteUser(String email) {

        User user = findUserByEmail(email);

        // Eliminar refresh tokens asociados
        refreshTokenRepository.deleteByUser(user);

        userRepository.delete(user);
    }

    // ==================== HELPERS ====================

    private User findUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new NotFoundException(
                                String.format(
                                        "Usuario con email %s no encontrado",
                                        email
                                )
                        )
                );
    }

    private UserResponseDTO mapToDTO(User user) {

        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}