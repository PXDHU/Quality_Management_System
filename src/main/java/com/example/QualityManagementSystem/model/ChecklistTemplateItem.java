package com.example.QualityManagementSystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "checklist_template_item")
public class ChecklistTemplateItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_item_id")
    private Long templateItemId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "template_id", referencedColumnName = "template_id")
    private ChecklistTemplate template;

    @ManyToOne(optional = false)
    @JoinColumn(name = "clause_id", referencedColumnName = "clause_id")
    private Clause_library clause;

    @Column(name = "custom_text", columnDefinition = "TEXT")
    private String customText;

    @Column(name = "custom_description", columnDefinition = "TEXT")
    private String customDescription;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChecklistTemplateItem that = (ChecklistTemplateItem) o;
        return templateItemId != null && templateItemId.equals(that.getTemplateItemId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
