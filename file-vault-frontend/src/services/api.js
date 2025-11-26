import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor - add token to all requests
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor - handle 401 errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

// Auth API
export const authAPI = {
  login: (credentials) => api.post('/auth/login', credentials),
  register: (userData) => api.post('/auth/register', userData),
}

// File API
export const fileAPI = {
  listFiles: (ownerId) => api.get(`/files?ownerId=${ownerId}`),
  
  getMetadata: (publicId) => api.get(`/files/${publicId}`),
  
  uploadFile: (ownerId, file, onUploadProgress) => {
    const formData = new FormData()
    formData.append('file', file)
    
    return api.post(`/files?ownerId=${ownerId}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress,
    })
  },
  
  updateFile: (publicId, ownerId, file, onUploadProgress) => {
    const formData = new FormData()
    formData.append('file', file)
    
    return api.put(`/files/${publicId}?ownerId=${ownerId}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress,
    })
  },
  
  downloadFile: (publicId, ownerId) => {
    return api.get(`/files/${publicId}/content?ownerId=${ownerId}`, {
      responseType: 'blob',
    })
  },
  
  deleteFile: (publicId, ownerId) => 
    api.delete(`/files/${publicId}?ownerId=${ownerId}`),
}

export default api
