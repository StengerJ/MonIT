package com.monit.server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "check_results")
@Data
@NoArgsConstructor
public class CheckResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "time", nullable = false)
    private Instant time;

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Column(name = "check_name", nullable = false)
    private String checkName;

    @Column(name = "check_type", nullable = false)
    private String checkType;

    @Column(nullable = false)
    private String status;

    private String message;
}
