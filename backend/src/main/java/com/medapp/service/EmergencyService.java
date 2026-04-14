package com.medapp.service;

import com.medapp.dto.EmergencyDto;
import com.medapp.model.EmergencyAlert;
import com.medapp.model.Patient;
import com.medapp.repository.EmergencyAlertRepository;
import com.medapp.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmergencyService {

    private final EmergencyAlertRepository alertRepository;
    private final PatientRepository        patientRepository;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Transactional
    public EmergencyDto.SosResponse triggerSos(EmergencyDto.SosRequest req) {
        Patient patient = patientRepository.findById(req.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + req.getPatientId()));

        EmergencyAlert alert = EmergencyAlert.builder()
                .patient(patient)
                .latitude(req.getLat())
                .longitude(req.getLng())
                .locationDescription(req.getLocation())
                .status(EmergencyAlert.AlertStatus.ACTIVE)
                .triggeredAt(LocalDateTime.now())
                .build();

        EmergencyAlert saved = alertRepository.save(alert);
        return EmergencyDto.SosResponse.builder()
                .alertId(saved.getId())
                .status("ACTIVE")
                .message("Emergency services have been notified. Help is on the way.")
                .build();
    }

    @Transactional
    public EmergencyDto.AlertResponse resolveAlert(Long alertId) {
        EmergencyAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));
        alert.setStatus(EmergencyAlert.AlertStatus.RESOLVED);
        alert.setResolvedAt(LocalDateTime.now());
        return mapToResponse(alertRepository.save(alert));
    }

    @Transactional(readOnly = true)
    public List<EmergencyDto.AlertResponse> getActiveAlerts() {
        return alertRepository.findByStatusOrderByTriggeredAtDesc(EmergencyAlert.AlertStatus.ACTIVE)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmergencyDto.AlertResponse> getAllAlerts() {
        return alertRepository.findAllByOrderByTriggeredAtDesc()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmergencyDto.AlertResponse> getAlertHistory(Long patientId) {
        return alertRepository.findByPatientIdOrderByTriggeredAtDesc(patientId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private EmergencyDto.AlertResponse mapToResponse(EmergencyAlert a) {
        return EmergencyDto.AlertResponse.builder()
                .id(a.getId())
                .patientId(a.getPatient().getId())
                .patientName(a.getPatient().getName())
                .latitude(a.getLatitude())
                .longitude(a.getLongitude())
                .locationDescription(a.getLocationDescription())
                .status(a.getStatus().name())
                .triggeredAt(a.getTriggeredAt() != null ? a.getTriggeredAt().format(DT_FMT) : null)
                .resolvedAt(a.getResolvedAt() != null ? a.getResolvedAt().format(DT_FMT) : null)
                .build();
    }
}
