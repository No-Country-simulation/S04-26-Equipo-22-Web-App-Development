package com.nocountry.webapp.config;

import com.nocountry.webapp.entity.Community;
import com.nocountry.webapp.entity.CommunityPost;
import com.nocountry.webapp.entity.enums.CommunityPostType;
import com.nocountry.webapp.repository.CommunityPostRepository;
import com.nocountry.webapp.repository.CommunityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test") // Evita ejecutar el seeder en tests
public class CommunityPostDataSeeder implements ApplicationRunner {

    private final CommunityPostRepository communityPostRepository;
    private final CommunityRepository communityRepository;

    @Override
    public void run(ApplicationArguments args) {

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime weekStart = now
                .with(java.time.DayOfWeek.MONDAY)
                .toLocalDate()
                .atStartOfDay();

        LocalDateTime weekEnd = weekStart
                .plusDays(6)
                .with(LocalTime.MAX);

        // Evitar duplicar datos de la semana actual
        if (
        communityPostRepository.existsByCollectedAtBetween(
                weekStart,
                weekEnd
        )
        ) {
        log.info("Current week already seeded");
        return;
        }

        log.info("Starting community post seeding...");

        // Obtener comunidades existentes
        List<Community> communities = communityRepository.findAll();

        // Validación por si no existen comunidades
        if (communities.size() < 4) {

        log.warn(
                "At least 4 communities are required for CommunityPost seeding. Found: {}",
                communities.size()
        );

        return;
        }

        /*
         * Usamos distintas comunidades para que:
         * - el dashboard tenga variedad
         * - los endpoints top/community funcionen bien
         * - el digest semanal tenga contenido útil
         */
        Community javaCommunity = communities.get(0);
        Community backendCommunity = communities.get(1);
        Community frontendCommunity = communities.get(2);
        Community pythonCommunity = communities.get(3);

        List<CommunityPost> posts = List.of(

              // ========================= QUESTIONS =========================

            createPost(
                    "¿Cómo estás manejando JWT con refresh tokens en Spring Boot?",
                    "Carlos Mendoza",
                    CommunityPostType.QUESTION,
                    54,
                    32,
                    "reddit_1001",
                    "https://www.reddit.com/r/SpringBoot/comments/1q3tjbh/jwt_auth_refresh_token/",
                    weekStart.plusDays(1),
                    javaCommunity
            ),

            createPost(
                    "¿Cómo resolverías el problema N+1 en JPA/Hibernate?",
                    "Lucía Fernández",
                    CommunityPostType.QUESTION,
                    67,
                    41,
                    "reddit_1002",
                    "https://www.reddit.com/r/SpringBoot/comments/1p32mxf/n1_query_problem/",
                    weekStart.plusDays(3),
                    backendCommunity
            ),

            // ========================= RESOURCES =========================

            createPost(
                    "Curso de Spring Boot de nivel básico a intermedio con ejemplo práctico de API REST.",
                    "TalentCircle Resources",
                    CommunityPostType.RESOURCE,
                    88,
                    11,
                    "reddit_2001",
                    "https://www.reddit.com/r/SpringBoot/comments/1tbsvwu/beginner_to_intermediate_sprint_boot_course/",
                    weekStart.plusDays(1),
                    javaCommunity
            ),

            // ========================= SESSIONS =========================

            createPost(
                    "Sesión sobre cómo iniciar y organizar una videoconferencia en Spring Boot.",
                    "TalentCircle Events",
                    CommunityPostType.SESSION,
                    74,
                    9,
                    "reddit_3001",
                    "https://www.reddit.com/r/SpringBoot/comments/1f2ef0f/video_conference_in_spring_boot/",
                    weekStart.plusDays(4),
                    backendCommunity
            ),

            // ========================= DISCUSSIONS =========================

            createPost(
                    "Debate: ¿Spring Boot directo en Linux o con Docker Compose para un proyecto real?",
                    "Fernando Castro",
                    CommunityPostType.DISCUSSION,
                    80,
                    57,
                    "reddit_4001",
                    "https://www.reddit.com/r/SpringBoot/comments/1lbal4n/is_it_better_to_use_spring_boot_directly_on_linux/",
                    weekStart.plusDays(2),
                    backendCommunity
            ),

            createPost(
                    "¿Qué conviene más en un equipo: React Query, Context o Redux/Zustand para manejar estado?",
                    "Sofía Herrera",
                    CommunityPostType.DISCUSSION,
                    72,
                    48,
                    "reddit_4002",
                    "https://www.reddit.com/r/reactjs/comments/1mqvj2k/best_way_to_organize_react_query_for_a_team/",
                    weekStart.plusDays(1),
                    frontendCommunity
            ),

            createPost(
                    "Experiencias usando IA y Python para automatizar generación de contenido técnico.",
                    "Sofía Herrera",
                    CommunityPostType.DISCUSSION,
                    72,
                    48,
                    "reddit_5001",
                    "https://www.reddit.com/r/Python/comments/1m5m3l8/using_ai_with_python_to_automate_content_creation/",
                    weekStart.plusDays(1),
                    pythonCommunity
            )
                
        );

        // Guardar todos los posts
        communityPostRepository.saveAll(posts);

        log.info("Community post seeding completed successfully");
        log.info("Seeded {} community posts", posts.size());
    }

    /**
     * Método helper para crear posts más fácilmente
     */
    private CommunityPost createPost(
            String content,
            String authorName,
            CommunityPostType type,
            Integer reactionsCount,
            Integer commentsCount,
            String externalPostId,
            String sourceUrl,
            LocalDateTime collectedAt,
            Community community
    ) {

        CommunityPost post = new CommunityPost();

        post.setContent(content);
        post.setAuthorName(authorName);
        post.setType(type);
        post.setReactionsCount(reactionsCount);
        post.setCommentsCount(commentsCount);
        post.setExternalPostId(externalPostId);
        post.setSourceUrl(sourceUrl);
        post.setCollectedAt(collectedAt);
        post.setCommunity(community);

        return post;
    }
}