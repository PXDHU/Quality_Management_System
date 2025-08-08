package com.example.QualityManagementSystem.repository;

import com.example.QualityManagementSystem.model.Checklist_item;
import com.example.QualityManagementSystem.model.Checklist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChecklistItemRepository extends JpaRepository<Checklist_item, Long> {
    List<Checklist_item> findByChecklist(Checklist checklist);
}
