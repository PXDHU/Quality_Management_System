package com.example.QualityManagementSystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "checklist")
public class Checklist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "checklist_id")
    private long checklist_id;

    @Enumerated(EnumType.STRING)
    @Column(name = "iso_standard", nullable = false)
    private ISO isoStandard;

    @ManyToOne(optional = false)
    @JoinColumn(name = "audit_id", referencedColumnName = "audit_id")
    private Audit audit;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @OneToMany(mappedBy = "checklist")
    private Set<Checklist_item> checklistItems = new HashSet<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime created_at = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updated_at = LocalDateTime.now();
}
