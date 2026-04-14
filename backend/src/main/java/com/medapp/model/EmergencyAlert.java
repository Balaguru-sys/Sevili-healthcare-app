package com.medapp.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "emergency_alerts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    private String latitude;
    private String longitude;
    private String locationDescription;

    @Enumerated(EnumType.STRING)
    private AlertStatus status;

    private LocalDateTime triggeredAt;
    private LocalDateTime resolvedAt;

    @PrePersist
    protected void onCreate() {
        triggeredAt = LocalDateTime.now();
        if (status == null) status = AlertStatus.ACTIVE;
    }

    public enum AlertStatus {
        ACTIVE, RESPONDED, RESOLVED, FALSE_ALARM
    }
}
