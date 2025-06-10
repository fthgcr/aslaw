package com.aslaw.service;

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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final CaseRepository caseRepository;
    
    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    @Autowired
    public DocumentService(DocumentRepository documentRepository, CaseRepository caseRepository) {
        this.documentRepository = documentRepository;
        this.caseRepository = caseRepository;
    }

    /**
     * Get documents by case ID
     */
    @Transactional(readOnly = true)
    public List<Document> getDocumentsByCaseId(Long caseId) {
        return documentRepository.findByLegalCase_Id(caseId);
    }

    /**
     * Get document by ID
     */
    @Transactional(readOnly = true)
    public Optional<Document> getDocumentById(Long id) {
        return documentRepository.findById(id);
    }

    /**
     * Upload document for a case
     */
    @Transactional
    public Document uploadDocument(Long caseId, MultipartFile file, String title, String description, Document.DocumentType type) throws IOException {
        // Validate case exists
        Case legalCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new IllegalArgumentException("Dava bulunamadı: " + caseId));

        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Dosya boş olamaz");
        }

        // Create directory if not exists
        Path uploadPath = Paths.get(uploadDir, "cases", caseId.toString());
        Files.createDirectories(uploadPath);

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        
        // Save file to disk
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Create document entity
        Document document = new Document();
        document.setTitle(title != null ? title : originalFilename);
        document.setFileName(originalFilename);
        document.setFilePath(filePath.toString());
        document.setContentType(file.getContentType());
        document.setFileSize(file.getSize());
        document.setDescription(description);
        document.setType(type);
        document.setLegalCase(legalCase);

        return documentRepository.save(document);
    }

    /**
     * Download document
     */
    @Transactional(readOnly = true)
    public Resource downloadDocument(Long documentId) throws IOException {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Doküman bulunamadı: " + documentId));

        Path filePath = Paths.get(document.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new IOException("Dosya bulunamadı veya okunamıyor: " + document.getFilePath());
        }
    }

    /**
     * Delete document
     */
    @Transactional
    public void deleteDocument(Long documentId) throws IOException {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Doküman bulunamadı: " + documentId));

        // Delete file from disk
        Path filePath = Paths.get(document.getFilePath());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }

        // Delete from database
        documentRepository.delete(document);
    }

    /**
     * Update document metadata
     */
    @Transactional
    public Document updateDocument(Long documentId, String title, String description, Document.DocumentType type) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Doküman bulunamadı: " + documentId));

        document.setTitle(title);
        document.setDescription(description);
        document.setType(type);

        return documentRepository.save(document);
    }

    /**
     * Get file info for download
     */
    @Transactional(readOnly = true)
    public Document getDocumentInfo(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Doküman bulunamadı: " + documentId));
    }
} 