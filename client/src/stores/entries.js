import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { entriesApi, votesApi } from '@/services/api'

export const useEntriesStore = defineStore('entries', () => {
  const allEntries = ref([])
  const currentEntry = ref(null)
  const loading = ref(false)
  const error = ref(null)
  const totalPages = ref(0)
  const currentPage = ref(0)

  const entries = computed(() => allEntries.value)

  // Fetch race condition handling
  let currentFetchId = 0

  function clearEntries() {
    allEntries.value = []
    totalPages.value = 0
    currentPage.value = 0
    error.value = null
    currentFetchId++ // Invalidate any pending requests
  }

  async function fetchEntriesByTopic(topicId, page = 0, size = 10, clearFirst = false) {
    if (clearFirst) clearEntries()

    const fetchId = ++currentFetchId
    loading.value = true
    error.value = null
    try {
      const response = await entriesApi.getByTopic(topicId, page, size)
      // Discard if a new fetch has started
      if (fetchId !== currentFetchId) return

      allEntries.value = response.data.content || response.data
      totalPages.value = response.data.totalPages || 1
      currentPage.value = page
    } catch (err) {
      if (fetchId !== currentFetchId) return

      console.error('Entries fetch error:', err.message)
      error.value = 'Entry\'ler yüklenemedi'
      allEntries.value = []
    } finally {
      if (fetchId === currentFetchId) {
        loading.value = false
      }
    }
  }



  async function fetchEntriesByTopicSlug(slug) {
    // This requires topic lookup first - not directly supported by API
    loading.value = true
    error.value = null
    try {
      // For now, return empty - TopicDetail should use fetchEntriesByTopic with topic.id
      allEntries.value = []
    } finally {
      loading.value = false
    }
  }

  async function fetchEntriesByAuthor(authorId, page = 0, size = 10) {
    loading.value = true
    error.value = null
    try {
      const response = await entriesApi.getByAuthor(authorId, page, size)
      allEntries.value = response.data.content || response.data
      totalPages.value = response.data.totalPages || 1
      currentPage.value = page
    } catch (err) {
      console.error('Author entries fetch error:', err.message)
      error.value = 'Kullanıcı entryleri yüklenemedi'
      allEntries.value = []
    } finally {
      loading.value = false
    }
  }

  async function fetchPopularEntries(page = 0, size = 10) {
    loading.value = true
    error.value = null
    try {
      const response = await entriesApi.getPopular(page, size)
      allEntries.value = response.data.content || response.data
      totalPages.value = response.data.totalPages || 1
    } catch (err) {
      console.error('Popular entries fetch error:', err.message)
      error.value = 'Popüler entry\'ler yüklenemedi'
      allEntries.value = []
    } finally {
      loading.value = false
    }
  }

  async function fetchLatestEntries(page = 0, size = 10) {
    clearEntries()
    loading.value = true
    error.value = null
    try {
      const response = await entriesApi.getLatest(page, size)
      allEntries.value = response.data.content || response.data
      totalPages.value = response.data.totalPages || 1
    } catch (err) {
      console.error('Latest entries fetch error:', err.message)
      error.value = 'Son entry\'ler yüklenemedi'
      allEntries.value = []
    } finally {
      loading.value = false
    }
  }

  async function fetchRandomEntries(size = 3) {
    clearEntries()
    loading.value = true
    error.value = null
    try {
      const response = await entriesApi.getRandom(0, size)
      allEntries.value = response.data.content || response.data
      // Random entries usually don't have pages, or we just want one batch.
      totalPages.value = 1
    } catch (err) {
      console.error('Random entries fetch error:', err.message)
      error.value = 'Rastgele entry\'ler yüklenemedi'
      allEntries.value = []
    } finally {
      loading.value = false
    }
  }

  async function fetchEntryById(id) {
    loading.value = true
    error.value = null
    try {
      const response = await entriesApi.getById(id)
      currentEntry.value = response.data
    } catch (err) {
      console.error('Entry fetch error:', err.message)
      error.value = 'Entry bulunamadı'
      currentEntry.value = null
    } finally {
      loading.value = false
    }
  }

  async function createEntry(entryData) {
    loading.value = true
    error.value = null
    try {
      const response = await entriesApi.create(entryData)
      const newEntry = response.data
      allEntries.value.push(newEntry)
      return { success: true, entry: newEntry }
    } catch (err) {
      error.value = err.response?.data?.message || 'Entry oluşturulamadı'
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  async function updateEntry(id, content) {
    loading.value = true
    error.value = null
    try {
      const response = await entriesApi.update(id, content)
      const updatedEntry = response.data
      const index = allEntries.value.findIndex(e => e.id === id)
      if (index !== -1) {
        allEntries.value[index] = updatedEntry
      }
      return { success: true, entry: updatedEntry }
    } catch (err) {
      error.value = err.response?.data?.message || 'Entry güncellenemedi'
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  async function deleteEntry(id, reason) {
    loading.value = true
    error.value = null
    try {
      if (reason) {
        // Pass as query param or body depending on API.
        // Controller expects @RequestParam reason.
        // Axios delete with params:
        await entriesApi.delete(id, reason)
      } else {
        await entriesApi.delete(id)
      }
      allEntries.value = allEntries.value.filter(e => e.id !== id)
      return { success: true }
    } catch (err) {
      error.value = err.response?.data?.message || 'Entry silinemedi'
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  async function vote(entryId, voteType) {
    try {
      await votesApi.vote({ entryId, voteType })

      // Locally update the entry
      const entry = allEntries.value.find(e => e.id === entryId)
      if (entry) {
        // Remove old vote count
        if (entry.currentUserVote === 'LIKE') entry.likeCount--
        else if (entry.currentUserVote === 'DISLIKE') entry.dislikeCount--
        else if (entry.currentUserVote === 'FAVORITE') entry.favoriteCount--

        // Toggle or set new vote
        if (entry.currentUserVote === voteType) {
          entry.currentUserVote = null
        } else {
          entry.currentUserVote = voteType
          if (voteType === 'LIKE') entry.likeCount++
          else if (voteType === 'DISLIKE') entry.dislikeCount++
          else if (voteType === 'FAVORITE') entry.favoriteCount++
        }
      }
      return { success: true }
    } catch (err) {
      return { success: false, message: err.response?.data?.message || 'Oy verilemedi' }
    }
  }

  // Legacy function for backwards compatibility
  function toggleFavorite(entryId) {
    const entry = allEntries.value.find(e => e.id === entryId)
    if (entry) {
      if (entry.currentUserVote === 'FAVORITE') {
        entry.currentUserVote = null
        entry.favoriteCount--
      } else {
        entry.currentUserVote = 'FAVORITE'
        entry.favoriteCount++
      }
    }
  }

  function addEntry(entry) {
    allEntries.value.unshift(entry)
  }

  return {
    entries,
    allEntries,
    currentEntry,
    loading,
    error,
    totalPages,
    currentPage,
    fetchEntriesByTopic,
    fetchEntriesByTopicSlug,
    fetchEntriesByAuthor,
    fetchPopularEntries,
    fetchLatestEntries,
    fetchRandomEntries,
    fetchEntryById,
    createEntry,
    updateEntry,
    deleteEntry,
    vote,
    toggleFavorite,
    addEntry,
    clearEntries,
  }
})
