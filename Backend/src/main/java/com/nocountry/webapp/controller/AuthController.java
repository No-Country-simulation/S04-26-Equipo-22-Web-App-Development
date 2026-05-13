package com.nocountry.webapp.controller;

import com.nocountry.webapp.dto.AuthResponseDTO;
import com.nocountry.webapp.dto.LoginRequestDTO;
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

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(
            @RequestBody RegisterRequestDTO request
    ) {

        String token = authService.register(
                request.getEmail(),
                request.getPassword(),
                request.getRole()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new AuthResponseDTO(token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @RequestBody LoginRequestDTO request
    ) {

        String token = authService.login(
                request.getEmail(),
                request.getPassword()
        );

        return ResponseEntity.ok(
                new AuthResponseDTO(token)
        );
    }
}