package com.monit.agent.checks;

import com.monit.common.CheckResult;
import com.monit.common.CheckStatus;

public class ProcessCheck implements HealthCheck {

    private final String name;
    private final String processName;
    private final ProcessLister lister;

    public ProcessCheck(String name, String processName, ProcessLister lister) {
        this.name = name;
        this.processName = processName;
        this.lister = lister;
    }

    @Override
    public CheckResult run() {
        boolean running = lister.runningProcessNames().stream()
                .anyMatch(p -> p.equalsIgnoreCase(processName));
        CheckStatus status = running ? CheckStatus.OK : CheckStatus.FAIL;
        String message = running
                ? "process '" + processName + "' is running"
                : "process '" + processName + "' is not running";
        return new CheckResult(name, "process", status, message);
    }
}
