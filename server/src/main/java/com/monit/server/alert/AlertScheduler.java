package com.monit.server.alert;

import com.monit.server.entity.ClientEntity;
import com.monit.server.entity.ClientStatus;
import com.monit.server.entity.MetricEntity;
import com.monit.server.mail.EmailService;
import com.monit.server.repository.ClientRepository;
import com.monit.server.repository.MetricRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AlertScheduler {

    private static final double WARN_THRESHOLD_PERCENT = 90.0;

    private final ClientRepository clientRepository;
    private final MetricRepository metricRepository;
    private final AlertStateService alertStateService;
    private final EmailService emailService;

    public AlertScheduler(ClientRepository clientRepository,
                           MetricRepository metricRepository,
                           AlertStateService alertStateService,
                           EmailService emailService) {
        this.clientRepository = clientRepository;
        this.metricRepository = metricRepository;
        this.alertStateService = alertStateService;
        this.emailService = emailService;
    }

    @Scheduled(fixedDelayString = "${monit.server.alert-sweep-ms:15000}")
    public void sweep() {
        for (ClientEntity client : clientRepository.findAll()) {
            evaluateAvailability(client);
            evaluateThreshold(client);
        }
    }

    private void evaluateAvailability(ClientEntity client) {
        boolean offline = client.getLastSeen() == null
                || client.getLastSeen().isBefore(Instant.now().minusSeconds(2L * client.getExpectedIntervalSeconds()));

        String newState = offline ? "OFFLINE" : "ONLINE";
        boolean changed = alertStateService.recordAndCheckTransition(client.getId(), "availability", newState);

        client.setStatus(offline ? ClientStatus.OFFLINE : (client.getStatus() == ClientStatus.WARNING ? ClientStatus.WARNING : ClientStatus.ONLINE));
        clientRepository.save(client);

        if (changed) {
            emailService.sendAlert(client.getId(), client.getHostname(), "availability", newState);
        }
    }

    private void evaluateThreshold(ClientEntity client) {
        MetricEntity latest = metricRepository.findFirstByClientIdOrderByTimeDesc(client.getId());
        if (latest == null) {
            return;
        }

        boolean breached = latest.getCpuPercent() > WARN_THRESHOLD_PERCENT
                || latest.getMemPercent() > WARN_THRESHOLD_PERCENT
                || latest.getMaxDiskPercent() > WARN_THRESHOLD_PERCENT;

        String newState = breached ? "WARNING" : "ONLINE";
        boolean changed = alertStateService.recordAndCheckTransition(client.getId(), "threshold", newState);

        if (breached && client.getStatus() != ClientStatus.OFFLINE) {
            client.setStatus(ClientStatus.WARNING);
            clientRepository.save(client);
        } else if (!breached && client.getStatus() == ClientStatus.WARNING) {
            client.setStatus(ClientStatus.ONLINE);
            clientRepository.save(client);
        }

        if (changed) {
            emailService.sendAlert(client.getId(), client.getHostname(), "threshold", newState);
        }
    }
}
