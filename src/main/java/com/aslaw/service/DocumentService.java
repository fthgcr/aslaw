package com.aslaw.service;

import com.aslaw.dto.DocumentDTO;
import com.aslaw.entity.Case;
import com.aslaw.entity.Document;
import com.aslaw.repository.CaseRepository;
import com.aslaw.repository.DocumentRepository;
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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final CaseRepository caseRepository;
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Autowired
    public DocumentService(DocumentRepository documentRepository, CaseRepository caseRepository) {
        this.documentRepository = documentRepository;
        this.caseRepository = caseRepository;
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

        // Generate unique filename
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
        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Create document entity
        Document document = new Document();
        document.setTitle(title);
        document.setDescription(description);
        document.setFileName(originalFileName);
        document.setFilePath(filePath.toString());
        document.setContentType(file.getContentType());
        document.setFileSize(file.getSize());
        document.setType(type);
        document.setLegalCase(legalCase);

        // Save and return as DTO
        Document savedDocument = documentRepository.save(document);
        Document documentWithDetails = documentRepository.findByIdWithCaseDetails(savedDocument.getId())
                .orElse(savedDocument);
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
     * Delete document
     */
    @Transactional
    public void deleteDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));
        
        // Delete file from disk
        try {
            Path filePath = Paths.get(document.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log error but don't fail the deletion
            System.err.println("Could not delete file: " + document.getFilePath());
        }
        
        documentRepository.delete(document);
    }

    /**
     * Get file resource for download
     */
    @Transactional(readOnly = true)
    public Resource getFileResource(Long id) throws MalformedURLException {
        Document document = documentRepository.findByIdWithCaseDetails(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));
        
        Path filePath = Paths.get(document.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());
        
        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new RuntimeException("File not found or not readable: " + document.getFileName());
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