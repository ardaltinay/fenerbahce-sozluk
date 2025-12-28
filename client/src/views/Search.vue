<template>
  <div class="search-page">
    <Header />

    <div class="page-container">
      <main class="main-content">
        <!-- Search Header -->
        <div class="search-header">
          <SearchIcon class="header-icon" />
          <h1>arama sonuçları</h1>
          <p v-if="searchQuery">"{{ searchQuery }}" için sonuçlar</p>
        </div>

        <!-- Search Input -->
        <div class="search-input-wrapper">
          <div class="search-input-box">
            <SearchIcon class="input-icon" />
            <input
              v-model="searchQuery"
              type="text"
              placeholder="başlık, entry veya yazar ara..."
              @keyup.enter="handleSearch"
            />
            <button v-if="searchQuery" class="clear-btn" @click="searchQuery = ''">
              <X class="icon" />
            </button>
          </div>
        </div>

        <!-- Filter Tabs -->
        <div class="filter-tabs">
          <button 
            v-for="filter in filters" 
            :key="filter.id"
            class="filter-tab"
            :class="{ 'active': activeFilter === filter.id }"
            @click="activeFilter = filter.id"
          >
            <component :is="filter.icon" class="tab-icon" />
            {{ filter.label }}
            <span class="tab-count">{{ getFilterCount(filter.id) }}</span>
          </button>
        </div>

        <!-- Results -->
        <div class="results-container">
          <!-- Loading -->
          <div v-if="loading" class="loading-state">
            <div v-for="i in 3" :key="i" class="skeleton-card">
              <div class="skeleton-line long"></div>
              <div class="skeleton-line short"></div>
            </div>
          </div>

          <!-- Empty Start -->
          <div v-else-if="!searchQuery" class="empty-state">
            <SearchIcon class="empty-icon" />
            <h2>aramaya başla</h2>
            <p>başlık, entry veya yazar aramak için yukarıdaki alanı kullan</p>
          </div>

          <!-- No Results -->
          <div v-else-if="results.length === 0" class="empty-state">
            <AlertCircle class="empty-icon" />
            <h2>sonuç bulunamadı</h2>
            <p>"{{ searchQuery }}" için sonuç bulunamadı</p>
          </div>

          <!-- Results List -->
          <div v-else class="results-list">
            <!-- Topics -->
            <template v-if="activeFilter === 'all' || activeFilter === 'topics'">
              <div 
                v-for="topic in topicResults" 
                :key="'t-' + topic.id" 
                class="result-card topic-result"
                @click="goToTopic(topic.id)"
              >
                <FileText class="result-icon" />
                <div class="result-content">
                  <h3>{{ topic.title }}</h3>
                  <span class="result-meta">{{ topic.entryCount }} entry</span>
                </div>
              </div>
            </template>

            <!-- Entries -->
            <template v-if="activeFilter === 'all' || activeFilter === 'entries'">
              <div 
                v-for="entry in entryResults" 
                :key="'e-' + entry.id" 
                class="result-card entry-result"
              >
                <MessageSquare class="result-icon" />
                <div class="result-content">
                  <p>{{ truncate(entry.content, 150) }}</p>
                  <span class="result-meta">{{ entry.authorUsername || entry.author?.username || '-' }} • {{ entry.topicTitle }}</span>
                </div>
              </div>
            </template>

            <!-- Authors -->
            <template v-if="activeFilter === 'all' || activeFilter === 'authors'">
              <router-link 
                v-for="author in authorResults" 
                :key="author.username"
                :to="`/biri/${author.username}`"
                class="result-card author-result"
              >
                <div class="author-avatar">{{ author.username.charAt(0).toUpperCase() }}</div>
                <div class="result-content">
                  <h3>{{ author.username }}</h3>
                  <span class="result-meta">{{ author.entryCount || 0 }} entry</span>
                </div>
              </router-link>
            </template>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search as SearchIcon, X, AlertCircle, FileText, MessageSquare, User } from 'lucide-vue-next'
import Header from '@/components/layout/Header.vue'
import { useTopicsStore } from '@/stores/topics'
import { useEntriesStore } from '@/stores/entries'
import { usersApi } from '@/services/api'

const route = useRoute()
const router = useRouter()
const topicsStore = useTopicsStore()
const entriesStore = useEntriesStore()

const searchQuery = ref(route.query.q || '')
const activeFilter = ref('all')
const loading = ref(false)

const filters = [
  { id: 'all', label: 'tümü', icon: SearchIcon },
  { id: 'topics', label: 'başlıklar', icon: FileText },
  { id: 'entries', label: 'entryler', icon: MessageSquare },
  { id: 'authors', label: 'yazarlar', icon: User },
]

const results = computed(() => [...topicResults.value, ...entryResults.value, ...authorResults.value])

