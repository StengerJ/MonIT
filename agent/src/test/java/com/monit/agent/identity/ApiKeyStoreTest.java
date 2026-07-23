package com.monit.agent.identity;

import com.monit.common.RegisterResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ApiKeyStoreTest {

    @Test
    void loadReturnsEmptyWhenFileDoesNotExist(@TempDir Path tempDir) {
        ApiKeyStore store = new ApiKeyStore(tempDir.resolve("agent.key"));

        Optional<RegisterResponse> loaded = store.load();

        assertThat(loaded).isEmpty();
    }

    @Test
    void savedIdentityCanBeLoadedBack(@TempDir Path tempDir) {
        ApiKeyStore store = new ApiKeyStore(tempDir.resolve("agent.key"));
        RegisterResponse response = new RegisterResponse("client-1", "api-key-123");

        store.save(response);
        Optional<RegisterResponse> loaded = store.load();

        assertThat(loaded).contains(response);
    }
}
