package com.medapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

public class ChatDto {

    @Data
    public static class MessageRequest {
        @NotNull  private Long   patientId;
        @NotBlank private String message;
        private String language;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageResponse {
        private String  response;
        private String  language;
        private Long    messageId;
        private String  timestamp;
        private boolean consultDoctor;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryItem {
        private Long   id;
        private String role;
        private String content;
        private String timestamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiContext {
        private VitalsDto.Response            latestVitals;
        private List<MedicalRecordDto.Response> records;
    }
}
