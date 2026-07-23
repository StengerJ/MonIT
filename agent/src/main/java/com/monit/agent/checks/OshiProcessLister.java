package com.monit.agent.checks;

import oshi.SystemInfo;
import oshi.software.os.OSProcess;

import java.util.List;

public class OshiProcessLister implements ProcessLister {

    private final SystemInfo systemInfo = new SystemInfo();

    @Override
    public List<String> runningProcessNames() {
        return systemInfo.getOperatingSystem()
                .getProcesses()
                .stream()
                .map(OSProcess::getName)
                .toList();
    }
}
