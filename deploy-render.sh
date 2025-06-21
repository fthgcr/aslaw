#!/bin/bash

# 🚀 Render.com Deployment Script
# Bu script Render deployment öncesi kontrolleri yapar

echo "🔍 Render.com Deployment Kontrolü Başlatılıyor..."

# Renk kodları
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Gerekli dosyaları kontrol et
echo -e "${BLUE}📁 Gerekli dosyalar kontrol ediliyor...${NC}"

required_files=(
    "render.yaml"
    "Dockerfile"
    "src/main/resources/application-prod.yml"
    "pom.xml"
)

for file in "${required_files[@]}"; do
    if [ -f "$file" ]; then
        echo -e "${GREEN}✅ $file mevcut${NC}"
    else
        echo -e "${RED}❌ $file eksik!${NC}"
        exit 1
    fi
done

# Maven build test
echo -e "${BLUE}🔨 Maven build test ediliyor...${NC}"
if command -v mvn &> /dev/null; then
    echo "Maven sürümü: $(mvn -version | head -n 1)"
    
    # Test build (dependencies check)
    echo "Dependencies kontrol ediliyor..."
    mvn dependency:resolve -q
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Maven dependencies OK${NC}"
    else
        echo -e "${YELLOW}⚠️  Maven dependencies sorun olabilir${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  Maven bulunamadı, build test atlanıyor${NC}"
fi

# GitHub token kontrolü
echo -e "${BLUE}🔑 GitHub Token kontrolü...${NC}"
if [ -z "$GITHUB_TOKEN" ]; then
    echo -e "${YELLOW}⚠️  GITHUB_TOKEN environment variable set edilmemiş${NC}"
    echo -e "${YELLOW}   Render Dashboard'dan manuel eklemeyi unutmayın!${NC}"
else
    echo -e "${GREEN}✅ GITHUB_TOKEN set edilmiş${NC}"
fi

# Application properties kontrolü
echo -e "${BLUE}⚙️  Application properties kontrolü...${NC}"
prod_props="src/main/resources/application-prod.yml"

if grep -q "DATABASE_URL" "$prod_props"; then
    echo -e "${GREEN}✅ Database URL konfigürasyonu OK${NC}"
else
    echo -e "${RED}❌ DATABASE_URL konfigürasyonu eksik!${NC}"
fi

if grep -q "JWT_SECRET" "$prod_props"; then
    echo -e "${GREEN}✅ JWT Secret konfigürasyonu OK${NC}"
else
    echo -e "${RED}❌ JWT_SECRET konfigürasyonu eksik!${NC}"
fi

# Render.yaml validation
echo -e "${BLUE}📋 render.yaml validation...${NC}"
if grep -q "aslaw-backend" "render.yaml"; then
    echo -e "${GREEN}✅ Service name OK${NC}"
else
    echo -e "${RED}❌ Service name eksik!${NC}"
fi

if grep -q "aslaw-db" "render.yaml"; then
    echo -e "${GREEN}✅ Database name OK${NC}"
else
    echo -e "${RED}❌ Database name eksik!${NC}"
fi

# Port kontrolü
echo -e "${BLUE}🌐 Port konfigürasyonu...${NC}"
if grep -q "PORT:8080" "$prod_props"; then
    echo -e "${GREEN}✅ Port konfigürasyonu OK${NC}"
else
    echo -e "${YELLOW}⚠️  PORT environment variable konfigürasyonu kontrol edin${NC}"
fi

# Dockerfile kontrolü
echo -e "${BLUE}🐳 Dockerfile kontrolü...${NC}"
if grep -q "GITHUB_TOKEN" "Dockerfile"; then
    echo -e "${GREEN}✅ GitHub authentication OK${NC}"
else
    echo -e "${RED}❌ GitHub authentication eksik!${NC}"
fi

if grep -q "actuator/health" "Dockerfile"; then
    echo -e "${GREEN}✅ Health check OK${NC}"
else
    echo -e "${YELLOW}⚠️  Health check konfigürasyonu kontrol edin${NC}"
fi

echo ""
echo -e "${BLUE}📋 DEPLOYMENT CHECKLIST:${NC}"
echo ""
echo "🔧 RENDER DASHBOARD'DA YAPILACAKLAR:"
echo "   1. GitHub Token ekle: GITHUB_TOKEN=ghp_xxxxxxxxxxxx"
echo "   2. Database oluştur: aslaw-db (PostgreSQL)"
echo "   3. Service deploy et: Blueprint ile"
echo ""
echo "🌐 DEPLOYMENT SONRASI KONTROLLER:"
echo "   • Health Check: https://your-app.onrender.com/actuator/health"
echo "   • API Docs: https://your-app.onrender.com/swagger-ui.html"
echo "   • Database: Render Dashboard → Database → Connect"
echo ""
echo -e "${GREEN}🚀 Deployment için hazır! Render.com'a gidebilirsiniz.${NC}"
echo ""
echo -e "${YELLOW}📖 Detaylı rehber: RENDER-DATABASE-MANAGEMENT.md${NC}" 