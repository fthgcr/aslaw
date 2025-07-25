package com.aslaw.controller;

import com.infracore.dto.UserDTO;
import com.infracore.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserService userService;

    /**
     * Get current user's profile
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> getCurrentUserProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).build();
            }

            String username = authentication.getName();
            System.out.println("UserProfileController: Getting profile for user: " + username);
            
            return userService.getUserByUsername(username)
                    .map(userDTO -> {
                        System.out.println("UserProfileController: Profile found for user: " + username);
                        return ResponseEntity.ok(userDTO);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.out.println("UserProfileController: Error getting profile: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Update current user's profile
     */
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> updateCurrentUserProfile(@RequestBody UserDTO profileUpdate) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).build();
            }

            String username = authentication.getName();
            System.out.println("UserProfileController: Updating profile for user: " + username);
            
            return userService.getUserByUsername(username)
                    .flatMap(currentUser -> {
                        System.out.println("UserProfileController: Current user found, updating profile");
                        return userService.updateUser(currentUser.getId(), profileUpdate);
                    })
                    .map(updatedUser -> {
                        System.out.println("UserProfileController: Profile updated successfully for user: " + username);
                        return ResponseEntity.ok(updatedUser);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.out.println("UserProfileController: Error updating profile: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
} 