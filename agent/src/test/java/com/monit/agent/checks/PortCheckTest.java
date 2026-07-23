package com.monit.agent.checks;

import com.monit.common.CheckResult;
import com.monit.common.CheckStatus;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;

import static org.assertj.core.api.Assertions.assertThat;

class PortCheckTest {

    @Test
    void reportsOkWhenPortIsOpen() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            int port = socket.getLocalPort();
            PortCheck check = new PortCheck("app-port", port);

            CheckResult result = check.run();

            assertThat(result.getStatus()).isEqualTo(CheckStatus.OK);
        }
    }

    @Test
    void reportsFailWhenPortIsClosed() throws IOException {
        int closedPort;
        try (ServerSocket socket = new ServerSocket(0)) {
            closedPort = socket.getLocalPort();
        }

        PortCheck check = new PortCheck("app-port", closedPort);

        CheckResult result = check.run();

        assertThat(result.getStatus()).isEqualTo(CheckStatus.FAIL);
    }
}
