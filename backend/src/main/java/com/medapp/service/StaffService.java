package com.medapp.service;

import com.medapp.dto.PatientDto;
import com.medapp.model.Patient;
import com.medapp.repository.PatientRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final PatientRepository   patientRepository;
   
    private final PasswordEncoder     passwordEncoder;

    @Transactional
    public PatientDto.PatientInfo createPatient(PatientDto.CreateRequest req) {
        if (patientRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + req.getEmail());
        }
        Patient patient = Patient.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(
                        req.getPassword() != null ? req.getPassword() : "ChangeMe@123"))
                .phone(req.getPhone())
                .language(req.getLanguage() != null ? req.getLanguage() : "EN")
                .role("ROLE_PATIENT")
                .build();
        patient = patientRepository.save(patient);
        return mapPatient(patient);
    }

    @Transactional
    public PatientDto.PatientInfo updatePatient(Long id, PatientDto.CreateRequest req) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + id));

        if (req.getName()  != null) patient.setName(req.getName());
        if (req.getPhone() != null) patient.setPhone(req.getPhone());
        if (req.getLanguage() != null) patient.setLanguage(req.getLanguage());
        // Allow email update only if not already taken by another patient
        if (req.getEmail() != null && !req.getEmail().equals(patient.getEmail())) {
            if (patientRepository.existsByEmail(req.getEmail())) {
                throw new IllegalArgumentException("Email already in use: " + req.getEmail());
            }
            patient.setEmail(req.getEmail());
        }
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            patient.setPassword(passwordEncoder.encode(req.getPassword()));
        }
        return mapPatient(patientRepository.save(patient));
    }

    @Transactional
    public void deletePatient(Long id) {
        if (!patientRepository.existsById(id)) {
            throw new IllegalArgumentException("Patient not found: " + id);
        }
        patientRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<PatientDto.PatientInfo> getAllPatients() {
        return patientRepository.findAll().stream()
                .map(this::mapPatient)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PatientDto.PatientInfo getPatient(Long id) {
        return patientRepository.findById(id)
                .map(this::mapPatient)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + id));
    }

    private PatientDto.PatientInfo mapPatient(Patient p) {
        return PatientDto.PatientInfo.builder()
                .id(p.getId()).name(p.getName())
                .email(p.getEmail()).phone(p.getPhone())
                .language(p.getLanguage())
                .build();
    }
}
