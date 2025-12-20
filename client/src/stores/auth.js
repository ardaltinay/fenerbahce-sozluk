import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/services/api'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(JSON.parse(localStorage.getItem('user')) || null)
  const token = ref(localStorage.getItem('token') || null)
  const loading = ref(false)
  const error = ref(null)

  const isAuthenticated = computed(() => !!token.value)
  const username = computed(() => user.value?.username || '')

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

      localStorage.setItem('token', data.token)
      localStorage.setItem('user', JSON.stringify(user.value))

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

      localStorage.setItem('token', data.token)
      localStorage.setItem('user', JSON.stringify(user.value))

      return { success: true, message: data.message }
    } catch (err) {
      error.value = err.response?.data?.message || err.response?.data?.errors || 'Kayıt başarısız'
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  function logout() {
    user.value = null
    token.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

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
    canDeleteUser,
    canDeleteTopic,
    login,
    register,
    logout,
    setUser,
  }
})

