package com.monit.server.alert;

import com.monit.server.entity.ClientEntity;
import com.monit.server.entity.MetricEntity;
import com.monit.server.mail.EmailService;
import com.monit.server.repository.ClientRepository;
import com.monit.server.repository.MetricRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlertSchedulerTest {

    @Test
    void marksClientOfflineWhenLastSeenExceedsTwiceExpectedInterval() {
        ClientRepository clientRepository = mock(ClientRepository.class);
        MetricRepository metricRepository = mock(MetricRepository.class);
        AlertStateService alertStateService = mock(AlertStateService.class);
        EmailService emailService = mock(EmailService.class);

        ClientEntity client = new ClientEntity();
        client.setId(UUID.randomUUID());
        client.setHostname("host-a");
        client.setExpectedIntervalSeconds(30);
        client.setLastSeen(Instant.now().minus(90, ChronoUnit.SECONDS));

        when(clientRepository.findAll()).thenReturn(List.of(client));
        when(metricRepository.findFirstByClientIdOrderByTimeDesc(client.getId())).thenReturn(null);
        when(alertStateService.recordAndCheckTransition(client.getId(), "availability", "OFFLINE")).thenReturn(true);

        AlertScheduler scheduler = new AlertScheduler(clientRepository, metricRepository, alertStateService, emailService);

        scheduler.sweep();

        verify(emailService).sendAlert(client.getId(), "host-a", "availability", "OFFLINE");
    }

    @Test
    void marksClientWarningWhenMetricsBreachThreshold() {
        ClientRepository clientRepository = mock(ClientRepository.class);
        MetricRepository metricRepository = mock(MetricRepository.class);
        AlertStateService alertStateService = mock(AlertStateService.class);
        EmailService emailService = mock(EmailService.class);

        ClientEntity client = new ClientEntity();
        client.setId(UUID.randomUUID());
        client.setHostname("host-a");
        client.setExpectedIntervalSeconds(30);
        client.setLastSeen(Instant.now());

        MetricEntity metric = new MetricEntity();
        metric.setCpuPercent(95.0);
        metric.setMemPercent(50.0);
        metric.setMaxDiskPercent(50.0);

        when(clientRepository.findAll()).thenReturn(List.of(client));
        when(metricRepository.findFirstByClientIdOrderByTimeDesc(client.getId())).thenReturn(metric);
        when(alertStateService.recordAndCheckTransition(client.getId(), "availability", "ONLINE")).thenReturn(false);
        when(alertStateService.recordAndCheckTransition(client.getId(), "threshold", "WARNING")).thenReturn(true);

        AlertScheduler scheduler = new AlertScheduler(clientRepository, metricRepository, alertStateService, emailService);

        scheduler.sweep();

        verify(emailService).sendAlert(client.getId(), "host-a", "threshold", "WARNING");
        verify(emailService, never()).sendAlert(any(), anyString(), anyString(), org.mockito.ArgumentMatchers.eq("OFFLINE"));
    }
}
