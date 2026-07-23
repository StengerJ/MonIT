package com.monit.server.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monit.common.RegisterRequest;
import com.monit.server.AbstractIntegrationTest;
import com.monit.server.repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class RegistrationControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClientRepository clientRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void registersNewClientWithValidBootstrapSecret() throws Exception {
        RegisterRequest request = new RegisterRequest("host-a", "change-me-shared-secret");

        mockMvc.perform(post("/api/register")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientId").exists())
                .andExpect(jsonPath("$.apiKey").exists());

        assertThat(clientRepository.count()).isEqualTo(1);
    }

    @Test
    void rejectsInvalidBootstrapSecret() throws Exception {
        RegisterRequest request = new RegisterRequest("host-a", "wrong-secret");

        mockMvc.perform(post("/api/register")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
