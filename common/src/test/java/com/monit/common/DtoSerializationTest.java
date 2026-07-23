package com.monit.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DtoSerializationTest {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void healthReportRoundTripsThroughJson() throws Exception {
        HealthReport report = HealthReport.builder()
                .clientId("client-1")
                .timestamp(Instant.parse("2026-07-22T10:00:00Z"))
                .cpuPercent(12.5)
                .memPercent(40.0)
                .uptimeSeconds(3600L)
                .netInBytes(1000L)
                .netOutBytes(2000L)
                .disks(List.of(new DiskUsage("/data", 55.5)))
                .checks(List.of(new CheckResult("nginx-running", "process", CheckStatus.OK, "running")))
                .build();

        String json = mapper.writeValueAsString(report);
        HealthReport parsed = mapper.readValue(json, HealthReport.class);

        assertThat(parsed).isEqualTo(report);
    }

    @Test
    void registerRequestAndResponseRoundTrip() throws Exception {
        RegisterRequest request = new RegisterRequest("host-a", "secret");
        String requestJson = mapper.writeValueAsString(request);
        assertThat(mapper.readValue(requestJson, RegisterRequest.class)).isEqualTo(request);

        RegisterResponse response = new RegisterResponse("client-1", "api-key-123");
        String responseJson = mapper.writeValueAsString(response);
        assertThat(mapper.readValue(responseJson, RegisterResponse.class)).isEqualTo(response);
    }
}
