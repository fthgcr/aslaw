package com.aslaw.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
@CrossOrigin(origins = "*")
public class TestController {

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @GetMapping
    public ResponseEntity<Map<String, Object>> testEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "ASLAW Backend is running successfully");
        response.put("timestamp", LocalDateTime.now());
        response.put("profile", activeProfile);
        response.put("version", "2.0.0");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "aslaw-backend");
        response.put("profile", activeProfile);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api-info")
    public ResponseEntity<Map<String, Object>> apiInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "ASLAW - Legal Management System");
        response.put("profile", activeProfile);
        response.put("timestamp", LocalDateTime.now());
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("auth", "/api/law/auth/login");
        endpoints.put("clients", "/api/clients");
        endpoints.put("cases", "/api/cases");
        endpoints.put("admin", "/api/admin");
        endpoints.put("dashboard", "/api/dashboard");
        endpoints.put("health", "/actuator/health");
        
        response.put("endpoints", endpoints);
        return ResponseEntity.ok(response);
    }
} 