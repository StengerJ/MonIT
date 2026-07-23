package com.monit.agent.checks;

public interface DiskUsageProvider {
    double usedPercent(String path);
}
