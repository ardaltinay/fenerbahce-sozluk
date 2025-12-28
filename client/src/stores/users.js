import { defineStore } from 'pinia'
import { ref } from 'vue'
import { usersApi } from '@/services/api'

export const useUsersStore = defineStore('users', () => {
  const searchResults = ref([])
  const lastSearchQuery = ref('')
  const loading = ref(false)
  const error = ref(null)

  async function search(query) {
    if (!query || query.length < 2) {
      searchResults.value = []
      lastSearchQuery.value = ''
      return
    }

    // Return cached results if query matches last successful search
    if (query === lastSearchQuery.value) {
      return searchResults.value
    }

    loading.value = true
    error.value = null

    try {
      const response = await usersApi.search(query)
      searchResults.value = response.data || []
      lastSearchQuery.value = query
      return response.data
    } catch (err) {
      console.error('User search error:', err)
      error.value = err.response?.data?.message || 'Arama sırasında bir hata oluştu'
      searchResults.value = []
      lastSearchQuery.value = ''
      return []
    } finally {
      loading.value = false
    }
  }

  function clearSearch() {
    searchResults.value = []
    error.value = null
    loading.value = false
  }

  return {
    searchResults,
    loading,
    error,
    search,
    clearSearch
  }
})
