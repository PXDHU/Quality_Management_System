package com.example.QualityManagementSystem.repository;

import com.example.QualityManagementSystem.model.NonConformity;
import com.example.QualityManagementSystem.model.AuthUser;
import com.example.QualityManagementSystem.model.Status;
import com.example.QualityManagementSystem.model.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NonConformityRepository extends JpaRepository<NonConformity, Long> {
    List<NonConformity> findByAudit_AuditId(Long auditId);
    List<NonConformity> findByAssignedToAndStatus(AuthUser user, Status status);
    List<NonConformity> findByAssignedTo(AuthUser user);
    List<NonConformity> findByStatus(Status status);
    List<NonConformity> findBySeverity(Severity severity);
    List<NonConformity> findByStatusAndSeverity(Status status, Severity severity);
    List<NonConformity> findByStatusAndAudit_AuditId(Status status, Long auditId);
    List<NonConformity> findBySeverityAndAudit_AuditId(Severity severity, Long auditId);
    List<NonConformity> findByStatusAndSeverityAndAudit_AuditId(Status status, Severity severity, Long auditId);

    @Query("SELECT n.severity, COUNT(n) FROM NonConformity n GROUP BY n.severity")
    List<Object[]> countGroupBySeverity();

    @Query("SELECT n.status, COUNT(n) FROM NonConformity n GROUP BY n.status")
    List<Object[]> countGroupByStatus();

    @Query("SELECT COALESCE(n.audit.department, 'UNKNOWN'), COUNT(n) FROM NonConformity n GROUP BY n.audit.department")
    List<Object[]> countGroupByDepartment();

    @Query("SELECT FUNCTION('DATE_TRUNC','month', n.createdAt), COUNT(n) FROM NonConformity n WHERE n.createdAt >= :from GROUP BY FUNCTION('DATE_TRUNC','month', n.createdAt) ORDER BY FUNCTION('DATE_TRUNC','month', n.createdAt)")
    List<Object[]> countByMonthSince(@Param("from") java.time.LocalDateTime from);

    @Query("SELECT n FROM NonConformity n WHERE (:clauseId IS NULL OR n.clause.clauseId = :clauseId) AND (:department IS NULL OR n.audit.department = :department) AND (:startFrom IS NULL OR n.audit.startDate >= :startFrom) AND (:startTo IS NULL OR n.audit.startDate <= :startTo)")
    List<NonConformity> searchForReports(@Param("clauseId") Long clauseId,
                                         @Param("department") String department,
                                         @Param("startFrom") java.time.LocalDate startFrom,
                                         @Param("startTo") java.time.LocalDate startTo);

    // Fetch NC with collections eagerly loaded to avoid lazy loading issues
    @Query("SELECT DISTINCT n FROM NonConformity n " +
           "LEFT JOIN FETCH n.actions " +
           "LEFT JOIN FETCH n.rcaSteps " +
           "WHERE n.nonConformityId = :ncId")
    NonConformity findByIdWithCollections(@Param("ncId") Long ncId);

    // Fetch all NCs with collections eagerly loaded
    @Query("SELECT DISTINCT n FROM NonConformity n " +
           "LEFT JOIN FETCH n.actions " +
           "LEFT JOIN FETCH n.rcaSteps")
    List<NonConformity> findAllWithCollections();

    // Fetch NCs by audit with collections eagerly loaded
    @Query("SELECT DISTINCT n FROM NonConformity n " +
           "LEFT JOIN FETCH n.actions " +
           "LEFT JOIN FETCH n.rcaSteps " +
           "WHERE n.audit.auditId = :auditId")
    List<NonConformity> findByAuditIdWithCollections(@Param("auditId") Long auditId);

    // Fetch NCs by assignee with collections eagerly loaded
    @Query("SELECT DISTINCT n FROM NonConformity n " +
           "LEFT JOIN FETCH n.actions " +
           "LEFT JOIN FETCH n.rcaSteps " +
           "WHERE n.assignedTo = :user")
    List<NonConformity> findByAssignedToWithCollections(@Param("user") AuthUser user);

    // Fetch NCs by assignee and status with collections eagerly loaded
    @Query("SELECT DISTINCT n FROM NonConformity n " +
           "LEFT JOIN FETCH n.actions " +
           "LEFT JOIN FETCH n.rcaSteps " +
           "WHERE n.assignedTo = :user AND n.status = :status")
    List<NonConformity> findByAssignedToAndStatusWithCollections(@Param("user") AuthUser user, @Param("status") Status status);

    // Fetch NCs by status with collections eagerly loaded
    @Query("SELECT DISTINCT n FROM NonConformity n " +
           "LEFT JOIN FETCH n.actions " +
           "LEFT JOIN FETCH n.rcaSteps " +
           "WHERE n.status = :status")
    List<NonConformity> findByStatusWithCollections(@Param("status") Status status);

    // Fetch NCs by severity with collections eagerly loaded
    @Query("SELECT DISTINCT n FROM NonConformity n " +
           "LEFT JOIN FETCH n.actions " +
           "LEFT JOIN FETCH n.rcaSteps " +
           "WHERE n.severity = :severity")
    List<NonConformity> findBySeverityWithCollections(@Param("severity") Severity severity);

    // Fetch NCs by status and severity with collections eagerly loaded
    @Query("SELECT DISTINCT n FROM NonConformity n " +
           "LEFT JOIN FETCH n.actions " +
           "LEFT JOIN FETCH n.rcaSteps " +
           "WHERE n.status = :status AND n.severity = :severity")
    List<NonConformity> findByStatusAndSeverityWithCollections(@Param("status") Status status, @Param("severity") Severity severity);

    // Fetch NCs by status and audit with collections eagerly loaded
    @Query("SELECT DISTINCT n FROM NonConformity n " +
           "LEFT JOIN FETCH n.actions " +
           "LEFT JOIN FETCH n.rcaSteps " +
           "WHERE n.status = :status AND n.audit.auditId = :auditId")
    List<NonConformity> findByStatusAndAuditIdWithCollections(@Param("status") Status status, @Param("auditId") Long auditId);

    // Fetch NCs by severity and audit with collections eagerly loaded
    @Query("SELECT DISTINCT n FROM NonConformity n " +
           "LEFT JOIN FETCH n.actions " +
           "LEFT JOIN FETCH n.rcaSteps " +
           "WHERE n.severity = :severity AND n.audit.auditId = :auditId")
    List<NonConformity> findBySeverityAndAuditIdWithCollections(@Param("severity") Severity severity, @Param("auditId") Long auditId);

    // Fetch NCs by status, severity, and audit with collections eagerly loaded
    @Query("SELECT DISTINCT n FROM NonConformity n " +
           "LEFT JOIN FETCH n.actions " +
           "LEFT JOIN FETCH n.rcaSteps " +
           "WHERE n.status = :status AND n.severity = :severity AND n.audit.auditId = :auditId")
    List<NonConformity> findByStatusAndSeverityAndAuditIdWithCollections(@Param("status") Status status, @Param("severity") Severity severity, @Param("auditId") Long auditId);

    // Fetch NCs by audit with collections eagerly loaded
    @Query("SELECT DISTINCT n FROM NonConformity n " +
           "LEFT JOIN FETCH n.actions " +
           "LEFT JOIN FETCH n.rcaSteps " +
           "WHERE n.audit.auditId = :auditId")
    List<NonConformity> findByAuditIdOnlyWithCollections(@Param("auditId") Long auditId);

    // Fetch NCs by status and audit with collections eagerly loaded
    @Query("SELECT DISTINCT n FROM NonConformity n " +
           "LEFT JOIN FETCH n.actions " +
           "LEFT JOIN FETCH n.rcaSteps " +
           "WHERE n.status = :status AND n.audit.auditId = :auditId")
    List<NonConformity> findByStatusAndAuditIdOnlyWithCollections(@Param("status") Status status, @Param("auditId") Long auditId);

    // Fetch NCs by severity and audit with collections eagerly loaded
    @Query("SELECT DISTINCT n FROM NonConformity n " +
           "LEFT JOIN FETCH n.actions " +
           "LEFT JOIN FETCH n.rcaSteps " +
           "WHERE n.severity = :severity AND n.audit.auditId = :auditId")
    List<NonConformity> findBySeverityAndAuditIdOnlyWithCollections(@Param("severity") Severity severity, @Param("auditId") Long auditId);

    // Fetch NCs by status, severity, and audit with collections eagerly loaded
    @Query("SELECT DISTINCT n FROM NonConformity n " +
           "LEFT JOIN FETCH n.actions " +
           "LEFT JOIN FETCH n.rcaSteps " +
           "WHERE n.status = :status AND n.severity = :severity AND n.audit.auditId = :auditId")
    List<NonConformity> findByStatusAndSeverityAndAuditIdOnlyWithCollections(@Param("status") Status status, @Param("severity") Severity severity, @Param("auditId") Long auditId);

    // Fetch NCs by status and severity with collections eagerly loaded
    @Query("SELECT DISTINCT n FROM NonConformity n " +
           "LEFT JOIN FETCH n.actions " +
           "LEFT JOIN FETCH n.rcaSteps " +
           "WHERE n.status = :status AND n.severity = :severity")
    List<NonConformity> findByStatusAndSeverityOnlyWithCollections(@Param("status") Status status, @Param("severity") Severity severity);
}
