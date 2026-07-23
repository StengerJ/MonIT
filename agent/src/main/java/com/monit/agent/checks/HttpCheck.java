package com.monit.agent.checks;

import com.monit.common.CheckResult;
import com.monit.common.CheckStatus;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HttpCheck implements HealthCheck {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    private final String name;
    private final String url;
    private final int expectStatus;

    public HttpCheck(String name, String url, int expectStatus) {
        this.name = name;
        this.url = url;
        this.expectStatus = expectStatus;
    }

    @Override
    public CheckResult run() {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();
            HttpResponse<Void> response = CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() == expectStatus) {
                return new CheckResult(name, "http", CheckStatus.OK,
                        "got expected status " + expectStatus);
            }
            return new CheckResult(name, "http", CheckStatus.FAIL,
                    "expected status " + expectStatus + " but got " + response.statusCode());
        } catch (Exception e) {
            return new CheckResult(name, "http", CheckStatus.FAIL, "request failed: " + e.getMessage());
        }
    }
}
