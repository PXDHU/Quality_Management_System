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

    @Column(name = "file_path", nullable = false)
    private String file_path;

    @ManyToOne(optional = false)
    @JoinColumn(name = "uploaded_by", referencedColumnName = "user_id", nullable = false)
    private AuthUser uploaded_by;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploaded_at = LocalDateTime.now();

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
