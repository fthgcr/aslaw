package com.aslaw.security;

import com.aslaw.entity.LawUser;
import com.aslaw.repository.LawUserRepository;
import com.infracore.entity.User;
import com.infracore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LawUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final LawUserRepository lawUserRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        // Check if user account is active
        if (!user.isActive()) {
            throw new UsernameNotFoundException("ACCOUNT_INACTIVE:" + username);
        }
        
        // Check if this user is also a law user
        Optional<LawUser> lawUser = lawUserRepository.findByUsername(username);
        
        if (lawUser.isPresent()) {
            // Create UserPrincipal with both base roles and law roles
            return LawUserPrincipal.create(user, lawUser.get());
        } else {
            // Create regular UserPrincipal with only base roles
            return LawUserPrincipal.create(user, null);
        }
    }
} 