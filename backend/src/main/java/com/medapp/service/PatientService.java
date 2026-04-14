package com.medapp.service;

import com.medapp.dto.AppointmentDto;
import com.medapp.dto.PatientDto;
import com.medapp.model.Patient;
import com.medapp.model.Vitals;
import com.medapp.repository.AppointmentRepository;
import com.medapp.repository.PatientRepository;
import com.medapp.repository.VitalsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository     patientRepository;
    private final VitalsRepository      vitalsRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentService    appointmentService;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Transactional(readOnly = true)
    public PatientDto.DashboardResponse getDashboard(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));

        PatientDto.PatientInfo patientInfo = PatientDto.PatientInfo.builder()
                .id(patient.getId()).name(patient.getName())
                .email(patient.getEmail()).phone(patient.getPhone())
                .language(patient.getLanguage())
                .build();

        PatientDto.VitalsDto vitalsDto = vitalsRepository
                .findFirstByPatientIdOrderByRecordedAtDesc(patientId)
                .map(this::mapVitals)
                .orElse(null);

        List<AppointmentDto.Response> upcoming = appointmentRepository
                .findUpcomingByPatient(patientId)
                .stream()
                .map(appointmentService::mapToResponse)
                .collect(Collectors.toList());

        return PatientDto.DashboardResponse.builder()
                .patient(patientInfo)
                .latestVitals(vitalsDto)
                .upcomingAppointments(upcoming)
                .build();
    }

    private PatientDto.VitalsDto mapVitals(Vitals v) {
        return PatientDto.VitalsDto.builder()
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
