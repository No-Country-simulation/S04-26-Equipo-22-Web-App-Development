package com.nocountry.webapp.controller;

import com.nocountry.webapp.dto.UpdatePasswordRequestDTO;
import com.nocountry.webapp.dto.UserResponseDTO;
import com.nocountry.webapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints para gestión de usuarios")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(
            summary = "Obtener usuario autenticado",
            description = "Retorna la información del usuario actualmente autenticado"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario obtenido exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content
            )
    })
    public ResponseEntity<UserResponseDTO> getCurrentUser(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                userService.getByEmail(authentication.getName())
        );
    }

    @PatchMapping("/me/password")
    @Operation(
            summary = "Actualizar contraseña",
            description = "Permite al usuario autenticado cambiar su contraseña"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Contraseña actualizada exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o contraseña incorrecta",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content
            )
    })
    public ResponseEntity<UserResponseDTO> updatePassword(
            Authentication authentication,
            @Valid @RequestBody UpdatePasswordRequestDTO requestDTO
    ) {

        UserResponseDTO updatedUser =
                userService.updatePassword(
                        authentication.getName(),
                        requestDTO
                );

        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/me")
    @Operation(
            summary = "Eliminar cuenta",
            description = "Elimina permanentemente la cuenta del usuario autenticado"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Cuenta eliminada exitosamente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content
            )
    })
    public ResponseEntity<Void> deleteCurrentUser(
            Authentication authentication
    ) {

        userService.deleteUser(authentication.getName());

        return ResponseEntity.noContent().build();
    }
}