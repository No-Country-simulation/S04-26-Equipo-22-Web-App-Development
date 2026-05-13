package com.nocountry.webapp.service;

import com.nocountry.webapp.entity.User;
import com.nocountry.webapp.entity.enums.Role;
import com.nocountry.webapp.exception.base.BusinessException;
import com.nocountry.webapp.exception.base.ConflictException;
import com.nocountry.webapp.exception.base.NotFoundException;
import com.nocountry.webapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Obtener todos los usuarios
     */
    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        
        if (users.isEmpty()) {
            throw new NotFoundException("No hay usuarios registrados en el sistema");
        }
        
        return users;
    }

    /**
     * Obtener usuario por ID
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Usuario con ID %d no encontrado", id)
                ));
    }

    /**
     * Obtener usuario por email
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Usuario con email %s no encontrado", email)
                ));
    }

    /**
     * Obtener usuarios por rol
     */
    public List<User> getUsersByRole(Role role) {
        List<User> users = userRepository.findByRole(role);
        
        if (users.isEmpty()) {
            throw new NotFoundException(
                    String.format("No hay usuarios con el rol %s", role.name())
            );
        }
        
        return users;
    }

    /**
     * Crear nuevo usuario (solo para administración, sin autenticación)
     * Para registro normal usar AuthService.register()
     */
    @Transactional
    public User createUser(String email, String password, Role role) {
        
        // Validar email
        if (email == null || email.isBlank()) {
            throw new BusinessException("El email no puede estar vacío");
        }
        
        // Validar password
        if (password == null || password.isBlank()) {
            throw new BusinessException("La contraseña no puede estar vacía");
        }
        
        if (password.length() < 6) {
            throw new BusinessException("La contraseña debe tener al menos 6 caracteres");
        }
        
        // Validar email único
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException(
                    String.format("El email %s ya está registrado", email)
            );
        }
        
        // Validar rol
        if (role == null) {
            throw new BusinessException("El rol es obligatorio");
        }
        
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .build();
        
        return userRepository.save(user);
    }

    /**
     * Actualizar usuario existente
     */
    @Transactional
    public User updateUser(Long id, String email, String password, Role role) {
        
        User existingUser = getUserById(id);
        
        // Actualizar email si viene y es diferente
        if (email != null && !email.isBlank() && !email.equals(existingUser.getEmail())) {
            
            // Validar que el nuevo email no exista en otro usuario
            if (userRepository.existsByEmail(email)) {
                throw new ConflictException(
                        String.format("El email %s ya está registrado por otro usuario", email)
                );
            }
            existingUser.setEmail(email);
        }
        
        // Actualizar contraseña si viene
        if (password != null && !password.isBlank()) {
            if (password.length() < 6) {
                throw new BusinessException("La contraseña debe tener al menos 6 caracteres");
            }
            existingUser.setPassword(passwordEncoder.encode(password));
        }
        
        // Actualizar rol si viene
        if (role != null) {
            existingUser.setRole(role);
        }
        
        return userRepository.save(existingUser);
    }

    /**
     * Actualizar solo el rol del usuario
     */
    @Transactional
    public User updateUserRole(Long id, Role role) {
        
        if (role == null) {
            throw new BusinessException("El rol es obligatorio");
        }
        
        User user = getUserById(id);
        user.setRole(role);
        
        return userRepository.save(user);
    }

    /**
     * Actualizar solo el email del usuario
     */
    @Transactional
    public User updateUserEmail(Long id, String newEmail) {
        
        if (newEmail == null || newEmail.isBlank()) {
            throw new BusinessException("El email no puede estar vacío");
        }
        
        User user = getUserById(id);
        
        if (user.getEmail().equals(newEmail)) {
            throw new BusinessException("El nuevo email es igual al actual");
        }
        
        if (userRepository.existsByEmail(newEmail)) {
            throw new ConflictException(
                    String.format("El email %s ya está registrado", newEmail)
            );
        }
        
        user.setEmail(newEmail);
        return userRepository.save(user);
    }

    /**
     * Actualizar solo la contraseña del usuario
     */
    @Transactional
    public User updateUserPassword(Long id, String newPassword) {
        
        if (newPassword == null || newPassword.isBlank()) {
            throw new BusinessException("La contraseña no puede estar vacía");
        }
        
        if (newPassword.length() < 6) {
            throw new BusinessException("La contraseña debe tener al menos 6 caracteres");
        }
        
        User user = getUserById(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        
        return userRepository.save(user);
    }

    /**
     * Eliminar usuario (eliminación física)
     */
    @Transactional
    public void deleteUser(Long id) {
        
        User user = getUserById(id);
        
        // Opcional: prevenir eliminación de ciertos usuarios críticos
        if (user.getEmail().equalsIgnoreCase("admin@system.com")) {
            throw new BusinessException("No se puede eliminar la cuenta de administrador principal");
        }
        
        userRepository.delete(user);
    }

    /**
     * Verificar si existe usuario por email
     */
    public boolean existsByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessException("El email no puede estar vacío");
        }
        return userRepository.existsByEmail(email);
    }

    /**
     * Obtener conteo de usuarios por rol
     */
    public long countUsersByRole(Role role) {
        if (role == null) {
            throw new BusinessException("El rol es obligatorio");
        }
        return userRepository.findByRole(role).size();
    }

    /**
     * Obtener conteo total de usuarios
     */
    public long getTotalUserCount() {
        return userRepository.count();
    }
}