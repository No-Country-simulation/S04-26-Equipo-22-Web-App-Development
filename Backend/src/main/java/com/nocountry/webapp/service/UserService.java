package com.nocountry.webapp.service;

import com.nocountry.webapp.dto.UserResponseDTO;
import com.nocountry.webapp.entity.User;
import com.nocountry.webapp.exception.base.NotFoundException;
import com.nocountry.webapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserResponseDTO getByEmail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new NotFoundException(
                                String.format("Usuario con email %s no encontrado", email)
                        )
                );

        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}