package com.monit.agent.checks;

import java.util.List;

public interface ProcessLister {
    List<String> runningProcessNames();
}
