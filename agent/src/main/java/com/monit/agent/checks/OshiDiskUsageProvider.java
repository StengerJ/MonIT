package com.monit.agent.checks;

import oshi.SystemInfo;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;

import java.util.List;

public class OshiDiskUsageProvider implements DiskUsageProvider {

    private final SystemInfo systemInfo = new SystemInfo();

    @Override
    public double usedPercent(String path) {
        FileSystem fileSystem = systemInfo.getOperatingSystem().getFileSystem();
        List<OSFileStore> stores = fileSystem.getFileStores();
        for (OSFileStore store : stores) {
            if (path.startsWith(store.getMount())) {
                long total = store.getTotalSpace();
                long usable = store.getUsableSpace();
                if (total <= 0) {
                    return 0.0;
                }
                return ((double) (total - usable) / total) * 100.0;
            }
        }
        return 0.0;
    }
}
