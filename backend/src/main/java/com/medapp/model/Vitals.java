package com.medapp.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vitals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vitals {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    private Integer heartRate;         // BPM
    private String bloodPressure;      // e.g. "118/76"
    private Integer oxygenSaturation;  // SpO2 %
    private Double sleepHours;
    private Integer sleepMinutes;

    // Activity rings
    private Integer moveKcal;
    private Integer moveGoal;
    private Integer exerciseMinutes;
    private Integer exerciseGoal;
    private Integer standHours;
    private Integer standGoal;

    private LocalDateTime recordedAt;

    @PrePersist
    protected void onCreate() {
        if (recordedAt == null) recordedAt = LocalDateTime.now();
    }
}
