package com.medapp.service;

import com.medapp.dto.AppointmentDto;
import com.medapp.model.Appointment;
import com.medapp.model.Doctor;
import com.medapp.model.Patient;
import com.medapp.repository.AppointmentRepository;
import com.medapp.repository.DoctorRepository;
import com.medapp.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository      doctorRepository;
    private final PatientRepository     patientRepository;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Transactional
    public AppointmentDto.Response book(Long patientId, AppointmentDto.BookRequest req) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));
        Doctor doctor = doctorRepository.findById(req.getDoctorId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + req.getDoctorId()));

        boolean conflict = appointmentRepository
                .existsByDoctorIdAndAppointmentDateAndTimeSlotAndStatusNot(
                        req.getDoctorId(), req.getDate(), req.getSlot(),
                        Appointment.AppointmentStatus.CANCELLED);
        if (conflict) {
            throw new IllegalStateException("This time slot is no longer available. Please choose another.");
        }

        Appointment appt = Appointment.builder()
                .patient(patient).doctor(doctor)
                .appointmentDate(req.getDate())
                .timeSlot(req.getSlot())
                .status(Appointment.AppointmentStatus.SCHEDULED)
                .build();

        return mapToResponse(appointmentRepository.save(appt));
    }

    @Transactional(readOnly = true)
    public List<AppointmentDto.Response> getUpcoming(Long patientId) {
        return appointmentRepository.findUpcomingByPatient(patientId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentDto.Response> getAllForPatient(Long patientId) {
        return appointmentRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public AppointmentDto.Response cancel(Long appointmentId, Long patientId) {
        Appointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        if (!appt.getPatient().getId().equals(patientId)) {
            throw new SecurityException("Not authorised to cancel this appointment");
        }
        appt.setStatus(Appointment.AppointmentStatus.CANCELLED);
        return mapToResponse(appointmentRepository.save(appt));
    }

    @Transactional(readOnly = true)
    public List<AppointmentDto.Response> getAllForStaff() {
        return appointmentRepository.findAll()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Staff can update appointment status directly.
     * Accepts: SCHEDULED, CANCELLED, COMPLETED, NO_SHOW
     */
    @Transactional
    public AppointmentDto.Response updateStatus(Long appointmentId, String status) {
        Appointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found: " + appointmentId));
        try {
            appt.setStatus(Appointment.AppointmentStatus.valueOf(status));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
        return mapToResponse(appointmentRepository.save(appt));
    }

    public AppointmentDto.Response mapToResponse(Appointment a) {
        return AppointmentDto.Response.builder()
                .id(a.getId())
                .patientId(a.getPatient().getId())
                .patientName(a.getPatient().getName())
                .doctorId(a.getDoctor().getId())
                .doctorName(a.getDoctor().getName())
                .doctorSpecialty(a.getDoctor().getSpecialty())
                .appointmentDate(a.getAppointmentDate())
                .timeSlot(a.getTimeSlot())
                .status(a.getStatus().name())
                .createdAt(a.getCreatedAt() != null ? a.getCreatedAt().format(DT_FMT) : null)
                .build();
    }
}
