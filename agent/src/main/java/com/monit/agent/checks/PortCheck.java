package com.monit.agent.checks;

import com.monit.common.CheckResult;
import com.monit.common.CheckStatus;

import java.net.InetSocketAddress;
import java.net.Socket;

public class PortCheck implements HealthCheck {

    private static final int TIMEOUT_MS = 1000;

    private final String name;
    private final int port;

    public PortCheck(String name, int port) {
        this.name = name;
        this.port = port;
    }

    @Override
    public CheckResult run() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", port), TIMEOUT_MS);
            return new CheckResult(name, "port", CheckStatus.OK, "port " + port + " is open");
        } catch (Exception e) {
            return new CheckResult(name, "port", CheckStatus.FAIL, "port " + port + " is not reachable: " + e.getMessage());
        }
    }
}
