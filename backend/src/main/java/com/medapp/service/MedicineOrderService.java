package com.medapp.service;

import com.medapp.dto.MedicineOrderDto;
import com.medapp.model.MedicalRecord;
import com.medapp.model.MedicineOrder;
import com.medapp.model.Patient;
import com.medapp.repository.MedicalRecordRepository;
import com.medapp.repository.MedicineOrderRepository;
import com.medapp.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicineOrderService {

    private final MedicineOrderRepository  orderRepository;
    private final PatientRepository        patientRepository;
    private final MedicalRecordRepository  recordRepository;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Transactional
    public MedicineOrderDto.Response createOrder(Long patientId, MedicineOrderDto.CreateRequest req) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));

        MedicalRecord prescription = recordRepository.findById(req.getPrescriptionRecordId())
                .orElseThrow(() -> new IllegalArgumentException("Prescription record not found"));

        if (!"PRESCRIPTION".equals(prescription.getType())) {
            throw new IllegalArgumentException("Selected record is not a prescription");
        }
        if (!prescription.getPatient().getId().equals(patientId)) {
            throw new SecurityException("Prescription does not belong to this patient");
        }

        MedicineOrder order = MedicineOrder.builder()
                .patient(patient)
                .prescriptionRecord(prescription)
                .notes(req.getNotes())
                .status(MedicineOrder.OrderStatus.PENDING)
                .build();

        return mapToResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<MedicineOrderDto.Response> getOrdersForPatient(Long patientId) {
        return orderRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicineOrderDto.Response> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public MedicineOrderDto.Response dispatch(Long orderId) {
        MedicineOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        order.setStatus(MedicineOrder.OrderStatus.DISPATCHED);
        order.setDispatchedAt(LocalDateTime.now());
        return mapToResponse(orderRepository.save(order));
    }

    @Transactional
    public MedicineOrderDto.Response markDelivered(Long orderId) {
        MedicineOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        order.setStatus(MedicineOrder.OrderStatus.DELIVERED);
        order.setDeliveredAt(LocalDateTime.now());
        return mapToResponse(orderRepository.save(order));
    }

    private MedicineOrderDto.Response mapToResponse(MedicineOrder o) {
        return MedicineOrderDto.Response.builder()
                .id(o.getId())
                .patientId(o.getPatient().getId())
                .patientName(o.getPatient().getName())
                .prescriptionRecordId(o.getPrescriptionRecord().getId())
                .prescriptionTitle(o.getPrescriptionRecord().getTitle())
                .prescriptionFileUrl(o.getPrescriptionRecord().getFileUrl())
                .status(o.getStatus().name())
                .notes(o.getNotes())
                .createdAt(o.getCreatedAt() != null ? o.getCreatedAt().format(DT_FMT) : null)
                .dispatchedAt(o.getDispatchedAt() != null ? o.getDispatchedAt().format(DT_FMT) : null)
                .deliveredAt(o.getDeliveredAt() != null ? o.getDeliveredAt().format(DT_FMT) : null)
                .build();
    }
}
