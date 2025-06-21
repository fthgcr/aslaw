package com.aslaw.config;

import com.aslaw.security.LawUserDetailsService;
import com.infracore.security.JwtAuthenticationFilter;
import com.infracore.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration("lawSecurityConfig")
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class LawSecurityConfig {

    private final LawUserDetailsService lawUserDetailsService;
    
    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Primary
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        return lawUserDetailsService;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider) {
        return new JwtAuthenticationFilter(jwtTokenProvider, lawUserDetailsService);
    }

    @Bean
    @Primary
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, 
            JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        
        // Context path'e göre matcher'ları ayarla
        final String authPattern;
        final String lawAuthPattern;
        final String publicPattern;
        final String swaggerPattern;
        final String apiDocsPattern;
        final String actuatorPattern;
        final String clientsPattern;
        final String casesPattern;
        final String adminPattern;
        final String dashboardPattern;
        final String documentsPattern;
        
        // Railway'de context-path /api olduğu için pattern'ları ayarla
        if (contextPath != null && contextPath.equals("/api")) {
            // Railway'de context path /api olduğu için, endpoint'ler /api prefix'i olmadan
            authPattern = "/auth/**";
            lawAuthPattern = "/law/auth/**";
            publicPattern = "/public/**";
            swaggerPattern = "/swagger-ui/**";
            apiDocsPattern = "/api-docs/**";
            actuatorPattern = "/actuator/**";
            clientsPattern = "/clients/**";
            casesPattern = "/cases/**";
            adminPattern = "/admin/**";
            dashboardPattern = "/dashboard/**";
            documentsPattern = "/documents/**";
        } else {
            // Local development için
            authPattern = "/api/auth/**";
            lawAuthPattern = "/api/law/auth/**";
            publicPattern = "/public/**";
            swaggerPattern = "/swagger-ui/**";
            apiDocsPattern = "/api-docs/**";
            actuatorPattern = "/actuator/**";
            clientsPattern = "/api/clients/**";
            casesPattern = "/api/cases/**";
            adminPattern = "/api/admin/**";
            dashboardPattern = "/api/dashboard/**";
            documentsPattern = "/api/documents/**";
        }
        
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("OPTIONS", "/**").permitAll()
                .requestMatchers(authPattern).permitAll()
                .requestMatchers(lawAuthPattern).permitAll()
                .requestMatchers(publicPattern).permitAll()
                .requestMatchers(swaggerPattern, apiDocsPattern).permitAll()
                .requestMatchers(actuatorPattern).permitAll()
                .requestMatchers("/health/**").permitAll()
                .requestMatchers("/test/**").permitAll()
                .requestMatchers("/**/test").permitAll()
                // API endpoint'leri için authentication gerekli
                .requestMatchers(clientsPattern).authenticated()
                .requestMatchers(casesPattern).authenticated()
                .requestMatchers(adminPattern).authenticated()
                .requestMatchers(dashboardPattern).authenticated()
                .requestMatchers(documentsPattern).authenticated()
                .anyRequest().authenticated())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    @Primary
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
} 