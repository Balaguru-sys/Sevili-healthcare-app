package com.medapp.controller;

import com.medapp.auth.AuthenticatedUser;
import com.medapp.dto.EmergencyDto;
import com.medapp.service.EmergencyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emergency")
@RequiredArgsConstructor
public class EmergencyController {

    private final EmergencyService emergencyService;

    @PostMapping("/sos")
    public ResponseEntity<EmergencyDto.SosResponse> triggerSos(
            @Valid @RequestBody EmergencyDto.SosRequest req,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        // Always use the authenticated patient's own ID — never trust client-supplied ID
        if ("ROLE_PATIENT".equals(principal.getRole())) {
            req.setPatientId(principal.getId());
        }
        return ResponseEntity.ok(emergencyService.triggerSos(req));
    }

    @PatchMapping("/{alertId}/resolve")
    public ResponseEntity<EmergencyDto.AlertResponse> resolve(@PathVariable Long alertId) {
        return ResponseEntity.ok(emergencyService.resolveAlert(alertId));
    }

    @GetMapping("/active")
    public ResponseEntity<List<EmergencyDto.AlertResponse>> activeAlerts() {
        return ResponseEntity.ok(emergencyService.getActiveAlerts());
    }

    @GetMapping("/all")
    public ResponseEntity<List<EmergencyDto.AlertResponse>> allAlerts() {
        return ResponseEntity.ok(emergencyService.getAllAlerts());
    }

    @GetMapping("/history/{patientId}")
    public ResponseEntity<List<EmergencyDto.AlertResponse>> history(@PathVariable Long patientId) {
        return ResponseEntity.ok(emergencyService.getAlertHistory(patientId));
    }
}
