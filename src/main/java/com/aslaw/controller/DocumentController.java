package com.aslaw.controller;

import com.aslaw.dto.DocumentDTO;
import com.aslaw.entity.Document;
import com.aslaw.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Base64;
import org.springframework.http.HttpStatus;
import com.infracore.entity.User;
import com.infracore.repository.UserRepository;
import com.aslaw.service.CaseService;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final UserRepository userRepository;
    private final CaseService caseService;

    /**
     * Get all documents
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER')")
    public ResponseEntity<List<DocumentDTO>> getAllDocuments() {
        try {
            List<DocumentDTO> documents = documentService.getAllDocuments();
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get documents by case ID
     */
    @GetMapping("/case/{caseId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLIENT') or hasRole('USER')")
    public ResponseEntity<List<DocumentDTO>> getDocumentsByCaseId(@PathVariable Long caseId, Authentication authentication) {
        try {
            // Check if user has access to this case
            if (!hasAccessToCase(caseId, authentication)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            List<DocumentDTO> documents = documentService.getDocumentsByCaseId(caseId);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get document by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLIENT') or hasRole('USER')")
    public ResponseEntity<DocumentDTO> getDocumentById(@PathVariable Long id, Authentication authentication) {
        try {
            Optional<Document> documentOpt = documentService.getDocumentById(id);
            if (documentOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Document document = documentOpt.get();
            
            // Check if user has access to this document's case
            if (!hasAccessToCase(document.getLegalCase().getId(), authentication)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            return ResponseEntity.ok(new DocumentDTO(document));
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Upload document (traditional file upload)
     */
    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER')")
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("type") Document.DocumentType type,
            @RequestParam("legalCaseId") Long caseId,
            @RequestParam(value = "description", required = false) String description) {
        
        try {
            DocumentDTO documentDTO = documentService.uploadDocument(file, title, description, type, caseId);
            return ResponseEntity.ok(documentDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", "File processing error: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    /**
     * Create document from base64 (for Angular integration)
     */
    @PostMapping("/create-base64")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER')")
    public ResponseEntity<?> createDocumentFromBase64(@RequestBody CreateBase64DocumentRequest request) {
        try {
            DocumentDTO documentDTO = documentService.createDocumentFromBase64(
                request.getTitle(),
                request.getDescription(),
                request.getType(),
                request.getLegalCaseId(),
                request.getFileName(),
                request.getContentType(),
                request.getBase64Content()
            );
            return ResponseEntity.ok(documentDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Creation failed: " + e.getMessage()));
        }
    }

    /**
     * Download document as base64
     */
    @GetMapping("/{id}/download-base64")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLIENT') or hasRole('USER')")
    public ResponseEntity<?> downloadDocumentAsBase64(@PathVariable Long id, Authentication authentication) {
        try {
            Optional<Document> documentOpt = documentService.getDocumentById(id);
            if (documentOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Document document = documentOpt.get();
            
            // Check if user has access to this document's case
            if (!hasAccessToCase(document.getLegalCase().getId(), authentication)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            String base64Content = documentService.downloadDocumentAsBase64(id);
            return ResponseEntity.ok(Map.of(
                "base64Content", base64Content,
                "fileName", document.getFileName(),
                "contentType", document.getContentType()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Document not found: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Download failed: " + e.getMessage());
        }
    }

    /**
     * Download document as resource (file download)
     */
    @GetMapping("/{id}/download")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLIENT') or hasRole('USER')")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id, Authentication authentication) {
        try {
            Optional<Document> documentOpt = documentService.getDocumentById(id);
            if (documentOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Document document = documentOpt.get();
            
            // Check if user has access to this document's case
            if (!hasAccessToCase(document.getLegalCase().getId(), authentication)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            Resource resource = documentService.downloadDocumentAsResource(id);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFileName() + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, document.getContentType());
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Update document metadata
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER')")
    public ResponseEntity<?> updateDocument(
            @PathVariable Long id,
            @RequestBody UpdateDocumentRequest request) {
        try {
            DocumentDTO documentDTO = documentService.updateDocument(
                id, request.getTitle(), request.getDescription(), request.getType());
            return ResponseEntity.ok(documentDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Update failed: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Update failed: " + e.getMessage());
        }
    }

    /**
     * Delete document
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER')")
    public ResponseEntity<?> deleteDocument(@PathVariable Long id) {
        try {
            documentService.deleteDocument(id);
            return ResponseEntity.ok(Map.of("message", "Document deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Document not found: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Deletion failed: " + e.getMessage());
        }
    }

    /**
     * Search documents by title
     */
    @GetMapping("/search/title")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER')")
    public ResponseEntity<List<DocumentDTO>> searchDocumentsByTitle(@RequestParam String title) {
        try {
            List<DocumentDTO> documents = documentService.searchDocumentsByTitle(title);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Search documents by file name
     */
    @GetMapping("/search/filename")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER')")
    public ResponseEntity<List<DocumentDTO>> searchDocumentsByFileName(@RequestParam String fileName) {
        try {
            List<DocumentDTO> documents = documentService.searchDocumentsByFileName(fileName);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get storage statistics
     */
    @GetMapping("/storage-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getStorageStats() {
        try {
            DocumentService.DocumentStorageStats stats = documentService.getStorageStats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalDocuments", stats.getTotalDocuments());
            response.put("base64Documents", stats.getBase64Documents());
            response.put("totalSizeBytes", stats.getTotalSizeBytes());
            response.put("totalSizeMB", stats.getTotalSizeMB());
            response.put("storageType", "base64");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Could not retrieve storage stats: " + e.getMessage()
            ));
        }
    }

    /**
     * Get system configuration
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("storageType", "base64");
        config.put("features", Map.of(
            "inDatabaseStorage", true,
            "fileSystemStorage", false,
            "cloudStorage", false,
            "base64Upload", true,
            "base64Download", true
        ));
        config.put("maxFileSize", "10MB");
        config.put("supportedFormats", List.of("PDF", "DOC", "DOCX", "XLS", "XLSX", "PNG", "JPG", "JPEG"));
        config.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(config);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Test database connection by getting document count
            DocumentService.DocumentStorageStats stats = documentService.getStorageStats();
            
            health.put("status", "UP");
            health.put("storage", "base64");
            health.put("database", "connected");
            health.put("totalDocuments", stats.getTotalDocuments());
            health.put("totalSizeMB", stats.getTotalSizeMB());
            health.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            health.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(503).body(health);
        }
    }

    /**
     * Test base64 conversion endpoint
     */
    @PostMapping("/test-base64")
    public ResponseEntity<Map<String, Object>> testBase64Conversion(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
            }

            Map<String, Object> result = new HashMap<>();
            result.put("fileName", file.getOriginalFilename());
            result.put("contentType", file.getContentType());
            result.put("fileSize", file.getSize());
            result.put("fileSizeFormatted", formatFileSize(file.getSize()));

            // Convert to base64 and back to test
            String base64Content = Base64.getEncoder().encodeToString(file.getBytes());
            byte[] decodedBytes = Base64.getDecoder().decode(base64Content);
            
            result.put("base64Length", base64Content.length());
            result.put("decodedSize", decodedBytes.length);
            result.put("conversionSuccess", decodedBytes.length == file.getSize());
            result.put("storageType", "base64");
            result.put("message", "File successfully converted to base64 and back");

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Test failed: " + e.getMessage()));
        }
    }

    /**
     * Get current client's own documents (from all their cases)
     */
    @GetMapping("/my-documents")
    @PreAuthorize("hasRole('CLIENT') or hasRole('USER')")
    public ResponseEntity<List<DocumentDTO>> getMyDocuments(Authentication authentication) {
        try {
            String currentUsername = authentication.getName();
            User currentUser = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new RuntimeException("Current user not found"));
            
            List<DocumentDTO> documents = documentService.getDocumentsByClientId(currentUser.getId());
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Utility method to format file size
     */
    private String formatFileSize(long bytes) {
        if (bytes == 0) return "0 Bytes";
        int k = 1024;
        String[] sizes = {"Bytes", "KB", "MB", "GB"};
        int i = (int) Math.floor(Math.log(bytes) / Math.log(k));
        return Math.round((bytes / Math.pow(k, i)) * 100.0) / 100.0 + " " + sizes[i];
    }

    // DTOs for request/response
    public static class CreateBase64DocumentRequest {
        private String title;
        private String description;
        private Document.DocumentType type;
        private Long legalCaseId;
        private String fileName;
        private String contentType;
        private String base64Content;

        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Document.DocumentType getType() { return type; }
        public void setType(Document.DocumentType type) { this.type = type; }

        public Long getLegalCaseId() { return legalCaseId; }
        public void setLegalCaseId(Long legalCaseId) { this.legalCaseId = legalCaseId; }

        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }

        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }

        public String getBase64Content() { return base64Content; }
        public void setBase64Content(String base64Content) { this.base64Content = base64Content; }
    }

    public static class UpdateDocumentRequest {
        private String title;
        private String description;
        private Document.DocumentType type;

        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Document.DocumentType getType() { return type; }
        public void setType(Document.DocumentType type) { this.type = type; }
    }

    /**
     * Check if current user has access to a specific case
     */
    private boolean hasAccessToCase(Long caseId, Authentication authentication) {
        try {
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
                return true;
            }
            
            // Client can only access their own cases
            if (isClient) {
                // Get case and check if client owns it
                Optional<com.aslaw.entity.Case> caseOpt = caseService.getCaseById(caseId);
                if (caseOpt.isPresent()) {
                    com.aslaw.entity.Case caseEntity = caseOpt.get();
                    return caseEntity.getClient() != null && caseEntity.getClient().getId().equals(currentUser.getId());
                }
            }
            
            return false;
        } catch (Exception e) {
            return false;
        }
    }
} 