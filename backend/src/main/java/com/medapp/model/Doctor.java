package com.medapp.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "doctors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String specialty;
    private String qualifications;
    private String bio;
    private Double rating;
    private Integer reviewCount;
    private Integer yearsExperience;
    private Integer surgeries;
    private Integer awards;
    private String avatarUrl;

    @ElementCollection
    @CollectionTable(name = "doctor_tags", joinColumns = @JoinColumn(name = "doctor_id"))
    @Column(name = "tag")
    private List<String> tags;
}
