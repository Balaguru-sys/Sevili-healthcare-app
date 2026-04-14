package com.medapp.controller;

import com.medapp.auth.AuthenticatedUser;
import com.medapp.dto.AppointmentDto;
import com.medapp.dto.ChatDto;
import com.medapp.dto.MedicalRecordDto;
import com.medapp.dto.MedicineOrderDto;
import com.medapp.dto.PatientDto;
import com.medapp.service.AppointmentService;
import com.medapp.service.ChatService;
import com.medapp.service.MedicalRecordService;
import com.medapp.service.MedicineOrderService;
import com.medapp.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService       patientService;
    private final AppointmentService   appointmentService;
    private final MedicalRecordService medicalRecordService;
    private final ChatService          chatService;
    private final MedicineOrderService medicineOrderService;

    // ── Dashboard ─────────────────────────────────────────────
    @GetMapping("/{id}/dashboard")
    public ResponseEntity<PatientDto.DashboardResponse> dashboard(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        assertPatientAccess(principal, id);
        return ResponseEntity.ok(patientService.getDashboard(id));
    }

    // ── Medical records ───────────────────────────────────────
    @GetMapping("/{id}/records")
    public ResponseEntity<List<MedicalRecordDto.Response>> records(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        assertPatientAccess(principal, id);
        return ResponseEntity.ok(medicalRecordService.getRecordsForPatient(id));
    }

    // ── AI context ────────────────────────────────────────────
    @GetMapping("/{id}/ai-context")
    public ResponseEntity<ChatDto.AiContext> aiContext(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        assertPatientAccess(principal, id);
        return ResponseEntity.ok(chatService.getAiContext(id));
    }

    // ── Appointments ──────────────────────────────────────────
    @GetMapping("/{id}/appointments")
    public ResponseEntity<List<AppointmentDto.Response>> appointments(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        assertPatientAccess(principal, id);
        return ResponseEntity.ok(appointmentService.getAllForPatient(id));
    }

    @GetMapping("/{id}/appointments/upcoming")
    public ResponseEntity<List<AppointmentDto.Response>> upcoming(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        assertPatientAccess(principal, id);
        return ResponseEntity.ok(appointmentService.getUpcoming(id));
    }

    @PostMapping("/{id}/appointments")
    public ResponseEntity<AppointmentDto.Response> book(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentDto.BookRequest req,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        assertPatientAccess(principal, id);
        return ResponseEntity.ok(appointmentService.book(id, req));
    }

    @PatchMapping("/{id}/appointments/{appointmentId}/cancel")
    public ResponseEntity<AppointmentDto.Response> cancel(
            @PathVariable Long id,
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        assertPatientAccess(principal, id);
        return ResponseEntity.ok(appointmentService.cancel(appointmentId, id));
    }

    // ── Medicine orders ───────────────────────────────────────
    @PostMapping("/{id}/orders")
    public ResponseEntity<MedicineOrderDto.Response> placeOrder(
            @PathVariable Long id,
            @Valid @RequestBody MedicineOrderDto.CreateRequest req,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        assertPatientAccess(principal, id);
        return ResponseEntity.ok(medicineOrderService.createOrder(id, req));
    }

    @GetMapping("/{id}/orders")
    public ResponseEntity<List<MedicineOrderDto.Response>> getOrders(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        assertPatientAccess(principal, id);
        return ResponseEntity.ok(medicineOrderService.getOrdersForPatient(id));
    }

    // ── Guards ────────────────────────────────────────────────
    private void assertPatientAccess(AuthenticatedUser principal, Long patientId) {
        if ("ROLE_STAFF".equals(principal.getRole())) return;
        if (!principal.getId().equals(patientId)) {
            throw new AccessDeniedException("Access denied");
        }
    }
}
