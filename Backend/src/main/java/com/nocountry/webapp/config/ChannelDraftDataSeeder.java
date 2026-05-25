package com.nocountry.webapp.config;

import com.nocountry.webapp.entity.ChannelDraft;
import com.nocountry.webapp.entity.User;
import com.nocountry.webapp.entity.WeeklyDigest;
import com.nocountry.webapp.entity.enums.ChannelDraftStatus;
import com.nocountry.webapp.repository.ChannelDraftRepository;
import com.nocountry.webapp.repository.UserRepository;
import com.nocountry.webapp.repository.WeeklyDigestRepository;
import com.nocountry.webapp.service.ChannelDraftService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
@DependsOn({
        "userDataSeeder",
        "weeklyDigestDataSeeder"
})
public class ChannelDraftDataSeeder implements ApplicationRunner {

    private final ChannelDraftRepository channelDraftRepository;
    private final WeeklyDigestRepository weeklyDigestRepository;
    private final UserRepository userRepository;
    private final ChannelDraftService channelDraftService;

    @Override
    public void run(ApplicationArguments args) {

        if (channelDraftRepository.count() > 0) {
            log.info("Channel drafts already seeded");
            return;
        }

        log.info("Starting channel draft seeding...");

        List<WeeklyDigest> digests = weeklyDigestRepository.findAll();

        if (digests.isEmpty()) {
            log.warn("No weekly digests found. Skipping draft seeding");
            return;
        }

        User editor = userRepository.findByEmail("user@talentcircle.com")
                .orElseThrow(() ->
                        new RuntimeException("Seeder editor user not found")
                );

        for (WeeklyDigest digest : digests) {

            try {

                List<ChannelDraft> drafts =
                        channelDraftService.createCompleteDrafts(
                                digest.getId(),

                                // NEWSLETTER
                                """
                                🚀 Weekly Newsletter - TalentCircle

                                Esta semana en la comunidad destacamos debates sobre arquitectura backend,
                                testing automatizado, Docker y buenas prácticas en APIs REST.

                                También hubo una gran participación en sesiones colaborativas y recursos
                                compartidos por miembros de la comunidad.

                                Temas destacados:
                                - Spring Boot
                                - Clean Architecture
                                - Microservicios
                                - DevOps
                                - Testing

                                Gracias a toda la comunidad por participar activamente.
                                """,

                                // LINKEDIN
                                """
                                🔥 Esta semana en TalentCircle se compartieron excelentes aportes sobre:

                                ✅ Spring Boot
                                ✅ Arquitectura limpia
                                ✅ Testing automatizado
                                ✅ APIs REST
                                ✅ DevOps

                                La comunidad sigue creciendo gracias al intercambio de conocimiento
                                y colaboración constante 🚀

                                #Java #Backend #SpringBoot #SoftwareEngineering
                                """,

                                // X / Twitter
                                """
                                🚀 Weekly Tech Recap:

                                Spring Boot, clean architecture, testing y DevOps fueron tendencia esta semana en TalentCircle.

                                Gran participación de la comunidad 🔥

                                #Java #Backend #DevOps
                                """,

                                editor.getId()
                        );

                // Variar estados para testing/demo
                if (!drafts.isEmpty()) {

                    drafts.get(0).setStatus(ChannelDraftStatus.APPROVED);

                    drafts.get(1).setStatus(ChannelDraftStatus.IN_REVIEW);

                    drafts.get(2).setStatus(ChannelDraftStatus.GENERATED);

                    channelDraftRepository.saveAll(drafts);
                }

            } catch (Exception e) {

                log.error(
                        "Error creating drafts for digest {}: {}",
                        digest.getId(),
                        e.getMessage()
                );
            }
        }

        log.info("Channel draft seeding completed successfully");
    }
}
