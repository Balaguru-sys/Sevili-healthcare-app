package com.medapp.controller;

import com.medapp.dto.VitalsDto;
import com.medapp.service.VitalsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vitals")
@RequiredArgsConstructor
public class VitalsController {

    private final VitalsService vitalsService;

    @GetMapping("/{patientId}/latest")
    public ResponseEntity<VitalsDto.Response> getLatestVitals(@PathVariable Long patientId) {
        return ResponseEntity.ok(vitalsService.getLatestVitals(patientId));
    }

    @GetMapping("/{patientId}/history")
    public ResponseEntity<List<VitalsDto.Response>> getHistory(@PathVariable Long patientId) {
        return ResponseEntity.ok(vitalsService.getVitalsHistory(patientId));
    }
}