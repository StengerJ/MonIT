package com.monit.agent.metrics;

import com.monit.common.DiskUsage;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import java.util.List;

public class MetricsCollector {

    private final SystemInfo systemInfo = new SystemInfo();
    private long[] previousTicks;

    public SystemMetrics collect() {
        HardwareAbstractionLayer hardware = systemInfo.getHardware();
        OperatingSystem os = systemInfo.getOperatingSystem();

        CentralProcessor processor = hardware.getProcessor();
        if (previousTicks == null) {
            previousTicks = processor.getSystemCpuLoadTicks();
        }
        double cpuLoad = processor.getSystemCpuLoadBetweenTicks(previousTicks);
        previousTicks = processor.getSystemCpuLoadTicks();
        double cpuPercent = cpuLoad * 100.0;

        GlobalMemory memory = hardware.getMemory();
        double memPercent = ((double) (memory.getTotal() - memory.getAvailable()) / memory.getTotal()) * 100.0;

        long uptimeSeconds = os.getSystemUptime();

        long netIn = 0;
        long netOut = 0;
        for (NetworkIF nif : hardware.getNetworkIFs()) {
            nif.updateAttributes();
            netIn += nif.getBytesRecv();
            netOut += nif.getBytesSent();
        }

        FileSystem fileSystem = os.getFileSystem();
        List<DiskUsage> disks = fileSystem.getFileStores().stream()
                .map(this::toDiskUsage)
                .toList();

        return new SystemMetrics(cpuPercent, memPercent, uptimeSeconds, netIn, netOut, disks);
    }

    private DiskUsage toDiskUsage(OSFileStore store) {
        long total = store.getTotalSpace();
        double usedPercent = total <= 0 ? 0.0
                : ((double) (total - store.getUsableSpace()) / total) * 100.0;
        return new DiskUsage(store.getMount(), usedPercent);
    }
}
