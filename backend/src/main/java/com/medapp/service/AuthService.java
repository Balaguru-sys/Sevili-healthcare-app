package com.medapp.service;

import com.medapp.auth.AuthenticatedUser;
import com.medapp.auth.JwtService;
import com.medapp.dto.AuthDto;
import com.medapp.model.Patient;
import com.medapp.model.StaffUser;
import com.medapp.repository.PatientRepository;
import com.medapp.repository.StaffUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PatientRepository    patientRepository;
    private final StaffUserRepository  staffUserRepository;
    private final PasswordEncoder      passwordEncoder;
    private final JwtService           jwtService;

    @Transactional
    public AuthDto.AuthResponse login(AuthDto.LoginRequest req) {
        // Try patient first, then staff
        var patientOpt = patientRepository.findByEmail(req.getEmail());
        if (patientOpt.isPresent()) {
            Patient p = patientOpt.get();
            if (!passwordEncoder.matches(req.getPassword(), p.getPassword())) {
                throw new IllegalArgumentException("Invalid email or password");
            }
            String token = jwtService.generateToken(p.getId(), p.getEmail(), p.getRole());
            return AuthDto.AuthResponse.builder()
                    .token(token).userId(p.getId())
                    .email(p.getEmail()).name(p.getName()).role(p.getRole())
                    .build();
        }

        var staffOpt = staffUserRepository.findByEmail(req.getEmail());
        if (staffOpt.isPresent()) {
            StaffUser s = staffOpt.get();
            if (!passwordEncoder.matches(req.getPassword(), s.getPassword())) {
                throw new IllegalArgumentException("Invalid email or password");
            }
            String token = jwtService.generateToken(s.getId(), s.getEmail(), s.getRole());
            return AuthDto.AuthResponse.builder()
                    .token(token).userId(s.getId())
                    .email(s.getEmail()).name(s.getName()).role(s.getRole())
                    .build();
        }

        throw new IllegalArgumentException("Invalid email or password");
    }

    @Transactional
    public AuthDto.AuthResponse register(AuthDto.RegisterRequest req) {
        if (patientRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        Patient patient = Patient.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .phone(req.getPhone())
                .language(req.getLanguage() != null ? req.getLanguage() : "EN")
                .role("ROLE_PATIENT")
                .build();
        patient = patientRepository.save(patient);
        String token = jwtService.generateToken(patient.getId(), patient.getEmail(), patient.getRole());
        return AuthDto.AuthResponse.builder()
                .token(token).userId(patient.getId())
                .email(patient.getEmail()).name(patient.getName()).role(patient.getRole())
                .build();
    }

    @Transactional(readOnly = true)
    public AuthDto.MeResponse me(AuthenticatedUser principal) {
        if ("ROLE_STAFF".equals(principal.getRole())) {
            StaffUser s = staffUserRepository.findById(principal.getId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            return AuthDto.MeResponse.builder()
                    .id(s.getId()).name(s.getName()).email(s.getEmail())
                    .role(s.getRole()).build();
        }
        Patient p = patientRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return AuthDto.MeResponse.builder()
                .id(p.getId()).name(p.getName()).email(p.getEmail())
                .phone(p.getPhone()).language(p.getLanguage()).role(p.getRole())
                .build();
    }
}
