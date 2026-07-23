package com.monit.server.metrics;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RetentionPolicyInitializerTest {

    @Test
    void appliesConfiguredRetentionToBothHypertables() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        RetentionPolicyInitializer initializer = new RetentionPolicyInitializer(jdbcTemplate, 90);

        initializer.run(null);

        verify(jdbcTemplate).update(
                eq("SELECT remove_retention_policy(?, if_exists => true)"), eq("metrics"));
        verify(jdbcTemplate).update(
                eq("SELECT add_retention_policy(?, ?::interval)"), eq("metrics"), eq("90 days"));
        verify(jdbcTemplate).update(
                eq("SELECT remove_retention_policy(?, if_exists => true)"), eq("check_results"));
        verify(jdbcTemplate).update(
                eq("SELECT add_retention_policy(?, ?::interval)"), eq("check_results"), eq("90 days"));
    }
}
