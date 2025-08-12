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
@Table(name = "document")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id")
    private long document_id;

    @Column(name = "file_name", nullable = false)
    private String file_name;

    @Column(name = "file_type", nullable = false)
    private String file_type;

    @Lob
    private byte[] data;

    // Many documents ↔ Many audits
    @ManyToMany(mappedBy = "documents")
    private Set<Audit> audits = new HashSet<>();

    // Many documents ↔ One uploader (AuthUser)
    @ManyToOne
    @JoinColumn(name = "uploaded_by", nullable = false)
    private AuthUser uploadedBy;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploaded_at = LocalDateTime.now();

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
