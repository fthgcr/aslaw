package com.aslaw.controller;

import com.aslaw.dto.DocumentDTO;
import com.aslaw.entity.Case;
import com.aslaw.entity.Document;
import com.aslaw.repository.CaseRepository;
import com.aslaw.service.DocumentService;
import com.infracore.entity.User;
import com.infracore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "http://localhost:4200")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get all documents
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<List<DocumentDTO>> getAllDocuments() {
        List<DocumentDTO> documents = documentService.getAllDocuments();
        return ResponseEntity.ok(documents);
    }

    /**
     * Get document by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<Document> getDocumentById(@PathVariable Long id) {
        Optional<Document> document = documentService.getDocumentById(id);
        return document.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get documents by case ID
     */
    @GetMapping("/case/{caseId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<List<DocumentDTO>> getDocumentsByCaseId(@PathVariable Long caseId) {
        List<DocumentDTO> documents = documentService.getDocumentsByCaseId(caseId);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get current client's own documents (from all their cases)
     */
    @GetMapping("/my-documents")
    @PreAuthorize("hasRole('CLIENT') or hasRole('USER')")
    public ResponseEntity<List<DocumentDTO>> getMyOwnDocuments() {
        try {
            // Get current user from security context
            String currentUsername = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getName();
            
            User currentUser = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new RuntimeException("Current user not found"));
            
            List<DocumentDTO> documents = documentService.getDocumentsByClientId(currentUser.getId());
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Upload document
     */
    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("type") String type,
            @RequestParam("legalCaseId") Long legalCaseId) {
        
        try {
            Document.DocumentType documentType = Document.DocumentType.valueOf(type.toUpperCase());
            DocumentDTO document = documentService.uploadDocument(file, title, description, documentType, legalCaseId);
            return ResponseEntity.ok(document);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid document type: " + type);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body("File upload failed: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body("Error: " + e.getMessage());
        }
    }

    /**
     * Create document (without file upload)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<?> createDocument(@RequestBody DocumentCreateRequest request) {
        try {
            // Validate case exists
            Case legalCase = caseRepository.findById(request.getLegalCaseId())
                    .orElseThrow(() -> new RuntimeException("Case not found with id: " + request.getLegalCaseId()));

            Document document = new Document();
            document.setTitle(request.getTitle());
            document.setDescription(request.getDescription());
            document.setType(request.getType());
            document.setLegalCase(legalCase);

            Document savedDocument = documentService.createDocument(document);
            return ResponseEntity.ok(savedDocument);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body("Error creating document: " + e.getMessage());
        }
    }

    /**
     * Update document
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<?> updateDocument(@PathVariable Long id, @RequestBody DocumentUpdateRequest request) {
        try {
            Document updatedDocument = new Document();
            updatedDocument.setTitle(request.getTitle());
            updatedDocument.setDescription(request.getDescription());
            updatedDocument.setType(request.getType());
            
            if (request.getLegalCaseId() != null) {
                Case legalCase = caseRepository.findById(request.getLegalCaseId())
                        .orElseThrow(() -> new RuntimeException("Case not found with id: " + request.getLegalCaseId()));
                updatedDocument.setLegalCase(legalCase);
            }

            Document document = documentService.updateDocument(id, updatedDocument);
            return ResponseEntity.ok(document);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body("Error updating document: " + e.getMessage());
        }
    }

    /**
     * Delete document
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteDocument(@PathVariable Long id) {
        try {
            documentService.deleteDocument(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body("Error deleting document: " + e.getMessage());
        }
    }

    /**
     * Download document
     */
    @GetMapping("/{id}/download")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        try {
            Optional<Document> documentOpt = documentService.getDocumentById(id);
            if (documentOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Document document = documentOpt.get();
            Resource resource = documentService.getFileResource(id);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(document.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + document.getFileName() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Search documents by title
     */
    @GetMapping("/search/title")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<List<DocumentDTO>> searchByTitle(@RequestParam String title) {
        List<DocumentDTO> documents = documentService.searchDocumentsByTitle(title);
        return ResponseEntity.ok(documents);
    }

    /**
     * Search documents by file name
     */
    @GetMapping("/search/filename")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<List<DocumentDTO>> searchByFileName(@RequestParam String fileName) {
        List<DocumentDTO> documents = documentService.searchDocumentsByFileName(fileName);
        return ResponseEntity.ok(documents);
    }

    // DTOs
    public static class DocumentCreateRequest {
        private String title;
        private String description;
        private Document.DocumentType type;
        private Long legalCaseId;

        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Document.DocumentType getType() { return type; }
        public void setType(Document.DocumentType type) { this.type = type; }
        
        public Long getLegalCaseId() { return legalCaseId; }
        public void setLegalCaseId(Long legalCaseId) { this.legalCaseId = legalCaseId; }
    }

    public static class DocumentUpdateRequest {
        private String title;
        private String description;
        private Document.DocumentType type;
        private Long legalCaseId;

        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Document.DocumentType getType() { return type; }
        public void setType(Document.DocumentType type) { this.type = type; }
        
        public Long getLegalCaseId() { return legalCaseId; }
        public void setLegalCaseId(Long legalCaseId) { this.legalCaseId = legalCaseId; }
    }
} 