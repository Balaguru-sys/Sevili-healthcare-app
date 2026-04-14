package com.medapp.repository;

import com.medapp.model.MedicineOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicineOrderRepository extends JpaRepository<MedicineOrder, Long> {
    List<MedicineOrder> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    List<MedicineOrder> findAllByOrderByCreatedAtDesc();
    List<MedicineOrder> findByStatusOrderByCreatedAtDesc(MedicineOrder.OrderStatus status);
}
