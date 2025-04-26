package com.aslaw.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "cases")
@Getter
@Setter
public class Case {

    @Column(nullable = false)
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

    public enum CaseStatus {
        OPEN, IN_PROGRESS, PENDING, CLOSED
    }

    public enum CaseType {
        CIVIL, CRIMINAL, FAMILY, CORPORATE, REAL_ESTATE, INTELLECTUAL_PROPERTY, OTHER
    }
}
