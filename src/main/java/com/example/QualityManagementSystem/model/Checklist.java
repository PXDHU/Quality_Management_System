package com.example.QualityManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "checklist")
// Only include explicitly annotated fields in equals/hashCode
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Checklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "checklist_id")
    @EqualsAndHashCode.Include           // use only id for equals/hashCode
    @ToString.Include
    private Long checklistId;

    @Enumerated(EnumType.STRING)
    @Column(name = "iso_standard", nullable = false)
    private ISO isoStandard;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "audit_id", referencedColumnName = "audit_id")
    @ToString.Exclude   // don't include parent in toString to avoid cycles
    private Audit audit;

    public void setAudit(Audit audit) {
        this.audit = audit;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    // collections should be excluded from equals/hashCode/toString
    @OneToMany(mappedBy = "checklist", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<Checklist_item> checklistItems = new HashSet<>();

    public void addChecklistItem(Checklist_item item) {
        if (checklistItems == null) {
            checklistItems = new HashSet<>();
        }
        if (!checklistItems.contains(item)) {
            checklistItems.add(item);
            item.setChecklist(this);
        }
    }

    public void removeChecklistItem(Checklist_item item) {
        if (checklistItems != null) {
            checklistItems.remove(item);
            if (item.getChecklist() == this) {
                item.setChecklist(null);
            }
        }
    }

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
