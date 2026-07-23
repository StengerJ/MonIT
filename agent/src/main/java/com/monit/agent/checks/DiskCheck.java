package com.monit.agent.checks;

import com.monit.common.CheckResult;
import com.monit.common.CheckStatus;

public class DiskCheck implements HealthCheck {

    private final String name;
    private final String path;
    private final double warnPercent;
    private final DiskUsageProvider provider;

    public DiskCheck(String name, String path, double warnPercent, DiskUsageProvider provider) {
        this.name = name;
        this.path = path;
        this.warnPercent = warnPercent;
        this.provider = provider;
    }

    @Override
    public CheckResult run() {
        double usedPercent = provider.usedPercent(path);
        CheckStatus status = usedPercent >= warnPercent ? CheckStatus.WARN : CheckStatus.OK;
        String message = String.format("%s used: %.1f%% (warn at %.1f%%)", path, usedPercent, warnPercent);
        return new CheckResult(name, "disk", status, message);
    }
}
