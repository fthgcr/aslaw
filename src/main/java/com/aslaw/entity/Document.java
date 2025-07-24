package com.aslaw.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@NoArgsConstructor
@AllArgsConstructor
public class Document {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false)
    private String fileName;
    
    @Column(nullable = false)
    private String contentType;
    
    @Column(nullable = false)
    private Long fileSize;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = true)
    private String filePath; // Keep for backward compatibility
    
    @Column(name = "public_url")
    private String publicUrl; // Keep for external links
    
    @Lob
    @Column(name = "base64_content", columnDefinition = "LONGTEXT")
    private String base64Content; // New: Base64 encoded file content
    
    @Column(name = "storage_type", nullable = false)
    private String storageType = "base64"; // "base64", "local", "cloudinary"
    
    @Column(name = "is_private")
    private Boolean isPrivate = true; // true for base64 storage
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType type;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "legal_case_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "documents"})
    private Case legalCase;
    
    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;
    
    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
    
    public enum DocumentType {
        COMPLAINT,
        ANSWER,
        MOTION,
        EXHIBIT,
        CONTRACT,
        CORRESPONDENCE,
        OTHER
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getPublicUrl() { return publicUrl; }
    public void setPublicUrl(String publicUrl) { this.publicUrl = publicUrl; }

    public String getBase64Content() { return base64Content; }
    public void setBase64Content(String base64Content) { this.base64Content = base64Content; }

    public String getStorageType() { return storageType; }
    public void setStorageType(String storageType) { this.storageType = storageType; }

    public Boolean getIsPrivate() { return isPrivate; }
    public void setIsPrivate(Boolean isPrivate) { this.isPrivate = isPrivate; }

    public DocumentType getType() { return type; }
    public void setType(DocumentType type) { this.type = type; }

    public Case getLegalCase() { return legalCase; }
    public void setLegalCase(Case legalCase) { this.legalCase = legalCase; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
}
