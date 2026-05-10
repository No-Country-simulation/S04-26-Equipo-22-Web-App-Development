package com.nocountry.webapp.integration.controller;

import com.nocountry.webapp.dto.CommunityRequestDTO;
import com.nocountry.webapp.entity.Community;
import com.nocountry.webapp.integration.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;


@DisplayName("Integration Tests for CommunityController")
public class CommunityControllerIntegrationTest extends BaseIntegrationTest {

    private CommunityRequestDTO validRequest;
    private CommunityRequestDTO updateRequest;

    @BeforeEach
    protected void setUp() {
        super.setUp();
        
        // Configurar request válido para pruebas
        validRequest = new CommunityRequestDTO();
        validRequest.setName("Java Developers Argentina");
        validRequest.setPlatform("Discord");
        validRequest.setActive(true);

        // Configurar request para actualización
        updateRequest = new CommunityRequestDTO();
        updateRequest.setName("Java Developers Latam");
        updateRequest.setPlatform("Slack");
        updateRequest.setActive(true);
    }

    // Método auxiliar para crear una comunidad y devolver su ID
    private Long createTestCommunity() throws Exception {
        String response = mockMvc.perform(post("/api/communities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

    // Cada bloque @Nested corresponde a un endpoint específico
    @Nested
    @DisplayName("POST /api/communities - Create Community")
    class CreateCommunityTests {

        @Test
        @DisplayName("Should create community successfully when data is valid")
        void createCommunity_WithValidData_ShouldReturn201() throws Exception {
            mockMvc.perform(post("/api/communities")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.name").value("Java Developers Argentina"))
                    .andExpect(jsonPath("$.platform").value("Discord"))
                    .andExpect(jsonPath("$.active").value(true));

            // Verify in database
            List<Community> communities = communityRepository.findAll();
            assertThat(communities).hasSize(1);
            assertThat(communities.get(0).getName()).isEqualTo("Java Developers Argentina");
        }

        @Test
        @DisplayName("Should return 400 when name is empty")
        void createCommunity_WithEmptyName_ShouldReturn400() throws Exception {
            validRequest.setName("");
            
            mockMvc.perform(post("/api/communities")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when name is null")
        void createCommunity_WithNullName_ShouldReturn400() throws Exception {
            validRequest.setName(null);
            
            mockMvc.perform(post("/api/communities")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 409 when community name already exists")
        void createCommunity_WithDuplicateName_ShouldReturn409() throws Exception {
            // Create first community
            mockMvc.perform(post("/api/communities")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated());

            // Try to create duplicate
            mockMvc.perform(post("/api/communities")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value(containsString("Ya existe una comunidad")));
        }

        @Test
        @DisplayName("Should trim whitespace from name")
        void createCommunity_ShouldTrimName() throws Exception {
            validRequest.setName("  JavaScript Masters  ");
            
            mockMvc.perform(post("/api/communities")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("JavaScript Masters"));
        }
    }

    @Nested
    @DisplayName("GET /api/communities - Get All Communities")
    class GetAllCommunitiesTests {

        @Test
        @DisplayName("Should return all communities (including inactive)")
        void getAllCommunities_ShouldReturnAllCommunities() throws Exception {
            // Create active community
            createTestCommunity();
            
            // Create inactive community
            CommunityRequestDTO inactiveRequest = new CommunityRequestDTO();
            inactiveRequest.setName("Inactive Community");
            inactiveRequest.setPlatform("Telegram");
            inactiveRequest.setActive(false);
            
            mockMvc.perform(post("/api/communities")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inactiveRequest)))
                    .andExpect(status().isCreated());

            // Get all communities
            mockMvc.perform(get("/api/communities"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].name").exists())
                    .andExpect(jsonPath("$[1].name").exists());
        }

        @Test
        @DisplayName("Should return empty list when no communities exist")
        void getAllCommunities_WhenNoCommunities_ShouldReturnEmptyList() throws Exception {
            mockMvc.perform(get("/api/communities"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/communities/active - Get Active Communities")
    class GetActiveCommunitiesTests {

        @Test
        @DisplayName("Should return only active communities")
        void getActiveCommunities_ShouldReturnOnlyActive() throws Exception {
            // Create active community
            createTestCommunity();
            
            // Create inactive community
            CommunityRequestDTO inactiveRequest = new CommunityRequestDTO();
            inactiveRequest.setName("Inactive Community");
            inactiveRequest.setPlatform("Telegram");
            inactiveRequest.setActive(false);
            
            mockMvc.perform(post("/api/communities")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inactiveRequest)))
                    .andExpect(status().isCreated());

            // Get only active
            mockMvc.perform(get("/api/communities/active"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].name").value("Java Developers Argentina"))
                    .andExpect(jsonPath("$[0].active").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/communities/{id} - Get Community By ID")
    class GetCommunityByIdTests {

        @Test
        @DisplayName("Should return community when exists")
        void getCommunityById_WhenExists_ShouldReturn200() throws Exception {
            Long id = createTestCommunity();
            
            mockMvc.perform(get("/api/communities/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.name").value("Java Developers Argentina"))
                    .andExpect(jsonPath("$.platform").value("Discord"));
        }

        @Test
        @DisplayName("Should return 404 when community not found")
        void getCommunityById_WhenNotFound_ShouldReturn404() throws Exception {
            mockMvc.perform(get("/api/communities/9999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("no encontrada")));
        }
    }

    @Nested
    @DisplayName("PUT /api/communities/{id} - Update Community")
    class UpdateCommunityTests {

        @Test
        @DisplayName("Should update community successfully")
        void updateCommunity_WithValidData_ShouldReturn200() throws Exception {
            Long id = createTestCommunity();
            
            mockMvc.perform(put("/api/communities/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Java Developers Latam"))
                    .andExpect(jsonPath("$.platform").value("Slack"))
                    .andExpect(jsonPath("$.active").value(true));

            // Verify in database
            Community updated = communityRepository.findById(id).orElseThrow();
            assertThat(updated.getName()).isEqualTo("Java Developers Latam");
            assertThat(updated.getPlatform()).isEqualTo("Slack");
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent community")
        void updateCommunity_WhenNotFound_ShouldReturn404() throws Exception {
            mockMvc.perform(put("/api/communities/9999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 409 when updating to duplicate name")
        void updateCommunity_WithDuplicateName_ShouldReturn409() throws Exception {
            // Create first community
            CommunityRequestDTO firstRequest = new CommunityRequestDTO();
            firstRequest.setName("First Community");
            firstRequest.setPlatform("Discord");
            firstRequest.setActive(true);

            mockMvc.perform(post("/api/communities")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(firstRequest)))
                    .andExpect(status().isCreated());
            
            // Create second community
            CommunityRequestDTO secondRequest = new CommunityRequestDTO();
            secondRequest.setName("Second Community");
            secondRequest.setPlatform("Discord");
            secondRequest.setActive(true);
            
            String response = mockMvc.perform(post("/api/communities")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(secondRequest)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            
            Long id2 = objectMapper.readTree(response).get("id").asLong();
            
            // Try to update second community with first community's name
            CommunityRequestDTO duplicateRequest = new CommunityRequestDTO();
            duplicateRequest.setName("First Community");
            duplicateRequest.setPlatform("Slack");
            duplicateRequest.setActive(true);
            
            mockMvc.perform(put("/api/communities/{id}", id2)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(duplicateRequest)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("PATCH /api/communities/{id}/activate - Activate Community")
    class ActivateCommunityTests {

        @Test
        @DisplayName("Should activate inactive community")
        void activateCommunity_WhenInactive_ShouldActivate() throws Exception {
            // Create inactive community
            CommunityRequestDTO inactiveRequest = new CommunityRequestDTO();
            inactiveRequest.setName("Inactive Community");
            inactiveRequest.setPlatform("Telegram");
            inactiveRequest.setActive(false);
            
            String response = mockMvc.perform(post("/api/communities")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inactiveRequest)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            
            Long id = objectMapper.readTree(response).get("id").asLong();
            
            // Activate
            mockMvc.perform(patch("/api/communities/{id}/activate", id))
                    .andExpect(status().isNoContent());

            // Verify
            Community activated = communityRepository.findById(id).orElseThrow();
            assertThat(activated.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should return 404 when activating non-existent community")
        void activateCommunity_WhenNotFound_ShouldReturn404() throws Exception {
            mockMvc.perform(patch("/api/communities/9999/activate"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /api/communities/{id}/deactivate - Deactivate Community")
    class DeactivateCommunityTests {

        @Test
        @DisplayName("Should deactivate active community")
        void deactivateCommunity_WhenActive_ShouldDeactivate() throws Exception {
            Long id = createTestCommunity();
            
            // Deactivate
            mockMvc.perform(patch("/api/communities/{id}/deactivate", id))
                    .andExpect(status().isNoContent());

            // Verify
            Community deactivated = communityRepository.findById(id).orElseThrow();
            assertThat(deactivated.isActive()).isFalse();
        }

        @Test
        @DisplayName("Should return 404 when deactivating non-existent community")
        void deactivateCommunity_WhenNotFound_ShouldReturn404() throws Exception {
            mockMvc.perform(patch("/api/communities/9999/deactivate"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/communities/{id} - Delete Community (Permanent)")
    class DeleteCommunityTests {

        @Test
        @DisplayName("Should permanently delete community")
        void deleteCommunity_WhenExists_ShouldReturn204() throws Exception {
            Long id = createTestCommunity();
            
            // Delete
            mockMvc.perform(delete("/api/communities/{id}", id))
                    .andExpect(status().isNoContent());

            // Verify deletion
            assertThat(communityRepository.findById(id)).isEmpty();
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent community")
        void deleteCommunity_WhenNotFound_ShouldReturn404() throws Exception {
            mockMvc.perform(delete("/api/communities/9999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Integration Scenarios")
    class IntegrationScenarios {

        @Test
        @DisplayName("Complete workflow: Create -> Get -> Update -> Deactivate -> Activate -> Delete")
        void completeCommunityWorkflow() throws Exception {
            // 1. Create
            String createResponse = mockMvc.perform(post("/api/communities")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            
            Long id = objectMapper.readTree(createResponse).get("id").asLong();
            
            // 2. Get
            mockMvc.perform(get("/api/communities/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Java Developers Argentina"));
            
            // 3. Update
            updateRequest.setName("Updated Community Name");
            mockMvc.perform(put("/api/communities/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated Community Name"));
            
            // 4. Deactivate
            mockMvc.perform(patch("/api/communities/{id}/deactivate", id))
                    .andExpect(status().isNoContent());
            
            // 5. Verify it's inactive in list (should not appear in active list)
            mockMvc.perform(get("/api/communities/active"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
            
            // 6. Activate
            mockMvc.perform(patch("/api/communities/{id}/activate", id))
                    .andExpect(status().isNoContent());
            
            // 7. Verify it's active again
            mockMvc.perform(get("/api/communities/active"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
            
            // 8. Delete
            mockMvc.perform(delete("/api/communities/{id}", id))
                    .andExpect(status().isNoContent());
            
            // 9. Verify deletion
            mockMvc.perform(get("/api/communities/{id}", id))
                    .andExpect(status().isNotFound());
        }
    }
}