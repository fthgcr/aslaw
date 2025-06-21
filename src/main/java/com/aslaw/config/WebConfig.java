package com.aslaw.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Railway'de static resource handling'i devre dışı bırak
        if (contextPath != null && contextPath.equals("/api")) {
            // Static resource mapping'leri kaldır
            registry.addResourceHandler("/static/**")
                    .addResourceLocations("classpath:/static/")
                    .setCachePeriod(0);
        } else {
            // Local development için normal static resource handling
            registry.addResourceHandler("/static/**")
                    .addResourceLocations("classpath:/static/");
            registry.addResourceHandler("/webjars/**")
                    .addResourceLocations("classpath:/META-INF/resources/webjars/");
        }
    }
} 