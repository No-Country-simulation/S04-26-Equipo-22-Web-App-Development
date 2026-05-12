package com.nocountry.webapp.entity;

import com.nocountry.webapp.entity.enums.Role; 
import jakarta.persistence.*;
import lombok.*; 
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity 
@Table(name = "users") 
@Getter @Setter 
@NoArgsConstructor
@AllArgsConstructor
@Builder // Te va a servir mucho para los tests y el DataSeeder
public class User implements UserDetails { // <--- 1. AGREGAMOS EL IMPLEMENTS

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; 

    // --- MÉTODOS OBLIGATORIOS DE USERDETAILS ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Usamos tu enum Role para darle los permisos a Spring
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return email; // Tu email actúa como identificador (username)
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Si es false, el usuario no podrá loguearse
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}