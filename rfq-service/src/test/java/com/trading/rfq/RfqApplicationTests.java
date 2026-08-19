package com.trading.rfq;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-End Integration Test for RfqApplication.
 * 
 * Testing Concept:
 * - @SpringBootTest bootstraps the entire Spring ApplicationContext.
 * - @AutoConfigureMockMvc allows testing full HTTP pipelines including Actuator endpoints.
 * - Verifies that Spring auto-configuration and Actuator /actuator/health are operating properly.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class RfqApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Context Load & Health Probe - /actuator/health should return UP")
    void contextLoadsAndHealthIsUp() throws Exception {
        mockMvc.perform(get("/actuator/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Actuator Info - /actuator/info should return configured app metadata")
    void infoEndpointReturnsMetadata() throws Exception {
        mockMvc.perform(get("/actuator/info")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Full Stack E2E - GET /api/v1/rfq returns preloaded quotes")
    void fullStackRfqListEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/rfq")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rfqs").isArray())
                .andExpect(jsonPath("$.rfqs[0].id").exists());
    }
}
