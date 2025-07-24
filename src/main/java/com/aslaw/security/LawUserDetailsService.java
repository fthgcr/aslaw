package com.aslaw.security;

import com.aslaw.entity.LawUser;
import com.aslaw.repository.LawUserRepository;
import com.infracore.entity.User;
import com.infracore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Primary
@RequiredArgsConstructor
public class LawUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final LawUserRepository lawUserRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            System.out.println("LawUserDetailsService: Loading user: " + username);
            
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            
            System.out.println("LawUserDetailsService: User found: " + user.getUsername() + ", active: " + user.isActive());
            System.out.println("LawUserDetailsService: User password (encoded): " + user.getPassword());
            System.out.println("LawUserDetailsService: User enabled: " + user.isEnabled());
            
            // Check if user account is active
            if (!user.isActive()) {
                throw new UsernameNotFoundException("ACCOUNT_INACTIVE:" + username);
            }
            
            // Check if this user is also a law user
            Optional<LawUser> lawUser = lawUserRepository.findByUsername(username);
            
            if (lawUser.isPresent()) {
                System.out.println("LawUserDetailsService: Law user found with roles: " + lawUser.get().getLawRoles().size());
                // Create UserPrincipal with both base roles and law roles
                return LawUserPrincipal.create(user, lawUser.get());
            } else {
                System.out.println("LawUserDetailsService: Regular user, base roles only: " + user.getRoles().size());
                // Create regular UserPrincipal with only base roles
                return LawUserPrincipal.create(user, null);
            }
        } catch (Exception e) {
            System.out.println("LawUserDetailsService: Error loading user " + username + " - " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
} 