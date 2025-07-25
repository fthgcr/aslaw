package com.aslaw.config;

import com.aslaw.security.LawUserDetailsService;
import com.infracore.security.JwtAuthenticationFilter;
import com.infracore.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;

@Configuration("lawSecurityConfig")
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class LawSecurityConfig {

    private final LawUserDetailsService lawUserDetailsService;

    @Value("${app.cors.allowed-origins:http://localhost:*,https://localhost:*,https://*.onrender.com,https://*.netlify.app,https://*.vercel.app}")
    private String[] allowedOrigins;

    @Bean("lawPasswordEncoder")
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Primary
    public DaoAuthenticationProvider daoAuthenticationProvider(@Qualifier("lawPasswordEncoder") PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(lawUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        return new JwtAuthenticationFilter(jwtTokenProvider, lawUserDetailsService);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, 
            JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        
        return http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(daoAuthenticationProvider(passwordEncoder()))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - context-path /api ile birlikte
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/law/auth/**").permitAll()
                .requestMatchers("/public/**").permitAll()
                
                // Documentation
                .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
                
                // Health checks
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/health/**").permitAll()
                .requestMatchers("/test/**").permitAll()
                
                // API endpoints - context-path ile birlikte çalışacak
                .requestMatchers("/api/clients/**").permitAll()
                .requestMatchers("/api/cases/**").permitAll()
                .requestMatchers("/api/admin/**").permitAll()
                .requestMatchers("/api/dashboard/**").permitAll()
                .requestMatchers("/api/documents/**").permitAll()
                .requestMatchers("/api/roles/**").permitAll()
                .requestMatchers("/api/user/**").permitAll()
                
                // Static resources
                .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**").permitAll()
                
                // OPTIONS requests
                .requestMatchers("OPTIONS", "/**").permitAll()
                
                // All other requests need authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Environment variable'dan veya default değerlerden origin'leri al
        configuration.setAllowedOriginPatterns(Arrays.asList(allowedOrigins));
        
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
} 