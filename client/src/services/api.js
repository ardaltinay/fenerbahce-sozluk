import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8081'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor - JWT token ekle
api.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor - 401 hatalarını yakala
api.interceptors.response.use(
  (response) => response,
  (error) => {
    // Auth endpoint'lerinde 401 alınca redirect yapma
    const isAuthEndpoint = error.config?.url?.includes('/api/auth/')
    const isOnAuthPage = window.location.pathname === '/giris' || window.location.pathname === '/kayit'

    if (error.response?.status === 401 && !isAuthEndpoint && !isOnAuthPage) {
      const authStore = useAuthStore()
      authStore.logout()
      window.location.href = '/giris'
    }
    return Promise.reject(error)
  }
)

// Auth API
export const authApi = {
  login: (credentials) => api.post('/api/auth/login', credentials),
  register: (userData) => api.post('/api/auth/register', userData),
}

// Topics API
export const topicsApi = {
  getAll: (page = 0, size = 20) =>
    api.get(`/api/topics`, { params: { page, size } }),
  getPopular: (page = 0, size = 20) =>
    api.get(`/api/topics/popular`, { params: { page, size } }),
  getTrends: (page = 0, size = 50) =>
    api.get(`/api/topics/trends`, { params: { page, size } }),
  getByCategory: (categoryId, page = 0, size = 20) =>
    api.get(`/api/topics/category/${categoryId}`, { params: { page, size } }),
  getBySlug: (slug) => api.get(`/api/topics/slug/${slug}`),
  getById: (id) => api.get(`/api/topics/${id}`),
  search: (keyword, page = 0, size = 20) =>
    api.get(`/api/topics/search`, { params: { keyword, page, size } }),
  create: (topicData) => api.post('/api/topics', topicData),
  delete: (id) => api.delete(`/api/topics/${id}`),
}

// Entries API
export const entriesApi = {
  getByTopic: (topicId, page = 0, size = 20) =>
    api.get(`/api/entries/topic/${topicId}`, { params: { page, size } }),
  getByAuthor: (authorId, page = 0, size = 20) =>
    api.get(`/api/entries/author/${authorId}`, { params: { page, size } }),
  getPopular: (page = 0, size = 20) =>
    api.get(`/api/entries/popular`, { params: { page, size } }),
  getLatest: (page = 0, size = 20) =>
    api.get(`/api/entries/latest`, { params: { page, size } }),
  getHistory: (page = 0, size = 20) =>
    api.get(`/api/entries/history`, { params: { page, size } }),
  getById: (id) => api.get(`/api/entries/${id}`),
  create: (entryData) => api.post('/api/entries', entryData),
  update: (id, content) => api.put(`/api/entries/${id}`, { content }),
  delete: (id) => api.delete(`/api/entries/${id}`),
}

// Votes API
export const votesApi = {
  vote: (voteData) => api.post('/api/votes', voteData),
  removeVote: (entryId) => api.delete(`/api/votes/${entryId}`),
}

// Categories API
export const categoriesApi = {
  getAll: () => api.get('/api/categories'),
  getById: (id) => api.get(`/api/categories/${id}`),
  getBySlug: (slug) => api.get(`/api/categories/slug/${slug}`),
}

// Users API
export const usersApi = {
  getByUsername: (username) => api.get(`/api/users/${username}`),
  getMe: () => api.get('/api/users/me'),
  delete: (id) => api.delete(`/api/users/${id}`),
  ban: (username) => api.post(`/api/users/${username}/ban`),
}

export default api

