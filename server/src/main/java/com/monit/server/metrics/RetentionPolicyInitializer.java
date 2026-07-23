package com.monit.server.metrics;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class RetentionPolicyInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final int retentionDays;

    public RetentionPolicyInitializer(JdbcTemplate jdbcTemplate,
                                       @Value("${monit.server.retention-days:30}") int retentionDays) {
        this.jdbcTemplate = jdbcTemplate;
        this.retentionDays = retentionDays;
    }

    @Override
    public void run(ApplicationArguments args) {
        apply("metrics");
        apply("check_results");
    }

    private void apply(String hypertable) {
        jdbcTemplate.update("SELECT remove_retention_policy(?, if_exists => true)", hypertable);
        jdbcTemplate.update("SELECT add_retention_policy(?, ?::interval)", hypertable, retentionDays + " days");
    }
}
