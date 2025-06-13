package com.aslaw.dto;

import com.aslaw.entity.Document;
import java.time.LocalDateTime;

public class DocumentDTO {
    private Long id;
    private String title;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private String description;
    private Document.DocumentType type;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    
    // Case information
    private Long legalCaseId;
    private String legalCaseTitle;
    private String legalCaseNumber;
    
    // Client information
    private Long clientId;
    private String clientName;
    
    // Assigned user information
    private Long assignedUserId;
    private String assignedUserName;

    // Constructors
    public DocumentDTO() {}

    public DocumentDTO(Document document) {
        this.id = document.getId();
        this.title = document.getTitle();
        this.fileName = document.getFileName();
        this.contentType = document.getContentType();
        this.fileSize = document.getFileSize();
        this.description = document.getDescription();
        this.type = document.getType();
        this.createdDate = document.getCreatedDate();
        this.updatedDate = document.getUpdatedDate();
        
        if (document.getLegalCase() != null) {
            this.legalCaseId = document.getLegalCase().getId();
            this.legalCaseTitle = document.getLegalCase().getTitle();
            this.legalCaseNumber = document.getLegalCase().getCaseNumber();
            
            if (document.getLegalCase().getClient() != null) {
                this.clientId = document.getLegalCase().getClient().getId();
                this.clientName = document.getLegalCase().getClient().getFirstName() + " " + 
                                document.getLegalCase().getClient().getLastName();
            }
            
            if (document.getLegalCase().getAssignedUser() != null) {
                this.assignedUserId = document.getLegalCase().getAssignedUser().getId();
                this.assignedUserName = document.getLegalCase().getAssignedUser().getFirstName() + " " + 
                                      document.getLegalCase().getAssignedUser().getLastName();
            }
        }
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

    public Document.DocumentType getType() { return type; }
    public void setType(Document.DocumentType type) { this.type = type; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    public Long getLegalCaseId() { return legalCaseId; }
    public void setLegalCaseId(Long legalCaseId) { this.legalCaseId = legalCaseId; }

    public String getLegalCaseTitle() { return legalCaseTitle; }
    public void setLegalCaseTitle(String legalCaseTitle) { this.legalCaseTitle = legalCaseTitle; }

    public String getLegalCaseNumber() { return legalCaseNumber; }
    public void setLegalCaseNumber(String legalCaseNumber) { this.legalCaseNumber = legalCaseNumber; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public Long getAssignedUserId() { return assignedUserId; }
    public void setAssignedUserId(Long assignedUserId) { this.assignedUserId = assignedUserId; }

    public String getAssignedUserName() { return assignedUserName; }
    public void setAssignedUserName(String assignedUserName) { this.assignedUserName = assignedUserName; }
} 