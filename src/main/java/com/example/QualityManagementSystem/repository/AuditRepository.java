package com.example.QualityManagementSystem.repository;

import com.example.QualityManagementSystem.model.Audit;
import com.example.QualityManagementSystem.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AuditRepository extends JpaRepository<Audit, Long> {
    List<Audit> findByStatus(Status status);

    @Query("SELECT a.status, COUNT(a) FROM Audit a GROUP BY a.status")
    List<Object[]> countGroupByStatus();

    @Query("SELECT COALESCE(a.department, 'UNKNOWN'), COUNT(a) FROM Audit a GROUP BY a.department")
    List<Object[]> countGroupByDepartment();

    @Query("SELECT DISTINCT a FROM Audit a JOIN a.auditors u WHERE u.userId = :auditorId")
    List<Audit> findByAuditor(@Param("auditorId") Long auditorId);

    @Query("SELECT a FROM Audit a WHERE (:department IS NULL OR a.department = :department) AND (:startFrom IS NULL OR a.startDate >= :startFrom) AND (:startTo IS NULL OR a.startDate <= :startTo)")
    List<Audit> searchByDepartmentAndStartDate(@Param("department") String department,
                                               @Param("startFrom") java.time.LocalDate startFrom,
                                               @Param("startTo") java.time.LocalDate startTo);

    List<Audit> findByStatusIn(List<Status> statuses);

    // Load audits with checklists and checklist items for detailed views
    @Query("SELECT DISTINCT a FROM Audit a " +
            "LEFT JOIN FETCH a.checklists c " +
            "LEFT JOIN FETCH c.checklistItems ci " +
            "LEFT JOIN FETCH ci.clause " +
            "LEFT JOIN FETCH a.auditors " +
            "LEFT JOIN FETCH a.createdBy " +
            "ORDER BY a.auditId")
    List<Audit> findAllWithChecklists();

    // ✅ Load a single audit by ID with checklists and checklist items (safe eager fetch)
    @Query("SELECT DISTINCT a FROM Audit a " +
            "LEFT JOIN FETCH a.checklists c " +
            "LEFT JOIN FETCH c.checklistItems ci " +
            "LEFT JOIN FETCH ci.clause " +
            "LEFT JOIN FETCH a.auditors " +
            "LEFT JOIN FETCH a.createdBy " +
            "WHERE a.auditId = :auditId")
    Optional<Audit> findByIdWithChecklistsAndItems(@Param("auditId") Long auditId);

    // Alternative approach: fetch audit and checklists separately to avoid lazy loading issues
    @Query("SELECT a FROM Audit a " +
            "LEFT JOIN FETCH a.auditors " +
            "LEFT JOIN FETCH a.createdBy " +
            "WHERE a.auditId = :auditId")
    Optional<Audit> findByIdWithAuditors(@Param("auditId") Long auditId);

    // Load audits by status with checklists
    @Query("SELECT DISTINCT a FROM Audit a " +
            "LEFT JOIN FETCH a.checklists c " +
            "LEFT JOIN FETCH c.checklistItems ci " +
            "LEFT JOIN FETCH ci.clause " +
            "LEFT JOIN FETCH a.auditors " +
            "LEFT JOIN FETCH a.createdBy " +
            "WHERE a.status = :status " +
            "ORDER BY a.auditId")
    List<Audit> findByStatusWithChecklists(@Param("status") Status status);

    // Load audits with basic checklist info (count only) for list views
    @Query("SELECT a, COUNT(c) as checklistCount FROM Audit a " +
            "LEFT JOIN a.checklists c " +
            "GROUP BY a.auditId " +
            "ORDER BY a.auditId")
    List<Object[]> findAllWithChecklistCount();
}
