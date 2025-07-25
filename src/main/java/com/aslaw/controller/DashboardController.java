package com.aslaw.controller;

import com.aslaw.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @Autowired
    private DataSource dataSource;

    @Autowired
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Get dashboard statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<DashboardService.DashboardStats> getDashboardStats() {
        try {
            DashboardService.DashboardStats stats = dashboardService.getDashboardStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.out.println("DashboardController: Error getting dashboard stats: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get client status summary
     */
    @GetMapping("/client-status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<DashboardService.ClientStatusSummary> getClientStatusSummary() {
        try {
            DashboardService.ClientStatusSummary summary = dashboardService.getClientStatusSummary();
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            System.out.println("DashboardController: Error getting client status summary: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get case types distribution
     */
    @GetMapping("/case-types")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<java.util.List<DashboardService.CaseTypeDistribution>> getCaseTypesDistribution() {
        try {
            java.util.List<DashboardService.CaseTypeDistribution> distribution = dashboardService.getCaseTypesDistribution();
            return ResponseEntity.ok(distribution);
        } catch (Exception e) {
            System.out.println("DashboardController: Error getting case types distribution: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get case status distribution
     */
    @GetMapping("/case-status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<java.util.List<DashboardService.CaseStatusDistribution>> getCaseStatusDistribution() {
        try {
            java.util.List<DashboardService.CaseStatusDistribution> distribution = dashboardService.getCaseStatusDistribution();
            return ResponseEntity.ok(distribution);
        } catch (Exception e) {
            System.out.println("DashboardController: Error getting case status distribution: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/recent-activities")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAWYER') or hasRole('CLERK')")
    public ResponseEntity<java.util.List<DashboardService.RecentActivity>> getRecentActivities() {
        try {
            java.util.List<DashboardService.RecentActivity> activities = dashboardService.getRecentActivities();
            return ResponseEntity.ok(activities);
        } catch (Exception e) {
            System.out.println("DashboardController: Error getting recent activities: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create test activities for demonstration
     */
    @PostMapping("/create-test-activities")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> createTestActivities() {
        try {
            dashboardService.createTestActivities();
            return ResponseEntity.ok("Test activities created successfully");
        } catch (Exception e) {
            System.out.println("DashboardController: Error creating test activities: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating test activities");
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Database connection test
            try (Connection connection = dataSource.getConnection()) {
                response.put("database", "connected");
                response.put("url", connection.getMetaData().getURL());
            }
        } catch (Exception e) {
            response.put("database", "error: " + e.getMessage());
        }
        
        response.put("status", "ok");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Dashboard API is working!");
    }
} 