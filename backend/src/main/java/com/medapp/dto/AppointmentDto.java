package com.medapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

public class AppointmentDto {

    @Data
    public static class BookRequest {
        @NotNull  private Long   doctorId;
        @NotBlank private String date;
        @NotBlank private String slot;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long   id;
        private Long   patientId;
        private String patientName;
        private Long   doctorId;
        private String doctorName;
        private String doctorSpecialty;
        private String appointmentDate;
        private String timeSlot;
        private String status;
        private String createdAt;
    }
}
