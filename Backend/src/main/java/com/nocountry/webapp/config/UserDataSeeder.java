package com.nocountry.webapp.config;

import com.nocountry.webapp.entity.User;
import com.nocountry.webapp.entity.enums.Role;
import com.nocountry.webapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class UserDataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {

        log.info("🔥 USER SEEDER STARTING...");

        createIfNotExists("admin@talentcircle.com", "Admin123*", Role.ADMIN);
        createIfNotExists("user@talentcircle.com", "User123*", Role.USER);

        log.info("========================================");
        log.info("🚀 USER SEEDER COMPLETED");
        log.info("----------------------------------------");
        log.info("👑 ADMIN -> admin@talentcircle.com / Admin123*");
        log.info("👤 USER  -> user@talentcircle.com / User123*");
        log.info("========================================");
    }

    private void createIfNotExists(String email, String password, Role role) {

        if (userRepository.existsByEmail(email)) {
            log.info("⏭ Skipping existing user: {}", email);
            return;
        }

        userRepository.save(
                User.builder()
                        .email(email)
                        .password(passwordEncoder.encode(password))
                        .role(role)
                        .build()
        );

        log.info("✅ Created user: {} with role {}", email, role);
    }
}