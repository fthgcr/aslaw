package com.aslaw.controller;

import com.aslaw.entity.Case;
import com.aslaw.service.CaseService;
import com.infracore.entity.User;
import com.infracore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import java.util.Optional;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;

@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
public class CaseController {

    private final CaseService caseService;
    private final UserRepository userRepository;

    /**
     * Get all cases sorted by creation date in descending order
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<List<Case>> getAllCases() {
        try {
            List<Case> cases = caseService.getAllCasesSortedByCreationDate();
            return ResponseEntity.ok(cases);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get cases by user ID
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<List<Case>> getCasesByUserId(@PathVariable Long userId) {
        try {
            List<Case> cases = caseService.getCasesByUserId(userId);
            return ResponseEntity.ok(cases);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get cases by client ID (Admin/Lawyer/Clerk access)
     */
    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<List<Case>> getCasesByClientId(@PathVariable Long clientId) {
        try {
            List<Case> cases = caseService.getCasesByClientId(clientId);
            return ResponseEntity.ok(cases);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get current client's own cases (Client access)
     */
    @GetMapping("/my-cases")
    @PreAuthorize("hasRole('CLIENT') or hasRole('USER')")
    public ResponseEntity<List<Case>> getMyOwnCases() {
        try {
            // Get current user from security context
            String currentUsername = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getName();
            
            User currentUser = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new RuntimeException("Current user not found"));
            
            List<Case> cases = caseService.getCasesByClientId(currentUser.getId());
            return ResponseEntity.ok(cases);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get cases by status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<List<Case>> getCasesByStatus(@PathVariable Case.CaseStatus status) {
        try {
            List<Case> cases = caseService.getCasesByStatus(status);
            return ResponseEntity.ok(cases);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get cases with pagination and sorting
     */
    @GetMapping("/paginated")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<Page<Case>> getCasesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : 
                Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Case> cases = caseService.getAllCases(pageable);
            return ResponseEntity.ok(cases);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get case by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK') or hasRole('CLIENT') or hasRole('USER')")
    public ResponseEntity<Case> getCaseById(@PathVariable Long id, Authentication authentication) {
        try {
            System.out.println("CaseController: getCaseById called with id: " + id);
            
            Optional<Case> caseOpt = caseService.getCaseById(id);
            if (caseOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Case caseEntity = caseOpt.get();
            
            // Check if current user is CLIENT/USER and trying to access their own case
            String currentUsername = authentication.getName();
            User currentUser = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new RuntimeException("Current user not found"));
            
            // Get user roles
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            boolean isLawyer = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_LAWYER"));
            boolean isClerk = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_CLERK"));
            boolean isClient = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_CLIENT") || auth.getAuthority().equals("ROLE_USER"));
            
            // Admin, Lawyer, Clerk can access all cases
            if (isAdmin || isLawyer || isClerk) {
                System.out.println("CaseController: Admin/Lawyer/Clerk access granted for case: " + caseEntity.getTitle());
                return ResponseEntity.ok(caseEntity);
            }
            
            // Client can only access their own cases
            if (isClient) {
                if (caseEntity.getClient() != null && caseEntity.getClient().getId().equals(currentUser.getId())) {
                    System.out.println("CaseController: Client access granted for their own case: " + caseEntity.getTitle());
                    return ResponseEntity.ok(caseEntity);
                } else {
                    System.out.println("CaseController: Client access denied - not their case");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
            
            // Default deny
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            
        } catch (Exception e) {
            System.out.println("CaseController: Error getting case: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create new case
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<?> createCase(@Valid @RequestBody CaseCreateRequest request) {
        try {
            Case caseEntity = new Case();
            caseEntity.setCaseNumber(request.getCaseNumber());
            caseEntity.setTitle(request.getTitle());
            caseEntity.setDescription(request.getDescription());
            caseEntity.setStatus(request.getStatus());
            caseEntity.setType(request.getType());
            caseEntity.setFilingDate(request.getFilingDate());

            // Set assigned user if provided
            if (request.getAssignedUserId() != null) {
                User assignedUser = userRepository.findById(request.getAssignedUserId())
                        .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı: " + request.getAssignedUserId()));
                caseEntity.setAssignedUser(assignedUser);
            }

            // Set client if provided
            if (request.getClientId() != null) {
                User client = userRepository.findById(request.getClientId())
                        .orElseThrow(() -> new IllegalArgumentException("Müvekkil bulunamadı: " + request.getClientId()));
                caseEntity.setClient(client);
            }

            Case createdCase = caseService.createCase(caseEntity);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCase);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Dava oluşturulurken bir hata oluştu"));
        }
    }

    /**
     * Update existing case
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<?> updateCase(@PathVariable Long id, @Valid @RequestBody CaseCreateRequest request) {
        try {
            Case caseDetails = new Case();
            caseDetails.setCaseNumber(request.getCaseNumber());
            caseDetails.setTitle(request.getTitle());
            caseDetails.setDescription(request.getDescription());
            caseDetails.setStatus(request.getStatus());
            caseDetails.setType(request.getType());
            caseDetails.setFilingDate(request.getFilingDate());

            // Set assigned user if provided
            if (request.getAssignedUserId() != null) {
                User assignedUser = userRepository.findById(request.getAssignedUserId())
                        .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı: " + request.getAssignedUserId()));
                caseDetails.setAssignedUser(assignedUser);
            }

            // Set client if provided
            if (request.getClientId() != null) {
                User client = userRepository.findById(request.getClientId())
                        .orElseThrow(() -> new IllegalArgumentException("Müvekkil bulunamadı: " + request.getClientId()));
                caseDetails.setClient(client);
            }

            Case updatedCase = caseService.updateCase(id, caseDetails);
            return ResponseEntity.ok(updatedCase);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Dava güncellenirken bir hata oluştu"));
        }
    }

    /**
     * Delete case
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCase(@PathVariable Long id) {
        try {
            caseService.deleteCase(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Dava silinirken bir hata oluştu"));
        }
    }

    /**
     * Test endpoint - no authentication required
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testCases() {
        try {
            System.out.println("CaseController: TEST endpoint called");
            List<Case> cases = caseService.getAllCases();
            System.out.println("CaseController: TEST - Found " + cases.size() + " total cases");
            
            Map<String, Object> response = Map.of(
                "totalCases", cases.size(),
                "casesExist", cases.size() > 0,
                "message", "Test endpoint working"
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("CaseController: TEST - Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Generate case number
     */
    @GetMapping("/generate-number")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<Map<String, String>> generateCaseNumber() {
        try {
            String caseNumber = caseService.generateCaseNumber();
            return ResponseEntity.ok(Map.of("caseNumber", caseNumber));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Debug CORS configuration
     */
    @GetMapping("/cors-debug")
    public ResponseEntity<Map<String, Object>> debugCors(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        String origin = request.getHeader("Origin");
        String referer = request.getHeader("Referer");
        String userAgent = request.getHeader("User-Agent");
        
        // Environment variable'dan CORS ayarlarını al
        String corsAllowedOrigins = System.getenv("CORS_ALLOWED_ORIGINS");
        
        response.put("requestOrigin", origin);
        response.put("requestReferer", referer);
        response.put("userAgent", userAgent);
        response.put("corsAllowedOrigins", corsAllowedOrigins);
        response.put("serverTime", java.time.LocalDateTime.now().toString());
        response.put("message", "CORS Debug endpoint working");
        
        System.out.println("CORS Debug - Origin: " + origin);
        System.out.println("CORS Debug - Referer: " + referer);
        System.out.println("CORS Debug - CORS_ALLOWED_ORIGINS env: " + corsAllowedOrigins);
        
        return ResponseEntity.ok(response);
    }

    // Request DTO for case creation/update
    public static class CaseCreateRequest {
        private String caseNumber;
        private String title;
        private String description;
        private Case.CaseStatus status;
        private Case.CaseType type;
        private LocalDate filingDate;
        private Long assignedUserId;
        private Long clientId;

        // Getters and setters
        public String getCaseNumber() { return caseNumber; }
        public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Case.CaseStatus getStatus() { return status; }
        public void setStatus(Case.CaseStatus status) { this.status = status; }

        public Case.CaseType getType() { return type; }
        public void setType(Case.CaseType type) { this.type = type; }

        public LocalDate getFilingDate() { return filingDate; }
        public void setFilingDate(LocalDate filingDate) { this.filingDate = filingDate; }

        public Long getAssignedUserId() { return assignedUserId; }
        public void setAssignedUserId(Long assignedUserId) { this.assignedUserId = assignedUserId; }

        public Long getClientId() { return clientId; }
        public void setClientId(Long clientId) { this.clientId = clientId; }
    }
} 