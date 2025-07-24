package com.aslaw.service;

import com.aslaw.dto.DocumentDTO;
import com.aslaw.entity.Case;
import com.aslaw.entity.Document;
import com.aslaw.repository.CaseRepository;
import com.aslaw.repository.DocumentRepository;
import com.infracore.entity.ActivityLog;
import com.infracore.service.ActivityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final CaseRepository caseRepository;
    private final ActivityLogService activityLogService;

    @Autowired
    public DocumentService(DocumentRepository documentRepository, CaseRepository caseRepository, 
                          ActivityLogService activityLogService) {
        this.documentRepository = documentRepository;
        this.caseRepository = caseRepository;
        this.activityLogService = activityLogService;
    }

    /**
     * Get all documents sorted by creation date in descending order
     */
    @Transactional(readOnly = true)
    public List<DocumentDTO> getAllDocuments() {
        List<Document> documents = documentRepository.findAllWithCaseDetails();
        return documents.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get document by ID
     */
    @Transactional(readOnly = true)
    public Optional<Document> getDocumentById(Long id) {
        return documentRepository.findByIdWithCaseDetails(id);
    }

    /**
     * Get document by ID (simple version)
     */
    @Transactional(readOnly = true)
    public Document findById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));
    }

    /**
     * Get documents by case ID
     */
    @Transactional(readOnly = true)
    public List<DocumentDTO> getDocumentsByCaseId(Long caseId) {
        List<Document> documents = documentRepository.findByCaseId(caseId);
        return documents.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get documents by client ID (from all client's cases)
     */
    @Transactional(readOnly = true)
    public List<DocumentDTO> getDocumentsByClientId(Long clientId) {
        List<Document> documents = documentRepository.findByClientId(clientId);
        return documents.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Upload document with base64 storage
     */
    @Transactional
    public DocumentDTO uploadDocument(MultipartFile file, String title, String description, 
                                    Document.DocumentType type, Long caseId) throws IOException {
        
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // Validate case
        Case legalCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found with id: " + caseId));

        // Convert file to base64
        byte[] fileBytes = file.getBytes();
        String base64Content = Base64.getEncoder().encodeToString(fileBytes);

        // Create document entity
        Document document = new Document();
        document.setTitle(title);
        document.setDescription(description);
        document.setFileName(StringUtils.cleanPath(file.getOriginalFilename()));
        document.setContentType(file.getContentType());
        document.setFileSize(file.getSize());
        document.setFilePath(""); // Empty for base64 storage
        document.setType(type);
        document.setLegalCase(legalCase);
        
        // Set base64 storage information
        document.setBase64Content(base64Content);
        // Temporarily disabled until database migration is applied
        // document.setStorageType("base64");
        // document.setIsPrivate(true);
        
        // Save document
        Document savedDocument = documentRepository.save(document);
        
        // Log activity
        this.logDocumentActivity(savedDocument, "UPLOAD");
        
        return convertToDTO(savedDocument);
    }

    /**
     * Create document from base64 content (for Angular integration)
     */
    @Transactional
    public DocumentDTO createDocumentFromBase64(String title, String description, 
                                              Document.DocumentType type, Long caseId,
                                              String fileName, String contentType, 
                                              String base64Content) {
        
        // Validate case
        Case legalCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found with id: " + caseId));

        // Decode base64 to get file size
        byte[] decodedBytes = Base64.getDecoder().decode(base64Content);
        
        // Create document entity
        Document document = new Document();
        document.setTitle(title);
        document.setDescription(description);
        document.setFileName(StringUtils.cleanPath(fileName));
        document.setContentType(contentType);
        document.setFileSize((long) decodedBytes.length);
        document.setFilePath(""); // Empty for base64 storage
        document.setType(type);
        document.setLegalCase(legalCase);
        document.setBase64Content(base64Content);
        // Temporarily disabled until database migration is applied
        // document.setStorageType("base64");
        // document.setIsPrivate(true);
        
        // Save document
        Document savedDocument = documentRepository.save(document);
        
        // Log activity
        this.logDocumentActivity(savedDocument, "CREATE");
        
        return convertToDTO(savedDocument);
    }

    /**
     * Download document as base64
     */
    @Transactional(readOnly = true)
    public String downloadDocumentAsBase64(Long id) {
        Document document = findById(id);
        
        if (document.getBase64Content() == null || document.getBase64Content().isEmpty()) {
            throw new RuntimeException("Document content not found: " + document.getFileName());
        }
        
        return document.getBase64Content();
    }

    /**
     * Download document as byte array resource
     */
    @Transactional(readOnly = true)
    public Resource downloadDocumentAsResource(Long id) {
        Document document = findById(id);
        
        if (document.getBase64Content() == null || document.getBase64Content().isEmpty()) {
            throw new RuntimeException("Document content not found: " + document.getFileName());
        }
        
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(document.getBase64Content());
            return new ByteArrayResource(decodedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error decoding document content: " + document.getFileName(), e);
        }
    }

    /**
     * Update document metadata (not content)
     */
    @Transactional
    public DocumentDTO updateDocument(Long id, String title, String description, Document.DocumentType type) {
        Document document = findById(id);
        
        document.setTitle(title);
        document.setDescription(description);
        document.setType(type);
        document.setUpdatedDate(LocalDateTime.now());
        
        Document updatedDocument = documentRepository.save(document);
        
        // Log activity
        this.logDocumentActivity(updatedDocument, "UPDATE");
        
        return convertToDTO(updatedDocument);
    }

    /**
     * Delete document
     */
    @Transactional
    public void deleteDocument(Long id) {
        Document document = findById(id);
        
        // Log activity before deletion
        this.logDocumentActivity(document, "DELETE");
        
        // Delete from database (base64 content will be deleted automatically)
        documentRepository.delete(document);
        
        System.out.println("📋 Document deleted: " + document.getTitle() + " (ID: " + document.getId() + ")");
    }

    /**
     * Search documents by title
     */
    @Transactional(readOnly = true)
    public List<DocumentDTO> searchDocumentsByTitle(String title) {
        List<Document> documents = documentRepository.findByTitleContainingIgnoreCase(title);
        return documents.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search documents by file name
     */
    @Transactional(readOnly = true)
    public List<DocumentDTO> searchDocumentsByFileName(String fileName) {
        List<Document> documents = documentRepository.findByFileNameContainingIgnoreCase(fileName);
        return documents.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get storage statistics
     */
    @Transactional(readOnly = true)
    public DocumentStorageStats getStorageStats() {
        List<Document> allDocuments = documentRepository.findAll();
        
        long totalDocuments = allDocuments.size();
        // Temporarily disabled until database migration is applied
        long base64Documents = allDocuments.size(); // Assume all are base64 for now
        // .mapToLong(doc -> "base64".equals(doc.getStorageType()) ? 1 : 0)
        // .sum();
        
        long totalSize = allDocuments.stream()
                .mapToLong(doc -> doc.getFileSize() != null ? doc.getFileSize() : 0)
                .sum();
        
        return new DocumentStorageStats(totalDocuments, base64Documents, totalSize);
    }

    /**
     * Convert Document entity to DTO
     */
    private DocumentDTO convertToDTO(Document document) {
        DocumentDTO dto = new DocumentDTO(document);
        // Don't include base64 content in DTO for performance
        return dto;
    }

    /**
     * Log document activity
     */
    private void logDocumentActivity(Document document, String action) {
        try {
            String clientName = document.getLegalCase() != null && document.getLegalCase().getClient() != null ? 
                document.getLegalCase().getClient().getFirstName() + " " + document.getLegalCase().getClient().getLastName() : 
                "Unknown Client";
            
            activityLogService.logDocumentCreated(
                document.getId(),
                document.getTitle(),
                document.getLegalCase() != null ? document.getLegalCase().getId() : 0L,
                document.getLegalCase() != null ? document.getLegalCase().getTitle() : "Unknown Case",
                document.getLegalCase() != null && document.getLegalCase().getClient() != null ? 
                    document.getLegalCase().getClient().getId() : 0L,
                clientName
            );
        } catch (Exception e) {
            System.err.println("⚠️ Could not log document activity: " + e.getMessage());
        }
    }

    /**
     * Storage statistics class
     */
    public static class DocumentStorageStats {
        private final long totalDocuments;
        private final long base64Documents;
        private final long totalSizeBytes;

        public DocumentStorageStats(long totalDocuments, long base64Documents, long totalSizeBytes) {
            this.totalDocuments = totalDocuments;
            this.base64Documents = base64Documents;
            this.totalSizeBytes = totalSizeBytes;
        }

        public long getTotalDocuments() { return totalDocuments; }
        public long getBase64Documents() { return base64Documents; }
        public long getTotalSizeBytes() { return totalSizeBytes; }
        public double getTotalSizeMB() { return Math.round(totalSizeBytes / (1024.0 * 1024.0) * 100.0) / 100.0; }
    }
} 