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
  forgotPassword: (email) => api.post('/api/auth/forgot-password', { email }),
  resetPassword: (token, newPassword) => api.post('/api/auth/reset-password', { token, newPassword }),
  validateResetToken: (token) => api.get(`/api/auth/validate-reset-token/${token}`),
}

// Topics API
export const topicsApi = {
  getAll: (page = 0, size = 20) =>
    api.get(`/api/topics`, { params: { page, size } }),
  getPopular: (page = 0, size = 20) =>
    api.get(`/api/topics/popular`, { params: { page, size } }),
  getTrends: (page = 0, size = 50) =>
    api.get(`/api/topics/trends`, { params: { page, size } }),
  getById: (id) => api.get(`/api/topics/${id}`),

  search: (keyword, page = 0, size = 20) =>
    api.get(`/api/topics/search`, { params: { keyword, page, size } }),
  create: (topicData) => api.post('/api/topics', topicData),
  delete: (id, reason) => api.delete(`/api/topics/${id}`, { params: { reason } }),
  updateTransfermarkt: (id, transfermarktId, topicType) =>
    api.put(`/api/topics/${id}/transfermarkt`, { transfermarktId, topicType }),
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
  getRandom: (page = 0, size = 3) =>
    api.get(`/api/entries/random`, { params: { page, size } }),
  getRandomPopular: () =>
    api.get(`/api/entries/random-popular`),

  getById: (id) => api.get(`/api/entries/${id}`),
  getTopLikedByAuthor: (authorId, limit = 5) =>
    api.get(`/api/entries/author/${authorId}/top-liked`, { params: { limit } }),
  getTopFavoritedByAuthor: (authorId, limit = 5) =>
    api.get(`/api/entries/author/${authorId}/top-favorited`, { params: { limit } }),
  create: (entryData) => api.post('/api/entries', entryData),
  update: (id, content) => api.put(`/api/entries/${id}`, { content }),
  delete: (id, reason) => api.delete(`/api/entries/${id}`, { params: { reason } }),
}

// Votes API
export const votesApi = {
  vote: (voteData) => api.post('/api/votes', voteData),

}

// Users API
export const usersApi = {
  getByUsername: (username) => api.get(`/api/users/${username}`),
  getFavorites: (username) => api.get(`/api/users/${username}/favorites`),
  getMe: () => api.get('/api/users/me'),
  search: (query) => api.get('/api/users/search', { params: { q: query } }),
  changePassword: (currentPassword, newPassword) =>
    api.post('/api/users/me/change-password', { currentPassword, newPassword }),
  deleteAccount: (password) =>
    api.post('/api/users/me/delete-account', { password }),
  delete: (id) => api.delete(`/api/users/${id}`),
  ban: (id, duration, reason) => api.post(`/api/users/${id}/ban`, { duration, reason }),
  promote: (id) => api.post(`/api/users/${id}/promote`),
  demote: (id) => api.post(`/api/users/${id}/demote`),
}

// Contact API
export const contactApi = {
  send: (data) => api.post('/api/contact', data),
}

// Stats API
export const statsApi = {
  get: () => api.get('/api/stats'),
}

// Transfermarkt API
export const transfermarktApi = {
  searchPlayers: (name) => api.get(`/api/transfermarkt/search/players/${encodeURIComponent(name)}`),
  searchClubs: (name) => api.get(`/api/transfermarkt/search/clubs/${encodeURIComponent(name)}`),
  getPlayer: (id) => api.get(`/api/transfermarkt/player/${id}`),
  getClub: (id) => api.get(`/api/transfermarkt/club/${id}`),
  linkToTopic: (topicId, type, transfermarktId) => api.post(`/api/transfermarkt/topics/${topicId}/link`, { type, transfermarktId }),
  unlinkFromTopic: (topicId) => api.delete(`/api/transfermarkt/topics/${topicId}/unlink`),
}

export default api


