package com.monit.server.web;

import com.monit.server.AbstractIntegrationTest;
import com.monit.server.entity.ClientEntity;
import com.monit.server.repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@AutoConfigureMockMvc
class DashboardControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ClientRepository clientRepository;

    @Test
    void overviewPageRendersSuccessfully() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void clientDetailPageRendersForKnownClient() throws Exception {
        ClientEntity client = new ClientEntity();
        client.setId(UUID.randomUUID());
        client.setHostname("host-a");
        client.setApiKey("key-" + UUID.randomUUID());
        clientRepository.save(client);

        mockMvc.perform(get("/clients/" + client.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("client-detail"));
    }

    @Test
    void alertsSettingsPageRendersSuccessfully() throws Exception {
        mockMvc.perform(get("/settings/alerts"))
                .andExpect(status().isOk())
                .andExpect(view().name("alerts-settings"));
    }
}
