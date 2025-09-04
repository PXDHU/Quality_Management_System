package com.example.QualityManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "checklist_item")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Checklist_item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long itemId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "checklist_id", referencedColumnName = "checklist_id")
    @ToString.Exclude
    private Checklist checklist;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "clause_id", referencedColumnName = "clause_id")
    @ToString.Exclude
    private Clause_library clause;

    public void setChecklist(Checklist checklist) {
        this.checklist = checklist;
    }

    public void setClause(Clause_library clause) {
        this.clause = clause;
    }

    @Column(name = "custom_text", columnDefinition = "TEXT")
    private String customText;

    @Enumerated(EnumType.STRING)
    @Column(name = "conformity_status")
    private ConformityStatus conformityStatus;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @Column(name = "evidence_notes", columnDefinition = "TEXT")
    private String evidenceNotes;

    @Column(name = "evaluated_by")
    private String evaluatedBy;

    @Column(name = "evaluated_at")
    private LocalDateTime evaluatedAt;

    @ManyToMany
    @JoinTable(
            name = "checklist_item_evidence",
            joinColumns = @JoinColumn(name = "item_id"),
            inverseJoinColumns = @JoinColumn(name = "document_id")
    )
    @OrderColumn(name = "document_order") // Hibernate will maintain order using this column
    @ToString.Exclude
    private List<Document> evidenceDocuments = new ArrayList<>();


    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
