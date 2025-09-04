package com.example.QualityManagementSystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "correction_plan")
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private Long planId;

    @Column(name = "plan_title", columnDefinition = "TEXT", nullable = false)
    private String planTitle;

    @Column(name = "plan_description", columnDefinition = "TEXT", nullable = false)
    private String planDescription;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @ManyToOne(optional = false)
    @JoinColumn(name = "assigned_auditor", referencedColumnName = "user_id")
    private AuthUser assignedAuditor;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @OneToMany(mappedBy = "plan")
    private List<Instance> instances = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Plan that = (Plan) o;
        return planId != null && planId.equals(that.getPlanId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
