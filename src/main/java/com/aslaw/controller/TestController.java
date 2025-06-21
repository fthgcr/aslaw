package com.aslaw.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/test")
@CrossOrigin(origins = "*")
public class TestController {

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @GetMapping
    public ResponseEntity<Map<String, Object>> testEndpoint(HttpServletRequest request) {
        log.info("=== TEST ENDPOINT CALLED ===");
        log.info("Request URL: {}", request.getRequestURL());
        log.info("Request URI: {}", request.getRequestURI());
        log.info("Context Path: {}", request.getContextPath());
        log.info("Servlet Path: {}", request.getServletPath());
        log.info("Path Info: {}", request.getPathInfo());
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "ASLAW Backend is running successfully");
        response.put("timestamp", LocalDateTime.now());
        response.put("profile", activeProfile);
        response.put("requestURL", request.getRequestURL().toString());
        response.put("requestURI", request.getRequestURI());
        response.put("contextPath", request.getContextPath());
        response.put("servletPath", request.getServletPath());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/debug")
    public ResponseEntity<Map<String, Object>> debugEndpoint(HttpServletRequest request) {
        log.info("=== DEBUG ENDPOINT CALLED ===");
        log.info("Request URL: {}", request.getRequestURL());
        log.info("Request URI: {}", request.getRequestURI());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Debug endpoint - Railway URL analysis");
        response.put("requestURL", request.getRequestURL().toString());
        response.put("requestURI", request.getRequestURI());
        response.put("contextPath", request.getContextPath());
        response.put("servletPath", request.getServletPath());
        response.put("pathInfo", request.getPathInfo());
        response.put("profile", activeProfile);
        response.put("timestamp", LocalDateTime.now());
        
        // Test URL'leri
        Map<String, String> testUrls = new HashMap<>();
        testUrls.put("working", "/api/api/cases");
        testUrls.put("failing", "/api/clients");
        testUrls.put("expected", "/api/clients");
        response.put("urlAnalysis", testUrls);
        
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

// Ayrı controller - Railway routing sorununu test etmek için
@Slf4j
@RestController
@CrossOrigin(origins = "*")
class RoutingTestController {

    @GetMapping("/clients")
    public ResponseEntity<Map<String, Object>> clientsEndpoint(HttpServletRequest request) {
        log.info("=== /clients ENDPOINT CALLED ===");
        log.info("Request URL: {}", request.getRequestURL());
        log.info("Request URI: {}", request.getRequestURI());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "SUCCESS! /clients endpoint is working");
        response.put("timestamp", LocalDateTime.now());
        response.put("requestURL", request.getRequestURL().toString());
        response.put("requestURI", request.getRequestURI());
        response.put("note", "This proves Railway routing is working");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/clients")
    public ResponseEntity<Map<String, Object>> apiClientsEndpoint(HttpServletRequest request) {
        log.info("=== /api/clients ENDPOINT CALLED ===");
        log.info("Request URL: {}", request.getRequestURL());
        log.info("Request URI: {}", request.getRequestURI());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "SUCCESS! /api/clients endpoint is working");
        response.put("timestamp", LocalDateTime.now());
        response.put("requestURL", request.getRequestURL().toString());
        response.put("requestURI", request.getRequestURI());
        response.put("note", "This is the expected API endpoint");
        return ResponseEntity.ok(response);
    }
} 