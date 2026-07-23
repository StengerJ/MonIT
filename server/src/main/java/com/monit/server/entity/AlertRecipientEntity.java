package com.monit.server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "alert_recipients")
@Data
@NoArgsConstructor
public class AlertRecipientEntity {

    @Id
    private UUID id;

    private String email;

    @Column(name = "client_id")
    private UUID clientId;
}
