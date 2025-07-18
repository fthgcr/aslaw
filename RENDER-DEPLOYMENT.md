# 🚀 Render.com Deployment Rehberi

Bu rehber, **aslaw-backend** projesini Render.com'da nasıl deploy edeceğinizi gösterir.

## 📋 Ön Hazırlık

### 1. GitHub Token Oluşturma
GitHub Packages'dan `infra-core` dependency'sini çekebilmek için:

1. GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
2. **Generate new token (classic)**
3. Scope'lar:
   - ✅ `read:packages` (GitHub Packages okuma)
   - ✅ `repo` (private repository erişimi)
4. Token'ı kopyalayın (örn: `ghp_xxxxxxxxxxxx`)

### 2. Gerekli Dosyalar
Projenizde şu dosyalar bulunmalı:
- ✅ `render.yaml` - Render konfigürasyonu
- ✅ `Dockerfile.render` - Optimize edilmiş Dockerfile
- ✅ `application-prod.yml` - Production ayarları

## 🔧 Render.com'da Deployment

### Adım 1: Render Hesabı Oluşturma
1. [render.com](https://render.com) → Sign up with GitHub
2. GitHub hesabınızla giriş yapın

### Adım 2: Yeni Service Oluşturma
1. Render Dashboard → **New +** → **Blueprint**
2. **Connect a repository** → `aslaw` projesini seçin
3. **Blueprint Name**: `aslaw-backend`
4. **Branch**: `main`

### Adım 3: Environment Variables Ekleme
Render Dashboard'da service'inizi seçin → **Environment** sekmesi:

```bash
# GitHub Authentication (ZORUNLU)
GITHUB_USERNAME=fthgcr
GITHUB_TOKEN=ghp_xxxxxxxxxxxx  # Yukarıda oluşturduğunuz token

# Spring Profile
SPRING_PROFILES_ACTIVE=prod

# Database (Otomatik oluşturulacak)
SPRING_DATASOURCE_URL=<render-db-url>
SPRING_DATASOURCE_USERNAME=<render-db-user>
SPRING_DATASOURCE_PASSWORD=<render-db-password>

# JWT Secret (Render otomatik oluşturacak)
JWT_SECRET=<auto-generated>

# File Upload
UPLOAD_PROVIDER=local
UPLOAD_DIR=/app/uploads
```

### Adım 4: Database Kurulumu
1. Render Dashboard → **New +** → **PostgreSQL**
2. **Name**: `aslaw-db`
3. **Database Name**: `aslaw`
4. **User**: `aslaw_user`
5. **Plan**: Free
6. **Region**: Frankfurt

### Adım 5: Service Bağlantısı
1. Web Service → **Environment** → **Add Environment Variable**
2. Database connection string'leri ekleyin:
   ```
   SPRING_DATASOURCE_URL=postgresql://...
   SPRING_DATASOURCE_USERNAME=aslaw_user
   SPRING_DATASOURCE_PASSWORD=...
   ```

## 📁 Dockerfile Seçimi

Render.com için `Dockerfile.render` kullanın:

```dockerfile
# render.yaml dosyasında
services:
  - type: web
    dockerfilePath: ./Dockerfile.render
```

## 🔍 Deployment Kontrolü

### Health Check
- URL: `https://your-app.onrender.com/actuator/health`
- Beklenen yanıt: `{"status":"UP"}`

### API Documentation
- Swagger UI: `https://your-app.onrender.com/swagger-ui.html`
- API Docs: `https://your-app.onrender.com/api-docs`

## 🚨 Yaygın Sorunlar ve Çözümler

### 1. GitHub Packages 401 Hatası
**Sorun**: `infra-core` dependency çekilemiyor
**Çözüm**: 
- `GITHUB_TOKEN` environment variable'ını kontrol edin
- Token'ın `read:packages` yetkisine sahip olduğundan emin olun

### 2. Database Connection Hatası
**Sorun**: PostgreSQL bağlantı hatası
**Çözüm**:
- Database'in aynı region'da olduğundan emin olun
- Connection string'in doğru formatda olduğunu kontrol edin

### 3. Build Timeout
**Sorun**: Maven build çok uzun sürüyor
**Çözüm**:
- Render'da build timeout'u artırın
- Multi-stage Dockerfile kullanın (zaten kullanılıyor)

### 4. Memory Hatası
**Sorun**: Container memory limit aşılıyor
**Çözüm**:
- JVM heap size'ı ayarlayın: `-Xmx512m`
- Render'da plan upgrade yapın

## 🔧 Render.yaml Konfigürasyonu

```yaml
services:
  - type: web
    name: aslaw-backend
    env: docker
    dockerfilePath: ./Dockerfile.render
    plan: free
    region: frankfurt
    branch: main
    buildCommand: ""
    startCommand: ""
    healthCheckPath: /actuator/health
    envVars:
      - key: GITHUB_USERNAME
        value: fthgcr
      - key: GITHUB_TOKEN
        sync: false
      - key: SPRING_PROFILES_ACTIVE
        value: prod
      # ... diğer env vars

databases:
  - name: aslaw-db
    databaseName: aslaw
    user: aslaw_user
    plan: free
    region: frankfurt
```

## 📊 Monitoring

### Logs
- Render Dashboard → Service → **Logs**
- Real-time log streaming

### Metrics
- Render Dashboard → Service → **Metrics**
- CPU, Memory, Network kullanımı

### Alerts
- Render Dashboard → Service → **Settings** → **Alerts**
- Deploy failure, service down alerts

## 🎯 Production Checklist

- [ ] GitHub token oluşturuldu ve eklendi
- [ ] Database oluşturuldu ve bağlandı
- [ ] Environment variables eklendi
- [ ] Health check çalışıyor
- [ ] API documentation erişilebilir
- [ ] SSL certificate aktif (Render otomatik)
- [ ] Custom domain eklendi (opsiyonel)

## 🆘 Destek

Sorun yaşarsanız:
1. Render Dashboard → **Logs** kontrol edin
2. GitHub Issues'da sorun bildirin
3. Render Community'de yardım isteyin

---

✅ **Başarılı deployment sonrası:**
- API Base URL: `https://your-app.onrender.com`
- Health Check: `https://your-app.onrender.com/actuator/health`
- Swagger UI: `https://your-app.onrender.com/swagger-ui.html` 