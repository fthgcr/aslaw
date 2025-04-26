package com.aslaw.repository;

import com.aslaw.entity.Case;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaseRepository extends JpaRepository<Case, Long> {
    List<Case> findByAssignedUser_Id(Long userId);
    List<Case> findByStatus(Case.CaseStatus status);
    boolean existsByCaseNumber(String caseNumber);
}
