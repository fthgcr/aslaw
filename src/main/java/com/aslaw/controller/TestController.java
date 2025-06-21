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
} 