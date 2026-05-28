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
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
@DependsOn("communityDataSeeder")
@Order(2)
public class CommunityPostDataSeeder implements ApplicationRunner {

    private final CommunityPostRepository communityPostRepository;
    private final CommunityRepository communityRepository;

    @Override
    public void run(ApplicationArguments args) {

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime weekStart = now.with(DayOfWeek.MONDAY)
                .toLocalDate()
                .atStartOfDay();

        LocalDateTime weekEnd = now.with(DayOfWeek.SUNDAY)
                .toLocalDate()
                .atTime(LocalTime.MAX);

        // Evitar duplicar datos
        if (communityPostRepository.existsByCollectedAtBetween(
                weekStart,
                weekEnd
        )) {

            log.info("Current week already seeded");
            return;
        }

        log.info("Starting community post seeding...");

        List<Community> communities = communityRepository.findAll();

        if (communities.isEmpty()) {

            log.warn("No communities found. Skipping post seeding.");

            return;
        }

        List<CommunityPost> posts = new ArrayList<>();

        for (Community community : communities) {

            addPostsForCommunity(
                    posts,
                    community,
                    weekStart
            );
        }

        communityPostRepository.saveAll(posts);

        log.info("Community post seeding completed successfully");
        log.info("Seeded {} community posts", posts.size());
    }

    private void addPostsForCommunity(
            List<CommunityPost> posts,
            Community community,
            LocalDateTime weekStart
    ) {

        String communityName = community.getName();

        List<String> contents = switch (communityName) {

            case "Java Developers Argentina" -> List.of(
                    "¿Vale la pena migrar a Java 21 en producción?",
                    "¿Cómo están manejando virtual threads en Spring Boot?",
                    "Buenas prácticas usando Spring Security con JWT.",
                    "¿Hibernate o jOOQ para proyectos grandes?",
                    "¿Qué usan para testing integration en Spring Boot?",
                    "¿Cómo optimizan consultas complejas en JPA?",
                    "Experiencias usando Kafka con microservicios Java.",
                    "¿Cuál es la mejor forma de estructurar APIs REST?",
                    "Errores comunes usando MapStruct y DTOs.",
                    "¿Qué opinan de GraalVM para microservicios?"
            );

            case "Backend Masters" -> List.of(
                    "¿Docker Compose o Kubernetes para un MVP?",
                    "¿Cómo escalan APIs REST en producción?",
                    "¿Qué estrategia usan para cache con Redis?",
                    "¿PostgreSQL o MongoDB para sistemas híbridos?",
                    "Experiencias usando RabbitMQ en microservicios.",
                    "¿Cómo documentan APIs con OpenAPI?",
                    "Buenas prácticas usando Clean Architecture.",
                    "¿Cómo manejan rate limiting?",
                    "¿Vale la pena usar GraphQL?",
                    "¿Cómo monitorean errores en backend?"
            );

            case "Frontend Latam" -> List.of(
                    "¿React Query o Zustand para manejo de estado?",
                    "¿Tailwind o CSS Modules?",
                    "¿Cómo organizan proyectos grandes en React?",
                    "Experiencias usando Next.js App Router.",
                    "¿Qué librería usan para formularios?",
                    "¿Vale la pena usar TypeScript siempre?",
                    "¿Cómo manejan autenticación JWT en frontend?",
                    "¿Qué opinan de Vite vs CRA?",
                    "Mejores prácticas para lazy loading.",
                    "¿Cómo optimizan performance en React?"
            );

            case "Python Community" -> List.of(
                    "¿FastAPI o Django para APIs modernas?",
                    "Experiencias usando Python con IA.",
                    "¿Qué librerías usan para automatización?",
                    "¿Cómo estructuran proyectos grandes en Python?",
                    "¿Pandas sigue siendo la mejor opción?",
                    "¿Cómo hacen web scraping hoy?",
                    "¿Poetry o pip tradicional?",
                    "¿Qué framework recomiendan para testing?",
                    "¿Cómo usan Celery en producción?",
                    "¿Vale la pena usar async en Python?"
            );

            case "DevOps Engineers" -> List.of(
                    "¿Terraform o Pulumi?",
                    "Buenas prácticas con GitHub Actions.",
                    "¿Kubernetes sigue siendo demasiado complejo?",
                    "¿Cómo manejan observabilidad?",
                    "¿Qué usan para CI/CD?",
                    "Experiencias usando AWS ECS.",
                    "¿Prometheus + Grafana o Datadog?",
                    "¿Cómo hacen rollback seguro?",
                    "¿Docker Swarm sigue vivo?",
                    "¿Qué usan para secret management?"
            );

            case "AI & Machine Learning Hub" -> List.of(
                    "¿LangChain o frameworks más simples?",
                    "Experiencias usando Gemini API.",
                    "¿Cómo hacen fine tuning de modelos?",
                    "¿RAG realmente vale la pena?",
                    "¿Qué vector database recomiendan?",
                    "¿Cómo manejan costos en IA?",
                    "¿Open source o APIs cerradas?",
                    "¿Qué usan para embeddings?",
                    "¿Cómo evalúan respuestas de LLMs?",
                    "¿Qué stack usan para apps IA?"
            );

            case "Data Science Argentina" -> List.of(
                    "¿Qué usan para análisis exploratorio?",
                    "¿Power BI o Tableau?",
                    "¿Cómo limpian datasets enormes?",
                    "¿Pandas o Polars?",
                    "¿Qué recomiendan para visualización?",
                    "Experiencias usando notebooks colaborativos.",
                    "¿Qué stack usan en Data Science?",
                    "¿Cómo despliegan modelos?",
                    "¿Feature engineering manual o automático?",
                    "¿Cómo versionan datasets?"
            );

            case "Cloud Computing Latam" -> List.of(
                    "¿AWS o GCP para startups?",
                    "¿Vale la pena serverless?",
                    "¿Cómo optimizan costos cloud?",
                    "Experiencias usando Cloud Run.",
                    "¿Qué usan para IaC?",
                    "¿Cómo manejan multi-cloud?",
                    "¿S3 o alternativas compatibles?",
                    "¿Qué recomiendan para backups?",
                    "¿Cómo hacen autoscaling?",
                    "¿Qué servicios cloud usan más?"
            );

            case "Mobile Developers" -> List.of(
                    "¿Flutter o React Native en 2026?",
                    "¿Cómo manejan push notifications?",
                    "¿Qué arquitectura usan en Android?",
                    "¿SwiftUI ya está maduro?",
                    "¿Cómo comparten código entre plataformas?",
                    "¿Firebase sigue siendo la mejor opción?",
                    "¿Qué usan para analytics mobile?",
                    "¿Cómo optimizan performance en apps?",
                    "¿Vale la pena Kotlin Multiplatform?",
                    "¿Cómo manejan autenticación mobile?"
            );

            case "Cybersecurity Experts" -> List.of(
                    "¿Cómo protegen APIs REST?",
                    "Buenas prácticas usando OAuth2.",
                    "¿Qué herramientas usan para pentesting?",
                    "¿Cómo detectan vulnerabilidades?",
                    "Experiencias usando SIEM.",
                    "¿Vale la pena Zero Trust?",
                    "¿Cómo manejan rotación de secretos?",
                    "¿Qué recomiendan para seguridad cloud?",
                    "¿Cómo hacen auditoría de logs?",
                    "¿Qué framework usan para security testing?"
            );

            default -> List.of(
                    "Discusión general sobre tecnología."
            );
        };

        for (int i = 0; i < contents.size(); i++) {

            CommunityPostType type = switch (i % 4) {
                case 0 -> CommunityPostType.QUESTION;
                case 1 -> CommunityPostType.DISCUSSION;
                case 2 -> CommunityPostType.RESOURCE;
                default -> CommunityPostType.SESSION;
            };

            posts.add(
                    createPost(
                            contents.get(i),
                            generateAuthor(i),
                            type,
                            40 + (i * 7),
                            10 + (i * 4),
                            generateExternalId(community, i),
                            generateSourceUrl(community),
                            weekStart.plusDays(i % 5),
                            community
                    )
            );
        }
    }

