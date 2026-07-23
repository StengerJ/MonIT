package com.monit.agent.checks;

import com.monit.common.CheckResult;
import com.monit.common.CheckStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessCheckTest {

    @Test
    void reportsOkWhenProcessIsRunning() {
        ProcessLister lister = () -> List.of("nginx", "java", "bash");
        ProcessCheck check = new ProcessCheck("nginx-running", "nginx", lister);

        CheckResult result = check.run();

        assertThat(result.getName()).isEqualTo("nginx-running");
        assertThat(result.getType()).isEqualTo("process");
        assertThat(result.getStatus()).isEqualTo(CheckStatus.OK);
    }

    @Test
    void reportsFailWhenProcessIsMissing() {
        ProcessLister lister = () -> List.of("java", "bash");
        ProcessCheck check = new ProcessCheck("nginx-running", "nginx", lister);

        CheckResult result = check.run();

        assertThat(result.getStatus()).isEqualTo(CheckStatus.FAIL);
        assertThat(result.getMessage()).contains("nginx");
    }
}
