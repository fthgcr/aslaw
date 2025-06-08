package com.aslaw_backend;

import com.aslaw.entity.Case;
import com.aslaw.entity.Document;
import com.aslaw.repository.CaseRepository;
import com.aslaw.repository.DocumentRepository;
import com.infracore.entity.User;
import com.infracore.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class DatabaseSchemaTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Test
    public void testEntityCreation() {
        // Test User entity (from infra-core)
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        
        User savedUser = userRepository.save(user);
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("testuser");

        // Test Case entity
        Case legalCase = new Case();
        legalCase.setCaseNumber("CASE-001");
        legalCase.setTitle("Test Case");
        legalCase.setDescription("Test Description");
        legalCase.setStatus(Case.CaseStatus.OPEN);
        legalCase.setType(Case.CaseType.CIVIL);
        legalCase.setFilingDate(LocalDate.now());
        legalCase.setAssignedUser(savedUser);
        
        Case savedCase = caseRepository.save(legalCase);
        assertThat(savedCase.getId()).isNotNull();
        assertThat(savedCase.getCaseNumber()).isEqualTo("CASE-001");

        // Test Document entity
        Document document = new Document();
        document.setTitle("Test Document");
        document.setFileName("test.pdf");
        document.setContentType("application/pdf");
        document.setFileSize(1024);
        document.setDescription("Test document description");
        document.setLegalCase(savedCase);
        document.setType(Document.DocumentType.COMPLAINT);
        
        Document savedDocument = documentRepository.save(document);
        assertThat(savedDocument.getId()).isNotNull();
        assertThat(savedDocument.getTitle()).isEqualTo("Test Document");

        // Test relationships
        assertThat(savedDocument.getLegalCase().getId()).isEqualTo(savedCase.getId());
        assertThat(savedCase.getAssignedUser().getId()).isEqualTo(savedUser.getId());
    }

    @Test
    public void testRepositoryQueries() {
        // Create test data
        User user = new User();
        user.setUsername("queryuser");
        user.setPassword("password");
        user.setEmail("query@example.com");
        user.setFirstName("Query");
        user.setLastName("User");
        user = userRepository.save(user);

        Case legalCase = new Case();
        legalCase.setCaseNumber("QUERY-001");
        legalCase.setTitle("Query Test Case");
        legalCase.setStatus(Case.CaseStatus.IN_PROGRESS);
        legalCase.setType(Case.CaseType.CRIMINAL);
        legalCase.setFilingDate(LocalDate.now());
        legalCase.setAssignedUser(user);
        legalCase = caseRepository.save(legalCase);

        // Test repository queries
        assertThat(caseRepository.findByAssignedUser_Id(user.getId())).hasSize(1);
        assertThat(caseRepository.findByStatus(Case.CaseStatus.IN_PROGRESS)).hasSize(1);
        assertThat(caseRepository.existsByCaseNumber("QUERY-001")).isTrue();
        assertThat(userRepository.findByUsername("queryuser")).isPresent();
    }
} 