const topicResults = computed(() => {
  if (!searchQuery.value) return []
  return topicsStore.topics.filter(t => 
    t.title.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

const entryResults = computed(() => {
  if (!searchQuery.value) return []
  return entriesStore.entries.filter(e => 
    e.content.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

// Author search now fetches from API
const authorSearchResults = ref([])

const authorResults = computed(() => {
  return authorSearchResults.value
})

async function fetchUserSearch(query) {
  if (!query || query.length < 2) {
    authorSearchResults.value = []
    return
  }
  try {
    const response = await usersApi.search(query)
    authorSearchResults.value = response.data || []
  } catch (e) {
    authorSearchResults.value = []
  }
}

function getFilterCount(id) {
  if (id === 'all') return results.value.length
  if (id === 'topics') return topicResults.value.length
  if (id === 'entries') return entryResults.value.length
  if (id === 'authors') return authorResults.value.length
  return 0
}

function handleSearch() {
  router.push({ path: '/arama', query: { q: searchQuery.value } })
}

function goToTopic(topicId) {
  router.push(`/baslik/${topicId}`)
}

function truncate(text, len) {
  return text.length > len ? text.substring(0, len) + '...' : text
}

watch(() => route.query.q, (newQuery) => {
  searchQuery.value = newQuery || ''
})

// Watch searchQuery to fetch users from API
watch(searchQuery, (newQuery) => {
  fetchUserSearch(newQuery)
}, { immediate: true })

onMounted(() => {
  topicsStore.fetchTopics()
  entriesStore.fetchLatestEntries()
})
</script>

<style scoped>
.search-page {
  min-height: 100vh;
  background: transparent;
  color: #e0e0e0;
}

.page-container {
  max-width: 1100px;
  margin: 0 auto;
  padding-top: 50px;
}

.main-content {
  padding: 1.5rem;
  max-width: 700px;
  margin: 0 auto;
}

/* Search Header */
.search-header {
  margin-bottom: 1.5rem;
}

.search-header .header-icon {
  width: 24px;
  height: 24px;
  color: #d4c84a;
  margin-bottom: 0.5rem;
}

.search-header h1 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #d4c84a;
  margin: 0 0 0.25rem;
}

.search-header p {
  font-size: 0.875rem;
  color: #888;
  margin: 0;
}

/* Search Input */
.search-input-wrapper {
  margin-bottom: 1.5rem;
}

.search-input-box {
  display: flex;
  align-items: center;
  background: rgba(26, 26, 46, 0.45);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 237, 0, 0.05);
  border-radius: 8px;
  padding: 0 1rem;
}

.search-input-box:focus-within {
  border-color: #d4c84a;
}

.input-icon {
  width: 18px;
  height: 18px;
  color: #555;
  flex-shrink: 0;
}

.search-input-box input {
  flex: 1;
  padding: 0.875rem 0.75rem;
  background: transparent;
  border: none;
  color: #e0e0e0;
  font-size: 0.9rem;
}

.search-input-box input:focus {
  outline: none;
}

.search-input-box input::placeholder {
  color: #555;
}

.clear-btn {
  padding: 0.5rem;
  background: none;
  border: none;
  color: #555;
  cursor: pointer;
}

.clear-btn:hover {
  color: #d4c84a;
}

.icon {
  width: 16px;
  height: 16px;
}

/* Filter Tabs */
.filter-tabs {
  display: flex;
  gap: 0;
  border-bottom: 1px solid #2a2a4a;
  margin-bottom: 1.5rem;
  overflow-x: auto;
}

.filter-tab {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  padding: 0.75rem 1rem;
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  color: #888;
  font-size: 0.8rem;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.15s;
}

.filter-tab:hover {
  color: #d4c84a;
}

.filter-tab.active {
  color: #d4c84a;
  border-bottom-color: #d4c84a;
}

.tab-icon {
  width: 14px;
  height: 14px;
}

.tab-count {
  font-size: 0.7rem;
  padding: 0.125rem 0.375rem;
  background: #2a2a4a;
  border-radius: 10px;
}

/* Results */
.results-container {
  min-height: 300px;
}

.loading-state {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.skeleton-card {
  background: rgba(26, 26, 46, 0.45);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 237, 0, 0.05);
  border-radius: 8px;
  padding: 1rem;
}

.skeleton-line {
  height: 14px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 4px;
  margin-bottom: 0.5rem;
}

.skeleton-line.long { width: 80%; }
.skeleton-line.short { width: 40%; margin-bottom: 0; }

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 3rem 1rem;
}

.empty-icon {
  width: 48px;
  height: 48px;
  color: #555;
  margin-bottom: 1rem;
}

.empty-state h2 {
  font-size: 1.1rem;
  color: #e0e0e0;
  margin: 0 0 0.5rem;
}

.empty-state p {
  font-size: 0.875rem;
  color: #666;
  margin: 0;
}

/* Results List */
.results-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.result-card {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 1.25rem;
  background: rgba(26, 26, 46, 0.45);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 237, 0, 0.05);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  text-decoration: none;
}

.result-card:hover {
  border-color: #d4c84a;
}

.result-icon {
  width: 18px;
  height: 18px;
  color: #555;
  flex-shrink: 0;
  margin-top: 2px;
}

.result-content {
  flex: 1;
  min-width: 0;
}

.result-content h3 {
  font-size: 0.9rem;
  font-weight: 500;
  color: #d4c84a;
  margin: 0 0 0.25rem;
}

.result-content p {
  font-size: 0.85rem;
  color: #aaa;
  margin: 0 0 0.25rem;
  line-height: 1.5;
}

.result-meta {
  font-size: 0.75rem;
  color: #666;
}

.author-avatar {
  width: 36px;
  height: 36px;
  background: #d4c84a;
  color: #0f0f1a;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.875rem;
  font-weight: 600;
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .main-content {
    padding: 1rem;
  }
  
  .filter-tabs {
    margin-left: -1rem;
    margin-right: -1rem;
    padding: 0 1rem;
  }
}
</style>
