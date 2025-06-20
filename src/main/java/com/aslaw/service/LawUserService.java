package com.aslaw.service;

import com.aslaw.dto.LawUserDTO;
import com.aslaw.entity.LawRole;
import com.aslaw.entity.LawUser;
import com.aslaw.repository.LawRoleRepository;
import com.aslaw.repository.LawUserRepository;
import com.infracore.entity.Role;
import com.infracore.entity.User;
import com.infracore.repository.RoleRepository;
import com.infracore.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LawUserService {

    private final LawUserRepository lawUserRepository;
    private final LawRoleRepository lawRoleRepository;
    private final UserService userService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<LawUserDTO> getAllLawUsers() {
        return lawUserRepository.findAll().stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LawUserDTO> getActiveLawyers() {
        return lawUserRepository.findActiveLawyers().stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LawUserDTO> getActiveClerks() {
        return lawUserRepository.findActiveClerks().stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LawUserDTO> getActiveLegalStaff() {
        return lawUserRepository.findActiveLegalStaff().stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<LawUserDTO> getLawUserById(Long id) {
        return lawUserRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Optional<LawUserDTO> getLawUserByUsername(String username) {
        return lawUserRepository.findByUsername(username)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Optional<LawUser> findByUsername(String username) {
        return lawUserRepository.findByUsername(username);
    }

    @Transactional
    public LawUserDTO createLawUser(CreateLawUserRequest request) {
        // Base User oluştur
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        user.setEnabled(true);
        user.setActive(true);

        // Base role ekle (MANAGER veya EMPLOYEE)
        Role baseRole = getBaseRoleForLawRole(request.getLawRole());
        user.addRole(baseRole);

        User savedUser = userService.createUser(user);

        // LawUser oluştur
        LawUser lawUser = new LawUser();
        lawUser.setUser(savedUser);
        lawUser.setBarNumber(request.getBarNumber());
        lawUser.setSpecialization(request.getSpecialization());
        lawUser.setExperienceYears(request.getExperienceYears());
        lawUser.setLawSchool(request.getLawSchool());
        lawUser.setGraduationYear(request.getGraduationYear());
        lawUser.setActive(true);

        // Law role ekle
        Optional<LawRole> lawRole = lawRoleRepository.findByName(LawRole.LawRoleName.valueOf(request.getLawRole()));
        lawRole.ifPresent(lawUser::addLawRole);

        LawUser savedLawUser = lawUserRepository.save(lawUser);
        return convertToDTO(savedLawUser);
    }

    @Transactional
    public Optional<LawUserDTO> updateLawUser(Long id, UpdateLawUserRequest request) {
        return lawUserRepository.findById(id)
                .map(existingLawUser -> {
                    User user = existingLawUser.getUser();
                    if (user != null) {
                        user.setUsername(request.getUsername());
                        user.setEmail(request.getEmail());
                        user.setFirstName(request.getFirstName());
                        user.setLastName(request.getLastName());
                        user.setPhoneNumber(request.getPhoneNumber());
                        user.setAddress(request.getAddress());
                        
                        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                            user.setPassword(passwordEncoder.encode(request.getPassword()));
                        }
                        
                        userService.saveUser(user);
                    }

                    existingLawUser.setBarNumber(request.getBarNumber());
                    existingLawUser.setSpecialization(request.getSpecialization());
                    existingLawUser.setExperienceYears(request.getExperienceYears());
                    existingLawUser.setLawSchool(request.getLawSchool());
                    existingLawUser.setGraduationYear(request.getGraduationYear());

                    // Law role güncelle
                    if (request.getLawRole() != null) {
                        existingLawUser.getLawRoles().clear();
                        Optional<LawRole> lawRole = lawRoleRepository.findByName(LawRole.LawRoleName.valueOf(request.getLawRole()));
                        lawRole.ifPresent(existingLawUser::addLawRole);
                    }

                    return convertToDTO(lawUserRepository.save(existingLawUser));
                });
    }

    @Transactional
    public boolean deactivateLawUser(Long id) {
        return lawUserRepository.findById(id)
                .map(lawUser -> {
                    lawUser.setActive(false);
                    if (lawUser.getUser() != null) {
                        lawUser.getUser().setEnabled(false);
                        userService.saveUser(lawUser.getUser());
                    }
                    lawUserRepository.save(lawUser);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public boolean deleteLawUser(Long id) {
        if (lawUserRepository.existsById(id)) {
            lawUserRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private Role getBaseRoleForLawRole(String lawRoleName) {
        // LAWYER, PARTNER -> MANAGER
        // CLERK, PARALEGAL, INTERN, LEGAL_ASSISTANT -> EMPLOYEE
        if ("LAWYER".equals(lawRoleName) || "PARTNER".equals(lawRoleName)) {
            return getBaseRole(Role.RoleName.MANAGER);
        } else {
            return getBaseRole(Role.RoleName.EMPLOYEE);
        }
    }

    private Role getBaseRole(Role.RoleName roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Base role not found: " + roleName));
    }

    private LawUserDTO convertToDTO(LawUser lawUser) {
        LawUserDTO dto = new LawUserDTO();
        dto.setId(lawUser.getId());
        dto.setBarNumber(lawUser.getBarNumber());
        dto.setSpecialization(lawUser.getSpecialization());
        dto.setExperienceYears(lawUser.getExperienceYears());
        dto.setLawSchool(lawUser.getLawSchool());
        dto.setGraduationYear(lawUser.getGraduationYear());
        dto.setActive(lawUser.getActive());
        dto.setCreatedDate(lawUser.getCreatedDate());
        dto.setUpdatedDate(lawUser.getUpdatedDate());

        if (lawUser.getUser() != null) {
            User user = lawUser.getUser();
            dto.setUserId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
            dto.setFirstName(user.getFirstName());
            dto.setLastName(user.getLastName());
            dto.setPhoneNumber(user.getPhoneNumber());
            dto.setAddress(user.getAddress());
            dto.setEnabled(user.isEnabled());
            dto.setUserActive(user.isActive());
        }

        if (lawUser.getLawRoles() != null) {
            Set<String> lawRoleNames = lawUser.getLawRoles().stream()
                    .map(role -> role.getName().name())
                    .collect(java.util.stream.Collectors.toSet());
            dto.setLawRoles(lawRoleNames);
        }

        return dto;
    }

    // Request DTOs
    public static class CreateLawUserRequest {
        private String username;
        private String password;
        private String email;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private String address;
        private String lawRole;
        private String barNumber;
        private String specialization;
        private Integer experienceYears;
        private String lawSchool;
        private Integer graduationYear;

        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        
        public String getLawRole() { return lawRole; }
        public void setLawRole(String lawRole) { this.lawRole = lawRole; }
        
        public String getBarNumber() { return barNumber; }
        public void setBarNumber(String barNumber) { this.barNumber = barNumber; }
        
        public String getSpecialization() { return specialization; }
        public void setSpecialization(String specialization) { this.specialization = specialization; }
        
        public Integer getExperienceYears() { return experienceYears; }
        public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }
        
        public String getLawSchool() { return lawSchool; }
        public void setLawSchool(String lawSchool) { this.lawSchool = lawSchool; }
        
        public Integer getGraduationYear() { return graduationYear; }
        public void setGraduationYear(Integer graduationYear) { this.graduationYear = graduationYear; }
    }

    public static class UpdateLawUserRequest {
        private String username;
        private String password;
        private String email;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private String address;
        private String lawRole;
        private String barNumber;
        private String specialization;
        private Integer experienceYears;
        private String lawSchool;
        private Integer graduationYear;

        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        
        public String getLawRole() { return lawRole; }
        public void setLawRole(String lawRole) { this.lawRole = lawRole; }
        
        public String getBarNumber() { return barNumber; }
        public void setBarNumber(String barNumber) { this.barNumber = barNumber; }
        
        public String getSpecialization() { return specialization; }
        public void setSpecialization(String specialization) { this.specialization = specialization; }
        
        public Integer getExperienceYears() { return experienceYears; }
        public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }
        
        public String getLawSchool() { return lawSchool; }
        public void setLawSchool(String lawSchool) { this.lawSchool = lawSchool; }
        
        public Integer getGraduationYear() { return graduationYear; }
        public void setGraduationYear(Integer graduationYear) { this.graduationYear = graduationYear; }
    }
} 