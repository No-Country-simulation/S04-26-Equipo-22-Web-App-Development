package com.nocountry.webapp.service;

import com.nocountry.webapp.entity.User;
import com.nocountry.webapp.entity.enums.Role;
import com.nocountry.webapp.exception.base.ConflictException;
import com.nocountry.webapp.exception.base.UnauthorizedException;
import com.nocountry.webapp.repository.UserRepository;
import com.nocountry.webapp.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    private final AuthenticationManager authenticationManager;

    /**
     * Registro de usuario
     */
    public String register(
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

        return jwtUtil.generateToken(userDetails);
    }

    /**
     * Login de usuario
     */
    public String login(
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

        UserDetails userDetails =
                org.springframework.security.core.userdetails.User
                        .builder()
                        .username(user.getEmail())
                        .password(user.getPassword())
                        .authorities(
                                "ROLE_" + user.getRole().name()
                        )
                        .build();

        return jwtUtil.generateToken(userDetails);
    }
}