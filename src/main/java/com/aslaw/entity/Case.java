package com.aslaw.entity;

import com.infracore.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "cases")
@Getter
@Setter
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
    private com.infracore.entity.User assignedUser;

    public enum CaseStatus {
        OPEN, IN_PROGRESS, PENDING, CLOSED
    }

    public enum CaseType {
        CIVIL, CRIMINAL, FAMILY, CORPORATE, REAL_ESTATE, INTELLECTUAL_PROPERTY, OTHER
    }
}
