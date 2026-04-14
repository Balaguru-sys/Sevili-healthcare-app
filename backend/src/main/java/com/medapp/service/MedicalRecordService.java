package com.medapp.service;

import com.medapp.dto.MedicalRecordDto;
import com.medapp.model.MedicalRecord;
import com.medapp.model.Patient;
import com.medapp.model.StaffUser;
import com.medapp.repository.MedicalRecordRepository;
import com.medapp.repository.PatientRepository;
import com.medapp.repository.StaffUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository recordRepository;
    private final PatientRepository       patientRepository;
    private final StaffUserRepository     staffUserRepository;
    private final CloudinaryService       cloudinaryService;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Transactional(readOnly = true)
    public List<MedicalRecordDto.Response> getRecordsForPatient(Long patientId) {
        patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));
        return recordRepository.findByPatientIdOrderByUploadedAtDesc(patientId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Upload a medical record with a file sent from the browser.
     * The file is uploaded to Cloudinary; the returned URL is stored in the DB.
     */
    @Transactional
    public MedicalRecordDto.Response uploadRecordWithFile(
            Long patientId, Long staffId, String type, String title, MultipartFile file) {

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));
        StaffUser staff = staffUserRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff not found: " + staffId));

        String fileUrl  = cloudinaryService.uploadFile(file, "records");
        String fileType = CloudinaryService.detectFileType(file.getContentType());

        MedicalRecord record = MedicalRecord.builder()
                .patient(patient)
                .uploadedBy(staff)
                .type(type)
                .title(title)
                .fileUrl(fileUrl)
                .fileType(fileType)
                .build();

        return mapToResponse(recordRepository.save(record));
    }

    /**
     * Legacy: upload record using a pre-existing URL (no file upload).
     * Kept for backward-compatibility with the JSON-only endpoint.
     */
    @Transactional
    public MedicalRecordDto.Response uploadRecord(Long patientId, Long staffId,
                                                  MedicalRecordDto.CreateRequest req) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));
        StaffUser staff = staffUserRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff not found: " + staffId));

        MedicalRecord record = MedicalRecord.builder()
                .patient(patient)
                .uploadedBy(staff)
                .type(req.getType())
                .title(req.getTitle())
                .fileUrl(req.getFileUrl())
                .fileType(req.getFileType() != null ? req.getFileType() : "OTHER")
                .build();

        return mapToResponse(recordRepository.save(record));
    }

    private MedicalRecordDto.Response mapToResponse(MedicalRecord r) {
        return MedicalRecordDto.Response.builder()
                .id(r.getId())
                .type(r.getType())
                .title(r.getTitle())
                .fileUrl(r.getFileUrl())
                .fileType(r.getFileType())
                .uploadedAt(r.getUploadedAt() != null ? r.getUploadedAt().format(DT_FMT) : null)
                .uploadedBy(r.getUploadedBy() != null ? r.getUploadedBy().getName() : null)
                .build();
    }
}
