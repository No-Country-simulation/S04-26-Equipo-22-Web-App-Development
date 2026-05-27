package com.nocountry.webapp.config;

import com.nocountry.webapp.entity.Community;
import com.nocountry.webapp.service.CommunityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
@Order(1)
public class CommunityDataSeeder implements ApplicationRunner {

    private final CommunityService communityService;

    @Override
    public void run(ApplicationArguments args) {

        // Evitar duplicar datos
        if (!communityService.getAllCommunities().isEmpty()) {
            log.info("Communities already seeded");
            return;
        }

        log.info("Starting community seeding...");

        List<Community> communities = List.of(
                createCommunity("Java Developers Argentina", "Discord", true),
                createCommunity("Backend Masters", "Slack", true),
                createCommunity("Frontend Latam", "Discord", true),
                createCommunity("Python Community", "Telegram", true),
                createCommunity("DevOps Engineers", "Slack", true),
                createCommunity("AI & Machine Learning Hub", "Discord", true),
                createCommunity("Data Science Argentina", "WhatsApp", true),
                createCommunity("Cloud Computing Latam", "Discord", true),
                createCommunity("Mobile Developers", "Telegram", true),
                createCommunity("Cybersecurity Experts", "Slack", false)
        );

        communities.forEach(communityService::createCommunity);

        log.info("Community seeding completed successfully");
    }

    private Community createCommunity(String name, String platform, boolean active) {
        Community community = new Community();
        community.setName(name);
        community.setPlatform(platform);
        community.setActive(active);
        return community;
    }
}