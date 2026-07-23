package com.monit.agent.identity;

import com.monit.common.RegisterResponse;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class ApiKeyStore {

    private final Path keyFilePath;

    public ApiKeyStore(Path keyFilePath) {
        this.keyFilePath = keyFilePath;
    }

    public Optional<RegisterResponse> load() {
        if (!Files.exists(keyFilePath)) {
            return Optional.empty();
        }
        try {
            List<String> lines = Files.readAllLines(keyFilePath);
            String[] parts = lines.get(0).split(":", 2);
            return Optional.of(new RegisterResponse(parts[0], parts[1]));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void save(RegisterResponse response) {
        try {
            if (keyFilePath.getParent() != null) {
                Files.createDirectories(keyFilePath.getParent());
            }
            Files.writeString(keyFilePath, response.getClientId() + ":" + response.getApiKey());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
