package com.medapp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

public class EmergencyDto {

    @Data
    public static class SosRequest {
        @NotNull private Long   patientId;
        private String lat;
        private String lng;
        private String location;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SosResponse {
        private Long   alertId;
        private String status;
        private String message;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertResponse {
        private Long   id;
        private Long   patientId;
        private String patientName;
        private String latitude;
        private String longitude;
        private String locationDescription;
        private String status;
        private String triggeredAt;
        private String resolvedAt;
    }
}
