package com.monit.agent;

import com.monit.agent.checks.HealthCheck;
import com.monit.agent.config.ChecksConfig;
import com.monit.agent.config.ChecksFactory;
import com.monit.agent.identity.ApiKeyStore;
import com.monit.agent.identity.RegistrationClient;
import com.monit.agent.metrics.MetricsCollector;
import com.monit.agent.net.MtlsClientFactory;
import com.monit.agent.schedule.ReportScheduler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.List;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(AgentApplication.ChecksProperties.class)
public class AgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(
            @Value("${monit.agent.mtls.enabled:false}") boolean mtlsEnabled,
            @Value("${monit.agent.mtls.keystore:}") String keystorePath,
            @Value("${monit.agent.mtls.keystore-password:}") String keystorePassword,
            @Value("${monit.agent.mtls.truststore:}") String truststorePath,
            @Value("${monit.agent.mtls.truststore-password:}") String truststorePassword) {
        return MtlsClientFactory.build(mtlsEnabled, keystorePath, keystorePassword, truststorePath, truststorePassword);
    }

    @Bean
    public RegistrationClient registrationClient(RestTemplate restTemplate) {
        return new RegistrationClient(restTemplate);
    }

    @Bean
    public ApiKeyStore apiKeyStore(@Value("${monit.agent.key-file:agent.key}") String keyFile) {
        return new ApiKeyStore(Path.of(keyFile));
    }

    @Bean
    public MetricsCollector metricsCollector() {
        return new MetricsCollector();
    }

    @Bean
    public List<HealthCheck> healthChecks(ChecksProperties checksProperties) {
        return new ChecksFactory().build(checksProperties.getChecks());
    }

    @Bean
    public ReportScheduler reportScheduler(RestTemplate restTemplate,
                                            RegistrationClient registrationClient,
                                            ApiKeyStore apiKeyStore,
                                            MetricsCollector metricsCollector,
                                            List<HealthCheck> healthChecks,
                                            @Value("${monit.agent.server-base-url}") String serverBaseUrl,
                                            @Value("${monit.agent.bootstrap-secret}") String bootstrapSecret) throws UnknownHostException {
        String hostname = InetAddress.getLocalHost().getHostName();
        return new ReportScheduler(restTemplate, registrationClient, apiKeyStore, metricsCollector,
                healthChecks, serverBaseUrl, hostname, bootstrapSecret);
    }

    @ConfigurationProperties(prefix = "monit.agent")
    public static class ChecksProperties {
        private List<ChecksConfig.CheckDefinition> checks;

        public List<ChecksConfig.CheckDefinition> getChecks() {
            return checks;
        }

        public void setChecks(List<ChecksConfig.CheckDefinition> checks) {
            this.checks = checks;
        }
    }
}
