package com.monit.server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "clients")
@Data
@NoArgsConstructor
public class ClientEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String hostname;

    @Column(name = "api_key", nullable = false, unique = true)
    private String apiKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClientStatus status = ClientStatus.ONLINE;

    @Column(name = "last_seen")
    private Instant lastSeen;

    @Column(name = "expected_interval_seconds", nullable = false)
    private int expectedIntervalSeconds = 30;
}
