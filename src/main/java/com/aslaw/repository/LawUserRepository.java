package com.aslaw.repository;

import com.aslaw.entity.LawRole;
import com.aslaw.entity.LawUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LawUserRepository extends JpaRepository<LawUser, Long> {
    
    // Basic user queries
    @Query("SELECT lu FROM LawUser lu WHERE lu.user.username = :username")
    Optional<LawUser> findByUsername(@Param("username") String username);
    
    @Query("SELECT CASE WHEN COUNT(lu) > 0 THEN true ELSE false END FROM LawUser lu WHERE lu.user.username = :username")
    boolean existsByUsername(@Param("username") String username);
    
    @Query("SELECT CASE WHEN COUNT(lu) > 0 THEN true ELSE false END FROM LawUser lu WHERE lu.user.email = :email")
    boolean existsByEmail(@Param("email") String email);
    
    Optional<LawUser> findByBarNumber(String barNumber);
    boolean existsByBarNumber(String barNumber);
    
    @Query("SELECT lu FROM LawUser lu WHERE lu.user.id = :userId")
    Optional<LawUser> findByUserId(@Param("userId") Long userId);
    
    // Law role based queries
    @Query("SELECT lu FROM LawUser lu JOIN lu.lawRoles lr WHERE lr.name = :lawRoleName")
    List<LawUser> findByLawRoleName(@Param("lawRoleName") LawRole.LawRoleName lawRoleName);
    
    @Query("SELECT lu FROM LawUser lu JOIN lu.lawRoles lr WHERE lr.name = :lawRoleName AND lu.user.enabled = true AND lu.active = true")
    List<LawUser> findActiveByLawRoleName(@Param("lawRoleName") LawRole.LawRoleName lawRoleName);
    
    @Query("SELECT lu FROM LawUser lu JOIN lu.lawRoles lr WHERE lr.name IN :lawRoleNames")
    List<LawUser> findByLawRoleNames(@Param("lawRoleNames") List<LawRole.LawRoleName> lawRoleNames);
    
    @Query("SELECT lu FROM LawUser lu JOIN lu.lawRoles lr WHERE lr.name IN :lawRoleNames AND lu.user.enabled = true AND lu.active = true")
    List<LawUser> findActiveByLawRoleNames(@Param("lawRoleNames") List<LawRole.LawRoleName> lawRoleNames);
    
    // Specific law role queries
    @Query("SELECT lu FROM LawUser lu JOIN lu.lawRoles lr WHERE lr.name = 'LAWYER'")
    List<LawUser> findAllLawyers();
    
    @Query("SELECT lu FROM LawUser lu JOIN lu.lawRoles lr WHERE lr.name = 'LAWYER' AND lu.user.enabled = true AND lu.active = true")
    List<LawUser> findActiveLawyers();
    
    @Query("SELECT lu FROM LawUser lu JOIN lu.lawRoles lr WHERE lr.name = 'CLERK'")
    List<LawUser> findAllClerks();
    
    @Query("SELECT lu FROM LawUser lu JOIN lu.lawRoles lr WHERE lr.name = 'CLERK' AND lu.user.enabled = true AND lu.active = true")
    List<LawUser> findActiveClerks();
    
    @Query("SELECT lu FROM LawUser lu JOIN lu.lawRoles lr WHERE lr.name IN ('LAWYER', 'CLERK')")
    List<LawUser> findAllLegalStaff();
    
    @Query("SELECT lu FROM LawUser lu JOIN lu.lawRoles lr WHERE lr.name IN ('LAWYER', 'CLERK') AND lu.user.enabled = true AND lu.active = true")
    List<LawUser> findActiveLegalStaff();
    
    @Query("SELECT lu FROM LawUser lu JOIN lu.lawRoles lr WHERE lr.name = 'PARTNER'")
    List<LawUser> findAllPartners();
    
    @Query("SELECT lu FROM LawUser lu JOIN lu.lawRoles lr WHERE lr.name = 'PARTNER' AND lu.user.enabled = true AND lu.active = true")
    List<LawUser> findActivePartners();
    
    // Specialization based queries
    List<LawUser> findBySpecialization(String specialization);
    
    @Query("SELECT lu FROM LawUser lu WHERE lu.specialization = :specialization AND lu.user.enabled = true AND lu.active = true")
    List<LawUser> findBySpecializationAndActive(@Param("specialization") String specialization);
    
    // Experience based queries
    @Query("SELECT lu FROM LawUser lu WHERE lu.experienceYears >= :minYears")
    List<LawUser> findByMinimumExperience(@Param("minYears") Integer minYears);
    
    @Query("SELECT lu FROM LawUser lu WHERE lu.experienceYears >= :minYears AND lu.experienceYears <= :maxYears")
    List<LawUser> findByExperienceRange(@Param("minYears") Integer minYears, @Param("maxYears") Integer maxYears);
    
    // Count queries
    @Query("SELECT COUNT(lu) FROM LawUser lu JOIN lu.lawRoles lr WHERE lr.name = :lawRoleName")
    Long countByLawRoleName(@Param("lawRoleName") LawRole.LawRoleName lawRoleName);
    
    @Query("SELECT COUNT(lu) FROM LawUser lu JOIN lu.lawRoles lr WHERE lr.name = :lawRoleName AND lu.user.enabled = true AND lu.active = true")
    Long countActiveByLawRoleName(@Param("lawRoleName") LawRole.LawRoleName lawRoleName);
    
    // Active users query
    @Query("SELECT lu FROM LawUser lu WHERE lu.user.enabled = true AND lu.active = true")
    List<LawUser> findAllActive();
} 