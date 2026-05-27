    package com.nocountry.webapp.integration;

    import com.fasterxml.jackson.databind.ObjectMapper;
    import com.nocountry.webapp.repository.CommunityRepository;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
    import org.springframework.boot.test.context.SpringBootTest;
    import org.springframework.test.context.ActiveProfiles;
    import org.springframework.test.context.TestPropertySource;
    import org.springframework.test.web.servlet.MockMvc;
    import org.springframework.transaction.annotation.Transactional;

    @SpringBootTest
    @AutoConfigureMockMvc(addFilters = false)
    @ActiveProfiles("test")
    @TestPropertySource(locations = "classpath:application-test.properties")
    @Transactional
    public abstract class BaseIntegrationTest {

        @Autowired
        protected MockMvc mockMvc;

        @Autowired
        protected ObjectMapper objectMapper;

        @Autowired
        protected CommunityRepository communityRepository;

    }