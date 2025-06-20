package com.aslaw.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileUploadConfig {

    @Value("${app.upload.provider:local}")
    private String uploadProvider;

    @Value("${app.upload.dir:/app/uploads}")
    private String uploadDir;

    // ImgBB Configuration (Free unlimited image hosting)
    @Value("${imgbb.api.key:}")
    private String imgbbApiKey;

    // Supabase Configuration (1GB free storage)
    @Value("${supabase.url:}")
    private String supabaseUrl;

    @Value("${supabase.anon.key:}")
    private String supabaseAnonKey;

    // Getters
    public String getUploadProvider() { return uploadProvider; }
    public String getUploadDir() { return uploadDir; }
    public String getImgbbApiKey() { return imgbbApiKey; }
    public String getSupabaseUrl() { return supabaseUrl; }
    public String getSupabaseAnonKey() { return supabaseAnonKey; }
} 