package com.medapp.dto;

import lombok.*;

public class VitalsDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
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
    }
}
