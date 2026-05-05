package com.nocountry.webapp.controller;

import com.nocountry.webapp.entity.User;
import com.nocountry.webapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public List<User> getAll() {
        return userService.listarUsuarios();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        return userService.guardarUsuario(user);
    }
}