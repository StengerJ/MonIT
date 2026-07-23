package com.monit.agent.config;

import com.monit.agent.checks.DiskCheck;
import com.monit.agent.checks.HealthCheck;
import com.monit.agent.checks.HttpCheck;
import com.monit.agent.checks.OshiDiskUsageProvider;
import com.monit.agent.checks.OshiProcessLister;
import com.monit.agent.checks.PortCheck;
import com.monit.agent.checks.ProcessCheck;

import java.util.ArrayList;
import java.util.List;

public class ChecksFactory {

    private final OshiProcessLister processLister = new OshiProcessLister();
    private final OshiDiskUsageProvider diskUsageProvider = new OshiDiskUsageProvider();

    public List<HealthCheck> build(List<ChecksConfig.CheckDefinition> definitions) {
        List<HealthCheck> result = new ArrayList<>();
        for (ChecksConfig.CheckDefinition def : definitions) {
            result.add(buildOne(def));
        }
        return result;
    }

    private HealthCheck buildOne(ChecksConfig.CheckDefinition def) {
        return switch (def.getType()) {
            case "process" -> new ProcessCheck(def.getName(), def.getProcessName(), processLister);
            case "port" -> new PortCheck(def.getName(), def.getPort());
            case "http" -> new HttpCheck(def.getName(), def.getUrl(), def.getExpectStatus());
            case "disk" -> new DiskCheck(def.getName(), def.getPath(), def.getWarnPercent(), diskUsageProvider);
            default -> throw new IllegalArgumentException("Unknown check type: " + def.getType());
        };
    }
}
