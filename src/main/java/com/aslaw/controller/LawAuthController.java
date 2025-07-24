package com.aslaw.controller;

import com.infracore.dto.JwtAuthenticationResponse;
import com.infracore.dto.LoginRequest;
import com.infracore.security.JwtTokenProvider;
import com.infracore.service.UserService;
import com.aslaw.security.LawUserPrincipal;
import com.aslaw.service.LawUserService;
import com.aslaw.entity.LawUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
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
    private final PasswordEncoder passwordEncoder;
    
    @Value("${app.security.jwt.secret:defaultSecretKeyForDevelopmentOnlyNotForProduction}")
    private String jwtSecret;
    
    @Value("${app.security.jwt.expiration:86400000}")
    private long jwtExpirationMs;

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
            
            // Cast to LawUserPrincipal to get law-specific information
            LawUserPrincipal lawUserPrincipal = (LawUserPrincipal) authentication.getPrincipal();
            
            // Create JWT token using manual method to avoid casting issues
            String jwt = createJwtToken(lawUserPrincipal);

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
    
    // Create JWT token manually using JJWT library directly
    private String createJwtToken(LawUserPrincipal userPrincipal) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
        
        String authorities = userPrincipal.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(java.util.stream.Collectors.joining(","));
        
        try {
            return Jwts.builder()
                    .setSubject(userPrincipal.getUsername())
                    .claim("authorities", authorities)
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(SignatureAlgorithm.HS256, jwtSecret.getBytes())
                    .compact();
        } catch (Exception e) {
            System.out.println("JWT creation failed: " + e.getMessage());
            // Fallback to simple token for debugging
            return "jwt-token-" + userPrincipal.getUsername() + "-" + System.currentTimeMillis();
        }
    }

    // Debug endpoint - production'da kaldırılmalı
    @PostMapping("/debug/password-test")
    public ResponseEntity<?> testPassword(@RequestBody PasswordTestRequest request) {
        try {
            System.out.println("Password test for: " + request.getUsername());
            
            // Veritabanından kullanıcıyı bul
            var user = userService.findByUsername(request.getUsername());
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found");
            }
            
            String storedPassword = user.getPassword();
            String providedPassword = request.getPassword();
            
            System.out.println("Stored password (encoded): " + storedPassword);
            System.out.println("Provided password (plain): " + providedPassword);
            
            // PasswordEncoder ile kontrol et
            boolean matches = passwordEncoder.matches(providedPassword, storedPassword);
            System.out.println("Password matches: " + matches);
            
            // Test için yeni encode edilen şifreyi göster
            String newlyEncoded = passwordEncoder.encode(providedPassword);
            System.out.println("Newly encoded password: " + newlyEncoded);
            
            return ResponseEntity.ok(java.util.Map.of(
                "username", request.getUsername(),
                "storedPassword", storedPassword,
                "providedPassword", providedPassword,
                "matches", matches,
                "newlyEncoded", newlyEncoded
            ));
        } catch (Exception e) {
            System.out.println("Password test failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Test failed: " + e.getMessage());
        }
    }

    // BCrypt testi için basit endpoint
    @GetMapping("/debug/bcrypt-test")
    public ResponseEntity<?> bcryptTest() {
        try {
            String plainPassword = "123123";
            String encoded1 = passwordEncoder.encode(plainPassword);
            String encoded2 = passwordEncoder.encode(plainPassword);
            
            boolean matches1 = passwordEncoder.matches(plainPassword, encoded1);
            boolean matches2 = passwordEncoder.matches(plainPassword, encoded2);
            
            // Veritabanındaki hash
            String dbHash = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.";
            boolean matchesDb = passwordEncoder.matches(plainPassword, dbHash);
            
            return ResponseEntity.ok(java.util.Map.of(
                "plainPassword", plainPassword,
                "encoded1", encoded1,
                "encoded2", encoded2,
                "matches1", matches1,
                "matches2", matches2,
                "dbHash", dbHash,
                "matchesDb", matchesDb
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("BCrypt test failed: " + e.getMessage());
        }
    }

    public static class PasswordTestRequest {
        private String username;
        private String password;
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
} 