package com.aslaw.repository;

import com.aslaw.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByLegalCase_Id(Long caseId);
    List<Document> findByType(Document.DocumentType type);
}
