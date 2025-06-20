package com.aslaw.entity;

import com.infracore.entity.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "law_roles")
@NoArgsConstructor
@AllArgsConstructor
public class LawRole {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", unique = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private LawRoleName name;
    
    @Column(name = "description")
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_role_id")
    private Role baseRole; // Temel rolü referans et (MANAGER, EMPLOYEE, etc.)
    
    @Column(name = "created_date")
    private LocalDateTime createdDate;
    
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
    
    public enum LawRoleName {
        LAWYER,          // Avukat - MANAGER base role ile
        CLERK,           // Katip - EMPLOYEE base role ile
        PARALEGAL,       // Hukuk danışmanı - EMPLOYEE base role ile
        PARTNER,         // Ortak - MANAGER base role ile
        INTERN,          // Stajyer - EMPLOYEE base role ile
        LEGAL_ASSISTANT  // Hukuk asistanı - EMPLOYEE base role ile
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LawRoleName getName() {
        return name;
    }

    public void setName(LawRoleName name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Role getBaseRole() {
        return baseRole;
    }

    public void setBaseRole(Role baseRole) {
        this.baseRole = baseRole;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }
}