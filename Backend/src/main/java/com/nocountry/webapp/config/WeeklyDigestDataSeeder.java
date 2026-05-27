package com.nocountry.webapp.config;

import com.nocountry.webapp.entity.Community;
import com.nocountry.webapp.entity.WeeklyDigest;
import com.nocountry.webapp.entity.enums.WeeklyDigestStatus;
import com.nocountry.webapp.repository.CommunityRepository;
import com.nocountry.webapp.repository.WeeklyDigestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.DependsOn;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

//@Component
@DependsOn("communityDataSeeder")
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class WeeklyDigestDataSeeder implements ApplicationRunner {

    private final WeeklyDigestRepository weeklyDigestRepository;
    private final CommunityRepository communityRepository;

    @Override
    public void run(ApplicationArguments args) {

        // Evitar duplicados
        if (weeklyDigestRepository.count() > 0) {
            log.info("Weekly digests already seeded");
            return;
        }

        log.info("Starting weekly digest seeding...");

        List<Community> communities = communityRepository.findAll();

        if (communities.isEmpty()) {
            log.warn("No communities found. Skipping weekly digest seeding");
            return;
        }

        List<WeeklyDigest> digests = new ArrayList<>();

        LocalDate currentWeekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        LocalDate currentWeekEnd = currentWeekStart.plusDays(6);

        for (Community community : communities) {

            // Digest actual PENDING
            digests.add(
                    createDigest(
                            community,
                            currentWeekStart,
                            currentWeekEnd,
                            """
                            Esta semana la comunidad tuvo una participación destacada en discusiones técnicas,
                            intercambio de recursos y sesiones colaborativas.
                            Se compartieron buenas prácticas de desarrollo backend,
                            arquitectura limpia y herramientas modernas para equipos ágiles.
                            """,
                            WeeklyDigestStatus.PENDING
                    )
            );

            // Digest anterior PROCESSED
            LocalDate previousWeekStart = currentWeekStart.minusWeeks(1);
            LocalDate previousWeekEnd = previousWeekStart.plusDays(6);

            digests.add(
                    createDigest(
                            community,
                            previousWeekStart,
                            previousWeekEnd,
                            """
                            La semana pasada se destacaron publicaciones sobre Spring Boot,
                            Docker, testing automatizado y optimización de APIs REST.
                            Además, hubo una alta interacción en preguntas de arquitectura
                            y buenas prácticas de clean code.
                            """,
                            WeeklyDigestStatus.PROCESSED
                    )
            );
        }

        weeklyDigestRepository.saveAll(digests);

        log.info("Weekly digest seeding completed successfully");
    }

    private WeeklyDigest createDigest(
            Community community,
            LocalDate weekStart,
            LocalDate weekEnd,
            String summary,
            WeeklyDigestStatus status
    ) {

        return WeeklyDigest.builder()
                .community(community)
                .weekStart(weekStart)
                .weekEnd(weekEnd)
                .summary(summary)
                .status(status)
                .build();
    }
}