package com.aslaw.repository;

import com.aslaw.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    /**
     * Find all documents with case details, ordered by creation date descending (newest first)
     */
    @Query("SELECT d FROM Document d LEFT JOIN FETCH d.legalCase ORDER BY d.createdDate DESC")
    List<Document> findAllWithCaseDetails();
    
    /**
     * Find documents by case ID
     */
    @Query("SELECT d FROM Document d LEFT JOIN FETCH d.legalCase WHERE d.legalCase.id = :caseId ORDER BY d.createdDate DESC")
    List<Document> findByCaseId(@Param("caseId") Long caseId);
    
    /**
     * Find documents by type
     */
    @Query("SELECT d FROM Document d LEFT JOIN FETCH d.legalCase WHERE d.type = :type ORDER BY d.createdDate DESC")
    List<Document> findByType(@Param("type") Document.DocumentType type);
    
    /**
     * Find documents by title containing (case insensitive)
     */
    @Query("SELECT d FROM Document d LEFT JOIN FETCH d.legalCase WHERE LOWER(d.title) LIKE LOWER(CONCAT('%', :title, '%')) ORDER BY d.createdDate DESC")
    List<Document> findByTitleContainingIgnoreCase(@Param("title") String title);
    
    /**
     * Find documents by file name containing (case insensitive)
     */
    @Query("SELECT d FROM Document d LEFT JOIN FETCH d.legalCase WHERE LOWER(d.fileName) LIKE LOWER(CONCAT('%', :fileName, '%')) ORDER BY d.createdDate DESC")
    List<Document> findByFileNameContainingIgnoreCase(@Param("fileName") String fileName);
    
    /**
     * Find document by ID with case details
     */
    @Query("SELECT d FROM Document d LEFT JOIN FETCH d.legalCase WHERE d.id = :id")
    Optional<Document> findByIdWithCaseDetails(@Param("id") Long id);
    
    /**
     * Find documents by client ID (from all client's cases)
     */
    @Query("SELECT d FROM Document d LEFT JOIN FETCH d.legalCase c WHERE c.client.id = :clientId ORDER BY d.createdDate DESC")
    List<Document> findByClientId(@Param("clientId") Long clientId);
}
