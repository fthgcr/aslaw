package com.aslaw.entity;

import com.infracore.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "law_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LawUser {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true)
    private User user;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "law_user_roles",
        joinColumns = @JoinColumn(name = "law_user_id"),
        inverseJoinColumns = @JoinColumn(name = "law_role_id")
    )
    private Set<LawRole> lawRoles = new HashSet<>();
    
    @Column(name = "bar_number", unique = true)
    private String barNumber; // Baro numarası (avukatlar için)
    
    @Column(name = "specialization")
    private String specialization; // Uzmanlık alanı
    
    @Column(name = "experience_years")
    private Integer experienceYears; // Deneyim yılı
    
    @Column(name = "law_school")
    private String lawSchool; // Hukuk fakültesi
    
    @Column(name = "graduation_year")
    private Integer graduationYear; // Mezuniyet yılı
    
    @Column(name = "created_date")
    private LocalDateTime createdDate;
    
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
    
    @Column(name = "is_active")
    private Boolean active = true;
    
    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
    
    // Convenience methods to access User properties
    public String getUsername() {
        return user != null ? user.getUsername() : null;
    }
    
    public String getEmail() {
        return user != null ? user.getEmail() : null;
    }
    
    public String getFirstName() {
        return user != null ? user.getFirstName() : null;
    }
    
    public String getLastName() {
        return user != null ? user.getLastName() : null;
    }
    
    public String getFullName() {
        if (user != null) {
            return user.getFirstName() + " " + user.getLastName();
        }
        return null;
    }
    
    public boolean isEnabled() {
        return user != null ? user.isEnabled() : false;
    }
    
    public boolean isUserActive() {
        return user != null ? user.isActive() : false;
    }
    
    public String getPhoneNumber() {
        return user != null ? user.getPhoneNumber() : null;
    }
    
    public String getAddress() {
        return user != null ? user.getAddress() : null;
    }
    
    // Law role management methods
    public void addLawRole(LawRole lawRole) {
        if (this.lawRoles == null) {
            this.lawRoles = new HashSet<>();
        }
        this.lawRoles.add(lawRole);
    }
    
    public void removeLawRole(LawRole lawRole) {
        if (this.lawRoles != null) {
            this.lawRoles.remove(lawRole);
        }
    }
    
    public boolean hasLawRole(LawRole.LawRoleName lawRoleName) {
        if (this.lawRoles == null) {
            return false;
        }
        return this.lawRoles.stream()
                .anyMatch(role -> role.getName().equals(lawRoleName));
    }
    
    public Set<String> getLawRoleNames() {
        Set<String> roleNames = new HashSet<>();
        if (this.lawRoles != null) {
            this.lawRoles.forEach(role -> roleNames.add(role.getName().name()));
        }
        return roleNames;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<LawRole> getLawRoles() {
        return lawRoles;
    }

    public void setLawRoles(Set<LawRole> lawRoles) {
        this.lawRoles = lawRoles;
    }

    public String getBarNumber() {
        return barNumber;
    }

    public void setBarNumber(String barNumber) {
        this.barNumber = barNumber;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public String getLawSchool() {
        return lawSchool;
    }

    public void setLawSchool(String lawSchool) {
        this.lawSchool = lawSchool;
    }

    public Integer getGraduationYear() {
        return graduationYear;
    }

    public void setGraduationYear(Integer graduationYear) {
        this.graduationYear = graduationYear;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}