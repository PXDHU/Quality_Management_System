package com.example.QualityManagementSystem.repository;

import com.example.QualityManagementSystem.model.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    @Query("SELECT d FROM Document d JOIN d.audits a WHERE a.auditId = :auditId")
    List<Document> findByAuditId(@Param("auditId") Long auditId);

    @Query("SELECT DISTINCT d FROM Document d " +
            "JOIN FETCH d.audits a " +
            "WHERE a.auditId = :auditId")
    List<Document> findByAuditIdWithEagerAudits(@Param("auditId") Long auditId);

    @Query("SELECT d FROM Document d WHERE d.uploadedBy.userId = :userId")
    List<Document> findByUploadedBy(@Param("userId") Long userId);

    @Query("SELECT d FROM Document d JOIN d.audits a WHERE a.auditId IN :auditIds")
    List<Document> findByAuditIds(@Param("auditIds") List<Long> auditIds);

    @Query("SELECT d FROM Document d JOIN d.nonConformities nc WHERE nc.nonConformityId = :ncId")
    List<Document> findByNcId(@Param("ncId") Long ncId);

    @Query("SELECT d FROM Document d WHERE d.isEvidence = :isEvidence")
    List<Document> findByIsEvidence(@Param("isEvidence") Boolean isEvidence);

    @Query("SELECT d FROM Document d WHERE d.clauseReference = :clauseReference")
    List<Document> findByClauseReference(@Param("clauseReference") String clauseReference);

    @Query("SELECT d FROM Document d WHERE d.department = :department")
    List<Document> findByDepartment(@Param("department") String department);

    @Query("SELECT d FROM Document d WHERE d.fileName LIKE %:fileName%")
    List<Document> findByFileNameContaining(@Param("fileName") String fileName);

    @Query("SELECT d FROM Document d WHERE d.description LIKE %:description%")
    List<Document> findByDescriptionContaining(@Param("description") String description);

    @Query("SELECT d FROM Document d WHERE d.tags LIKE %:tag%")
    List<Document> findByTagsContaining(@Param("tag") String tag);

    @Query("SELECT d FROM Document d WHERE d.uploadedAt BETWEEN :fromDate AND :toDate")
    List<Document> findByUploadedAtBetween(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);

    // Advanced search with multiple criteria
    @Query("SELECT d FROM Document d WHERE " +
            "(:fileName IS NULL OR d.fileName LIKE %:fileName%) AND " +
            "(:fileType IS NULL OR d.fileType = :fileType) AND " +
            "(:department IS NULL OR d.department = :department) AND " +
            "(:clauseReference IS NULL OR d.clauseReference = :clauseReference) AND " +
            "(:isEvidence IS NULL OR d.isEvidence = :isEvidence) AND " +
            "(:uploadedBy IS NULL OR d.uploadedBy.fullName LIKE %:uploadedBy%) AND " +
            "(:description IS NULL OR d.description LIKE %:description%) AND " +
            "(:tags IS NULL OR d.tags LIKE %:tags%)")
    Page<Document> searchDocuments(
            @Param("fileName") String fileName,
            @Param("fileType") String fileType,
            @Param("department") String department,
            @Param("clauseReference") String clauseReference,
            @Param("isEvidence") Boolean isEvidence,
            @Param("uploadedBy") String uploadedBy,
            @Param("description") String description,
            @Param("tags") String tags,
            Pageable pageable
    );

    // Search documents by audit with pagination
    @Query("SELECT d FROM Document d JOIN d.audits a WHERE a.auditId = :auditId")
    Page<Document> findByAuditIdWithPagination(@Param("auditId") Long auditId, Pageable pageable);

    // Search documents by NC with pagination
    @Query("SELECT d FROM Document d JOIN d.nonConformities nc WHERE nc.nonConformityId = :ncId")
    Page<Document> findByNcIdWithPagination(@Param("ncId") Long ncId, Pageable pageable);

    // Get most accessed documents
    @Query("SELECT d FROM Document d ORDER BY d.accessCount DESC")
    Page<Document> findMostAccessedDocuments(Pageable pageable);

    // Get recently accessed documents
    @Query("SELECT d FROM Document d WHERE d.lastAccessed IS NOT NULL ORDER BY d.lastAccessed DESC")
    Page<Document> findRecentlyAccessedDocuments(Pageable pageable);

    // ✅ Fix: fetch audits & NCs to avoid LazyInitializationException
    @Query("SELECT DISTINCT d FROM Document d " +
            "LEFT JOIN FETCH d.audits " +
            "LEFT JOIN FETCH d.nonConformities")
    List<Document> findAllWithRelations();
}
