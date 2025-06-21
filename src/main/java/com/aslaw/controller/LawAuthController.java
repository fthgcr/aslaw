package com.aslaw.controller;

import com.infracore.dto.JwtAuthenticationResponse;
import com.infracore.dto.LoginRequest;
import com.infracore.security.JwtTokenProvider;
import com.infracore.service.UserService;
import com.aslaw.security.LawUserPrincipal;
import com.aslaw.service.LawUserService;
import com.aslaw.entity.LawUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/law/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
public class LawAuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final LawUserService lawUserService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            System.out.println("LawAuthController: Login attempt for username: " + loginRequest.getUsername());
            
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            System.out.println("LawAuthController: Authentication successful");
            SecurityContextHolder.getContext().setAuthentication(authentication);
            LawUserPrincipal lawUserPrincipal = (LawUserPrincipal) authentication.getPrincipal();
            String jwt = jwtTokenProvider.generateToken(authentication);

            // Return the primary law role if available, otherwise base role
            String roleToReturn = lawUserPrincipal.getPrimaryRole();
            System.out.println("LawAuthController: Generated JWT token, role: " + roleToReturn);

            return ResponseEntity.ok(new JwtAuthenticationResponse(jwt, roleToReturn));
        } catch (Exception e) {
            System.out.println("LawAuthController: Login failed - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(401).body("Login failed: " + e.getMessage());
        }
    }
} 