package com.monit.server.mail;

import com.monit.server.entity.AlertRecipientEntity;
import com.monit.server.repository.AlertRecipientRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final AlertRecipientRepository alertRecipientRepository;

    public EmailService(JavaMailSender mailSender, AlertRecipientRepository alertRecipientRepository) {
        this.mailSender = mailSender;
        this.alertRecipientRepository = alertRecipientRepository;
    }

    public void sendAlert(UUID clientId, String hostname, String alertType, String newState) {
        for (AlertRecipientEntity recipient : alertRecipientRepository.findGlobalAndForClient(clientId)) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipient.getEmail());
            message.setSubject("[MonIT] " + hostname + " " + alertType + " -> " + newState);
            message.setText(hostname + " changed " + alertType + " state to " + newState + ".");
            mailSender.send(message);
        }
    }
}
