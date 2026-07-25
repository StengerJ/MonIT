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
        // These are SELECT statements (they call functions that return a value), so they must go
        // through query(...) rather than update(...) - update() rejects any statement that returns
        // a ResultSet with "A result was returned when none was expected."
        jdbcTemplate.query("SELECT remove_retention_policy(?, if_exists => true)", (rs, rowNum) -> null, hypertable);
        jdbcTemplate.query("SELECT add_retention_policy(?, ?::interval)", (rs, rowNum) -> null, hypertable, retentionDays + " days");
    }
}
