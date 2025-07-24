package com.aslaw.config;

import com.infracore.entity.Role;
import com.infracore.entity.User;
import com.aslaw.entity.LawRole;
import com.infracore.repository.RoleRepository;
import com.infracore.repository.UserRepository;
import com.aslaw.repository.LawRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.annotation.Order;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@Order(1) // LawDataSeeder'dan önce çalışsın
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private LawRoleRepository lawRoleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initializeBaseRoles();
        initializeLawRoles();
        ensureAdminUser();
    }

    private void initializeBaseRoles() {
        createRoleIfNotExists(Role.RoleName.MANAGER, "Manager role with administrative privileges");
        createRoleIfNotExists(Role.RoleName.EMPLOYEE, "Standard employee role");
        createRoleIfNotExists(Role.RoleName.ADMIN, "System administrator role");
    }

    private void initializeLawRoles() {
        Optional<Role> managerRole = roleRepository.findByName(Role.RoleName.MANAGER);
        Optional<Role> employeeRole = roleRepository.findByName(Role.RoleName.EMPLOYEE);

        if (managerRole.isPresent() && employeeRole.isPresent()) {
            createLawRoleIfNotExists(LawRole.LawRoleName.LAWYER, "Licensed attorney", managerRole.get());
            createLawRoleIfNotExists(LawRole.LawRoleName.PARTNER, "Law firm partner", managerRole.get());
            createLawRoleIfNotExists(LawRole.LawRoleName.CLERK, "Legal clerk", employeeRole.get());
            createLawRoleIfNotExists(LawRole.LawRoleName.PARALEGAL, "Paralegal assistant", employeeRole.get());
            createLawRoleIfNotExists(LawRole.LawRoleName.INTERN, "Legal intern", employeeRole.get());
            createLawRoleIfNotExists(LawRole.LawRoleName.LEGAL_ASSISTANT, "Legal assistant", employeeRole.get());
        }
    }
    
    private void ensureAdminUser() {
        Optional<User> adminUser = userRepository.findByUsername("admin");
        
        if (adminUser.isPresent()) {
            // Admin kullanıcısı varsa şifresini güncelle
            User admin = adminUser.get();
            String encodedPassword = passwordEncoder.encode("123123");
            admin.setPassword(encodedPassword);
            userRepository.save(admin);
            System.out.println("DataInitializer: Updated admin password with new encoding");
            System.out.println("DataInitializer: New encoded password: " + encodedPassword);
        } else {
            // Admin kullanıcısı yoksa oluştur
            createAdminUser();
        }
    }
    
    private void createAdminUser() {
        Optional<Role> adminRole = roleRepository.findByName(Role.RoleName.ADMIN);
        if (adminRole.isPresent()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123123"));
            admin.setEmail("admin@lexofis.com");
            admin.setFirstName("System");
            admin.setLastName("Admin");
            admin.setEnabled(true);
            admin.setActive(true);
            admin.setCreatedDate(LocalDateTime.now());
            admin.setUpdatedDate(LocalDateTime.now());
            admin.addRole(adminRole.get());
            
            userRepository.save(admin);
            System.out.println("DataInitializer: Created admin user with encoded password");
        }
    }

    private void createRoleIfNotExists(Role.RoleName name, String description) {
        if (!roleRepository.findByName(name).isPresent()) {
            Role role = new Role();
            role.setName(name);
            role.setDescription(description);
            roleRepository.save(role);
            System.out.println("Created base role: " + name);
        }
    }

    private void createLawRoleIfNotExists(LawRole.LawRoleName name, String description, Role baseRole) {
        if (!lawRoleRepository.findByName(name).isPresent()) {
            LawRole lawRole = new LawRole();
            lawRole.setName(name);
            lawRole.setDescription(description);
            lawRole.setBaseRole(baseRole);
            lawRole.setActive(true);
            lawRoleRepository.save(lawRole);
            System.out.println("Created law role: " + name + " with base role: " + baseRole.getName());
        }
    }
} 