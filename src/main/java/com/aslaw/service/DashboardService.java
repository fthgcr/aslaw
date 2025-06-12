package com.aslaw.service;

import com.aslaw.entity.Case;
import com.aslaw.entity.Client;
import com.aslaw.entity.Document;
import com.aslaw.repository.CaseRepository;
import com.aslaw.repository.ClientRepository;
import com.aslaw.repository.DocumentRepository;
import com.infracore.entity.Role;
import com.infracore.entity.User;
import com.infracore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final CaseRepository caseRepository;
    private final DocumentRepository documentRepository;

    @Autowired
    public DashboardService(UserRepository userRepository, 
                           ClientRepository clientRepository,
                           CaseRepository caseRepository,
                           DocumentRepository documentRepository) {
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.caseRepository = caseRepository;
        this.documentRepository = documentRepository;
    }

    /**
     * Get dashboard statistics
     */
    @Transactional(readOnly = true)
    public DashboardStats getDashboardStats() {
        // Total clients (users with USER role)
        long totalClients = userRepository.findAll().stream()
                .filter(user -> user.hasRole(Role.RoleName.USER))
                .count();

        // This month's new clients
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);
        
        long monthlyNewClients = userRepository.findAll().stream()
                .filter(user -> user.hasRole(Role.RoleName.USER))
                .filter(user -> user.getCreatedDate() != null 
                    && user.getCreatedDate().isAfter(startOfMonth) 
                    && user.getCreatedDate().isBefore(endOfMonth))
                .count();

        // Previous month's new clients for comparison
        YearMonth previousMonth = currentMonth.minusMonths(1);
        LocalDateTime startOfPreviousMonth = previousMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfPreviousMonth = previousMonth.atEndOfMonth().atTime(23, 59, 59);
        
        long previousMonthNewClients = userRepository.findAll().stream()
                .filter(user -> user.hasRole(Role.RoleName.USER))
                .filter(user -> user.getCreatedDate() != null 
                    && user.getCreatedDate().isAfter(startOfPreviousMonth) 
                    && user.getCreatedDate().isBefore(endOfPreviousMonth))
                .count();

        // Calculate percentage change
        double clientGrowthPercentage = 0.0;
        if (previousMonthNewClients > 0) {
            clientGrowthPercentage = ((double) (monthlyNewClients - previousMonthNewClients) / previousMonthNewClients) * 100;
        } else if (monthlyNewClients > 0) {
            clientGrowthPercentage = 100.0; // 100% increase from 0
        }

        // Total cases
        long totalCases = caseRepository.count();

        // Active cases (not closed)
        long activeCases = caseRepository.findAll().stream()
                .filter(caseItem -> caseItem.getStatus() != Case.CaseStatus.CLOSED)
                .count();

        // Total documents
        long totalDocuments = documentRepository.count();

        return new DashboardStats(
                totalClients,
                monthlyNewClients,
                clientGrowthPercentage,
                totalCases,
                activeCases,
                totalDocuments
        );
    }

    /**
     * Dashboard statistics DTO
     */
    public static class DashboardStats {
        private final long totalClients;
        private final long monthlyNewClients;
        private final double clientGrowthPercentage;
        private final long totalCases;
        private final long activeCases;
        private final long totalDocuments;

        public DashboardStats(long totalClients, long monthlyNewClients, double clientGrowthPercentage,
                             long totalCases, long activeCases, long totalDocuments) {
            this.totalClients = totalClients;
            this.monthlyNewClients = monthlyNewClients;
            this.clientGrowthPercentage = clientGrowthPercentage;
            this.totalCases = totalCases;
            this.activeCases = activeCases;
            this.totalDocuments = totalDocuments;
        }

        // Getters
        public long getTotalClients() { return totalClients; }
        public long getMonthlyNewClients() { return monthlyNewClients; }
        public double getClientGrowthPercentage() { return clientGrowthPercentage; }
        public long getTotalCases() { return totalCases; }
        public long getActiveCases() { return activeCases; }
        public long getTotalDocuments() { return totalDocuments; }
    }
} 