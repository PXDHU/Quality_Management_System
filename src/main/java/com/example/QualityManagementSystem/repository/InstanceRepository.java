package com.example.QualityManagementSystem.repository;

import com.example.QualityManagementSystem.model.Audit;
import com.example.QualityManagementSystem.model.Instance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InstanceRepository extends JpaRepository<Instance, Long> {
    List<Instance> findByAudit(Audit audit);

    @Query("SELECT COUNT(i) FROM Instance i WHERE i.audit.auditId = :auditId")
    int countByAuditId(Long auditId);

    @Query("SELECT COUNT(i) FROM Instance i WHERE i.audit.auditId = :auditId AND i.conformity_status IS NOT NULL")
    int countEvaluatedByAuditId(Long auditId);
}
