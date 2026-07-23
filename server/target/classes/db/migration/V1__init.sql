CREATE EXTENSION IF NOT EXISTS timescaledb;

CREATE TABLE clients (
    id UUID PRIMARY KEY,
    hostname VARCHAR(255) NOT NULL,
    api_key VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ONLINE',
    last_seen TIMESTAMPTZ,
    expected_interval_seconds INTEGER NOT NULL DEFAULT 30
);

CREATE TABLE metrics (
    time TIMESTAMPTZ NOT NULL,
    client_id UUID NOT NULL REFERENCES clients (id),
    cpu_percent DOUBLE PRECISION NOT NULL,
    mem_percent DOUBLE PRECISION NOT NULL,
    max_disk_percent DOUBLE PRECISION NOT NULL,
    net_in_bytes BIGINT NOT NULL,
    net_out_bytes BIGINT NOT NULL,
    uptime_seconds BIGINT NOT NULL
);
SELECT create_hypertable('metrics', 'time');
CREATE INDEX idx_metrics_client_time ON metrics (client_id, time DESC);
SELECT add_retention_policy('metrics', INTERVAL '30 days');

CREATE TABLE check_results (
    id BIGSERIAL NOT NULL,
    time TIMESTAMPTZ NOT NULL,
    client_id UUID NOT NULL REFERENCES clients (id),
    check_name VARCHAR(255) NOT NULL,
    check_type VARCHAR(50) NOT NULL,
    status VARCHAR(10) NOT NULL,
    message VARCHAR(1000)
);
SELECT create_hypertable('check_results', 'time');
CREATE INDEX idx_check_results_client_time ON check_results (client_id, time DESC);
SELECT add_retention_policy('check_results', INTERVAL '30 days');

CREATE TABLE alert_recipients (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE alert_state (
    client_id UUID NOT NULL REFERENCES clients (id),
    alert_type VARCHAR(20) NOT NULL,
    current_state VARCHAR(20) NOT NULL,
    last_notified_at TIMESTAMPTZ,
    PRIMARY KEY (client_id, alert_type)
);
