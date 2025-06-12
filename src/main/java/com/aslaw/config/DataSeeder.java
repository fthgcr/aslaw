package com.aslaw.config;

import com.aslaw.entity.Case;
import com.aslaw.entity.Document;
import com.aslaw.repository.CaseRepository;
import com.aslaw.repository.DocumentRepository;
import com.infracore.entity.Role;
import com.infracore.entity.User;
import com.infracore.repository.RoleRepository;
import com.infracore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

// @Component  // Mock data seeder disabled - uncomment to enable test data
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedRoles();
        seedUsers();
        seedCases();
        seedDocuments();
    }

    private void seedRoles() {
        for (Role.RoleName roleName : Role.RoleName.values()) {
            if (!roleRepository.findByName(roleName).isPresent()) {
                Role role = new Role();
                role.setName(roleName);
                role.setDescription(roleName.name() + " role");
                role.setIsActive(true);
                roleRepository.save(role);
                System.out.println("Created role: " + roleName);
            }
        }
    }

    private void seedUsers() {
        // Create admin user
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@aslaw.com");
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setEnabled(true);
            admin.setActive(true);

            Optional<Role> adminRole = roleRepository.findByName(Role.RoleName.ADMIN);
            if (adminRole.isPresent()) {
                admin.addRole(adminRole.get());
            }

            userRepository.save(admin);
            System.out.println("Created admin user");
        }

        // Create test clients (users with USER role)
        String[] clientNames = {
                "Ahmet,Yılmaz,ahmet.yilmaz@email.com",
                "Fatma,Kaya,fatma.kaya@email.com",
                "Mehmet,Demir,mehmet.demir@email.com",
                "Ayşe,Şahin,ayse.sahin@email.com",
                "Mustafa,Çelik,mustafa.celik@email.com",
                "Zeynep,Öztürk,zeynep.ozturk@email.com",
                "Ali,Korkmaz,ali.korkmaz@email.com",
                "Emine,Aksoy,emine.aksoy@email.com"
        };

        Optional<Role> userRole = roleRepository.findByName(Role.RoleName.USER);
        
        for (int i = 0; i < clientNames.length; i++) {
            String[] parts = clientNames[i].split(",");
            String username = parts[0].toLowerCase() + "." + parts[1].toLowerCase();
            
            if (!userRepository.existsByUsername(username)) {
                User client = new User();
                client.setUsername(username);
                client.setPassword(passwordEncoder.encode("password123"));
                client.setEmail(parts[2]);
                client.setFirstName(parts[0]);
                client.setLastName(parts[1]);
                client.setEnabled(true);
                client.setActive(true);

                // Set creation dates - some this month, some last month
                if (i < 4) {
                    // This month's clients
                    client.setCreatedDate(LocalDateTime.now().minusDays(i * 3));
                } else {
                    // Last month's clients
                    client.setCreatedDate(LocalDateTime.now().minusMonths(1).minusDays(i * 2));
                }

                if (userRole.isPresent()) {
                    client.addRole(userRole.get());
                }

                userRepository.save(client);
                System.out.println("Created client: " + username);
            }
        }
    }

    private void seedCases() {
        if (caseRepository.count() < 5) {
            User admin = userRepository.findByUsername("admin").orElse(null);
            
            String[] caseTitles = {
                    "İş Mahkemesi Davası",
                    "Boşanma Davası",
                    "Trafik Kazası Tazminat Davası",
                    "Miras Davası",
                    "Kira Sözleşmesi Uyuşmazlığı"
            };

            Case.CaseStatus[] statuses = {
                    Case.CaseStatus.OPEN,
                    Case.CaseStatus.IN_PROGRESS,
                    Case.CaseStatus.PENDING,
                    Case.CaseStatus.CLOSED,
                    Case.CaseStatus.IN_PROGRESS
            };

            Case.CaseType[] types = {
                    Case.CaseType.CIVIL,
                    Case.CaseType.FAMILY,
                    Case.CaseType.CIVIL,
                    Case.CaseType.FAMILY,
                    Case.CaseType.CIVIL
            };

            for (int i = 0; i < caseTitles.length; i++) {
                Case legalCase = new Case();
                legalCase.setCaseNumber("CASE-2024-" + String.format("%03d", i + 1));
                legalCase.setTitle(caseTitles[i]);
                legalCase.setDescription("Test case description for " + caseTitles[i]);
                legalCase.setStatus(statuses[i]);
                legalCase.setType(types[i]);
                legalCase.setFilingDate(LocalDate.now().minusDays(i * 10));
                
                if (admin != null) {
                    legalCase.setAssignedUser(admin);
                }

                caseRepository.save(legalCase);
                System.out.println("Created case: " + legalCase.getCaseNumber());
            }
        }
    }

    private void seedDocuments() {
        if (documentRepository.count() < 3) {
            Case firstCase = caseRepository.findAll().stream().findFirst().orElse(null);
            
            if (firstCase != null) {
                String[] docTitles = {
                        "Dava Dilekçesi",
                        "İş Sözleşmesi",
                        "Tanık İfadesi"
                };

                Document.DocumentType[] types = {
                        Document.DocumentType.COMPLAINT,
                        Document.DocumentType.CONTRACT,
                        Document.DocumentType.OTHER
                };

                for (int i = 0; i < docTitles.length; i++) {
                    Document document = new Document();
                    document.setTitle(docTitles[i]);
                    document.setFileName(docTitles[i].toLowerCase().replace(" ", "_") + ".pdf");
                    document.setFilePath("/uploads/" + document.getFileName());
                    document.setContentType("application/pdf");
                    document.setFileSize(1024 * (i + 1));
                    document.setDescription("Test document: " + docTitles[i]);
                    document.setLegalCase(firstCase);
                    document.setType(types[i]);

                    documentRepository.save(document);
                    System.out.println("Created document: " + document.getTitle());
                }
            }
        }
    }
} 