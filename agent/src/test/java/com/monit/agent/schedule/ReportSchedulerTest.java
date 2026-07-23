package com.monit.agent.schedule;

import com.monit.agent.checks.HealthCheck;
import com.monit.agent.identity.ApiKeyStore;
import com.monit.agent.identity.RegistrationClient;
import com.monit.agent.metrics.MetricsCollector;
import com.monit.agent.metrics.SystemMetrics;
import com.monit.common.CheckResult;
import com.monit.common.CheckStatus;
import com.monit.common.DiskUsage;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ReportSchedulerTest {

    @Test
    void registersOnFirstRunThenPushesReportWithApiKeyHeader() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);

        server.expect(requestTo("http://server:8081/api/register"))
                .andExpect(method(POST))
                .andRespond(withSuccess("{\"clientId\":\"client-1\",\"apiKey\":\"api-key-123\"}", MediaType.APPLICATION_JSON));

        server.expect(requestTo("http://server:8081/api/reports"))
                .andExpect(method(POST))
                .andExpect(header("X-API-Key", "api-key-123"))
                .andExpect(jsonPath("$.clientId").value("client-1"))
                .andExpect(jsonPath("$.cpuPercent").value(10.0))
                .andRespond(withSuccess());

        ApiKeyStore apiKeyStore = mock(ApiKeyStore.class);
        when(apiKeyStore.load()).thenReturn(Optional.empty());

        RegistrationClient registrationClient = new RegistrationClient(restTemplate);

        MetricsCollector metricsCollector = mock(MetricsCollector.class);
        when(metricsCollector.collect()).thenReturn(
                new SystemMetrics(10.0, 20.0, 100L, 1000L, 2000L, List.of(new DiskUsage("/data", 30.0))));

        HealthCheck check = mock(HealthCheck.class);
        when(check.run()).thenReturn(new CheckResult("nginx-running", "process", CheckStatus.OK, "running"));

        ReportScheduler scheduler = new ReportScheduler(
                restTemplate,
                registrationClient,
                apiKeyStore,
                metricsCollector,
                List.of(check),
                "http://server:8081",
                "host-a",
                "shared-secret");

        scheduler.pushReport();

        server.verify();
    }
}
