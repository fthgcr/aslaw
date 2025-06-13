package com.aslaw.service;

import com.aslaw.entity.Case;
import com.aslaw.entity.Document;
import com.aslaw.repository.CaseRepository;
import com.aslaw.repository.DocumentRepository;
import com.infracore.entity.Role;
import com.infracore.entity.User;
import com.infracore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Comparator;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final CaseRepository caseRepository;
    private final DocumentRepository documentRepository;

    @Autowired
    public DashboardService(UserRepository userRepository,

                           CaseRepository caseRepository,
                           DocumentRepository documentRepository) {
        this.userRepository = userRepository;
        this.caseRepository = caseRepository;
        this.documentRepository = documentRepository;
    }

    /**
     * Get client status summary
     */
    @Transactional(readOnly = true)
    public ClientStatusSummary getClientStatusSummary() {
        List<User> allClients = userRepository.findAll().stream()
                .filter(user -> user.hasRole(Role.RoleName.USER))
                .toList();

        long activeClients = allClients.stream()
                .filter(User::isEnabled)
                .count();
        
        long inactiveClients = allClients.size() - activeClients;

        return new ClientStatusSummary(activeClients, inactiveClients);
    }

    /**
     * Get case types distribution
     */
    @Transactional(readOnly = true)
    public List<CaseTypeDistribution> getCaseTypesDistribution() {
        List<Case> allCases = caseRepository.findAll();
        
        Map<Case.CaseType, Long> typeCount = allCases.stream()
                .collect(Collectors.groupingBy(Case::getType, Collectors.counting()));

        return typeCount.entrySet().stream()
                .map(entry -> new CaseTypeDistribution(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .toList();
    }

    /**
     * Get case status distribution
     */
    @Transactional(readOnly = true)
    public List<CaseStatusDistribution> getCaseStatusDistribution() {
        List<Case> allCases = caseRepository.findAll();
        
        Map<Case.CaseStatus, Long> statusCount = allCases.stream()
                .collect(Collectors.groupingBy(Case::getStatus, Collectors.counting()));

        return statusCount.entrySet().stream()
                .map(entry -> new CaseStatusDistribution(entry.getKey(), entry.getValue()))
                .toList();
    }

    /**
     * Get recent activities
     */
    @Transactional(readOnly = true)
    public List<RecentActivity> getRecentActivities() {
        List<RecentActivity> activities = new ArrayList<>();
        
        // Get recent clients (users with USER role)
        List<User> recentClients = userRepository.findAll().stream()
                .filter(user -> user.hasRole(Role.RoleName.USER))
                .filter(user -> user.getCreatedDate() != null)
                .sorted((a, b) -> b.getCreatedDate().compareTo(a.getCreatedDate()))
                .limit(3)
                .toList();
        
        for (User client : recentClients) {
            activities.add(new RecentActivity(
                "CLIENT_ADDED",
                "Yeni müvekkil eklendi: " + client.getFirstName() + " " + client.getLastName(),
                client.getCreatedDate(),
                "pi-user-plus"
            ));
        }
        
        // Get recent cases
        List<Case> recentCases = caseRepository.findAll().stream()
                .filter(caseItem -> caseItem.getCreatedDate() != null)
                .sorted((a, b) -> b.getCreatedDate().compareTo(a.getCreatedDate()))
                .limit(3)
                .toList();
        
        for (Case caseItem : recentCases) {
            activities.add(new RecentActivity(
                "CASE_CREATED",
                "Yeni dava oluşturuldu: " + caseItem.getTitle(),
                caseItem.getCreatedDate(),
                "pi-briefcase"
            ));
        }
        
        // Get recent documents
        List<Document> recentDocuments = documentRepository.findAll().stream()
                .filter(document -> document.getCreatedDate() != null)
                .sorted((a, b) -> b.getCreatedDate().compareTo(a.getCreatedDate()))
                .limit(3)
                .toList();
        
        for (Document document : recentDocuments) {
            activities.add(new RecentActivity(
                "DOCUMENT_UPLOADED",
                "Doküman yüklendi: " + document.getTitle(),
                document.getCreatedDate(),
                "pi-file-o"
            ));
        }
        
        // Sort all activities by date and return top 5
        return activities.stream()
                .sorted((a, b) -> b.getCreatedDate().compareTo(a.getCreatedDate()))
                .limit(5)
                .toList();
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

    /**
     * Client status summary DTO
     */
    public static class ClientStatusSummary {
        private final long activeClients;
        private final long inactiveClients;

        public ClientStatusSummary(long activeClients, long inactiveClients) {
            this.activeClients = activeClients;
            this.inactiveClients = inactiveClients;
        }

        public long getActiveClients() { return activeClients; }
        public long getInactiveClients() { return inactiveClients; }
        public long getTotalClients() { return activeClients + inactiveClients; }
    }

    /**
     * Case type distribution DTO
     */
    public static class CaseTypeDistribution {
        private final Case.CaseType type;
        private final long count;

        public CaseTypeDistribution(Case.CaseType type, long count) {
            this.type = type;
            this.count = count;
        }

        public Case.CaseType getType() { return type; }
        public long getCount() { return count; }
        public String getTypeName() {
            return switch (type) {
                case CIVIL -> "Hukuk";
                case CRIMINAL -> "Ceza";
                case FAMILY -> "Aile";
                case CORPORATE -> "Ticaret";
                case REAL_ESTATE -> "Emlak";
                case INTELLECTUAL_PROPERTY -> "Fikri Mülkiyet";
                case CAR_DEPRECIATION -> "Araç Değer Kaybı";
                case OTHER -> "Diğer";
            };
        }
    }

    /**
     * Case status distribution DTO
     */
    public static class CaseStatusDistribution {
        private final Case.CaseStatus status;
        private final long count;

        public CaseStatusDistribution(Case.CaseStatus status, long count) {
            this.status = status;
            this.count = count;
        }

        public Case.CaseStatus getStatus() { return status; }
        public long getCount() { return count; }
        public String getStatusName() {
            return switch (status) {
                case OPEN -> "Açık";
                case IN_PROGRESS -> "Devam Eden";
                case PENDING -> "Beklemede";
                case CLOSED -> "Kapalı";
            };
        }
    }

    /**
     * Recent activity DTO
     */
    public static class RecentActivity {
        private final String type;
        private final String description;
        private final LocalDateTime createdDate;
        private final String icon;

        public RecentActivity(String type, String description, LocalDateTime createdDate, String icon) {
            this.type = type;
            this.description = description;
            this.createdDate = createdDate;
            this.icon = icon;
        }

        public String getType() { return type; }
        public String getDescription() { return description; }
        public LocalDateTime getCreatedDate() { return createdDate; }
        public String getIcon() { return icon; }
        
        public String getTimeAgo() {
            LocalDateTime now = LocalDateTime.now();
            long minutes = ChronoUnit.MINUTES.between(createdDate, now);
            long hours = ChronoUnit.HOURS.between(createdDate, now);
            long days = ChronoUnit.DAYS.between(createdDate, now);
            
            if (minutes < 60) {
                return minutes <= 1 ? "1 dakika önce" : minutes + " dakika önce";
            } else if (hours < 24) {
                return hours <= 1 ? "1 saat önce" : hours + " saat önce";
            } else {
                return days <= 1 ? "1 gün önce" : days + " gün önce";
            }
        }
    }
} 