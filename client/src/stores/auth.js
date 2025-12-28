import { defineStore } from 'pinia'
import { ref, computed, onUnmounted } from 'vue'
import { authApi } from '@/services/api'

// Session duration: 24 hours in milliseconds
const SESSION_DURATION = 24 * 60 * 60 * 1000

export const useAuthStore = defineStore('auth', () => {
  const user = ref(JSON.parse(localStorage.getItem('user')) || null)
  const token = ref(localStorage.getItem('token') || null)
  const tokenExpiresAt = ref(localStorage.getItem('tokenExpiresAt') ? parseInt(localStorage.getItem('tokenExpiresAt')) : null)
  const loading = ref(false)
  const error = ref(null)
  let expirationTimer = null

  const isAuthenticated = computed(() => !!token.value && !isTokenExpired())
  const username = computed(() => user.value?.username || '')

  // Check if token is expired
  function isTokenExpired() {
    if (!tokenExpiresAt.value) return true
    return Date.now() >= tokenExpiresAt.value
  }

  // Setup expiration timer
  function setupExpirationTimer() {
    if (expirationTimer) {
      clearTimeout(expirationTimer)
    }

    if (!tokenExpiresAt.value) return

    const timeUntilExpiry = tokenExpiresAt.value - Date.now()

    if (timeUntilExpiry <= 0) {
      // Token already expired
      logout()
      window.location.href = '/giris?expired=true'
      return
    }

    // Set timer to logout when token expires
    expirationTimer = setTimeout(() => {
      logout()
      window.location.href = '/giris?expired=true'
    }, timeUntilExpiry)
  }

  // Check token on store initialization
  function initializeAuth() {
    if (token.value && isTokenExpired()) {
      logout()
    } else if (token.value) {
      setupExpirationTimer()
    }
  }

  // Role-based computed properties
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const isModerator = computed(() => user.value?.role === 'MODERATOR')
  const isModeratorOrAdmin = computed(() => isAdmin.value || isModerator.value)

  // Permission helper functions
  function canEditEntry(entry) {
    if (!isAuthenticated.value) return false
    const isOwner = entry.authorUsername === username.value || entry.author?.username === username.value
    return isOwner || isModeratorOrAdmin.value
  }

  function canDeleteEntry(entry) {
    if (!isAuthenticated.value) return false
    const isOwner = entry.authorUsername === username.value || entry.author?.username === username.value
    return isOwner || isModeratorOrAdmin.value
  }

  function canDeleteUser(targetUsername) {
    if (!isAuthenticated.value) return false
    if (!isAdmin.value) return false
    if (targetUsername === username.value) return false // Can't delete yourself
    return true
  }

  function canBanUser(targetRole) {
    if (!isAuthenticated.value) return false
    if (targetRole === 'ADMIN') return false // No one can ban/unban admins via UI

    if (isAdmin.value) return true // Admin can ban Moderator or User

    if (isModerator.value) {
      // Moderator can only ban/unban Users
      return targetRole === 'USER'
    }

    return false
  }

  async function login(credentials) {
    loading.value = true
    error.value = null
    try {
      const response = await authApi.login(credentials)
      const data = response.data

      user.value = {
        username: data.username,
        email: data.email,
        role: data.role,
      }
      token.value = data.token

      // Use server-provided expiration if available, otherwise use default
      const expiresIn = data.expiresIn || SESSION_DURATION
      tokenExpiresAt.value = Date.now() + expiresIn

      localStorage.setItem('token', data.token)
      localStorage.setItem('tokenExpiresAt', tokenExpiresAt.value.toString())
      localStorage.setItem('user', JSON.stringify(user.value))

      setupExpirationTimer()

      return { success: true, message: data.message }
    } catch (err) {
      error.value = err.response?.data?.message || 'Giriş başarısız'
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  async function register(userData) {
    loading.value = true
    error.value = null
    try {
      const response = await authApi.register(userData)
      const data = response.data

      user.value = {
        username: data.username,
        email: data.email,
        role: data.role,
      }
      token.value = data.token
      tokenExpiresAt.value = Date.now() + SESSION_DURATION

      localStorage.setItem('token', data.token)
      localStorage.setItem('tokenExpiresAt', tokenExpiresAt.value.toString())
      localStorage.setItem('user', JSON.stringify(user.value))

      setupExpirationTimer()

      return { success: true, message: data.message }
    } catch (err) {
      error.value = err.response?.data?.message || err.response?.data?.errors || 'Kayıt başarısız'
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  function logout() {
    if (expirationTimer) {
      clearTimeout(expirationTimer)
      expirationTimer = null
    }
    user.value = null
    token.value = null
    tokenExpiresAt.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('tokenExpiresAt')
    localStorage.removeItem('user')
  }

  // Initialize auth check on store creation
  initializeAuth()

  function setUser(userData) {
    user.value = userData
  }

  function canDeleteTopic(topic) {
    if (!isAuthenticated.value) return false
    const isOwner = topic.authorUsername === username.value
    return isOwner || isModeratorOrAdmin.value
  }

  return {
    user,
    token,
    loading,
    error,
    isAuthenticated,
    username,
    isAdmin,
    isModerator,
    isModeratorOrAdmin,
    canEditEntry,
    canDeleteEntry,
    canDeleteEntry,
    canDeleteUser,
    canBanUser,
    canDeleteTopic,
    login,
    register,
    logout,
    setUser,
  }
})

