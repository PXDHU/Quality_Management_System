package com.example.QualityManagementSystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rca_step")
public class RCAStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rca_step_id")
    private Long rcaStepId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "non_conformity_id", referencedColumnName = "non_conformity_id")
    private NonConformity nc;

    @Column(name = "step_number", nullable = false)
    private int stepNumber; // 1..5

    @Column(name = "why_text", columnDefinition = "TEXT", nullable = false)
    private String whyText;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RCAStep that = (RCAStep) o;
        return rcaStepId != null && rcaStepId.equals(that.getRcaStepId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
