package com.medapp.controller;

import com.medapp.dto.AppointmentDto;
import com.medapp.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping("/staff/all")
    public ResponseEntity<List<AppointmentDto.Response>> getAllForStaff() {
        return ResponseEntity.ok(appointmentService.getAllForStaff());
    }
}
