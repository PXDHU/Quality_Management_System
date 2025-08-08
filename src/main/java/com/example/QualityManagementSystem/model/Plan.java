package com.example.QualityManagementSystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "correction_plan")
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private long plan_id;

    @Column(name = "plan_title", columnDefinition = "TEXT", nullable = false)
    private String plan_title;

    @Column(name = "plan_description", columnDefinition = "TEXT", nullable = false)
    private String plan_description;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @ManyToOne(optional = false)
    @JoinColumn(name = "assigned_auditor", referencedColumnName = "user_id")
    private AuthUser assigned_auditor;

    @Column(name = "due_date", nullable = false)
    private LocalDate due_date;

    @OneToMany(mappedBy = "plan")
    private Set<Instance> instances = new HashSet<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime created_at = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updated_at = LocalDateTime.now();
}
