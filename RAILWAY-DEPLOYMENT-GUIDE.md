# Railway Deployment Guide

## ✅ Tamamlanan Konfigürasyonlar

### 1. Railway Servisleri
- ✅ **PostgreSQL Database** oluşturuldu
- ✅ **Web Service** (vibrant-dedication) oluşturuldu
- ✅ **Database bağlantısı** yapılandırıldı

### 2. Environment Variables
- ✅ `DATABASE_URL` → PostgreSQL bağlantısı
- ✅ `SPRING_PROFILES_ACTIVE` → railway
- ✅ `JWT_SECRET` → Otomatik oluşturuldu
- ✅ `GITHUB_USERNAME` → fthgcr
- ❌ `GITHUB_TOKEN` → **Manuel eklenmesi gerekiyor**

### 3. Konfigürasyon Dosyaları
- ✅ `railway.toml` → Railway build/deploy ayarları
- ✅ `Dockerfile.railway` → Railway için optimize edilmiş
- ✅ `application-railway.yml` → Railway için Spring konfigürasyonu

## 🚀 Deployment Adımları

### 1. GitHub Token Ekleme
```bash
# GitHub Personal Access Token oluşturun:
# GitHub → Settings → Developer settings → Personal access tokens
# Scopes: read:packages, repo

# Token'ı Railway'e ekleyin:
railway variables --set GITHUB_TOKEN=your_token_here
```

### 2. Deployment Başlatma
```bash
# Mevcut dizinde olduğunuzdan emin olun
cd aslaw

# Deploy işlemini başlatın
railway up
```

### 3. Deployment İzleme
```bash
# Log'ları izleyin
railway logs

# Service durumunu kontrol edin
railway status

# URL'i alın
railway domain
```

## 🔧 Konfigürasyon Detayları

### Database
- **Type**: PostgreSQL
- **Connection**: Otomatik `${{Postgres.DATABASE_URL}}`
- **Pool Size**: 5 connection
- **Schema**: Liquibase ile otomatik migration

### Health Check
- **Path**: `/api/actuator/health`
- **Timeout**: 300 saniye
- **Start Period**: 60 saniye

### File Uploads
- **Volume**: `aslaw-uploads`
- **Mount Path**: `/app/uploads`
- **Max Size**: 10MB

### Security
- **JWT**: Otomatik secret key
- **HTTPS**: Railway otomatik sağlar
- **Non-root user**: Güvenlik için appuser

## 📋 Troubleshooting

### Build Hatası
```bash
# Log'ları kontrol edin
railway logs --build

# Environment variable'ları kontrol edin
railway variables
```

### Database Bağlantı Sorunu
```bash
# Database servisini kontrol edin
railway service Postgres
railway variables
```

### GitHub Packages Hatası
```bash
# Token'ın doğru olduğunu kontrol edin
railway variables --set GITHUB_TOKEN=new_token
```

## 🌐 Post-Deployment

### 1. Domain Ayarlama
```bash
# Custom domain eklemek için
railway domain add your-domain.com
```

### 2. SSL Sertifikası
- Railway otomatik SSL sağlar
- Custom domain için DNS ayarları gerekli

### 3. Monitoring
- Railway Dashboard'da metrics mevcut
- `/api/actuator/health` endpoint'i health check için

## 📊 Resource Limits

### Free Plan
- **RAM**: 512MB
- **CPU**: 0.5 vCPU
- **Database**: 1GB storage
- **Build Time**: 10 dakika
- **Monthly Usage**: $5 credit

### Optimizasyonlar
- Multi-stage Docker build
- Connection pooling
- Health check optimizasyonu
- Volume mounting 