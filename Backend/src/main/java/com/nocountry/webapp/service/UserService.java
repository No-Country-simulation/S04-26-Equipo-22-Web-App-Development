package com.nocountry.webapp.service;

import com.nocountry.webapp.entity.User;
import com.nocountry.webapp.entity.enums.Role; 
import com.nocountry.webapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> listarUsuarios() {
        return userRepository.findAll();
    }

    public User guardarUsuario(User user) {
    
        if (user.getRole() == null) {
            user.setRole(Role.ROLE_USER);
        }
        return userRepository.save(user);
    }
}