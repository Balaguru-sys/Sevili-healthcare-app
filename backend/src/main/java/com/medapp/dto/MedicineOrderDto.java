package com.medapp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

public class MedicineOrderDto {

    @Data
    public static class CreateRequest {
        @NotNull
        private Long prescriptionRecordId;
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long   id;
        private Long   patientId;
        private String patientName;
        private Long   prescriptionRecordId;
        private String prescriptionTitle;
        private String prescriptionFileUrl;
        private String status;
        private String notes;
        private String createdAt;
        private String dispatchedAt;
        private String deliveredAt;
    }
}
