package com.monit.agent.config;

import lombok.Data;

import java.util.List;

public class ChecksConfig {

    private List<CheckDefinition> checks;

    public List<CheckDefinition> getChecks() {
        return checks;
    }

    public void setChecks(List<CheckDefinition> checks) {
        this.checks = checks;
    }

    @Data
    public static class CheckDefinition {
        private String type;
        private String name;
        private String processName;
        private Integer port;
        private String url;
        private Integer expectStatus;
        private String path;
        private Double warnPercent;
    }
}
