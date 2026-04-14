package com.medapp.service;

import com.medapp.dto.VitalsDto;
import com.medapp.model.Patient;
import com.medapp.model.Vitals;
import com.medapp.repository.PatientRepository;
import com.medapp.repository.VitalsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VitalsService {

    private final VitalsRepository  vitalsRepository;
    private final PatientRepository patientRepository;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Transactional
    public VitalsDto.Response uploadVitals(Long patientId, VitalsDto.CreateRequest req) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));

        Vitals vitals = Vitals.builder()
                .patient(patient)
                .heartRate(req.getHeartRate())
                .bloodPressure(req.getBloodPressure())
                .oxygenSaturation(req.getOxygenSaturation())
                .sleepHours(req.getSleepHours())
                .sleepMinutes(req.getSleepMinutes())
                .moveKcal(req.getMoveKcal())
                .moveGoal(req.getMoveGoal())
                .exerciseMinutes(req.getExerciseMinutes())
                .exerciseGoal(req.getExerciseGoal())
                .standHours(req.getStandHours())
                .standGoal(req.getStandGoal())
                .recordedAt(LocalDateTime.now())
                .build();

        return mapToResponse(vitalsRepository.save(vitals));
    }

    @Transactional(readOnly = true)
    public List<VitalsDto.Response> getVitalsHistory(Long patientId) {
        patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));
        return vitalsRepository.findByPatientIdOrderByRecordedAtDesc(patientId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VitalsDto.Response getLatestVitals(Long patientId) {
        patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));
        return vitalsRepository.findFirstByPatientIdOrderByRecordedAtDesc(patientId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new IllegalStateException("No vitals recorded yet for patient " + patientId));
    }

    private VitalsDto.Response mapToResponse(Vitals v) {
        return VitalsDto.Response.builder()
                .id(v.getId())
                .heartRate(v.getHeartRate())
                .bloodPressure(v.getBloodPressure())
                .oxygenSaturation(v.getOxygenSaturation())
                .sleepHours(v.getSleepHours())
                .sleepMinutes(v.getSleepMinutes())
                .moveKcal(v.getMoveKcal())
                .moveGoal(v.getMoveGoal())
                .exerciseMinutes(v.getExerciseMinutes())
                .exerciseGoal(v.getExerciseGoal())
                .standHours(v.getStandHours())
                .standGoal(v.getStandGoal())
                .recordedAt(v.getRecordedAt() != null ? v.getRecordedAt().format(DT_FMT) : null)
                .build();
    }
}
