# 🔧 Railway Deployment - GitHub Packages Fix

## Problem
Railway build error: `status code: 401, reason phrase: Unauthorized (401)` when accessing GitHub Packages for `infra-core` dependency.

## Solution: Add GitHub Authentication to Railway

### ✅ What's Fixed:
- ✅ Updated Dockerfile to create Maven settings.xml during build
- ✅ Added GitHub authentication support in Docker build

### 🔑 Required Railway Environment Variables:

Add these to Railway Dashboard → Variables:

```bash
# GitHub Packages Authentication
GITHUB_USERNAME=fthgcr
GITHUB_TOKEN=your-github-personal-access-token

# File Storage (Railway Volumes)
UPLOAD_PROVIDER=local
UPLOAD_DIR=/app/uploads

# Security
JWT_SECRET=6bd41f34f6af2ec7481ff05afa1a6db42337caf89a77de97cded7d86ce16faaa
```

### 🎯 How to Get GitHub Token:

1. Go to GitHub.com → Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Click "Generate new token (classic)"
3. Select scopes:
   - ✅ `read:packages` (to download packages)
   - ✅ `repo` (if private repos)
4. Copy the generated token
5. Add to Railway as `GITHUB_TOKEN`

### 🚀 Deploy Steps:

1. ✅ Push updated Dockerfile to GitHub
2. ✅ Add environment variables in Railway
3. ✅ Re-deploy from Railway dashboard
4. ✅ Build should succeed now!

### ⚡ Quick Railway Setup:

```bash
# Railway Dashboard → Variables → Add these:
GITHUB_USERNAME=fthgcr
GITHUB_TOKEN=ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
UPLOAD_PROVIDER=local
UPLOAD_DIR=/app/uploads
JWT_SECRET=6bd41f34f6af2ec7481ff05afa1a6db42337caf89a77de97cded7d86ce16faaa
```

Then click "Deploy" again! 