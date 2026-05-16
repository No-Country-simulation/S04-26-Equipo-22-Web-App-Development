package com.nocountry.webapp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()

                // 📄 Info general de la API
                .info(new Info()
                        .title("TalentCircle API Documentation")
                        .version("1.0")
                        .description("""
                                Documentación de la API de TalentCircle.

                                🔗 Backend: http://localhost:8080
                                """)
                        .contact(new Contact()
                                .name("TalentCircle Team")
                        )
                )

                // 🌍 Servers
                .addServersItem(new Server()
                        .url("http://localhost:8080")
                        .description("Servidor Local")
                )

                // 🔐 JWT Security (Swagger Authorize button)
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                );
    }
}