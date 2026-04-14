package com.medapp.controller;

import com.medapp.dto.DoctorDto;
import com.medapp.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    public ResponseEntity<List<DoctorDto.Response>> listAll() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorDto.Response> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<List<DoctorDto.AvailabilitySlot>> availability(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getAvailability(id));
    }
}
