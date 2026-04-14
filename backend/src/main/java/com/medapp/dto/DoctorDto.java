package com.medapp.dto;

import lombok.*;
import java.util.List;

public class DoctorDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long         id;
        private String       name;
        private String       specialty;
        private String       qualifications;
        private String       bio;
        private Double       rating;
        private Integer      reviewCount;
        private Integer      yearsExperience;
        private Integer      surgeries;
        private Integer      awards;
        private String       avatarUrl;
        private List<String> tags;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvailabilitySlot {
        private Long   id;
        private String slotDate;
        private String timeSlot;
        private boolean available;
    }

    @Data
    public static class CreateRequest {
        private String       name;
        private String       specialty;
        private String       qualifications;
        private String       bio;
        private Double       rating;
        private Integer      yearsExperience;
        private Integer      surgeries;
        private Integer      awards;
        private String       avatarUrl;
        private List<String> tags;
    }
}
