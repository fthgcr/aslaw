package com.aslaw.controller;

import com.aslaw.entity.Document;
import com.aslaw.service.DocumentService;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/documents")
@CrossOrigin(origins = "http://localhost:4200")
public class DocumentController {

    private final DocumentService documentService;

    @Autowired
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * Get documents by case ID
     */
    @GetMapping("/case/{caseId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER')")
    public ResponseEntity<List<Document>> getDocumentsByCaseId(@PathVariable Long caseId) {
        try {
            System.out.println("DocumentController: getDocumentsByCaseId called with caseId: " + caseId);
            List<Document> documents = documentService.getDocumentsByCaseId(caseId);
            System.out.println("DocumentController: Found " + documents.size() + " documents for case " + caseId);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            System.out.println("DocumentController: Error getting documents: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Upload document
     */
    @PostMapping("/case/{caseId}/upload")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER')")
    public ResponseEntity<?> uploadDocument(
            @PathVariable Long caseId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "type", defaultValue = "OTHER") Document.DocumentType type) {
        
        try {
            Document document = documentService.uploadDocument(caseId, file, title, description, type);
            return ResponseEntity.status(HttpStatus.CREATED).body(document);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Dosya yüklenirken bir hata oluştu"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Beklenmeyen bir hata oluştu"));
        }
    }

    /**
     * Download document
     */
    @GetMapping("/{documentId}/download")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER')")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long documentId) {
        try {
            Document document = documentService.getDocumentInfo(documentId);
            Resource resource = documentService.downloadDocument(documentId);

            // Encode filename for proper download
            String encodedFilename = URLEncoder.encode(document.getFileName(), StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(document.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + document.getFileName() + "\"; filename*=UTF-8''" + encodedFilename)
                    .contentLength(document.getFileSize())
                    .body(resource);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get document info
     */
    @GetMapping("/{documentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER')")
    public ResponseEntity<Document> getDocument(@PathVariable Long documentId) {
        try {
            Document document = documentService.getDocumentInfo(documentId);
            return ResponseEntity.ok(document);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update document metadata
     */
    @PutMapping("/{documentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER')")
    public ResponseEntity<?> updateDocument(
            @PathVariable Long documentId,
            @RequestBody DocumentUpdateRequest request) {
        
        try {
            Document document = documentService.updateDocument(
                    documentId, 
                    request.getTitle(), 
                    request.getDescription(), 
                    request.getType()
            );
            return ResponseEntity.ok(document);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Doküman güncellenirken bir hata oluştu"));
        }
    }

    /**
     * Delete document
     */
    @DeleteMapping("/{documentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER')")
    public ResponseEntity<?> deleteDocument(@PathVariable Long documentId) {
        try {
            documentService.deleteDocument(documentId);
            return ResponseEntity.ok()
                    .body(Map.of("message", "Doküman başarıyla silindi"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Dosya silinirken bir hata oluştu"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Beklenmeyen bir hata oluştu"));
        }
    }

    // Request DTO for document update
    public static class DocumentUpdateRequest {
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
} 