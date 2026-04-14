package com.medapp.repository;

import com.medapp.model.EmergencyAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyAlertRepository extends JpaRepository<EmergencyAlert, Long> {
    List<EmergencyAlert> findByPatientIdOrderByTriggeredAtDesc(Long patientId);
    List<EmergencyAlert> findByStatusOrderByTriggeredAtDesc(EmergencyAlert.AlertStatus status);
    List<EmergencyAlert> findAllByOrderByTriggeredAtDesc();
}
