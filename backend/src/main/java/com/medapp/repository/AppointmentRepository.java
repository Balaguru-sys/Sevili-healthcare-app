package com.medapp.repository;

import com.medapp.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    List<Appointment> findByDoctorIdOrderByCreatedAtDesc(Long doctorId);

    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId AND a.status = 'SCHEDULED' ORDER BY a.appointmentDate ASC")
    List<Appointment> findUpcomingByPatient(Long patientId);

    boolean existsByDoctorIdAndAppointmentDateAndTimeSlotAndStatusNot(
            Long doctorId, String date, String timeSlot, Appointment.AppointmentStatus status);
}
