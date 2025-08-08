package com.example.QualityManagementSystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name="checklist_item")
public class Checklist_item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private long item_id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "checklist_id", referencedColumnName = "checklist_id")
    private Checklist checklist;

    @ManyToOne(optional = false)
    @JoinColumn(name = "clause_id", referencedColumnName = "clause_id")
    private Clause_library clause;

    @Column(name = "custom_text", columnDefinition = "TEXT")
    private String custom_text;

    @Enumerated(EnumType.STRING)
    @Column(name = "conformity_status", nullable = false)
    private ConformityStatus conformity_status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime created_at = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updated_at = LocalDateTime.now();
}
