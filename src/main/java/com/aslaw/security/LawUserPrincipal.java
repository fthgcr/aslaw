package com.aslaw.security;

import com.aslaw.entity.LawRole;
import com.aslaw.entity.LawUser;
import com.infracore.entity.Role;
import com.infracore.entity.User;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Builder
public class LawUserPrincipal implements UserDetails {
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean accountNonExpired;
    private final boolean accountNonLocked;
    private final boolean credentialsNonExpired;
    private final boolean enabled;
    private final Set<Role> roles;
    private final Set<LawRole> lawRoles;

    public static LawUserPrincipal create(User user, LawUser lawUser) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Add base role authorities
        if (user.getRoles() != null) {
            user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                    .forEach(authorities::add);
        }
        
        // Add law role authorities if law user exists
        if (lawUser != null && lawUser.getLawRoles() != null) {
            lawUser.getLawRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                    .forEach(authorities::add);
        }

        return LawUserPrincipal.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(user.isEnabled())
                .roles(user.getRoles())
                .lawRoles(lawUser != null ? lawUser.getLawRoles() : null)
                .build();
    }

    // Helper methods for role checking
    public boolean hasRole(Role.RoleName roleName) {
        return roles != null && roles.stream()
                .anyMatch(role -> role.getName().equals(roleName));
    }

    public boolean hasLawRole(LawRole.LawRoleName lawRoleName) {
        return lawRoles != null && lawRoles.stream()
                .anyMatch(role -> role.getName().equals(lawRoleName));
    }

    public Set<String> getRoleNames() {
        if (roles == null) return null;
        return roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
    }

    public Set<String> getLawRoleNames() {
        if (lawRoles == null) return null;
        return lawRoles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
    }

    public String getPrimaryRole() {
        // Return law role if available, otherwise base role
        if (lawRoles != null && !lawRoles.isEmpty()) {
            return lawRoles.stream()
                    .findFirst()
                    .map(role -> role.getName().name())
                    .orElse("USER");
        }
        
        if (roles != null && !roles.isEmpty()) {
            return roles.stream()
                    .findFirst()
                    .map(role -> role.getName().name())
                    .orElse("USER");
        }
        
        return "USER";
    }
} 