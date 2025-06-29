package com.aslaw.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.infracore.entity.BaseEntity;
import com.infracore.entity.User;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "cases")
public class Case extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String caseNumber;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CaseStatus status;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CaseType type;

    @Column(nullable = false)
    private LocalDate filingDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_user_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "roles", "version", 
                          "createdDate", "lastModifiedDate", "createdBy", "lastModifiedBy", "deleted"})
    private User assignedUser;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "roles", "version",
                          "createdDate", "lastModifiedDate", "createdBy", "lastModifiedBy", "deleted"})
    private User client;

    public enum CaseStatus {
        OPEN, IN_PROGRESS, PENDING, CLOSED
    }

    public enum CaseType {
        CAR_DEPRECIATION, CIVIL, CRIMINAL, FAMILY, CORPORATE, REAL_ESTATE, INTELLECTUAL_PROPERTY, OTHER
    }

    // Manual getters and setters to avoid Lombok compilation issues
    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CaseStatus getStatus() {
        return status;
    }

    public void setStatus(CaseStatus status) {
        this.status = status;
    }

    public CaseType getType() {
        return type;
    }

    public void setType(CaseType type) {
        this.type = type;
    }

    public LocalDate getFilingDate() {
        return filingDate;
    }

    public void setFilingDate(LocalDate filingDate) {
        this.filingDate = filingDate;
    }

    public User getAssignedUser() {
        return assignedUser;
    }

    public void setAssignedUser(User assignedUser) {
        this.assignedUser = assignedUser;
    }

    public User getClient() {
        return client;
    }

    public void setClient(User client) {
        this.client = client;
    }
}
