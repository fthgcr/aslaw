package com.aslaw.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
@CrossOrigin(origins = "*")
public class TestController {

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @GetMapping
    public ResponseEntity<Map<String, Object>> testEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Test endpoint is working");
        response.put("timestamp", LocalDateTime.now());
        response.put("contextPath", contextPath);
        response.put("environment", "Railway");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "aslaw-backend");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/context")
    public ResponseEntity<Map<String, Object>> contextInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("contextPath", contextPath);
        response.put("fullPath", contextPath + "/test/context");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/urls")
    public ResponseEntity<Map<String, Object>> urlMappings(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("contextPath", contextPath);
        response.put("requestURL", request.getRequestURL().toString());
        response.put("requestURI", request.getRequestURI());
        response.put("servletPath", request.getServletPath());
        
        Map<String, String> correctUrls = new HashMap<>();
        if (contextPath != null && contextPath.equals("/api")) {
            correctUrls.put("clients", "https://vibrant-dedication-production-125e.up.railway.app/api/clients");
            correctUrls.put("cases", "https://vibrant-dedication-production-125e.up.railway.app/api/cases");
            correctUrls.put("admin", "https://vibrant-dedication-production-125e.up.railway.app/api/admin");
            correctUrls.put("dashboard", "https://vibrant-dedication-production-125e.up.railway.app/api/dashboard");
            correctUrls.put("auth", "https://vibrant-dedication-production-125e.up.railway.app/api/law/auth/login");
        } else {
            correctUrls.put("clients", "http://localhost:8081/api/clients");
            correctUrls.put("cases", "http://localhost:8081/api/cases");
            correctUrls.put("admin", "http://localhost:8081/api/admin");
            correctUrls.put("dashboard", "http://localhost:8081/api/dashboard");
            correctUrls.put("auth", "http://localhost:8081/api/law/auth/login");
        }
        
        response.put("correctApiUrls", correctUrls);
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
} 