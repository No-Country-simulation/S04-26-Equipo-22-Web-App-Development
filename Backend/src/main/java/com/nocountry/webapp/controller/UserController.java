package com.nocountry.webapp.controller;

import com.nocountry.webapp.dto.*;
import com.nocountry.webapp.entity.User;
import com.nocountry.webapp.entity.enums.Role;
import com.nocountry.webapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints para gestión de usuarios")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener todos los usuarios", 
               description = "Retorna una lista de todos los usuarios registrados - Solo para ADMIN")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuarios obtenidos exitosamente"),
        @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN", content = @Content),
        @ApiResponse(responseCode = "404", description = "No hay usuarios registrados", content = @Content)
    })
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserResponseDTO> response = users.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isSameUser(authentication, #id)")
    @Operation(summary = "Obtener usuario por ID", 
               description = "Retorna un usuario específico según su ID - ADMIN o el propio usuario")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content),
        @ApiResponse(responseCode = "403", description = "No autorizado", content = @Content)
    })
    public ResponseEntity<UserResponseDTO> getUserById(
            @Parameter(description = "ID del usuario", example = "1", required = true)
            @PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(convertToResponseDTO(user));
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener usuario por email", 
               description = "Retorna un usuario específico según su email - Solo para ADMIN")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content),
        @ApiResponse(responseCode = "403", description = "No autorizado", content = @Content)
    })
    public ResponseEntity<UserResponseDTO> getUserByEmail(
            @Parameter(description = "Email del usuario", example = "user@example.com", required = true)
            @PathVariable String email) {
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(convertToResponseDTO(user));
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener usuarios por rol", 
               description = "Retorna una lista de usuarios con un rol específico - Solo para ADMIN")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuarios encontrados"),
        @ApiResponse(responseCode = "404", description = "No hay usuarios con ese rol", content = @Content),
        @ApiResponse(responseCode = "403", description = "No autorizado", content = @Content)
    })
    public ResponseEntity<List<UserResponseDTO>> getUsersByRole(
            @Parameter(description = "Rol del usuario", example = "USER", required = true)
            @PathVariable Role role) {
        List<User> users = userService.getUsersByRole(role);
        List<UserResponseDTO> response = users.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear nuevo usuario", 
               description = "Crea un nuevo usuario en el sistema - Solo para ADMIN")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
        @ApiResponse(responseCode = "409", description = "Email ya registrado", content = @Content),
        @ApiResponse(responseCode = "403", description = "No autorizado", content = @Content)
    })
    public ResponseEntity<UserResponseDTO> createUser(
            @Valid @RequestBody UserCreateDTO createDTO) {
        User user = userService.createUser(
                createDTO.getEmail(),
                createDTO.getPassword(),
                createDTO.getRole()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(convertToResponseDTO(user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar usuario completamente", 
               description = "Actualiza todos los datos de un usuario existente - Solo para ADMIN")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content),
        @ApiResponse(responseCode = "409", description = "Email ya registrado por otro usuario", content = @Content),
        @ApiResponse(responseCode = "403", description = "No autorizado", content = @Content)
    })
    public ResponseEntity<UserResponseDTO> updateUser(
            @Parameter(description = "ID del usuario", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDTO updateDTO) {
        User user = userService.updateUser(
                id,
                updateDTO.getEmail(),
                updateDTO.getPassword(),
                updateDTO.getRole()
        );
        return ResponseEntity.ok(convertToResponseDTO(user));
    }

    @PatchMapping("/{id}/email")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isSameUser(authentication, #id)")
    @Operation(summary = "Actualizar email del usuario", 
               description = "Actualiza solo el email de un usuario - ADMIN o el propio usuario")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Email inválido", content = @Content),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content),
        @ApiResponse(responseCode = "409", description = "Email ya registrado", content = @Content),
        @ApiResponse(responseCode = "403", description = "No autorizado", content = @Content)
    })
    public ResponseEntity<UserResponseDTO> updateUserEmail(
            @Parameter(description = "ID del usuario", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UserEmailUpdateDTO emailUpdateDTO) {
        User user = userService.updateUserEmail(id, emailUpdateDTO.getEmail());
        return ResponseEntity.ok(convertToResponseDTO(user));
    }

    @PatchMapping("/{id}/password")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isSameUser(authentication, #id)")
    @Operation(summary = "Actualizar contraseña del usuario", 
               description = "Actualiza solo la contraseña de un usuario - ADMIN o el propio usuario")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contraseña actualizada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Contraseña inválida", content = @Content),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content),
        @ApiResponse(responseCode = "403", description = "No autorizado", content = @Content)
    })
    public ResponseEntity<UserResponseDTO> updateUserPassword(
            @Parameter(description = "ID del usuario", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UserPasswordUpdateDTO passwordUpdateDTO) {
        User user = userService.updateUserPassword(id, passwordUpdateDTO.getPassword());
        return ResponseEntity.ok(convertToResponseDTO(user));
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar rol del usuario", 
               description = "Actualiza solo el rol de un usuario - Solo para ADMIN")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rol actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Rol inválido", content = @Content),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content),
        @ApiResponse(responseCode = "403", description = "No autorizado", content = @Content)
    })
    public ResponseEntity<UserResponseDTO> updateUserRole(
            @Parameter(description = "ID del usuario", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UserRoleUpdateDTO roleUpdateDTO) {
        User user = userService.updateUserRole(id, roleUpdateDTO.getRole());
        return ResponseEntity.ok(convertToResponseDTO(user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar usuario", 
               description = "Elimina físicamente un usuario de la base de datos - Solo para ADMIN")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Usuario eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content),
        @ApiResponse(responseCode = "403", description = "No autorizado", content = @Content),
        @ApiResponse(responseCode = "400", description = "No se puede eliminar el usuario", content = @Content)
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID del usuario", example = "1", required = true)
            @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener total de usuarios", 
               description = "Retorna el número total de usuarios registrados - Solo para ADMIN")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Total obtenido exitosamente"),
        @ApiResponse(responseCode = "403", description = "No autorizado", content = @Content)
    })
    public ResponseEntity<Long> getTotalUserCount() {
        return ResponseEntity.ok(userService.getTotalUserCount());
    }

    @GetMapping("/count/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener total de usuarios por rol", 
               description = "Retorna el número de usuarios con un rol específico - Solo para ADMIN")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Total obtenido exitosamente"),
        @ApiResponse(responseCode = "403", description = "No autorizado", content = @Content)
    })
    public ResponseEntity<Long> countUsersByRole(
            @Parameter(description = "Rol del usuario", example = "USER", required = true)
            @PathVariable Role role) {
        return ResponseEntity.ok(userService.countUsersByRole(role));
    }

    @GetMapping("/exists/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Verificar si existe usuario por email", 
               description = "Verifica si ya existe un usuario con el email proporcionado - Solo para ADMIN")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Verificación exitosa"),
        @ApiResponse(responseCode = "403", description = "No autorizado", content = @Content)
    })
    public ResponseEntity<Boolean> existsByEmail(
            @Parameter(description = "Email del usuario", example = "user@example.com", required = true)
            @PathVariable String email) {
        return ResponseEntity.ok(userService.existsByEmail(email));
    }

    // Método de conversión
    private UserResponseDTO convertToResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}