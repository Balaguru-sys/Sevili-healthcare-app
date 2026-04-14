package com.medapp.service;

import com.medapp.dto.DoctorDto;
import com.medapp.model.Doctor;
import com.medapp.model.DoctorAvailability;
import com.medapp.repository.DoctorAvailabilityRepository;
import com.medapp.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository             doctorRepository;
    private final DoctorAvailabilityRepository availabilityRepository;

    @Transactional(readOnly = true)
    public List<DoctorDto.Response> getAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DoctorDto.Response getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + id));
        return mapToResponse(doctor);
    }

    @Transactional(readOnly = true)
    public List<DoctorDto.AvailabilitySlot> getAvailability(Long doctorId) {
        doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + doctorId));
        LocalDate from = LocalDate.now();
        LocalDate to   = from.plusDays(14);
        return availabilityRepository
                .findByDoctorIdAndSlotDateBetweenOrderBySlotDateAscTimeSlotAsc(doctorId, from, to)
                .stream()
                .map(this::mapSlot)
                .collect(Collectors.toList());
    }

    @Transactional
    public DoctorDto.Response createDoctor(DoctorDto.CreateRequest req) {
        Doctor doctor = Doctor.builder()
                .name(req.getName())
                .specialty(req.getSpecialty())
                .qualifications(req.getQualifications())
                .bio(req.getBio())
                .rating(req.getRating() != null ? req.getRating() : 0.0)
                .reviewCount(0)
                .yearsExperience(req.getYearsExperience())
                .surgeries(req.getSurgeries() != null ? req.getSurgeries() : 0)
                .awards(req.getAwards() != null ? req.getAwards() : 0)
                .avatarUrl(req.getAvatarUrl())
                .tags(req.getTags())
                .build();
        return mapToResponse(doctorRepository.save(doctor));
    }

    @Transactional
    public DoctorDto.Response updateDoctor(Long id, DoctorDto.CreateRequest req) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + id));

        if (req.getName()           != null) doctor.setName(req.getName());
        if (req.getSpecialty()      != null) doctor.setSpecialty(req.getSpecialty());
        if (req.getQualifications() != null) doctor.setQualifications(req.getQualifications());
        if (req.getBio()            != null) doctor.setBio(req.getBio());
        if (req.getRating()         != null) doctor.setRating(req.getRating());
        if (req.getYearsExperience()!= null) doctor.setYearsExperience(req.getYearsExperience());
        if (req.getSurgeries()      != null) doctor.setSurgeries(req.getSurgeries());
        if (req.getAwards()         != null) doctor.setAwards(req.getAwards());
        if (req.getAvatarUrl()      != null) doctor.setAvatarUrl(req.getAvatarUrl());
        if (req.getTags()           != null) doctor.setTags(req.getTags());

        return mapToResponse(doctorRepository.save(doctor));
    }

    @Transactional
    public void deleteDoctor(Long id) {
        if (!doctorRepository.existsById(id)) {
            throw new IllegalArgumentException("Doctor not found: " + id);
        }
        doctorRepository.deleteById(id);
    }

    public DoctorDto.Response mapToResponse(Doctor d) {
        return DoctorDto.Response.builder()
                .id(d.getId())
                .name(d.getName())
                .specialty(d.getSpecialty())
                .qualifications(d.getQualifications())
                .bio(d.getBio())
                .rating(d.getRating())
                .reviewCount(d.getReviewCount())
                .yearsExperience(d.getYearsExperience())
                .surgeries(d.getSurgeries())
                .awards(d.getAwards())
                .avatarUrl(d.getAvatarUrl())
                .tags(d.getTags())
                .build();
    }

    private DoctorDto.AvailabilitySlot mapSlot(DoctorAvailability s) {
        return DoctorDto.AvailabilitySlot.builder()
                .id(s.getId())
                .slotDate(s.getSlotDate().toString())
                .timeSlot(s.getTimeSlot())
                .available(s.isAvailable())
                .build();
    }
}
