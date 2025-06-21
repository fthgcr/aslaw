package com.aslaw.controller;

import com.aslaw.service.ClientService;
import com.infracore.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ClientController {

    private final ClientService clientService;

    /**
     * Get all clients with optional filtering
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<List<User>> getAllClients(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) String search) {
        try {
            // If any filter parameters are provided, use filtered search
            if (firstName != null || lastName != null || email != null || 
                username != null || phoneNumber != null || enabled != null || search != null) {
                List<User> clients = clientService.getFilteredClients(
                    firstName, lastName, email, username, phoneNumber, enabled, search);
                return ResponseEntity.ok(clients);
            }
            
            // Otherwise return all clients
            List<User> clients = clientService.getAllClients();
            return ResponseEntity.ok(clients);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get clients with pagination
     */
    @GetMapping("/paginated")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<Page<User>> getClientsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : 
                Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<User> clients = clientService.getAllClients(pageable);
            return ResponseEntity.ok(clients);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get client by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<User> getClientById(@PathVariable Long id) {
        try {
            return clientService.getClientById(id)
                    .map(client -> ResponseEntity.ok(client))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create new client
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<?> createClient(@RequestBody User client) {
        try {
            System.out.println("ClientController: createClient called with: " + client.getUsername());
            User createdClient = clientService.createClient(client);
            System.out.println("ClientController: Client created successfully with ID: " + createdClient.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdClient);
        } catch (IllegalArgumentException e) {
            System.out.println("ClientController: Validation error: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.out.println("ClientController: Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Müvekkil oluşturulurken bir hata oluştu"));
        }
    }

    /**
     * Update existing client
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<?> updateClient(@PathVariable Long id, @RequestBody UpdateClientRequest request) {
        try {
            System.out.println("ClientController: updateClient called for ID: " + id);
            System.out.println("ClientController: Username: " + request.getUsername());
            System.out.println("ClientController: Email: " + request.getEmail());
            System.out.println("ClientController: Password provided: " + (request.getPassword() != null ? "YES (length: " + request.getPassword().length() + ")" : "NO"));
            
            // Convert DTO to User entity
            User clientDetails = new User();
            clientDetails.setUsername(request.getUsername());
            clientDetails.setEmail(request.getEmail());
            clientDetails.setFirstName(request.getFirstName());
            clientDetails.setLastName(request.getLastName());
            clientDetails.setPhoneNumber(request.getPhoneNumber());
            clientDetails.setAddress(request.getAddress());
            clientDetails.setNotes(request.getNotes());
            clientDetails.setEnabled(request.isEnabled());
            clientDetails.setActive(request.isActive());
            clientDetails.setPassword(request.getPassword()); // Şifre alanını set et
            
            User updatedClient = clientService.updateClient(id, clientDetails);
            
            System.out.println("ClientController: Client updated successfully");
            return ResponseEntity.ok(updatedClient);
        } catch (IllegalArgumentException e) {
            System.out.println("ClientController: Validation error: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.out.println("ClientController: Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Müvekkil güncellenirken bir hata oluştu"));
        }
    }

    // DTO for client update requests
    public static class UpdateClientRequest {
        private String username;
        private String password;
        private String email;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private String address;
        private String notes;
        private boolean enabled;
        private boolean active;

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
        
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }

    /**
     * Delete client
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteClient(@PathVariable Long id) {
        try {
            clientService.deleteClient(id);
            return ResponseEntity.ok()
                    .body(Map.of("message", "Müvekkil başarıyla silindi"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Müvekkil silinirken bir hata oluştu"));
        }
    }

    /**
     * Toggle client status (enable/disable)
     */
    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<?> toggleClientStatus(@PathVariable Long id) {
        try {
            User updatedClient = clientService.toggleClientStatus(id);
            String statusMessage = updatedClient.isEnabled() ? "etkinleştirildi" : "devre dışı bırakıldı";
            return ResponseEntity.ok()
                    .body(Map.of(
                        "message", "Müvekkil durumu " + statusMessage,
                        "client", updatedClient
                    ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Müvekkil durumu değiştirilirken bir hata oluştu"));
        }
    }

    /**
     * Search clients
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<List<User>> searchClients(@RequestParam String q) {
        try {
            List<User> clients = clientService.searchClients(q);
            return ResponseEntity.ok(clients);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get client statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<ClientService.ClientStats> getClientStats() {
        try {
            ClientService.ClientStats stats = clientService.getClientStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Check if username exists
     */
    @GetMapping("/check-username")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@RequestParam String username) {
        try {
            // Implementation would need to be added to service
            return ResponseEntity.ok(Map.of("exists", false)); // Placeholder
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Check if email exists
     */
    @GetMapping("/check-email")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        try {
            // Implementation would need to be added to service
            return ResponseEntity.ok(Map.of("exists", false)); // Placeholder
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 