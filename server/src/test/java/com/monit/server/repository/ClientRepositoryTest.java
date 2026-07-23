package com.monit.server.repository;

import com.monit.server.AbstractIntegrationTest;
import com.monit.server.entity.ClientEntity;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ClientRepositoryTest extends AbstractIntegrationTest {

    @org.springframework.beans.factory.annotation.Autowired
    private ClientRepository clientRepository;

    @Test
    void savesAndFindsClientByApiKey() {
        ClientEntity client = new ClientEntity();
        client.setId(UUID.randomUUID());
        client.setHostname("host-a");
        client.setApiKey("api-key-123");

        clientRepository.save(client);

        Optional<ClientEntity> found = clientRepository.findByApiKey("api-key-123");

        assertThat(found).isPresent();
        assertThat(found.get().getHostname()).isEqualTo("host-a");
    }
}
