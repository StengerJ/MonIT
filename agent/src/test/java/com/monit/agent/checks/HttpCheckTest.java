package com.monit.agent.checks;

import com.monit.common.CheckResult;
import com.monit.common.CheckStatus;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;

import static org.assertj.core.api.Assertions.assertThat;

class HttpCheckTest {

    private HttpServer server;
    private int port;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/health", exchange -> {
            byte[] body = "ok".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.createContext("/broken", exchange -> {
            exchange.sendResponseHeaders(500, -1);
            exchange.close();
        });
        server.start();
        port = server.getAddress().getPort();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void reportsOkWhenStatusMatches() {
        HttpCheck check = new HttpCheck("api-health", "http://localhost:" + port + "/health", 200);

        CheckResult result = check.run();

        assertThat(result.getStatus()).isEqualTo(CheckStatus.OK);
    }

    @Test
    void reportsFailWhenStatusDoesNotMatch() {
        HttpCheck check = new HttpCheck("api-health", "http://localhost:" + port + "/broken", 200);

        CheckResult result = check.run();

        assertThat(result.getStatus()).isEqualTo(CheckStatus.FAIL);
    }
}
