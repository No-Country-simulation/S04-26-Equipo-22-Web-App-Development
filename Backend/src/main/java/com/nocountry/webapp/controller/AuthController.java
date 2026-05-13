package com.nocountry.webapp.controller;

import com.nocountry.webapp.dto.AuthResponseDTO;
import com.nocountry.webapp.dto.LoginRequestDTO;
import com.nocountry.webapp.dto.RefreshTokenRequestDTO;
import com.nocountry.webapp.dto.RegisterRequestDTO;
import com.nocountry.webapp.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registro
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(
            @RequestBody RegisterRequestDTO request
    ) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        authService.register(
                                request.getEmail(),
                                request.getPassword(),
                                request.getRole()
                        )
                );
    }

    /**
     * Login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @RequestBody LoginRequestDTO request
    ) {

        return ResponseEntity.ok(
                authService.login(
                        request.getEmail(),
                        request.getPassword()
                )
        );
    }

    /**
     * Refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refreshToken(
            @RequestBody RefreshTokenRequestDTO request
    ) {

        return ResponseEntity.ok(
                authService.refreshToken(
                        request.getRefreshToken()
                )
        );
    }

    /**
     * Logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestBody RefreshTokenRequestDTO request
    ) {

        authService.logout(
                request.getRefreshToken()
        );

        return ResponseEntity.ok().build();
    }
}