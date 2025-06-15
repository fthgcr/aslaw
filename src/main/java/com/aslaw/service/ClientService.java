package com.aslaw.service;

import com.infracore.entity.Role;
import com.infracore.entity.User;
import com.infracore.repository.RoleRepository;
import com.infracore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ClientService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    ClientService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Get all clients (users with USER role)
     */
    @Transactional(readOnly = true)
    public List<User> getAllClients() {
        return userRepository.findAll().stream()
                .filter(user -> user.hasRole(Role.RoleName.USER))
                .toList();
    }

    /**
     * Get all clients with pagination
     */
    @Transactional(readOnly = true)
    public Page<User> getAllClients(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(user -> user.hasRole(Role.RoleName.USER) ? user : null)
                .map(user -> user);
    }

    /**
     * Get client by ID
     */
    @Transactional(readOnly = true)
    public Optional<User> getClientById(Long id) {
        return userRepository.findById(id)
                .filter(user -> user.hasRole(Role.RoleName.USER));
    }

    /**
     * Create new client
     */
    @Transactional
    public User createClient(User client) {
        // Validate required fields
        if (client.getUsername() == null || client.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Kullanıcı adı gereklidir");
        }
        if (client.getEmail() == null || client.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email gereklidir");
        }
        // Generate username if not provided but firstName and lastName are available
        if ((client.getUsername() == null || client.getUsername().trim().isEmpty()) &&
            client.getFirstName() != null && !client.getFirstName().trim().isEmpty() &&
            client.getLastName() != null && !client.getLastName().trim().isEmpty()) {
            
            String normalizedFirstName = normalizeString(client.getFirstName().trim());
            String normalizedLastName = normalizeString(client.getLastName().trim());
            String generatedUsername = normalizedFirstName + "." + normalizedLastName;
            
            // Check if username already exists, if so add a number
            String finalUsername = generatedUsername;
            int counter = 1;
            while (userRepository.existsByUsername(finalUsername)) {
                finalUsername = generatedUsername + counter;
                counter++;
            }
            
            client.setUsername(finalUsername);
        }

        // Generate default password if not provided
        if (client.getPassword() == null || client.getPassword().trim().isEmpty()) {
            client.setPassword("123456");
        }

        // Check if username already exists
        if (userRepository.existsByUsername(client.getUsername())) {
            throw new IllegalArgumentException("Bu kullanıcı adı zaten kullanılıyor");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(client.getEmail())) {
            throw new IllegalArgumentException("Bu email adresi zaten kullanılıyor");
        }

        // Encode password
        client.setPassword(passwordEncoder.encode(client.getPassword()));

        // Set default values
        client.setEnabled(true);
        client.setActive(true);
        client.setCreatedDate(LocalDateTime.now());
        client.setUpdatedDate(LocalDateTime.now());

        // Assign USER role
        Optional<Role> userRole = roleRepository.findByName(Role.RoleName.USER);
        if (userRole.isPresent()) {
            client.addRole(userRole.get());
        } else {
            throw new RuntimeException("USER role not found in database");
        }

        return userRepository.save(client);
    }

    /**
     * Normalize string for username generation (convert Turkish characters and make lowercase)
     */
    private String normalizeString(String str) {
        return str.toLowerCase()
                .replace("ğ", "g")
                .replace("ü", "u")
                .replace("ş", "s")
                .replace("ı", "i")
                .replace("ö", "o")
                .replace("ç", "c")
                .replaceAll("[^a-z0-9]", ""); // Remove any non-alphanumeric characters
    }

    /**
     * Update existing client
     */
    @Transactional
    public User updateClient(Long id, User clientDetails) {
        User existingClient = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Müvekkil bulunamadı: " + id));

        // Verify it's actually a client (has USER role)
        if (!existingClient.hasRole(Role.RoleName.USER)) {
            throw new IllegalArgumentException("Bu kullanıcı bir müvekkil değil");
        }

        // Check if new username already exists (excluding current user)
        if (!clientDetails.getUsername().equals(existingClient.getUsername()) &&
            userRepository.existsByUsername(clientDetails.getUsername())) {
            throw new IllegalArgumentException("Bu kullanıcı adı zaten kullanılıyor");
        }

        // Check if new email already exists (excluding current user)
        if (!clientDetails.getEmail().equals(existingClient.getEmail()) &&
            userRepository.existsByEmail(clientDetails.getEmail())) {
            throw new IllegalArgumentException("Bu email adresi zaten kullanılıyor");
        }

        // Update fields
        existingClient.setUsername(clientDetails.getUsername());
        existingClient.setFirstName(clientDetails.getFirstName());
        existingClient.setLastName(clientDetails.getLastName());
        existingClient.setEmail(clientDetails.getEmail());
        existingClient.setEnabled(clientDetails.isEnabled());
        existingClient.setActive(clientDetails.isActive());
        existingClient.setPhoneNumber(clientDetails.getPhoneNumber());
        existingClient.setAddress(clientDetails.getAddress());
        existingClient.setNotes(clientDetails.getNotes());
        existingClient.setUpdatedDate(LocalDateTime.now());

        // Update password only if provided
        if (clientDetails.getPassword() != null && !clientDetails.getPassword().trim().isEmpty()) {
            System.out.println("ClientService: Updating password for user: " + existingClient.getUsername());
            System.out.println("ClientService: New password received: " + clientDetails.getPassword());
            existingClient.setPassword(passwordEncoder.encode(clientDetails.getPassword()));
            System.out.println("ClientService: Password encoded and set successfully");
        } else {
            System.out.println("ClientService: No password provided for update, skipping password change");
        }

        User savedClient = userRepository.save(existingClient);
        System.out.println("ClientService: Client saved successfully. Password hash starts with: " + 
            (savedClient.getPassword() != null ? savedClient.getPassword().substring(0, Math.min(10, savedClient.getPassword().length())) + "..." : "null"));
        return savedClient;
    }

    /**
     * Delete client
     */
    @Transactional
    public void deleteClient(Long id) {
        User client = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Müvekkil bulunamadı: " + id));

        // Verify it's actually a client (has USER role)
        if (!client.hasRole(Role.RoleName.USER)) {
            throw new IllegalArgumentException("Bu kullanıcı bir müvekkil değil");
        }

        userRepository.delete(client);
    }

    /**
     * Toggle client status (enabled/disabled)
     */
    @Transactional
    public User toggleClientStatus(Long id) {
        User client = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Müvekkil bulunamadı: " + id));

        // Verify it's actually a client (has USER role)
        if (!client.hasRole(Role.RoleName.USER)) {
            throw new IllegalArgumentException("Bu kullanıcı bir müvekkil değil");
        }

        client.setEnabled(!client.isEnabled());
        client.setUpdatedDate(LocalDateTime.now());

        return userRepository.save(client);
    }

    /**
     * Get filtered clients
     */
    @Transactional(readOnly = true)
    public List<User> getFilteredClients(
            String firstName,
            String lastName,
            String email,
            String username,
            String phoneNumber,
            Boolean enabled,
            String search) {
        
        List<User> allClients = userRepository.findAll().stream()
                .filter(user -> user.hasRole(Role.RoleName.USER))
                .toList();

        return allClients.stream()
                .filter(client -> firstName == null || client.getFirstName().toLowerCase().contains(firstName.toLowerCase()))
                .filter(client -> lastName == null || client.getLastName().toLowerCase().contains(lastName.toLowerCase()))
                .filter(client -> email == null || client.getEmail().toLowerCase().contains(email.toLowerCase()))
                .filter(client -> username == null || client.getUsername().toLowerCase().contains(username.toLowerCase()))
                .filter(client -> phoneNumber == null || (client.getPhoneNumber() != null && client.getPhoneNumber().toLowerCase().contains(phoneNumber.toLowerCase())))
                .filter(client -> enabled == null || client.isEnabled() == enabled)
                .filter(client -> search == null || 
                    client.getFirstName().toLowerCase().contains(search.toLowerCase()) ||
                    client.getLastName().toLowerCase().contains(search.toLowerCase()) ||
                    client.getUsername().toLowerCase().contains(search.toLowerCase()) ||
                    client.getEmail().toLowerCase().contains(search.toLowerCase()) ||
                    (client.getPhoneNumber() != null && client.getPhoneNumber().toLowerCase().contains(search.toLowerCase())))
                .toList();
    }

    /**
     * Search clients
     */
    @Transactional(readOnly = true)
    public List<User> searchClients(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllClients();
        }

        String searchTerm = keyword.toLowerCase().trim();
        return userRepository.findAll().stream()
                .filter(user -> user.hasRole(Role.RoleName.USER))
                .filter(user -> 
                    user.getFirstName().toLowerCase().contains(searchTerm) ||
                    user.getLastName().toLowerCase().contains(searchTerm) ||
                    user.getUsername().toLowerCase().contains(searchTerm) ||
                    user.getEmail().toLowerCase().contains(searchTerm) ||
                    (user.getPhoneNumber() != null && user.getPhoneNumber().toLowerCase().contains(searchTerm))
                )
                .toList();
    }

    /**
     * Get client statistics
     */
    @Transactional(readOnly = true)
    public ClientStats getClientStats() {
        List<User> allClients = getAllClients();
        long totalClients = allClients.size();
        long activeClients = allClients.stream()
                .filter(User::isEnabled)
                .count();
        long inactiveClients = totalClients - activeClients;

        return new ClientStats(totalClients, activeClients, inactiveClients);
    }

    /**
     * Client statistics DTO
     */
    public static class ClientStats {
        private final long totalClients;
        private final long activeClients;
        private final long inactiveClients;

        public ClientStats(long totalClients, long activeClients, long inactiveClients) {
            this.totalClients = totalClients;
            this.activeClients = activeClients;
            this.inactiveClients = inactiveClients;
        }

        public long getTotalClients() { return totalClients; }
        public long getActiveClients() { return activeClients; }
        public long getInactiveClients() { return inactiveClients; }
    }
} 