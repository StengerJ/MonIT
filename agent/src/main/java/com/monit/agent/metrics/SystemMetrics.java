package com.monit.agent.metrics;

import com.monit.common.DiskUsage;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SystemMetrics {
    private double cpuPercent;
    private double memPercent;
    private long uptimeSeconds;
    private long netInBytes;
    private long netOutBytes;
    private List<DiskUsage> disks;
}
