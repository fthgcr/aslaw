package com.aslaw.controller;

import com.infracore.dto.RegistrationRequest;
import com.infracore.dto.UserDTO;
import com.infracore.entity.Role;
import com.infracore.entity.User;
import com.infracore.repository.RoleRepository;
import com.infracore.service.UserService;
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

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminController {

    private final UserService userService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    AdminController(UserService userService, RoleRepository roleRepository, PasswordEncoder passwordEncoder){
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

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
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER')")
    public ResponseEntity<List<UserDTO>> getAllLawyers() {
        try {
            List<UserDTO> lawyers = userService.getAllLawyers();
            System.out.println("Lawyers found using new method: " + lawyers.size());
            return ResponseEntity.ok(lawyers);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get active lawyers only
     */
    @GetMapping("/lawyers/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER')")
    public ResponseEntity<List<UserDTO>> getActiveLawyers() {
        try {
            List<UserDTO> activeLawyers = userService.getActiveLawyers();
            return ResponseEntity.ok(activeLawyers);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all clients (users with USER role)
     */
    @GetMapping("/clients")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER')")
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER')")
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER')")
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER')")
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

            // Create new user
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

    /**
     * Update user
     */
    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        try {
            UserDTO updatedUser = userService.updateUser(id, request.toUserDTO())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "User updated successfully",
                    "user", updatedUser
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update user: " + e.getMessage()));
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
        private String email;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private String address;

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
    }
} 