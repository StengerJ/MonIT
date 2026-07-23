package com.monit.agent.config;

import com.monit.agent.checks.DiskCheck;
import com.monit.agent.checks.HealthCheck;
import com.monit.agent.checks.HttpCheck;
import com.monit.agent.checks.PortCheck;
import com.monit.agent.checks.ProcessCheck;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChecksFactoryTest {

    @Test
    void buildsOneHealthCheckPerDefinition() {
        ChecksConfig.CheckDefinition process = new ChecksConfig.CheckDefinition();
        process.setType("process");
        process.setName("nginx-running");
        process.setProcessName("nginx");

        ChecksConfig.CheckDefinition port = new ChecksConfig.CheckDefinition();
        port.setType("port");
        port.setName("app-port");
        port.setPort(8080);

        ChecksConfig.CheckDefinition http = new ChecksConfig.CheckDefinition();
        http.setType("http");
        http.setName("api-health");
        http.setUrl("http://localhost:8080/health");
        http.setExpectStatus(200);

        ChecksConfig.CheckDefinition disk = new ChecksConfig.CheckDefinition();
        disk.setType("disk");
        disk.setName("data-volume");
        disk.setPath("/data");
        disk.setWarnPercent(85.0);

        List<HealthCheck> checks = new ChecksFactory().build(List.of(process, port, http, disk));

        assertThat(checks).hasSize(4);
        assertThat(checks.get(0)).isInstanceOf(ProcessCheck.class);
        assertThat(checks.get(1)).isInstanceOf(PortCheck.class);
        assertThat(checks.get(2)).isInstanceOf(HttpCheck.class);
        assertThat(checks.get(3)).isInstanceOf(DiskCheck.class);
    }

    @Test
    void rejectsUnknownCheckType() {
        ChecksConfig.CheckDefinition unknown = new ChecksConfig.CheckDefinition();
        unknown.setType("bogus");
        unknown.setName("whatever");

        List<ChecksConfig.CheckDefinition> defs = List.of(unknown);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> new ChecksFactory().build(defs));
    }
}
