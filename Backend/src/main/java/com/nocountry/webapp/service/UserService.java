package com.nocountry.webapp.service;

import com.nocountry.webapp.entity.User;
import com.nocountry.webapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> listarUsuarios() {
        return userRepository.findAll();
    }

    public User obtenerPorId(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public User guardarUsuario(User user) {
        return userRepository.save(user);
    }

    public void eliminarUsuario(Long id) {
        userRepository.deleteById(id);
    }
}