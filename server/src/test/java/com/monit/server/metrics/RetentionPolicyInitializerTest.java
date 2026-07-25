package com.monit.server.metrics;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RetentionPolicyInitializerTest {

    @Test
    void appliesConfiguredRetentionToBothHypertables() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        RetentionPolicyInitializer initializer = new RetentionPolicyInitializer(jdbcTemplate, 90);

        initializer.run(null);

        // These calls go through query(...), not update(...): remove_retention_policy and
        // add_retention_policy are SELECT statements that return a row, and update() rejects
        // any statement that returns a ResultSet.
        verify(jdbcTemplate).query(
                eq("SELECT remove_retention_policy(?, if_exists => true)"), any(RowMapper.class), eq("metrics"));
        verify(jdbcTemplate).query(
                eq("SELECT add_retention_policy(?, ?::interval)"), any(RowMapper.class), eq("metrics"), eq("90 days"));
        verify(jdbcTemplate).query(
                eq("SELECT remove_retention_policy(?, if_exists => true)"), any(RowMapper.class), eq("check_results"));
        verify(jdbcTemplate).query(
                eq("SELECT add_retention_policy(?, ?::interval)"), any(RowMapper.class), eq("check_results"), eq("90 days"));
    }
}
