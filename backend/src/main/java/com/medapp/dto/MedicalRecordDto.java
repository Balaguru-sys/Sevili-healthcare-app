package com.medapp.dto;

import lombok.*;

public class MedicalRecordDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long   id;
        private String type;
        private String title;
        private String fileUrl;
        private String fileType;   // PDF, IMAGE, TEXT, OTHER
        private String uploadedAt;
        private String uploadedBy;
    }

    @Data
    public static class CreateRequest {
        private String type;
        private String title;
        private String fileUrl;
        private String fileType;
    }
}
