package com.nocountry.webapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.nocountry.webapp.entity.enums.Role; 
import jakarta.persistence.*;
import lombok.*; 


@Entity 
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(columnNames = "email")
) 
@Getter 
@Setter 
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role; 
}