    private String generateAuthor(int index) {

        List<String> authors = List.of(
                "Carlos Mendoza",
                "Lucía Fernández",
                "Sofía Herrera",
                "Fernando Castro",
                "TalentCircle Team",
                "Mariano López",
                "Camila Torres",
                "Javier Ruiz",
                "Valentina Gómez",
                "Andrés Silva"
        );

        return authors.get(index % authors.size());
    }

    private String generateExternalId(
            Community community,
            int index
    ) {

        return community.getName()
                .replace(" ", "_")
                .toLowerCase()
                + "_" + index;
    }

    private String generateSourceUrl(Community community) {

        return switch (community.getName()) {

            case "Java Developers Argentina" ->
                    "https://www.reddit.com/r/java/";

            case "Backend Masters" ->
                    "https://www.reddit.com/r/backend/";

            case "Frontend Latam" ->
                    "https://www.reddit.com/r/reactjs/";

            case "Python Community" ->
                    "https://www.reddit.com/r/Python/";

            case "DevOps Engineers" ->
                    "https://www.reddit.com/r/devops/";

            case "AI & Machine Learning Hub" ->
                    "https://www.reddit.com/r/MachineLearning/";

            case "Data Science Argentina" ->
                    "https://www.reddit.com/r/datascience/";

            case "Cloud Computing Latam" ->
                    "https://www.reddit.com/r/aws/";

            case "Mobile Developers" ->
                    "https://www.reddit.com/r/androiddev/";

            case "Cybersecurity Experts" ->
                    "https://www.reddit.com/r/cybersecurity/";

            default ->
                    "https://www.reddit.com/";
        };
    }

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
