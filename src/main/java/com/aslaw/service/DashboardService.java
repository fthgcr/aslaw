package com.aslaw.service;

import com.aslaw.entity.Case;
import com.aslaw.entity.Document;
import com.aslaw.repository.CaseRepository;
import com.aslaw.repository.DocumentRepository;
import com.infracore.dto.UserDTO;
import com.infracore.entity.ActivityLog;
import com.infracore.entity.Role;
import com.infracore.entity.User;
import com.infracore.repository.UserRepository;
import com.infracore.service.ActivityLogService;
import com.infracore.service.UserService;
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
    private final UserService userService;
    private final CaseRepository caseRepository;
    private final DocumentRepository documentRepository;
    private final ActivityLogService activityLogService;

    @Autowired
    public DashboardService(UserRepository userRepository,
                           UserService userService,
                           CaseRepository caseRepository,
                           DocumentRepository documentRepository,
                           ActivityLogService activityLogService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.caseRepository = caseRepository;
        this.documentRepository = documentRepository;
        this.activityLogService = activityLogService;
    }

    /**
     * Get client status summary
     */
    @Transactional(readOnly = true)
    public ClientStatusSummary getClientStatusSummary() {
        // UserService'i kullan
        List<UserDTO> allClients = userService.getAllClients();
        List<UserDTO> activeClients = userService.getActiveClients();

        long activeCount = activeClients.size();
        long inactiveCount = allClients.size() - activeCount;

        return new ClientStatusSummary(activeCount, inactiveCount);
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
     * Get recent activities from activity log with fallback to entity-based activities
     */
    @Transactional(readOnly = true)
    public List<RecentActivity> getRecentActivities() {
        try {
            List<ActivityLog> activityLogs = activityLogService.getRecentActivities(5); // Son 5 aktivite
            
            if (!activityLogs.isEmpty()) {
                return activityLogs.stream()
                        .map(this::convertToRecentActivity)
                        .limit(5) // Güvenlik için limit ekle
                        .toList();
            }
        } catch (Exception e) {
            System.out.println("DashboardService: Error getting activity logs, falling back to entity-based activities: " + e.getMessage());
        }
        
        // Fallback to entity-based activities if no activity logs found
        return getEntityBasedActivities();
    }

    /**
     * Get activities from entities (fallback method)
     */
    private List<RecentActivity> getEntityBasedActivities() {
        List<RecentActivity> activities = new ArrayList<>();
        
        // Get recent clients
        List<User> recentClients = userRepository.findAllClients().stream()
                .filter(user -> user.getCreatedDate() != null)
                .sorted((a, b) -> b.getCreatedDate().compareTo(a.getCreatedDate()))
                .limit(3)
                .toList();
        
        for (User client : recentClients) {
            activities.add(new RecentActivity(
                "CLIENT_CREATED",
                "Yeni müvekkil eklendi: " + client.getFirstName() + " " + client.getLastName(),
                client.getCreatedDate(),
                "pi-user-plus",
                new RecentActivity.PerformedBy(0L, "System", "User", "system"),
                new RecentActivity.TargetEntity(client.getId(), client.getFirstName() + " " + client.getLastName(), "CLIENT"),
                null
            ));
        }
        
        // Get recent cases
        List<Case> recentCases = caseRepository.findAll().stream()
                .filter(caseItem -> caseItem.getCreatedDate() != null)
                .sorted((a, b) -> b.getCreatedDate().compareTo(a.getCreatedDate()))
                .limit(3)
                .toList();
        
        for (Case caseItem : recentCases) {
            String clientName = caseItem.getClient() != null ? 
                caseItem.getClient().getFirstName() + " " + caseItem.getClient().getLastName() : "Bilinmeyen Müvekkil";
            
            activities.add(new RecentActivity(
                "CASE_CREATED",
                "Yeni dava oluşturuldu: " + caseItem.getTitle(),
                caseItem.getCreatedDate(),
                "pi-briefcase",
                new RecentActivity.PerformedBy(0L, "System", "User", "system"),
                new RecentActivity.TargetEntity(caseItem.getId(), caseItem.getTitle(), "CASE"),
                caseItem.getClient() != null ? 
                    new RecentActivity.RelatedEntity(caseItem.getClient().getId(), clientName, "CLIENT") : null
            ));
        }
        
        // Get recent documents
        List<Document> recentDocuments = documentRepository.findAll().stream()
                .filter(document -> document.getCreatedDate() != null)
                .sorted((a, b) -> b.getCreatedDate().compareTo(a.getCreatedDate()))
                .limit(3)
                .toList();
        
        for (Document document : recentDocuments) {
            String clientName = "Bilinmeyen Müvekkil";
            Long clientId = 0L;
            
            if (document.getLegalCase() != null && document.getLegalCase().getClient() != null) {
                clientName = document.getLegalCase().getClient().getFirstName() + " " + 
                           document.getLegalCase().getClient().getLastName();
                clientId = document.getLegalCase().getClient().getId();
            }
            
            activities.add(new RecentActivity(
                "DOCUMENT_CREATED",
                "Doküman yüklendi: " + document.getTitle(),
                document.getCreatedDate(),
                "pi-file-plus",
                new RecentActivity.PerformedBy(0L, "System", "User", "system"),
                new RecentActivity.TargetEntity(document.getId(), document.getTitle(), "DOCUMENT"),
                new RecentActivity.RelatedEntity(clientId, clientName, "CLIENT")
            ));
        }
        
        // Sort all activities by date and return top 5
        return activities.stream()
                .sorted((a, b) -> b.getCreatedDate().compareTo(a.getCreatedDate()))
                .limit(5)
                .toList();
    }

    /**
     * Convert ActivityLog to RecentActivity
     */
    private RecentActivity convertToRecentActivity(ActivityLog activityLog) {
        RecentActivity.PerformedBy performedBy = new RecentActivity.PerformedBy(
            activityLog.getPerformedById(),
            activityLog.getPerformedByName().split(" ")[0], // firstName
            activityLog.getPerformedByName().contains(" ") ? 
                activityLog.getPerformedByName().substring(activityLog.getPerformedByName().indexOf(" ") + 1) : "", // lastName
            activityLog.getPerformedByUsername()
        );

        RecentActivity.TargetEntity targetEntity = new RecentActivity.TargetEntity(
            activityLog.getTargetEntityId(),
            activityLog.getTargetEntityName(),
            activityLog.getTargetEntityType().name()
        );

        RecentActivity.RelatedEntity relatedEntity = null;
        if (activityLog.getRelatedEntityId() != null) {
            relatedEntity = new RecentActivity.RelatedEntity(
                activityLog.getRelatedEntityId(),
                activityLog.getRelatedEntityName(),
                activityLog.getRelatedEntityType().name()
            );
        }

        return new RecentActivity(
            activityLog.getType().name(),
            activityLog.getDescription(),
            activityLog.getCreatedDate(),
            getIconForActivityType(activityLog.getType()),
            performedBy,
            targetEntity,
            relatedEntity
        );
    }

    /**
     * Get icon for activity type
     */
    private String getIconForActivityType(ActivityLog.ActivityType type) {
        switch (type) {
            case CLIENT_CREATED:
            case CLIENT_UPDATED:
                return "pi-user-plus";
            case CASE_CREATED:
            case CASE_UPDATED:
            case CASE_ASSIGNED:
                return "pi-briefcase";
            case DOCUMENT_CREATED:
            case DOCUMENT_UPDATED:
                return "pi-file-plus";
            case USER_CREATED:
            case USER_UPDATED:
                return "pi-users";
            case CLIENT_DELETED:
            case CASE_DELETED:
            case DOCUMENT_DELETED:
                return "pi-trash";
            default:
                return "pi-clock";
        }
    }

    /**
     * Get dashboard statistics
     */
    @Transactional(readOnly = true)
    public DashboardStats getDashboardStats() {
        // Active clients (USER role + enabled + active)
        List<UserDTO> allUserRoleUsers = userService.getAllClients(); // USER rolündeki tüm kullanıcılar
        
        System.out.println("=== DETAILED USER DEBUG ===");
        System.out.println("All USER role users:");
        for (UserDTO user : allUserRoleUsers) {
            System.out.println("- User: " + user.getFirstName() + " " + user.getLastName() + 
                " (ID: " + user.getId() + ") - Enabled: " + user.isEnabled() + 
                ", Active: " + user.isActive() + ", Roles: " + user.getRoles());
        }
        
        long totalClients = allUserRoleUsers.stream()
                .filter(UserDTO::isActive) // Aktif olanları filtrele
                .count();

        System.out.println("Filtered active users:");
        allUserRoleUsers.stream()
                .filter(user -> user.isEnabled() && user.isActive())
                .forEach(user -> System.out.println("- ACTIVE: " + user.getFirstName() + " " + user.getLastName() + 
                    " (ID: " + user.getId() + ") - Enabled: " + user.isEnabled() + 
                    ", Active: " + user.isActive()));

        // Last 30 days' new clients (not just this month)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        
        long monthlyNewClients = allUserRoleUsers.stream()
                .filter(user -> user.getCreatedDate() != null 
                    && user.getCreatedDate().isAfter(thirtyDaysAgo))
                .count();

        // Previous 30 days' new clients for comparison (31-60 days ago)
        LocalDateTime sixtyDaysAgo = LocalDateTime.now().minusDays(60);
        LocalDateTime thirtyOneDaysAgo = LocalDateTime.now().minusDays(31);
        
        long previousMonthNewClients = allUserRoleUsers.stream()
                .filter(user -> user.getCreatedDate() != null 
                    && user.getCreatedDate().isAfter(sixtyDaysAgo) 
                    && user.getCreatedDate().isBefore(thirtyOneDaysAgo))
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

        System.out.println("=== DASHBOARD STATS DEBUG ===");
        System.out.println("- All USER role users count: " + allUserRoleUsers.size());
        System.out.println("- Total Active Clients (USER role + enabled + active): " + totalClients);
        System.out.println("- Last 30 Days New Clients: " + monthlyNewClients);
        System.out.println("- Previous 30 Days New Clients: " + previousMonthNewClients);
        System.out.println("- Client Growth Percentage: " + clientGrowthPercentage + "%");
        System.out.println("- Total Cases: " + totalCases);
        System.out.println("- Active Cases: " + activeCases);
        System.out.println("==============================");

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
        private final String timeAgo;
        
        // User who performed the action
        private final PerformedBy performedBy;
        
        // Target entity details
        private final TargetEntity targetEntity;
        
        // Related entity (e.g., client for a case, case for a document)
        private final RelatedEntity relatedEntity;

        public RecentActivity(String type, String description, LocalDateTime createdDate, String icon,
                             PerformedBy performedBy, TargetEntity targetEntity, RelatedEntity relatedEntity) {
            this.type = type;
            this.description = description;
            this.createdDate = createdDate;
            this.icon = icon;
            this.performedBy = performedBy;
            this.targetEntity = targetEntity;
            this.relatedEntity = relatedEntity;
            this.timeAgo = calculateTimeAgo(createdDate);
        }

        public String getType() { return type; }
        public String getDescription() { return description; }
        public LocalDateTime getCreatedDate() { return createdDate; }
        public String getIcon() { return icon; }
        public PerformedBy getPerformedBy() { return performedBy; }
        public TargetEntity getTargetEntity() { return targetEntity; }
        public RelatedEntity getRelatedEntity() { return relatedEntity; }

        public String getTimeAgo() { return timeAgo; }

        private String calculateTimeAgo(LocalDateTime dateTime) {
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

        public static class PerformedBy {
            private final Long id;
            private final String firstName;
            private final String lastName;
            private final String username;

            public PerformedBy(Long id, String firstName, String lastName, String username) {
                this.id = id;
                this.firstName = firstName;
                this.lastName = lastName;
                this.username = username;
            }

            public Long getId() { return id; }
            public String getFirstName() { return firstName; }
            public String getLastName() { return lastName; }
            public String getUsername() { return username; }
        }

        public static class TargetEntity {
            private final Long id;
            private final String name;
            private final String type;

            public TargetEntity(Long id, String name, String type) {
                this.id = id;
                this.name = name;
                this.type = type;
            }

            public Long getId() { return id; }
            public String getName() { return name; }
            public String getType() { return type; }
        }

        public static class RelatedEntity {
            private final Long id;
            private final String name;
            private final String type;

            public RelatedEntity(Long id, String name, String type) {
                this.id = id;
                this.name = name;
                this.type = type;
            }

            public Long getId() { return id; }
            public String getName() { return name; }
            public String getType() { return type; }
        }
    }

    /**
     * Create test activities for demonstration purposes
     */
    @Transactional
    public void createTestActivities() {
        // Create some test activity logs
        activityLogService.logActivity(
            ActivityLog.ActivityType.CLIENT_CREATED,
            "Test müvekkil eklendi",
            1L,
            "Ahmet Yılmaz",
            ActivityLog.EntityType.CLIENT
        );

        activityLogService.logActivity(
            ActivityLog.ActivityType.CASE_CREATED,
            "Test davası oluşturuldu",
            1L,
            "Trafik Kazası Davası",
            ActivityLog.EntityType.CASE,
            1L,
            "Ahmet Yılmaz",
            ActivityLog.EntityType.CLIENT,
            "Test dava detayları"
        );

        activityLogService.logActivity(
            ActivityLog.ActivityType.DOCUMENT_CREATED,
            "Test dokümanı yüklendi",
            1L,
            "Dilekçe.pdf",
            ActivityLog.EntityType.DOCUMENT,
            1L,
            "Ahmet Yılmaz",
            ActivityLog.EntityType.CLIENT,
            "Test doküman detayları"
        );

        System.out.println("DashboardService: Test activities created successfully");
    }
} 