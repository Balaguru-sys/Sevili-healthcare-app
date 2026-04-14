package com.medapp.config;

import com.medapp.model.*;
import com.medapp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Profile("dev")
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final PatientRepository            patientRepository;
    private final StaffUserRepository          staffUserRepository;
    private final DoctorRepository             doctorRepository;
    private final VitalsRepository             vitalsRepository;
    private final AppointmentRepository        appointmentRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final PasswordEncoder              passwordEncoder;

    @Override
    public void run(String... args) {
        seedStaff();
        seedDoctors();
        seedPatients();
        log.info("✅ MedApp dev data seeded");
    }

    private void seedStaff() {
        if (staffUserRepository.count() > 0) return;
        staffUserRepository.save(StaffUser.builder()
                .name("Admin User")
                .email("admin@medapp.com")
                .password(passwordEncoder.encode("Staff@123"))
                .role("ROLE_STAFF")
                .department("Administration")
                .build());
    }

    private void seedPatients() {
        if (patientRepository.count() > 0) return;

        Patient sarah = patientRepository.save(Patient.builder()
                .name("Sarah Chen")
                .email("sarah.chen@example.com")
                .password(passwordEncoder.encode("Patient@123"))
                .phone("+1 (555) 100-2000")
                .language("EN")
                .role("ROLE_PATIENT")
                .build());

        vitalsRepository.save(Vitals.builder()
                .patient(sarah)
                .heartRate(72).bloodPressure("118/76").oxygenSaturation(98)
                .sleepHours(7.0).sleepMinutes(42)
                .moveKcal(450).moveGoal(600)
                .exerciseMinutes(45).exerciseGoal(30)
                .standHours(10).standGoal(12)
                .recordedAt(LocalDateTime.now())
                .build());

        Doctor firstDoctor = doctorRepository.findAll().stream().findFirst().orElse(null);
        Doctor secondDoctor = doctorRepository.findAll().stream().skip(1).findFirst().orElse(null);

        if (firstDoctor != null) {
            appointmentRepository.save(Appointment.builder()
                    .patient(sarah).doctor(firstDoctor)
                    .appointmentDate(LocalDate.now().plusDays(1).toString())
                    .timeSlot("10:30 AM - 11:30 AM")
                    .status(Appointment.AppointmentStatus.SCHEDULED)
                    .build());
        }
        if (secondDoctor != null) {
            appointmentRepository.save(Appointment.builder()
                    .patient(sarah).doctor(secondDoctor)
                    .appointmentDate(LocalDate.now().minusDays(2).toString())
                    .timeSlot("09:00 AM - 10:00 AM")
                    .status(Appointment.AppointmentStatus.COMPLETED)
                    .build());
        }
    }

    private void seedDoctors() {
        if (doctorRepository.count() > 0) return;

        Doctor d1 = doctorRepository.save(Doctor.builder()
                .name("Dr. Julian Vanåe").specialty("Neurological Surgery")
                .qualifications("MD, PhD")
                .bio("World-renowned Neurological Surgeon specialising in minimally invasive spinal procedures and complex brain mapping.")
                .rating(4.9).reviewCount(120).yearsExperience(15).surgeries(2400).awards(42)
                .tags(List.of("Brain Mapping", "Spinal Trauma", "Minimally Invasive"))
                .build());

        Doctor d2 = doctorRepository.save(Doctor.builder()
                .name("Dr. Julian Sterling").specialty("Cardiology")
                .qualifications("MD, FACC")
                .bio("Senior Cardiologist with expertise in interventional cardiology and preventive heart care.")
                .rating(4.8).reviewCount(95).yearsExperience(12).surgeries(1800).awards(28)
                .tags(List.of("Interventional Cardiology", "Echocardiography", "Preventive Care"))
                .build());

        Doctor d3 = doctorRepository.save(Doctor.builder()
                .name("Dr. Elena Rossi").specialty("Clinical Nutrition")
                .qualifications("MD, RDN")
                .bio("Clinical Nutritionist and metabolic health specialist.")
                .rating(4.9).reviewCount(180).yearsExperience(10).surgeries(0).awards(15)
                .tags(List.of("Metabolic Health", "Sports Nutrition", "Weight Management"))
                .build());

        Doctor d4 = doctorRepository.save(Doctor.builder()
                .name("Dr. Priya Menon").specialty("General Medicine")
                .qualifications("MBBS, MD")
                .bio("Experienced general physician focused on preventive care and chronic disease management.")
                .rating(4.7).reviewCount(210).yearsExperience(8).surgeries(0).awards(10)
                .tags(List.of("Preventive Care", "Chronic Disease", "Diabetes"))
                .build());

        Doctor d5 = doctorRepository.save(Doctor.builder()
                .name("Dr. Aaron Blake").specialty("Orthopedics")
                .qualifications("MD, MS Orth")
                .bio("Orthopedic surgeon specialising in sports injuries and joint replacements.")
                .rating(4.8).reviewCount(145).yearsExperience(11).surgeries(1200).awards(18)
                .tags(List.of("Sports Injuries", "Joint Replacement", "Physiotherapy"))
                .build());

        // Seed availability for all doctors over next 14 days
        List<Doctor> doctors = List.of(d1, d2, d3, d4, d5);
        String[] slots = {"09:00 AM - 10:00 AM", "11:00 AM - 12:00 PM",
                          "02:00 PM - 03:00 PM", "04:00 PM - 05:00 PM"};

        for (Doctor doctor : doctors) {
            for (int dayOffset = 1; dayOffset <= 14; dayOffset++) {
                LocalDate date = LocalDate.now().plusDays(dayOffset);
                if (date.getDayOfWeek().getValue() <= 5) { // Mon-Fri only
                    for (String slot : slots) {
                        availabilityRepository.save(DoctorAvailability.builder()
                                .doctor(doctor).slotDate(date).timeSlot(slot).available(true)
                                .build());
                    }
                }
            }
        }
    }
}
