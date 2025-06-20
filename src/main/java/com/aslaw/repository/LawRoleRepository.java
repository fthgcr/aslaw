package com.aslaw.repository;

import com.aslaw.entity.LawRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LawRoleRepository extends JpaRepository<LawRole, Long> {
    
    Optional<LawRole> findByName(LawRole.LawRoleName name);
    
    List<LawRole> findByIsActiveTrue();
    
    boolean existsByName(LawRole.LawRoleName name);
    
    List<LawRole> findByBaseRoleId(Long baseRoleId);
} 