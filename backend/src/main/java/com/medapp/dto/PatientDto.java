package com.medapp.dto;

import lombok.*;
import java.util.List;

public class PatientDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardResponse {
        private PatientInfo          patient;
        private VitalsDto            latestVitals;
        private List<AppointmentDto.Response> upcomingAppointments;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatientInfo {
        private Long   id;
        private String name;
        private String email;
        private String phone;
        private String language;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VitalsDto {
        private Long    id;
        private Integer heartRate;
        private String  bloodPressure;
        private Integer oxygenSaturation;
        private Double  sleepHours;
        private Integer sleepMinutes;
        private Integer moveKcal;
        private Integer moveGoal;
        private Integer exerciseMinutes;
        private Integer exerciseGoal;
        private Integer standHours;
        private Integer standGoal;
        private String  recordedAt;
    }

    @Data
    public static class CreateRequest {
        private String name;
        private String email;
        private String password;
        private String phone;
        private String language;
    }
}
