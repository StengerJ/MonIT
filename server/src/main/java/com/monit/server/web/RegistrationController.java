package com.monit.server.web;

import com.monit.common.RegisterRequest;
import com.monit.common.RegisterResponse;
import com.monit.server.entity.ClientEntity;
import com.monit.server.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
public class RegistrationController {

    private final ClientRepository clientRepository;
    private final String bootstrapSecret;

    public RegistrationController(ClientRepository clientRepository,
                                   @Value("${monit.bootstrap-secret}") String bootstrapSecret) {
        this.clientRepository = clientRepository;
        this.bootstrapSecret = bootstrapSecret;
    }

    @PostMapping("/api/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        if (!bootstrapSecret.equals(request.getBootstrapSecret())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ClientEntity client = new ClientEntity();
        client.setId(UUID.randomUUID());
        client.setHostname(request.getHostname());
        client.setApiKey(UUID.randomUUID().toString());
        client.setLastSeen(Instant.now());
        clientRepository.save(client);

        RegisterResponse response = new RegisterResponse(client.getId().toString(), client.getApiKey());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
