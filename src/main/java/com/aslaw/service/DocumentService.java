package com.aslaw.service;

import com.aslaw.dto.DocumentDTO;
import com.aslaw.entity.Case;
import com.aslaw.entity.Document;
import com.aslaw.repository.CaseRepository;
import com.aslaw.repository.DocumentRepository;
import com.infracore.entity.ActivityLog;
import com.infracore.service.ActivityLogService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final CaseRepository caseRepository;
    private final ActivityLogService activityLogService;
    private final Cloudinary cloudinary;
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    @Value("${app.upload.provider:local}")
    private String uploadProvider;

    @Autowired
    public DocumentService(DocumentRepository documentRepository, CaseRepository caseRepository, 
                          ActivityLogService activityLogService, Cloudinary cloudinary) {
        this.documentRepository = documentRepository;
        this.caseRepository = caseRepository;
        this.activityLogService = activityLogService;
        this.cloudinary = cloudinary;
    }

    /**
     * Get all documents sorted by creation date descending
     */
    @Transactional(readOnly = true)
    public List<DocumentDTO> getAllDocuments() {
        List<Document> documents = documentRepository.findAllWithCaseDetails();
        return documents.stream()
                .map(DocumentDTO::new)
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
                .map(DocumentDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Get documents by client ID (from all client's cases)
     */
    @Transactional(readOnly = true)
    public List<DocumentDTO> getDocumentsByClientId(Long clientId) {
        List<Document> documents = documentRepository.findByClientId(clientId);
        return documents.stream()
                .map(DocumentDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Get documents by type
     */
    @Transactional(readOnly = true)
    public List<DocumentDTO> getDocumentsByType(Document.DocumentType type) {
        List<Document> documents = documentRepository.findByType(type);
        return documents.stream()
                .map(DocumentDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Create document without file upload
     */
    public Document createDocument(Document document) {
        return documentRepository.save(document);
    }

    /**
     * Upload document with file
     */
    @Transactional
    public DocumentDTO uploadDocument(MultipartFile file, String title, String description, 
                                 Document.DocumentType type, Long legalCaseId) throws IOException {
        
        // Validate case exists with details
        Case legalCase = caseRepository.findByIdWithDetails(legalCaseId)
                .orElseThrow(() -> new RuntimeException("Case not found with id: " + legalCaseId));

        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Dosya boş olamaz");
        }

        String filePath;
        String publicUrl = null;
        
        if ("cloudinary".equals(uploadProvider)) {
            // Upload to Cloudinary (PRIVATE)
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), 
                ObjectUtils.asMap(
                    "resource_type", "auto",
                    "folder", "aslaw-documents",
                    "public_id", UUID.randomUUID().toString(),
                    "type", "private"  // PRIVATE UPLOAD
                ));
            
            // Private dosyalar için signed URL gerekir
            String publicId = (String) uploadResult.get("public_id");
            
            // Private dosya için basit URL (authenticated access gerekir)
            publicUrl = cloudinary.url().resourceType("auto").type("private").generate(publicId);
                
            filePath = publicId;
        } else {
            // Local file storage (development)
            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = "";
            if (originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Save file to disk
            Path localFilePath = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), localFilePath, StandardCopyOption.REPLACE_EXISTING);
            filePath = localFilePath.toString();
        }

        // Create document entity
        Document document = new Document();
        document.setTitle(title);
        document.setDescription(description);
        document.setFileName(StringUtils.cleanPath(file.getOriginalFilename()));
        document.setFilePath(filePath);
        document.setContentType(file.getContentType());
        document.setFileSize(file.getSize());
        document.setType(type);
        document.setLegalCase(legalCase);
        
        // Set storage information
        document.setStorageType(uploadProvider);
        document.setIsPrivate("cloudinary".equals(uploadProvider));
        
        // Set public URL if using cloud storage
        if (publicUrl != null) {
            document.setPublicUrl(publicUrl);
        }

        // Save and return as DTO
        Document savedDocument = documentRepository.save(document);
        Document documentWithDetails = documentRepository.findByIdWithCaseDetails(savedDocument.getId())
                .orElse(savedDocument);

        // Log activity
        String clientName = legalCase.getClient() != null ? 
            legalCase.getClient().getFirstName() + " " + legalCase.getClient().getLastName() : "Bilinmeyen Müvekkil";
        
        activityLogService.logDocumentCreated(
            savedDocument.getId(),
            savedDocument.getTitle(),
            legalCase.getId(),
            legalCase.getTitle(),
            legalCase.getClient() != null ? legalCase.getClient().getId() : 0L,
            clientName
        );

        return new DocumentDTO(documentWithDetails);
    }

    /**
     * Update document
     */
    @Transactional
    public Document updateDocument(Long id, Document updatedDocument) {
        return documentRepository.findById(id)
                .map(document -> {
                    document.setTitle(updatedDocument.getTitle());
                    document.setDescription(updatedDocument.getDescription());
                    document.setType(updatedDocument.getType());
                    if (updatedDocument.getLegalCase() != null) {
                        document.setLegalCase(updatedDocument.getLegalCase());
                    }
                    return documentRepository.save(document);
                })
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));
    }

    /**
     * Delete document (with Cloudinary support)
     */
    @Transactional
    public void deleteDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));
        
        try {
            if ("cloudinary".equals(uploadProvider) && document.getFilePath() != null) {
                // Delete from Cloudinary
                cloudinary.uploader().destroy(document.getFilePath(), 
                    ObjectUtils.asMap("type", "private", "resource_type", "auto"));
                System.out.println("🗑️ Cloudinary file deleted: " + document.getFilePath());
            } else {
                // Delete local file
                Path filePath = Paths.get(document.getFilePath());
                Files.deleteIfExists(filePath);
                System.out.println("🗑️ Local file deleted: " + document.getFilePath());
            }
        } catch (Exception e) {
            // Log error but don't fail the deletion
            System.err.println("⚠️ Could not delete file: " + document.getFilePath() + " - " + e.getMessage());
        }
        
        // Delete from database
        documentRepository.delete(document);
        
        // Log activity (simple log)
        System.out.println("📋 Document deleted: " + document.getTitle() + " (ID: " + document.getId() + ")");
    }

    /**
     * Get file resource for download (with Cloudinary support)
     */
    @Transactional(readOnly = true)
    public Resource getFileResource(Long id) throws MalformedURLException {
        Document document = documentRepository.findByIdWithCaseDetails(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));
        
        if ("cloudinary".equals(uploadProvider) && document.getPublicUrl() != null) {
            // For Cloudinary private files, return the signed URL as a UrlResource
            try {
                return new UrlResource(document.getPublicUrl());
            } catch (MalformedURLException e) {
                throw new RuntimeException("Invalid Cloudinary URL for document: " + document.getFileName(), e);
            }
        } else {
            // Local file handling
            Path filePath = Paths.get(document.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found or not readable: " + document.getFileName());
            }
        }
    }

    /**
     * Get signed download URL for Cloudinary private files
     */
    @Transactional(readOnly = true)
    public String getSignedDownloadUrl(Long id) {
        Document document = findById(id);
        
        if ("cloudinary".equals(uploadProvider) && document.getFilePath() != null) {
            try {
                // Generate signed URL for private file (valid for 1 hour)
                return cloudinary.url()
                    .resourceType("auto")
                    .type("private")
                    .signed(true)
                    .generate(document.getFilePath());
            } catch (Exception e) {
                throw new RuntimeException("Could not generate signed URL for document: " + document.getFileName(), e);
            }
        } else {
            // For local files, return the document's public URL or null
            return document.getPublicUrl();
        }
    }

    /**
     * Search documents by title
     */
    @Transactional(readOnly = true)
    public List<DocumentDTO> searchDocumentsByTitle(String title) {
        List<Document> documents = documentRepository.findByTitleContainingIgnoreCase(title);
        return documents.stream()
                .map(DocumentDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Search documents by file name
     */
    @Transactional(readOnly = true)
    public List<DocumentDTO> searchDocumentsByFileName(String fileName) {
        List<Document> documents = documentRepository.findByFileNameContainingIgnoreCase(fileName);
        return documents.stream()
                .map(DocumentDTO::new)
                .collect(Collectors.toList());
    }
} 