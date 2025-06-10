package com.aslaw.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.infracore.entity.BaseEntity;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    @JsonIgnore
    private com.infracore.entity.User assignedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    @JsonIgnore
    private Client client;

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

    public com.infracore.entity.User getAssignedUser() {
        return assignedUser;
    }

    public void setAssignedUser(com.infracore.entity.User assignedUser) {
        this.assignedUser = assignedUser;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
