package com.aslaw.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        String requestUri = (String) request.getAttribute("jakarta.servlet.error.request_uri");
        
        // Angular route istekleri için sessiz 404 dönüş
        if (statusCode == 404 && (requestUri != null && !requestUri.startsWith("/api/"))) {
            return ResponseEntity.notFound().build();
        }
        
        // Diğer hatalar için normal error response
        return ResponseEntity.status(statusCode != null ? statusCode : 500)
            .body(Map.of(
                "error", "Not Found",
                "status", statusCode != null ? statusCode : 500,
                "path", requestUri != null ? requestUri : "unknown"
            ));
    }
} 