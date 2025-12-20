import { defineStore } from 'pinia'
import { ref } from 'vue'
import { topicsApi } from '@/services/api'

export const useTopicsStore = defineStore('topics', () => {

  const topics = ref([])
  const sidebarTopics = ref([])
  const popularTopics = ref([])
  const channelTopics = ref([])
  const currentTopic = ref(null)
  const loading = ref(false)
  const error = ref(null)
  const totalPages = ref(0)
  const currentPage = ref(0)

  async function fetchTopics(page = 0, size = 20) {
    loading.value = true
    error.value = null
    try {
      const response = await topicsApi.getAll(page, size)
      topics.value = response.data.content || response.data
      totalPages.value = response.data.totalPages || 1
      currentPage.value = page

      if (!currentTopic.value && topics.value.length > 0) {
        currentTopic.value = topics.value[0]
      }
    } catch (err) {
      console.error('Topics fetch error:', err.message)
      error.value = 'Başlıklar yüklenemedi'
      topics.value = []
    } finally {
      loading.value = false
    }
  }

  async function fetchPopularTopics(page = 0, size = 20) {
    loading.value = true
    error.value = null
    try {
      const response = await topicsApi.getPopular(page, size)
      popularTopics.value = response.data.content || response.data
      totalPages.value = response.data.totalPages || 1
    } catch (err) {
      console.error('Popular topics fetch error:', err.message)
      error.value = 'Popüler başlıklar yüklenemedi'
      popularTopics.value = []
    } finally {
      loading.value = false
    }
  }

  async function fetchTrendingTopics(page = 0, size = 10) {
    loading.value = true
    error.value = null
    try {
      const response = await topicsApi.getTrends(page, size)
      topics.value = response.data.content || response.data
      totalPages.value = response.data.totalPages || 1

      if (!currentTopic.value && topics.value.length > 0) {
        currentTopic.value = topics.value[0]
      }
    } catch (err) {
      console.error('Trending topics fetch error:', err.message)
      error.value = 'Gündem yüklenemedi'
      topics.value = []
    } finally {
      loading.value = false
    }
  }

  async function fetchSidebarTopics(page = 0, size = 50) {
    // No loading state change to prevent UI flicker on main loader
    try {
      const response = await topicsApi.getTrends(page, size)
      sidebarTopics.value = response.data.content || response.data
    } catch (err) {
      console.error('Sidebar topics fetch error:', err.message)
      // Don't clear sidebar on error to keep previous state if possible
    }
  }

  async function fetchTopicBySlug(slug) {
    loading.value = true
    error.value = null
    try {
      const response = await topicsApi.getBySlug(slug)
      currentTopic.value = response.data
    } catch (err) {
      console.error('Topic fetch error:', err.message)
      error.value = 'Başlık bulunamadı'
      currentTopic.value = null
    } finally {
      loading.value = false
    }
  }

  async function fetchTopicsByCategory(categoryId, page = 0, size = 20) {
    loading.value = true
    error.value = null
    try {
      const response = await topicsApi.getByCategory(categoryId, page, size)
      channelTopics.value = response.data.content || response.data
      totalPages.value = response.data.totalPages || 1

      if (!currentTopic.value && topics.value.length > 0) {
        currentTopic.value = topics.value[0]
      }
    } catch (err) {
      console.error('Category topics fetch error:', err.message)
      error.value = 'Kategori başlıkları yüklenemedi'
      channelTopics.value = []
    } finally {
      loading.value = false
    }
  }

  async function searchTopics(keyword, page = 0, size = 20) {
    loading.value = true
    error.value = null
    try {
      const response = await topicsApi.search(keyword, page, size)
      topics.value = response.data.content || response.data
      totalPages.value = response.data.totalPages || 1
    } catch (err) {
      console.error('Search error:', err.message)
      error.value = 'Arama yapılamadı'
      topics.value = []
    } finally {
      loading.value = false
    }
  }

  async function createTopic(topicData) {
    loading.value = true
    error.value = null
    try {
      const response = await topicsApi.create(topicData)
      const newTopic = response.data
      topics.value.unshift(newTopic)
      return newTopic
    } catch (err) {
      error.value = err.response?.data?.message || 'Başlık oluşturulamadı'
      throw err
    } finally {
      loading.value = false
    }
  }

  function setCurrentTopic(topic) {
    currentTopic.value = topic
  }

  async function deleteTopic(topicId) {
    try {
      await topicsApi.delete(topicId)
      // Remove from local topics list
      topics.value = topics.value.filter(t => t.id !== topicId)
      currentTopic.value = null
      return { success: true }
    } catch (err) {
      console.error('Topic delete error:', err.message)
      return { success: false, message: err.response?.data?.message || 'Başlık silinemedi' }
    }
  }

  return {
    topics,
    sidebarTopics,
    popularTopics,
    channelTopics,
    currentTopic,
    loading,
    error,
    totalPages,
    currentPage,
    fetchTopics,
    fetchPopularTopics,
    fetchTrendingTopics,
    fetchSidebarTopics,
    fetchTopicsByCategory,
    fetchTopicBySlug,
    searchTopics,
    createTopic,
    deleteTopic,
    setCurrentTopic,
  }
})
