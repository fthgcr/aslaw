package com.aslaw.service;

import com.aslaw.entity.Case;
import com.aslaw.repository.CaseRepository;
import com.infracore.entity.ActivityLog;
import com.infracore.entity.User;
import com.infracore.repository.UserRepository;
import com.infracore.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CaseService {

    private final CaseRepository caseRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    @Autowired
    public CaseService(CaseRepository caseRepository, UserRepository userRepository, ActivityLogService activityLogService) {
        this.caseRepository = caseRepository;
        this.userRepository = userRepository;
        this.activityLogService = activityLogService;
    }

    /**
     * Get all cases sorted by creation date in descending order
     */
    @Transactional(readOnly = true)
    public List<Case> getAllCasesSortedByCreationDate() {
        return caseRepository.findAllWithDetails();
    }

    /**
     * Get all cases
     */
    @Transactional(readOnly = true)
    public List<Case> getAllCases() {
        return caseRepository.findAll();
    }

    /**
     * Get all cases with pagination
     */
    @Transactional(readOnly = true)
    public Page<Case> getAllCases(Pageable pageable) {
        return caseRepository.findAll(pageable);
    }

    /**
     * Get case by ID
     */
    @Transactional(readOnly = true)
    public Optional<Case> getCaseById(Long id) {
        return caseRepository.findByIdWithDetails(id);
    }

    /**
     * Get cases by assigned user ID
     */
    @Transactional(readOnly = true)
    public List<Case> getCasesByUserId(Long userId) {
        return caseRepository.findByAssignedUser_Id(userId);
    }

    /**
     * Get cases by client ID
     */
    @Transactional(readOnly = true)
    public List<Case> getCasesByClientId(Long clientId) {
        return caseRepository.findByClient_Id(clientId);
    }

    /**
     * Get cases by status
     */
    @Transactional(readOnly = true)
    public List<Case> getCasesByStatus(Case.CaseStatus status) {
        return caseRepository.findByStatus(status);
    }

    /**
     * Create new case
     */
    @Transactional
    public Case createCase(Case caseEntity) {
        // Validate required fields
        if (caseEntity.getCaseNumber() == null || caseEntity.getCaseNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Dava numarası gereklidir");
        }
        if (caseEntity.getTitle() == null || caseEntity.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Dava başlığı gereklidir");
        }

        // Check if case number already exists
        if (caseRepository.existsByCaseNumber(caseEntity.getCaseNumber())) {
            throw new IllegalArgumentException("Bu dava numarası zaten kullanılıyor");
        }

        // Timestamps will be set automatically by BaseEntity
        Case savedCase = caseRepository.save(caseEntity);

        // Log activity
        String clientName = savedCase.getClient() != null ? 
            savedCase.getClient().getFirstName() + " " + savedCase.getClient().getLastName() : null;
        
        if (clientName != null) {
            activityLogService.logCaseCreated(
                savedCase.getId(), 
                savedCase.getTitle(), 
                savedCase.getClient().getId(), 
                clientName
            );
        } else {
            activityLogService.logActivity(
                ActivityLog.ActivityType.CASE_CREATED,
                "Yeni dava oluşturuldu: " + savedCase.getTitle(),
                savedCase.getId(),
                savedCase.getTitle(),
                ActivityLog.EntityType.CASE
            );
        }

        return savedCase;
    }

    /**
     * Update existing case
     */
    @Transactional
    public Case updateCase(Long id, Case caseDetails) {
        Case existingCase = caseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dava bulunamadı: " + id));

        // Check if new case number already exists (excluding current case)
        if (!caseDetails.getCaseNumber().equals(existingCase.getCaseNumber()) &&
            caseRepository.existsByCaseNumber(caseDetails.getCaseNumber())) {
            throw new IllegalArgumentException("Bu dava numarası zaten kullanılıyor");
        }

        // Update fields
        existingCase.setCaseNumber(caseDetails.getCaseNumber());
        existingCase.setTitle(caseDetails.getTitle());
        existingCase.setDescription(caseDetails.getDescription());
        existingCase.setStatus(caseDetails.getStatus());
        existingCase.setType(caseDetails.getType());
        existingCase.setFilingDate(caseDetails.getFilingDate());
        existingCase.setAssignedUser(caseDetails.getAssignedUser());
        // Updated date will be set automatically by BaseEntity

        return caseRepository.save(existingCase);
    }

    /**
     * Delete case
     */
    @Transactional
    public void deleteCase(Long id) {
        Case caseEntity = caseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dava bulunamadı: " + id));

        caseRepository.delete(caseEntity);
    }

    /**
     * Check if case number exists
     */
    @Transactional(readOnly = true)
    public boolean existsByCaseNumber(String caseNumber) {
        return caseRepository.existsByCaseNumber(caseNumber);
    }

    /**
     * Generate unique case number
     */
    public String generateCaseNumber() {
        String year = String.valueOf(java.time.LocalDate.now().getYear());
        String prefix = "CASE-" + year + "-";
        
        int counter = 1;
        String caseNumber;
        
        do {
            caseNumber = prefix + String.format("%04d", counter);
            counter++;
        } while (caseRepository.existsByCaseNumber(caseNumber));
        
        return caseNumber;
    }
} 