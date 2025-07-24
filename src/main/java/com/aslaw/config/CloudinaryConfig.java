package com.aslaw.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "app.upload.provider", havingValue = "cloudinary", matchIfMissing = false)
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name:dummy}")
    private String cloudName;

    @Value("${cloudinary.api-key:dummy}")
    private String apiKey;

    @Value("${cloudinary.api-secret:dummy}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        log.info("🔧 Initializing Cloudinary with cloud-name: {}", cloudName);
        
        if ("dummy".equals(cloudName) || "dummy".equals(apiKey) || "dummy".equals(apiSecret)) {
            log.warn("⚠️ Cloudinary credentials not set! Using dummy configuration.");
            // Return a dummy cloudinary instance for local development
            return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dummy",
                "api_key", "dummy",
                "api_secret", "dummy"
            ));
        }
        
        log.info("✅ Cloudinary configured successfully for cloud: {}", cloudName);
        return new Cloudinary(ObjectUtils.asMap(
            "cloud_name", cloudName,
            "api_key", apiKey,
            "api_secret", apiSecret
        ));
    }
} 