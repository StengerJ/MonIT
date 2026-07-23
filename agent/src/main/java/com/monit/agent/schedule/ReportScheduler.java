package com.monit.agent.schedule;

import com.monit.agent.checks.HealthCheck;
import com.monit.agent.identity.ApiKeyStore;
import com.monit.agent.identity.RegistrationClient;
import com.monit.agent.metrics.MetricsCollector;
import com.monit.agent.metrics.SystemMetrics;
import com.monit.common.CheckResult;
import com.monit.common.HealthReport;
import com.monit.common.RegisterResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class ReportScheduler {

    private final RestTemplate restTemplate;
    private final RegistrationClient registrationClient;
    private final ApiKeyStore apiKeyStore;
    private final MetricsCollector metricsCollector;
    private final List<HealthCheck> checks;
    private final String serverBaseUrl;
    private final String hostname;
    private final String bootstrapSecret;

    private RegisterResponse identity;

    public ReportScheduler(RestTemplate restTemplate,
                            RegistrationClient registrationClient,
                            ApiKeyStore apiKeyStore,
                            MetricsCollector metricsCollector,
                            List<HealthCheck> checks,
                            String serverBaseUrl,
                            String hostname,
                            String bootstrapSecret) {
        this.restTemplate = restTemplate;
        this.registrationClient = registrationClient;
        this.apiKeyStore = apiKeyStore;
        this.metricsCollector = metricsCollector;
        this.checks = checks;
        this.serverBaseUrl = serverBaseUrl;
        this.hostname = hostname;
        this.bootstrapSecret = bootstrapSecret;
    }

    @Scheduled(fixedDelayString = "${monit.agent.push-interval-ms:30000}")
    public void pushReport() {
        RegisterResponse currentIdentity = ensureRegistered();

        SystemMetrics metrics = metricsCollector.collect();
        List<CheckResult> checkResults = checks.stream().map(HealthCheck::run).toList();

        HealthReport report = HealthReport.builder()
                .clientId(currentIdentity.getClientId())
                .timestamp(Instant.now())
                .cpuPercent(metrics.getCpuPercent())
                .memPercent(metrics.getMemPercent())
                .uptimeSeconds(metrics.getUptimeSeconds())
                .netInBytes(metrics.getNetInBytes())
                .netOutBytes(metrics.getNetOutBytes())
                .disks(metrics.getDisks())
                .checks(checkResults)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", currentIdentity.getApiKey());
        restTemplate.postForEntity(serverBaseUrl + "/api/reports", new HttpEntity<>(report, headers), Void.class);
    }

    private RegisterResponse ensureRegistered() {
        if (identity != null) {
            return identity;
        }
        Optional<RegisterResponse> stored = apiKeyStore.load();
        if (stored.isPresent()) {
            identity = stored.get();
            return identity;
        }
        RegisterResponse registered = registrationClient.register(serverBaseUrl, hostname, bootstrapSecret);
        apiKeyStore.save(registered);
        identity = registered;
        return identity;
    }
}
