package com.medapp.repository;

import com.medapp.model.Vitals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VitalsRepository extends JpaRepository<Vitals, Long> {
    Optional<Vitals> findFirstByPatientIdOrderByRecordedAtDesc(Long patientId);
    List<Vitals> findByPatientIdOrderByRecordedAtDesc(Long patientId);
}
