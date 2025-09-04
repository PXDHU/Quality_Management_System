package com.example.QualityManagementSystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "clause_library")
public class Clause_library {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "clause_id")
    private Long clauseId;

    @Column(name = "clause_number")
    private String clauseNumber;

    @Column(name = "clause_name", columnDefinition = "TEXT")
    private String clauseName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "standard", nullable = false)
    private ISO standard;

    @Column(name = "version", nullable = false)
    private String version = "1.0";

    @Column(name = "effective_date")
    private LocalDateTime effectiveDate;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "category")
    private String category;

    @Column(name = "risk_level")
    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel = RiskLevel.MEDIUM;

    @OneToMany(mappedBy = "clause")
    private List<Checklist_item> checklistItems = new ArrayList<>();

    @OneToMany(mappedBy = "clause")
    private List<Instance> instances = new ArrayList<>();

    @OneToMany(mappedBy = "sourceClause", cascade = CascadeType.ALL)
    private List<ComplianceMapping> sourceMappings = new ArrayList<>();

    @OneToMany(mappedBy = "targetClause", cascade = CascadeType.ALL)
    private List<ComplianceMapping> targetMappings = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Clause_library that = (Clause_library) o;
        return clauseId != null && clauseId.equals(that.getClauseId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
