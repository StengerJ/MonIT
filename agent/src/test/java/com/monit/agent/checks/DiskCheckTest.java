package com.monit.agent.checks;

import com.monit.common.CheckResult;
import com.monit.common.CheckStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DiskCheckTest {

    @Test
    void reportsOkWhenBelowThreshold() {
        DiskUsageProvider provider = path -> 50.0;
        DiskCheck check = new DiskCheck("data-volume", "/data", 85.0, provider);

        CheckResult result = check.run();

        assertThat(result.getStatus()).isEqualTo(CheckStatus.OK);
    }

    @Test
    void reportsWarnWhenAtOrAboveThreshold() {
        DiskUsageProvider provider = path -> 90.0;
        DiskCheck check = new DiskCheck("data-volume", "/data", 85.0, provider);

        CheckResult result = check.run();

        assertThat(result.getStatus()).isEqualTo(CheckStatus.WARN);
        assertThat(result.getMessage()).contains("90.0");
    }
}
