package com.medapp.controller;

import com.medapp.dto.AppointmentDto;
import com.medapp.dto.DoctorDto;
import com.medapp.dto.EmergencyDto;
import com.medapp.dto.MedicalRecordDto;
import com.medapp.dto.MedicineOrderDto;
import com.medapp.dto.PatientDto;
import com.medapp.dto.VitalsDto;
import com.medapp.auth.AuthenticatedUser;
import com.medapp.service.AppointmentService;
import com.medapp.service.DoctorService;
import com.medapp.service.EmergencyService;
import com.medapp.service.MedicalRecordService;
import com.medapp.service.MedicineOrderService;
import com.medapp.service.StaffService;
import com.medapp.service.VitalsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STAFF')")
public class StaffController {

    private final StaffService         staffService;
    private final DoctorService        doctorService;
    private final MedicalRecordService medicalRecordService;
    private final VitalsService        vitalsService;
    private final AppointmentService   appointmentService;
    private final EmergencyService     emergencyService;
    private final MedicineOrderService medicineOrderService;

    // ── Patients ──────────────────────────────────────────────
    @GetMapping("/patients")
    public ResponseEntity<List<PatientDto.PatientInfo>> listPatients() {
        return ResponseEntity.ok(staffService.getAllPatients());
    }

    @GetMapping("/patients/{id}")
    public ResponseEntity<PatientDto.PatientInfo> getPatient(@PathVariable Long id) {
        return ResponseEntity.ok(staffService.getPatient(id));
    }

    @PostMapping("/patients")
    public ResponseEntity<PatientDto.PatientInfo> createPatient(
            @RequestBody PatientDto.CreateRequest req) {
        return ResponseEntity.ok(staffService.createPatient(req));
    }

    /** UPDATE 2 — Edit patient */
    @PutMapping("/patients/{id}")
    public ResponseEntity<PatientDto.PatientInfo> updatePatient(
            @PathVariable Long id,
            @RequestBody PatientDto.CreateRequest req) {
        return ResponseEntity.ok(staffService.updatePatient(id, req));
    }

    /** UPDATE 2 — Delete patient */
    @DeleteMapping("/patients/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        staffService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }

    // ── Doctors ───────────────────────────────────────────────
    @PostMapping("/doctors")
    public ResponseEntity<DoctorDto.Response> createDoctor(
            @RequestBody DoctorDto.CreateRequest req) {
        return ResponseEntity.ok(doctorService.createDoctor(req));
    }

    @PutMapping("/doctors/{id}")
    public ResponseEntity<DoctorDto.Response> updateDoctor(
            @PathVariable Long id,
            @RequestBody DoctorDto.CreateRequest req) {
        return ResponseEntity.ok(doctorService.updateDoctor(id, req));
    }

    @DeleteMapping("/doctors/{id}")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }

    // ── Medical records (Cloudinary) ──────────────────────────
    @PostMapping(value = "/patients/{patientId}/records",
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MedicalRecordDto.Response> uploadRecordFile(
            @PathVariable Long patientId,
            @RequestParam("type")  String type,
            @RequestParam("title") String title,
            @RequestParam("file")  MultipartFile file,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(
                medicalRecordService.uploadRecordWithFile(patientId, principal.getId(), type, title, file));
    }

    @GetMapping("/patients/{patientId}/records")
    public ResponseEntity<List<MedicalRecordDto.Response>> getRecords(
            @PathVariable Long patientId) {
        return ResponseEntity.ok(medicalRecordService.getRecordsForPatient(patientId));
    }

    // ── Vitals ────────────────────────────────────────────────
    @PostMapping("/patients/{patientId}/vitals")
    public ResponseEntity<VitalsDto.Response> uploadVitals(
            @PathVariable Long patientId,
            @RequestBody VitalsDto.CreateRequest req) {
        return ResponseEntity.ok(vitalsService.uploadVitals(patientId, req));
    }

    @GetMapping("/patients/{patientId}/vitals")
    public ResponseEntity<List<VitalsDto.Response>> getVitalsHistory(
            @PathVariable Long patientId) {
        return ResponseEntity.ok(vitalsService.getVitalsHistory(patientId));
    }

    /** UPDATE 4 — Latest vitals for prefill */
    @GetMapping("/patients/{patientId}/vitals/latest")
    public ResponseEntity<VitalsDto.Response> getLatestVitals(
            @PathVariable Long patientId) {
        return ResponseEntity.ok(vitalsService.getLatestVitals(patientId));
    }

    // ── Appointments ──────────────────────────────────────────
    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentDto.Response>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllForStaff());
    }

    @PatchMapping("/appointments/{id}/approve")
    public ResponseEntity<AppointmentDto.Response> approveAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.updateStatus(id, "SCHEDULED"));
    }

    @PatchMapping("/appointments/{id}/reject")
    public ResponseEntity<AppointmentDto.Response> rejectAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.updateStatus(id, "CANCELLED"));
    }

    @PatchMapping("/appointments/{id}/complete")
    public ResponseEntity<AppointmentDto.Response> completeAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.updateStatus(id, "COMPLETED"));
    }

    // ── Emergency ─────────────────────────────────────────────
    @GetMapping("/emergency")
    public ResponseEntity<List<EmergencyDto.AlertResponse>> getActiveAlerts() {
        return ResponseEntity.ok(emergencyService.getActiveAlerts());
    }

    @GetMapping("/emergency/all")
    public ResponseEntity<List<EmergencyDto.AlertResponse>> getAllAlerts() {
        return ResponseEntity.ok(emergencyService.getAllAlerts());
    }

    @PatchMapping("/emergency/{alertId}/resolve")
    public ResponseEntity<EmergencyDto.AlertResponse> resolveAlert(@PathVariable Long alertId) {
        return ResponseEntity.ok(emergencyService.resolveAlert(alertId));
    }

    // ── Medicine orders ───────────────────────────────────────
    @GetMapping("/orders")
    public ResponseEntity<List<MedicineOrderDto.Response>> getAllOrders() {
        return ResponseEntity.ok(medicineOrderService.getAllOrders());
    }

    @PatchMapping("/orders/{id}/dispatch")
    public ResponseEntity<MedicineOrderDto.Response> dispatchOrder(@PathVariable Long id) {
        return ResponseEntity.ok(medicineOrderService.dispatch(id));
    }

    @PatchMapping("/orders/{id}/deliver")
    public ResponseEntity<MedicineOrderDto.Response> deliverOrder(@PathVariable Long id) {
        return ResponseEntity.ok(medicineOrderService.markDelivered(id));
    }
}
