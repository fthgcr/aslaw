package com.aslaw.repository;

import com.aslaw.entity.Case;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaseRepository extends JpaRepository<Case, Long> {
    List<Case> findByAssignedUser_Id(Long userId);
    
    @Query("SELECT c FROM Case c LEFT JOIN FETCH c.client LEFT JOIN FETCH c.assignedUser WHERE c.client.id = :clientId")
    List<Case> findByClient_Id(@Param("clientId") Long clientId);
    
    List<Case> findByStatus(Case.CaseStatus status);
    boolean existsByCaseNumber(String caseNumber);
    
    @Query("SELECT c FROM Case c LEFT JOIN FETCH c.client LEFT JOIN FETCH c.assignedUser ORDER BY c.createdDate DESC")
    List<Case> findAllWithDetails();
}
