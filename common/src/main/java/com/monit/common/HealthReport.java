package com.monit.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthReport {
    private String clientId;
    private Instant timestamp;
    private double cpuPercent;
    private double memPercent;
    private long uptimeSeconds;
    private long netInBytes;
    private long netOutBytes;
    private List<DiskUsage> disks;
    private List<CheckResult> checks;
}
