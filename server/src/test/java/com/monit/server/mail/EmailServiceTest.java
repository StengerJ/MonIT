package com.monit.server.mail;

import com.monit.server.entity.AlertRecipientEntity;
import com.monit.server.repository.AlertRecipientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailServiceTest {

    @Test
    void sendsOneEmailPerRecipient() {
        AlertRecipientRepository recipientRepository = mock(AlertRecipientRepository.class);
        UUID clientId = UUID.randomUUID();
        AlertRecipientEntity a = new AlertRecipientEntity();
        a.setId(UUID.randomUUID());
        a.setEmail("admin1@example.com");
        AlertRecipientEntity b = new AlertRecipientEntity();
        b.setId(UUID.randomUUID());
        b.setEmail("admin2@example.com");
        when(recipientRepository.findGlobalAndForClient(clientId)).thenReturn(List.of(a, b));

        JavaMailSender mailSender = mock(JavaMailSender.class);

        EmailService service = new EmailService(mailSender, recipientRepository);

        service.sendAlert(clientId, "host-a", "availability", "OFFLINE");

        verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendsNothingWhenNoRecipientsConfigured() {
        AlertRecipientRepository recipientRepository = mock(AlertRecipientRepository.class);
        UUID clientId = UUID.randomUUID();
        when(recipientRepository.findGlobalAndForClient(clientId)).thenReturn(List.of());
        JavaMailSender mailSender = mock(JavaMailSender.class);

        EmailService service = new EmailService(mailSender, recipientRepository);

        service.sendAlert(clientId, "host-a", "availability", "OFFLINE");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void perClientRecipientOnlyGetsAlertsForItsOwnClient() {
        AlertRecipientRepository recipientRepository = mock(AlertRecipientRepository.class);
        UUID clientId = UUID.randomUUID();
        UUID otherClientId = UUID.randomUUID();
        AlertRecipientEntity scoped = new AlertRecipientEntity();
        scoped.setId(UUID.randomUUID());
        scoped.setEmail("owner@example.com");
        scoped.setClientId(clientId);
        when(recipientRepository.findGlobalAndForClient(clientId)).thenReturn(List.of(scoped));
        when(recipientRepository.findGlobalAndForClient(otherClientId)).thenReturn(List.of());

        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailService service = new EmailService(mailSender, recipientRepository);

        service.sendAlert(otherClientId, "host-b", "availability", "OFFLINE");
        verify(mailSender, never()).send(any(SimpleMailMessage.class));

        service.sendAlert(clientId, "host-a", "availability", "OFFLINE");
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
