package com.example.QualityManagementSystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "clause_library")
public class Clause_library {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "clause_id")
    private long clause_id;

    @Column(name = "clause_number", precision = 2, scale = 1)
    private BigDecimal clause_number;

    @Column(name = "clause_name", columnDefinition = "TEXT")
    private String clause_name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "standard", nullable = false)
    private ISO standard;

    @OneToMany(mappedBy = "clause")
    private Set<Checklist_item> checklistItems = new HashSet<>();

    @OneToMany(mappedBy = "clause")
    private Set<Instance> instances = new HashSet<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime created_at = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updated_at = LocalDateTime.now();
}
