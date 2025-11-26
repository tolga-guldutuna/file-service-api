# FileVault Frontend 🚀

Modern, responsive React frontend for the FileVault File Management System.

## ✨ Features

- 🔐 **JWT Authentication** - Secure login/registration
- 📁 **File Management** - Upload, download, update, delete
- 🎨 **Modern UI** - Clean design with Tailwind CSS
- 📱 **Responsive** - Works on all devices
- ⚡ **Fast** - Built with Vite
- 🔄 **Real-time Progress** - Upload progress indicators
- 🗂️ **Drag & Drop** - Easy file uploads

## 🛠️ Tech Stack

- **React 18** - UI library
- **React Router** - Client-side routing
- **Tailwind CSS** - Utility-first CSS
- **Axios** - HTTP client
- **React Hook Form** - Form validation
- **Lucide React** - Beautiful icons
- **Vite** - Build tool

## 📋 Prerequisites

- Node.js 18+ and npm/yarn
- Backend API running on `http://localhost:8080`

## 🚀 Getting Started

### 1. Install Dependencies

```bash
npm install
```

### 2. Start Development Server

```bash
npm run dev
```

The app will be available at `http://localhost:3000`

### 3. Build for Production

```bash
npm run build
```

### 4. Preview Production Build

```bash
npm run preview
```

## 📁 Project Structure

```
file-vault-frontend/
├── src/
│   ├── components/         # Reusable components
│   │   ├── Layout.jsx      # Main layout with header
│   │   └── ProtectedRoute.jsx
│   ├── context/           # React Context
│   │   └── AuthContext.jsx
│   ├── pages/             # Page components
│   │   ├── Login.jsx      # Login/Register page
│   │   ├── Dashboard.jsx  # File list dashboard
│   │   ├── Upload.jsx     # File upload page
│   │   └── FileDetails.jsx # File details page
│   ├── services/          # API services
│   │   └── api.js         # Axios configuration
│   ├── App.jsx           # Main app component
│   ├── main.jsx          # Entry point
│   └── index.css         # Global styles
├── public/               # Static assets
├── index.html           # HTML template
├── vite.config.js       # Vite configuration
├── tailwind.config.js   # Tailwind configuration
└── package.json         # Dependencies
```

## 🔧 Configuration

### API Base URL

The API base URL is configured in `src/services/api.js`:

```javascript
const API_BASE_URL = 'http://localhost:8080/api'
```

To change it, update this constant or use an environment variable:

```javascript
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
```

Then create a `.env` file:

```bash
VITE_API_BASE_URL=https://your-api-domain.com/api
```

### Vite Proxy (Development)

For CORS issues during development, the Vite proxy is configured in `vite.config.js`:

```javascript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    }
  }
}
```

## 📱 Pages

### 1. Login/Register (`/login`)
- User authentication
- Email/password validation
- Auto-redirect after successful login
- Toggle between login and registration

### 2. Dashboard (`/dashboard`)
- List all user files
- Search and filter
- Quick actions (view, download, delete)
- Responsive table layout

### 3. Upload (`/upload`)
- Drag & drop file upload
- Multiple file selection
- Upload progress tracking
- File validation (type, size)
- Real-time status updates

### 4. File Details (`/file/:publicId`)
- File metadata display
- Download file
- Update file (replace content)
- Delete file
- File preview placeholder

## 🎨 Styling

### Tailwind Utility Classes

Common utility classes used throughout the app:

```css
.btn-primary     - Blue primary button
.btn-secondary   - Gray secondary button
.btn-danger      - Red danger button
.input-field     - Styled input field
.card            - White card with shadow
```

### Custom Colors

Primary color palette (blue):
- 50-900: Various shades of blue
- Default: `blue-600` (#2563eb)

## 🔐 Authentication Flow

1. User submits login/register form
2. API request to backend
3. Backend returns JWT token + user data
4. Token stored in localStorage
5. Token automatically added to all API requests via Axios interceptor
6. Protected routes check authentication status
7. Automatic redirect to login if token invalid/expired

## 📝 API Integration

All API calls are centralized in `src/services/api.js`:

### Auth API
- `authAPI.login(credentials)` - User login
- `authAPI.register(userData)` - User registration

### File API
- `fileAPI.listFiles(ownerId)` - Get all files
- `fileAPI.getMetadata(publicId)` - Get file details
- `fileAPI.uploadFile(ownerId, file, onProgress)` - Upload file
- `fileAPI.updateFile(publicId, ownerId, file)` - Update file
- `fileAPI.downloadFile(publicId, ownerId)` - Download file
- `fileAPI.deleteFile(publicId, ownerId)` - Delete file

## 🐛 Troubleshooting

### CORS Errors

If you encounter CORS errors:

1. Make sure backend has CORS configured
2. Use Vite proxy in development (already configured)
3. For production, configure CORS on backend to allow frontend domain

### Port Already in Use

If port 3000 is already in use, change it in `vite.config.js`:

```javascript
server: {
  port: 3001, // Change to any available port
}
```

### Token Expiration

If you get 401 errors:
1. Token might be expired
2. Clear localStorage and login again
3. Check token expiration time in backend configuration

## 🚀 Deployment

### Netlify / Vercel

1. Build the project:
```bash
npm run build
```

2. Deploy the `dist` folder

3. Set environment variable:
```bash
VITE_API_BASE_URL=https://your-backend-api.com/api
```

### Nginx

Example Nginx configuration:

```nginx
server {
    listen 80;
    server_name yourdomain.com;
    root /var/www/file-vault-frontend/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://backend:8080;
    }
}
```

## 📄 License

Apache License 2.0

## 👨‍💻 Developer

Built with ❤️ for the Etstur Java Developer Challenge

---

**Note**: Make sure the backend API is running before starting the frontend.
