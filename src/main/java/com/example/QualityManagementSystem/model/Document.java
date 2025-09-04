package com.example.QualityManagementSystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

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
@Table(name = "document")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id")
    private Long documentId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_type", nullable = false)
    private String fileType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize; // File size in bytes

    @Lob
    private byte[] data;

    // Many documents ↔ Many audits
    @ManyToMany(mappedBy = "documents")
    private List<Audit> audits = new ArrayList<>();

    // Many documents ↔ Many NCs (for evidence linking)
    @ManyToMany
    @JoinTable(
            name = "document_nc",
            joinColumns = @JoinColumn(name = "document_id"),
            inverseJoinColumns = @JoinColumn(name = "non_conformity_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"document_id", "non_conformity_id"})
    )
    private List<NonConformity> nonConformities = new ArrayList<>();

    // Many documents ↔ One uploader (AuthUser)
    @ManyToOne
    @JoinColumn(name = "uploaded_by", nullable = false)
    private AuthUser uploadedBy;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "tags")
    private String tags; // Comma-separated tags for search

    @Column(name = "clause_reference")
    private String clauseReference; // ISO clause reference

    @Column(name = "department")
    private String department; // Department related to the document

    @Column(name = "is_evidence", nullable = false)
    private Boolean isEvidence = false; // Whether this document is evidence for NCs

    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed; // Track when document was last accessed

    @Column(name = "access_count", nullable = false)
    private Integer accessCount = 0; // Track number of times accessed

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document that = (Document) o;
        return documentId != null && documentId.equals(that.getDocumentId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
