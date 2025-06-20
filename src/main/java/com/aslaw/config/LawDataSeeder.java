package com.aslaw.config;

import com.aslaw.entity.LawRole;
import com.aslaw.repository.LawRoleRepository;
import com.infracore.entity.Role;
import com.infracore.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Order(2) // DataSeeder çalıştıktan sonra çalışsın
public class LawDataSeeder implements CommandLineRunner {

    @Autowired
    private LawRoleRepository lawRoleRepository;
    
    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        seedLawRoles();
    }

    private void seedLawRoles() {
        // LAWYER -> MANAGER base role
        createLawRoleIfNotExists(LawRole.LawRoleName.LAWYER, Role.RoleName.MANAGER, "Avukat - Hukuk bürosunda avukat olarak çalışan personel");
        
        // CLERK -> EMPLOYEE base role  
        createLawRoleIfNotExists(LawRole.LawRoleName.CLERK, Role.RoleName.EMPLOYEE, "Katip - Hukuk bürosunda katip olarak çalışan personel");
        
        // PARALEGAL -> EMPLOYEE base role
        createLawRoleIfNotExists(LawRole.LawRoleName.PARALEGAL, Role.RoleName.EMPLOYEE, "Paralegal - Hukuk asistanı");
        
        // PARTNER -> MANAGER base role
        createLawRoleIfNotExists(LawRole.LawRoleName.PARTNER, Role.RoleName.MANAGER, "Partner - Hukuk bürosu ortağı");
        
        // INTERN -> EMPLOYEE base role
        createLawRoleIfNotExists(LawRole.LawRoleName.INTERN, Role.RoleName.EMPLOYEE, "Stajyer - Hukuk bürosunda stajyer");
        
        // LEGAL_ASSISTANT -> EMPLOYEE base role
        createLawRoleIfNotExists(LawRole.LawRoleName.LEGAL_ASSISTANT, Role.RoleName.EMPLOYEE, "Hukuk Asistanı - Hukuki işlerde yardımcı personel");
    }

    private void createLawRoleIfNotExists(LawRole.LawRoleName lawRoleName, Role.RoleName baseRoleName, String description) {
        if (!lawRoleRepository.findByName(lawRoleName).isPresent()) {
            // Base role'ü bul
            Optional<Role> baseRole = roleRepository.findByName(baseRoleName);
            
            if (baseRole.isPresent()) {
                LawRole lawRole = new LawRole();
                lawRole.setName(lawRoleName);
                lawRole.setDescription(description);
                lawRole.setBaseRole(baseRole.get());
                lawRole.setActive(true);
                
                lawRoleRepository.save(lawRole);
                System.out.println("Created law role: " + lawRoleName + " (base: " + baseRoleName + ")");
            } else {
                System.err.println("Base role not found: " + baseRoleName + " for law role: " + lawRoleName);
            }
        }
    }
} 