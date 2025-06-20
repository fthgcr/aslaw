# Railway Environment Variables Setup

## Required Environment Variables

Add these in Railway Dashboard → Project → Variables:

### File Upload (Cloudinary)
```
UPLOAD_PROVIDER=cloudinary
CLOUDINARY_CLOUD_NAME=your-cloud-name-from-dashboard
CLOUDINARY_API_KEY=your-api-key-from-dashboard  
CLOUDINARY_API_SECRET=your-api-secret-from-dashboard
```

### Security
```
JWT_SECRET=your-super-secure-256-bit-jwt-secret-key-here
```

### Database (Auto-configured by Railway)
```
DATABASE_URL=postgresql://... (Railway sets this automatically)
PORT=8080 (Railway sets this automatically)
```

## How to Set in Railway:

1. Go to railway.app
2. Select your project
3. Go to Variables tab
4. Click "New Variable"
5. Add each variable one by one:
   - Variable: UPLOAD_PROVIDER
   - Value: cloudinary
6. Repeat for all variables above

## Testing Upload Provider:

### Local Development:
- Uses UPLOAD_PROVIDER=local (default)
- Files saved to uploads/ folder

### Production (Railway):
- Uses UPLOAD_PROVIDER=cloudinary 
- Files saved to Cloudinary cloud storage
- Returns public URLs for direct access 