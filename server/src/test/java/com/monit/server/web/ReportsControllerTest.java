package com.monit.server.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.monit.common.CheckResult;
import com.monit.common.CheckStatus;
import com.monit.common.DiskUsage;
import com.monit.common.HealthReport;
import com.monit.server.AbstractIntegrationTest;
import com.monit.server.entity.ClientEntity;
import com.monit.server.repository.CheckResultRepository;
import com.monit.server.repository.ClientRepository;
import com.monit.server.repository.MetricRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class ReportsControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private MetricRepository metricRepository;
    @Autowired
    private CheckResultRepository checkResultRepository;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void acceptsReportAndPersistsMetricsAndChecks() throws Exception {
        ClientEntity client = new ClientEntity();
        client.setId(UUID.randomUUID());
        client.setHostname("host-a");
        client.setApiKey("valid-key");
        clientRepository.save(client);

        HealthReport report = HealthReport.builder()
                .clientId(client.getId().toString())
                .timestamp(Instant.now())
                .cpuPercent(10.0)
                .memPercent(20.0)
                .uptimeSeconds(100L)
                .netInBytes(1000L)
                .netOutBytes(2000L)
                .disks(List.of(new DiskUsage("/data", 30.0)))
                .checks(List.of(new CheckResult("nginx-running", "process", CheckStatus.OK, "running")))
                .build();

        mockMvc.perform(post("/api/reports")
                        .header("X-API-Key", "valid-key")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(report)))
                .andExpect(status().isAccepted());

        assertThat(metricRepository.findRecentByClientId(client.getId())).hasSize(1);
        assertThat(checkResultRepository.findTop20ByClientIdOrderByTimeDesc(client.getId())).hasSize(1);
        assertThat(clientRepository.findById(client.getId()).get().getLastSeen()).isNotNull();
    }

    @Test
    void rejectsReportWithoutValidApiKey() throws Exception {
        HealthReport report = HealthReport.builder()
                .clientId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .cpuPercent(10.0)
                .memPercent(20.0)
                .disks(List.of())
                .checks(List.of())
                .build();

        mockMvc.perform(post("/api/reports")
                        .header("X-API-Key", "bad-key")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(report)))
                .andExpect(status().isUnauthorized());
    }
}
