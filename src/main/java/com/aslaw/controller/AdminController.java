package com.aslaw.controller;

import com.infracore.dto.RegistrationRequest;
import com.infracore.dto.UserDTO;
import com.infracore.entity.Role;
import com.infracore.entity.User;
import com.infracore.repository.RoleRepository;
import com.infracore.service.UserService;
import com.aslaw.service.LawUserService;
import com.aslaw.service.ClientService;
import com.aslaw.repository.LawUserRepository;
import com.aslaw.dto.LawUserDTO;
import com.aslaw.entity.LawUser;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminController {

    private final UserService userService;
    private final LawUserService lawUserService;
    private final LawUserRepository lawUserRepository;
    private final ClientService clientService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor otomatik olarak @RequiredArgsConstructor tarafından oluşturulduğu için manuel constructor kaldırıldı

    /**
     * Get all users (only for admin)
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        try {
            List<UserDTO> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get lawyers only (users with LAWYER role)
     */
    @GetMapping("/lawyers")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<List<UserDTO>> getAllLawyers() {
        try {
            // Sadece aktif avukatları getir
            List<LawUserDTO> activeLawyers = lawUserService.getActiveLawyers();
            System.out.println("Active lawyers found: " + activeLawyers.size());
            
            // LawUserDTO'ları UserDTO'ya dönüştür
            List<UserDTO> userDTOs = activeLawyers.stream()
                    .map(this::convertLawUserToUserDTO)
                    .toList();
            
            return ResponseEntity.ok(userDTOs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get active lawyers only
     */
    @GetMapping("/lawyers/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<List<LawUserDTO>> getActiveLawyers() {
        try {
            List<LawUserDTO> activeLawyers = lawUserService.getActiveLawyers();
            return ResponseEntity.ok(activeLawyers);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get clerks only (users with CLERK role)
     */
    @GetMapping("/clerks")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<List<UserDTO>> getAllClerks() {
        try {
            List<LawUserDTO> activeClerks = lawUserService.getActiveClerks();
            System.out.println("Active clerks found: " + activeClerks.size());
            
            // LawUserDTO'ları UserDTO'ya dönüştür
            List<UserDTO> userDTOs = activeClerks.stream()
                    .map(this::convertLawUserToUserDTO)
                    .toList();
            
            return ResponseEntity.ok(userDTOs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get active clerks only
     */
    @GetMapping("/clerks/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<List<LawUserDTO>> getActiveClerks() {
        try {
            List<LawUserDTO> activeClerks = lawUserService.getActiveClerks();
            return ResponseEntity.ok(activeClerks);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all legal staff (lawyers and clerks)
     */
    @GetMapping("/legal-staff")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<List<UserDTO>> getAllLegalStaff() {
        try {
            List<LawUserDTO> activeLegalStaff = lawUserService.getActiveLegalStaff();
            System.out.println("Active legal staff found: " + activeLegalStaff.size());
            
            // LawUserDTO'ları UserDTO'ya dönüştür
            List<UserDTO> userDTOs = activeLegalStaff.stream()
                    .map(this::convertLawUserToUserDTO)
                    .toList();
            
            return ResponseEntity.ok(userDTOs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get active legal staff only
     */
    @GetMapping("/legal-staff/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<List<LawUserDTO>> getActiveLegalStaff() {
        try {
            List<LawUserDTO> activeLegalStaff = lawUserService.getActiveLegalStaff();
            return ResponseEntity.ok(activeLegalStaff);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all clients (users with CLIENT role)
     */
    @GetMapping("/clients")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<List<UserDTO>> getAllClients() {
        try {
            List<UserDTO> clients = userService.getAllClients();
            return ResponseEntity.ok(clients);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get active clients only
     */
    @GetMapping("/clients/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<List<UserDTO>> getActiveClients() {
        try {
            List<UserDTO> activeClients = userService.getActiveClients();
            return ResponseEntity.ok(activeClients);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all admins
     */
    @GetMapping("/admins")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllAdmins() {
        try {
            List<UserDTO> admins = userService.getAllAdmins();
            return ResponseEntity.ok(admins);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get users by specific role
     */
    @GetMapping("/users/role/{roleName}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<List<UserDTO>> getUsersByRole(@PathVariable String roleName) {
        try {
            Role.RoleName role = Role.RoleName.valueOf(roleName.toUpperCase());
            List<UserDTO> users = userService.getUsersByRole(role);
            return ResponseEntity.ok(users);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(List.of()); // Return empty list for invalid role
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get count of users by role
     */
    @GetMapping("/users/count/role/{roleName}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<?> getUserCountByRole(@PathVariable String roleName) {
        try {
            Role.RoleName role = Role.RoleName.valueOf(roleName.toUpperCase());
            Long count = userService.countUsersByRole(role);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid role name", "count", 0L));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create new user (lawyer, client, etc.)
     */
    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest request) {
        try {
            // Check if username already exists
            if (userService.existsByUsername(request.getUsername())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Username already exists"));
            }

            // Check if email already exists
            if (userService.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email already exists"));
            }

            // Check if this is a law role (LAWYER, CLERK, etc.)
            if (isLawRole(request.getRole())) {
                return createLawUserFromRequest(request);
            }

            // Create regular user with base roles
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
            user.setCreatedDate(LocalDateTime.now());

            // Assign role
            Role role = roleRepository.findByName(Role.RoleName.valueOf(request.getRole()))
                    .orElseThrow(() -> new RuntimeException("Role not found: " + request.getRole()));
            user.addRole(role);

            User savedUser = userService.createUser(user);
            UserDTO userDTO = UserDTO.fromEntity(savedUser);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "User created successfully",
                    "user", userDTO
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create user: " + e.getMessage()));
        }
    }

    private boolean isLawRole(String role) {
        return "LAWYER".equals(role) || "CLERK".equals(role) || "PARTNER".equals(role) || 
               "PARALEGAL".equals(role) || "INTERN".equals(role) || "LEGAL_ASSISTANT".equals(role);
    }

    private ResponseEntity<?> createLawUserFromRequest(CreateUserRequest request) {
        try {
            // Convert to LawUserService.CreateLawUserRequest
            LawUserService.CreateLawUserRequest lawUserRequest = new LawUserService.CreateLawUserRequest();
            lawUserRequest.setUsername(request.getUsername());
            lawUserRequest.setPassword(request.getPassword());
            lawUserRequest.setEmail(request.getEmail());
            lawUserRequest.setFirstName(request.getFirstName());
            lawUserRequest.setLastName(request.getLastName());
            lawUserRequest.setPhoneNumber(request.getPhoneNumber());
            lawUserRequest.setAddress(request.getAddress());
            lawUserRequest.setLawRole(request.getRole());
            
            // Default values for law-specific fields
            lawUserRequest.setBarNumber(null);
            lawUserRequest.setSpecialization("Genel Hukuk");
            lawUserRequest.setExperienceYears(0);
            lawUserRequest.setLawSchool("");
            lawUserRequest.setGraduationYear(null);

            LawUserDTO lawUser = lawUserService.createLawUser(lawUserRequest);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Law user created successfully",
                    "user", convertLawUserToUserDTO(lawUser)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create law user: " + e.getMessage()));
        }
    }

    private UserDTO convertLawUserToUserDTO(LawUserDTO lawUser) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(lawUser.getUserId());
        userDTO.setUsername(lawUser.getUsername());
        userDTO.setEmail(lawUser.getEmail());
        userDTO.setFirstName(lawUser.getFirstName());
        userDTO.setLastName(lawUser.getLastName());
        userDTO.setPhoneNumber(lawUser.getPhoneNumber());
        userDTO.setAddress(lawUser.getAddress());
        userDTO.setEnabled(lawUser.isEnabled());
        userDTO.setActive(lawUser.isUserActive());
        userDTO.setCreatedDate(lawUser.getCreatedDate());
        userDTO.setUpdatedDate(lawUser.getUpdatedDate());
        userDTO.setRoles(lawUser.getLawRoles());
        return userDTO;
    }

    /**
     * Create law user (lawyer, clerk, etc.)
     */
    @PostMapping("/law-users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createLawUser(@Valid @RequestBody LawUserService.CreateLawUserRequest request) {
        try {
            // Check if username already exists
            if (userService.existsByUsername(request.getUsername())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Username already exists"));
            }

            // Check if email already exists
            if (userService.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email already exists"));
            }

            // Check if bar number already exists (for lawyers)
            if (request.getBarNumber() != null && !request.getBarNumber().isEmpty()) {
                if (lawUserRepository.existsByBarNumber(request.getBarNumber())) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Bar number already exists"));
                }
            }

            LawUserDTO lawUser = lawUserService.createLawUser(request);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Law user created successfully",
                    "lawUser", lawUser
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create law user: " + e.getMessage()));
        }
    }

    /**
     * Update user
     */
    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        try {
            // Check if this is a law user by role or by checking if LawUser exists
            boolean isLawUser = (request.getRole() != null && isLawRole(request.getRole())) || 
                               lawUserRepository.findByUserId(id).isPresent();
            
            if (isLawUser) {
                return updateLawUser(id, request);
            }

            // Update regular user
            User existingUser = userService.findByIdEntity(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Update basic fields
            existingUser.setUsername(request.getUsername());
            existingUser.setEmail(request.getEmail());
            existingUser.setFirstName(request.getFirstName());
            existingUser.setLastName(request.getLastName());
            existingUser.setPhoneNumber(request.getPhoneNumber());
            existingUser.setAddress(request.getAddress());
            existingUser.setUpdatedDate(LocalDateTime.now());

            // Update password only if provided
            if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
                System.out.println("AdminController: Updating password for user: " + existingUser.getUsername());
                System.out.println("AdminController: New password received: " + request.getPassword());
                existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
                System.out.println("AdminController: Password encoded and set successfully");
            } else {
                System.out.println("AdminController: No password provided for update, skipping password change");
            }

            // Update role if provided
            if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
                existingUser.getRoles().clear();
                Role role = roleRepository.findByName(Role.RoleName.valueOf(request.getRole()))
                        .orElseThrow(() -> new RuntimeException("Role not found: " + request.getRole()));
                existingUser.addRole(role);
            }

            User savedUser = userService.saveUser(existingUser);
            UserDTO userDTO = UserDTO.fromEntity(savedUser);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "User updated successfully",
                    "user", userDTO
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update user: " + e.getMessage()));
        }
    }

    private ResponseEntity<?> updateLawUser(Long userId, UpdateUserRequest request) {
        try {
            // Find existing law user
            LawUser existingLawUser = lawUserRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Law user not found"));

            // Convert UpdateUserRequest to LawUserService.UpdateLawUserRequest
            LawUserService.UpdateLawUserRequest lawUserRequest = new LawUserService.UpdateLawUserRequest();
            lawUserRequest.setUsername(request.getUsername());
            lawUserRequest.setPassword(request.getPassword());
            lawUserRequest.setEmail(request.getEmail());
            lawUserRequest.setFirstName(request.getFirstName());
            lawUserRequest.setLastName(request.getLastName());
            lawUserRequest.setPhoneNumber(request.getPhoneNumber());
            lawUserRequest.setAddress(request.getAddress());
            
            // Law role güncelleme
            if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
                lawUserRequest.setLawRole(request.getRole());
            }
            
            // Law-specific fields için mevcut değerleri koru
            lawUserRequest.setBarNumber(existingLawUser.getBarNumber());
            lawUserRequest.setSpecialization(existingLawUser.getSpecialization());
            lawUserRequest.setExperienceYears(existingLawUser.getExperienceYears());
            lawUserRequest.setLawSchool(existingLawUser.getLawSchool());
            lawUserRequest.setGraduationYear(existingLawUser.getGraduationYear());

            Optional<LawUserDTO> updatedLawUser = lawUserService.updateLawUser(existingLawUser.getId(), lawUserRequest);
            
            if (updatedLawUser.isPresent()) {
                UserDTO userDTO = convertLawUserToUserDTO(updatedLawUser.get());
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Law user updated successfully",
                        "user", userDTO
                ));
            } else {
                throw new RuntimeException("Failed to update law user");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update law user: " + e.getMessage()));
        }
    }

    /**
     * Delete user
     */
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            boolean deleted = userService.deleteUser(id);
            if (deleted) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "User deleted successfully"
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete user: " + e.getMessage()));
        }
    }

    /**
     * Deactivate user (soft delete)
     */
    @PatchMapping("/users/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deactivateUser(@PathVariable Long id) {
        try {
            User user = userService.findByIdEntity(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            user.setActive(false);
            user.setUpdatedDate(LocalDateTime.now());
            
            userService.saveUser(user);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "User deactivated successfully"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to deactivate user: " + e.getMessage()));
        }
    }

    /**
     * Get available roles
     */
    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Role>> getAllRoles() {
        try {
            List<Role> roles = roleRepository.findAll();
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Request DTOs
    public static class CreateUserRequest {
        private String username;
        private String password;
        private String email;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private String address;
        private String role; // LAWYER, USER, etc.

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
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    public static class UpdateUserRequest {
        private String username;
        private String password; // Şifre alanı eklendi
        private String email;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private String address;
        private String role; // Rol alanı eklendi

        public UserDTO toUserDTO() {
            UserDTO dto = new UserDTO();
            dto.setUsername(this.username);
            dto.setEmail(this.email);
            dto.setFirstName(this.firstName);
            dto.setLastName(this.lastName);
            return dto;
        }

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
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}