package com.aslaw.entity;

import com.infracore.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "documents")
@Getter
@Setter
public class Document extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private long fileSize;

    @Column(length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id")
    private Case legalCase;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DocumentType type;

    public enum DocumentType {
        COMPLAINT, ANSWER, MOTION, EXHIBIT, CONTRACT, CORRESPONDENCE, OTHER
    }
}
