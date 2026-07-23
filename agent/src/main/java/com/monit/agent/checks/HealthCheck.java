package com.monit.agent.checks;

import com.monit.common.CheckResult;

public interface HealthCheck {
    CheckResult run();
}
