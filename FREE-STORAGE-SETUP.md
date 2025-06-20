# 🆓 BEDAVA File Storage Setup Guide

## Option 1: Railway Volumes (RECOMMENDED - ZERO CONFIG)

### ✅ What's Already Done:
- ✅ `railway.toml` created with volume config
- ✅ `application-prod.properties` updated
- ✅ Upload path set to `/app/uploads`

### ⚙️ Railway Environment Variables:
```bash
UPLOAD_PROVIDER=local
UPLOAD_DIR=/app/uploads
JWT_SECRET=your-secure-jwt-secret
```

### 🚀 Deploy Steps:
1. Push to GitHub
2. Deploy to Railway  
3. Railway automatically creates persistent volume
4. Upload files → Saved to `/app/uploads` (persistent!)

### ✅ Benefits:
- ❌ NO external service needed
- ❌ NO API keys needed  
- ❌ NO configuration needed
- ✅ Files survive container restarts
- ✅ Completely FREE
- ✅ Works with ALL file types

---

## Option 2: Supabase Storage (1GB Free)

### 📋 Setup Steps:
1. Go to supabase.com
2. Create account
3. Create new project
4. Go to Storage → Create bucket: "aslaw-documents"
5. Copy Project URL and anon key

### ⚙️ Railway Environment Variables:
```bash
UPLOAD_PROVIDER=supabase
SUPABASE_URL=https://yourproject.supabase.co
SUPABASE_ANON_KEY=your-anon-key
JWT_SECRET=your-secure-jwt-secret
```

### ✅ Benefits:
- ✅ 1GB free storage
- ✅ CDN included (fast access)
- ✅ Real-time updates
- ✅ Professional service

---

## Option 3: ImgBB (Unlimited Images)

### 📋 Setup Steps:
1. Go to imgbb.com/api
2. Get free API key
3. Only works for images (PNG, JPG, GIF)

### ⚙️ Railway Environment Variables:
```bash
UPLOAD_PROVIDER=imgbb
IMGBB_API_KEY=your-api-key
JWT_SECRET=your-secure-jwt-secret
```

### ⚠️ Limitations:
- Only images (no PDF, Word docs)
- Good for profile pictures, case photos

---

## 🏆 RECOMMENDATION: Use Railway Volumes

**For your law office app, Railway Volumes is perfect because:**
- ✅ Works with legal documents (PDF, Word, etc.)
- ✅ Zero external dependencies
- ✅ Completely free
- ✅ No API rate limits
- ✅ Already configured and ready!

**Just deploy with these environment variables:**
```bash
UPLOAD_PROVIDER=local
UPLOAD_DIR=/app/uploads  
JWT_SECRET=6bd41f34f6af2ec7481ff05afa1a6db42337caf89a77de97cded7d86ce16faaa
``` 