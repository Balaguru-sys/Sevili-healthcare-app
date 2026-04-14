package com.medapp.repository;

import com.medapp.model.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {
    List<DoctorAvailability> findByDoctorIdAndSlotDateBetweenOrderBySlotDateAscTimeSlotAsc(
            Long doctorId, LocalDate from, LocalDate to);
    List<DoctorAvailability> findByDoctorIdAndSlotDateAndAvailableTrue(Long doctorId, LocalDate date);
}
