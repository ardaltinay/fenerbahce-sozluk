<template>
  <div class="app">
    <Header ref="headerRef" @tab-change="handleTabChange" />

    <div class="page-container">
      <!-- Desktop Layout -->
      <div class="desktop-layout">
        <!-- Sidebar -->
        <aside class="sidebar">
          <div class="sidebar-header">bugünün konuları</div>
          <nav class="topic-list">
            <button
              v-for="topic in topicsStore.sidebarTopics"
              :key="topic.id"
              class="topic-item"
              :class="{ 'active': selectedTopic?.id === topic.id }"
              @click="selectTopic(topic)"
            >
              <span class="topic-title">{{ topic.title }}</span>
              <span class="topic-count">{{ formatCount(topic.entryCount) }}</span>
            </button>
          </nav>
        </aside>

        <!-- Main Content -->
        <main class="main-content">
          <!-- Popüler Başlıklar (Topic seçili değil ve Tab 'popular' veya 'home' veya 'channel' ise) -->
          <div v-if="!selectedTopic && (activeTab === 'popular' || activeTab === 'home' || activeTab === 'gundem' || activeTab === 'channel' || !activeTab)" class="popular-section">
            <div class="popular-grid">
              <div 
                v-for="topic in (activeTab === 'gundem' ? topicsStore.topics : (activeTab === 'channel' ? topicsStore.channelTopics : topicsStore.popularTopics))" 
                :key="topic.id" 
                class="popular-card"
                @click="selectTopic(topic)"
              >
                <h3>
                  <span v-if="topic.categoryName" class="badge">{{ topic.categoryName }}</span>
                  {{ topic.title }}
                </h3>
                <div class="card-meta">
                  <span>{{ formatCount(topic.entryCount) }} entry</span>
                  <span>{{ formatDate(topic.createdAt) }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Entry Listesi (Seçili Topic varsa VEYA Tab 'tarihte'/'son' ise) -->
          <div v-else class="entries-section">
            <div v-if="selectedTopic" class="topic-header">
              <button class="back-btn" @click="selectedTopic = null">
                <ArrowLeft class="icon" />
              </button>
              <h1>{{ selectedTopic.title }}</h1>
            </div>
            <div v-else-if="activeTab === 'tarihte'" class="topic-header">
              <h1>tarihte bugün</h1>
            </div>
            <div v-else-if="activeTab === 'son'" class="topic-header">
              <h1>son entryler</h1>
            </div>
            <div v-else-if="activeTab === 'channel'" class="topic-header">
              <h1>{{ route.query.name || 'kanal' }}</h1>
            </div>

            <!-- Entry Listesi veya Boş Durum -->
            <template v-if="currentEntries.length > 0">
              <article v-for="entry in currentEntries" :key="entry.id" class="entry">
                <div v-if="activeTab === 'son'" class="entry-topic-ref">
                  <span class="ref-text">(bkz: </span>
                  <router-link :to="`/baslik/${entry.topicSlug || entry.topicId}`" class="ref-link">
                    {{ entry.topicTitle }}
                  </router-link>
                  <span class="ref-text">)</span>
                </div>
                <div class="entry-body" v-html="formatContent(entry.content)"></div>
                <footer class="entry-footer">
                  <div class="actions">
                    <button 
                      :class="{ 'liked': entry.currentUserVote === 'LIKE' }"
                      @click="vote(entry.id, 'LIKE')"
                    >
                      <ThumbsUp class="icon-sm" />
                      <span>{{ entry.likeCount || 0 }}</span>
                    </button>
                    <button 
                      :class="{ 'disliked': entry.currentUserVote === 'DISLIKE' }"
                      @click="vote(entry.id, 'DISLIKE')"
                    >
                      <ThumbsDown class="icon-sm" />
                      <span>{{ entry.dislikeCount || 0 }}</span>
                    </button>
                    <button 
                      :class="{ 'favorited': entry.currentUserVote === 'FAVORITE' }"
                      @click="vote(entry.id, 'FAVORITE')"
                    >
                      <Star class="icon-sm" />
                      <span>{{ entry.favoriteCount || 0 }}</span>
                    </button>
                    <button @click="shareEntry(entry.id)">
                      <Share2 class="icon-sm" />
                    </button>
                  </div>
                  <div class="meta">
                    <router-link :to="`/biri/${entry.authorUsername || entry.author?.username}`" class="author">
                      {{ entry.authorUsername || entry.author?.username || '-' }}
                    </router-link>
                    <span class="date">{{ formatDate(entry.createdAt) }}</span>
                  </div>
                </footer>
              </article>
            </template>

            <!-- Boş Durum -->
            <div v-else class="empty-state">
              <MessageSquare class="empty-icon" />
              <p class="empty-title">bu başlıkta henüz entry girilmemiş</p>
              <span class="empty-hint">ilk entry'yi sen yazabilirsin!</span>
            </div>

            <!-- Entry Gir Alanı (Sadece Topic Seçiliyse veya Gundem/Home tabında topic seçilince) -->
            <!-- Son ve Tarihte Bugün tablarında gizle -->
            <div class="entry-form" v-if="authStore.isAuthenticated && activeTab !== 'son' && activeTab !== 'tarihte'">
              <textarea v-model="newEntry" placeholder="entry gir..." rows="3"></textarea>
              <button :disabled="newEntry.length < 10" @click="submitEntry">yolla</button>
            </div>
          </div>
        </main>
      </div>

      <!-- Mobile Layout -->
      <div class="mobile-layout">
        <!-- Varsayılan: Popüler Başlıklar -->
        <div v-if="mobileView === 'home'" class="mobile-popular">
          <div 
            v-for="topic in topicsStore.popularTopics" 
            :key="topic.id" 
            class="mobile-card"
            @click="openMobileEntries(topic)"
          >
            <h3>
              <span v-if="topic.categoryName" class="badge">{{ topic.categoryName }}</span>
              {{ topic.title }}
            </h3>
            <div class="card-meta">
              <span>{{ formatCount(topic.entryCount) }} entry</span>
            </div>
          </div>
        </div>

        <!-- Gündem: Başlık Listesi -->
        <div v-else-if="mobileView === 'gundem'" class="mobile-topics">
          <div class="mobile-section-header">
            <button @click="mobileView = 'home'"><ArrowLeft class="icon" /></button>
            <span>bugünün konuları</span>
          </div>
          <button
            v-for="topic in topicsStore.topics"
            :key="topic.id"
            class="mobile-topic-item"
            @click="openMobileEntries(topic)"
          >
            <span>
              <span v-if="topic.categoryName" class="badge">{{ topic.categoryName }}</span>
              {{ topic.title }}
            </span>
            <span class="count">{{ formatCount(topic.entryCount) }}</span>
          </button>
        </div>

        <!-- Entry Listesi -->
        <div v-else-if="mobileView === 'entries'" class="mobile-entries">
          <div class="mobile-section-header">
            <button @click="mobileView = previousMobileView"><ArrowLeft class="icon" /></button>
            <span>{{ selectedTopic?.title }}</span>
          </div>

          <!-- Boş Durum - Mobile -->
          <div v-if="currentEntries.length === 0" class="empty-state">
            <MessageSquare class="empty-icon" />
            <p class="empty-title">bu başlıkta henüz entry girilmemiş</p>
            <span class="empty-hint">ilk entry'yi sen yazabilirsin!</span>
          </div>

          <template v-else>
            <article v-for="entry in currentEntries" :key="entry.id" class="mobile-entry">
              <p v-html="formatContent(entry.content)"></p>
              <footer>
                <div class="actions">
                  <button 
                    :class="{ 'liked': entry.currentUserVote === 'LIKE' }"
                    @click="vote(entry.id, 'LIKE')"
                  >
                    <ThumbsUp class="icon-sm" />
                    <span>{{ entry.likeCount || 0 }}</span>
                  </button>
                  <button 
                    :class="{ 'disliked': entry.currentUserVote === 'DISLIKE' }"
                    @click="vote(entry.id, 'DISLIKE')"
                  >
                    <ThumbsDown class="icon-sm" />
                    <span>{{ entry.dislikeCount || 0 }}</span>
                  </button>
                  <button 
                    :class="{ 'favorited': entry.currentUserVote === 'FAVORITE' }"
                    @click="vote(entry.id, 'FAVORITE')"
                  >
                    <Star class="icon-sm" />
                    <span>{{ entry.favoriteCount || 0 }}</span>
                  </button>
                </div>
                <div class="info">
                  <router-link :to="`/biri/${entry.authorUsername || entry.author?.username}`" class="author">
                    {{ entry.authorUsername || entry.author?.username || '-' }}
                  </router-link>
                  <span class="time">{{ formatDate(entry.createdAt) }}</span>
                </div>
              </footer>
            </article>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Star, Share2, ArrowLeft, ThumbsUp, ThumbsDown, MessageSquare } from 'lucide-vue-next'
import Header from '@/components/layout/Header.vue'
import { useTopicsStore } from '@/stores/topics'
import { useEntriesStore } from '@/stores/entries'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const topicsStore = useTopicsStore()
const entriesStore = useEntriesStore()
const authStore = useAuthStore()

const headerRef = ref(null)
const selectedTopic = ref(null)
const newEntry = ref('')

// Mobile view state: 'home', 'gundem', 'entries'
const mobileView = ref('home')
const previousMobileView = ref('home')

const currentEntries = computed(() => entriesStore.entries)

// Header'dan tab değişikliği (mobil için)
const activeTab = ref('popular') // Default tab

function handleTabChange(tab) {
  selectedTopic.value = null
  activeTab.value = tab
  
  if (tab === 'popular' || tab === 'home') {
    mobileView.value = 'home'
    topicsStore.fetchPopularTopics(0, 10)
    // If home (logo click), we also want to populate Sidebar (Trending)
    if (tab === 'home') {
      topicsStore.fetchTrendingTopics(0, 10)
    }
  } else if (tab === 'son') {
    mobileView.value = 'entries'
    entriesStore.fetchLatestEntries()
  } else if (tab === 'gundem') {
    mobileView.value = 'gundem'
    // Gündem tab uses topicsStore.topics (fetched as trends on mount)
    // No need to fetch popular topics here
    // topicsStore.fetchTrendingTopics() // Already fetched on mount and periodically?
    // If we want to refresh:
    topicsStore.fetchTrendingTopics(0, 10)
  } else if (tab === 'channel') {
    mobileView.value = 'channel'
    const catId = route.query.category
    if (catId) {
      topicsStore.fetchTopicsByCategory(catId, 0, 10)
    }
  }
}

// Watch for route query changes (for category filter)
watch(() => route.query.category, (newCatId) => {
  if (newCatId) {
    activeTab.value = 'channel'
    mobileView.value = 'channel'
    topicsStore.fetchTopicsByCategory(newCatId, 0, 10)
  }
})

function selectTopic(topic) {
  selectedTopic.value = topic
  topicsStore.setCurrentTopic(topic)
  entriesStore.fetchEntriesByTopic(topic.id)
  // Header'daki aktif tab'ı temizle
  headerRef.value?.clearActiveTab()
}

function openMobileEntries(topic) {
  previousMobileView.value = mobileView.value
  selectedTopic.value = topic
  topicsStore.setCurrentTopic(topic)
  entriesStore.fetchEntriesByTopic(topic.id)
  mobileView.value = 'entries'
}

async function vote(entryId, voteType) {
  if (!authStore.isAuthenticated) {
    alert('Oy vermek için giriş yapmalısınız')
    return
  }
  await entriesStore.vote(entryId, voteType)
}

function shareEntry(entryId) {
  const url = `${window.location.origin}/entry/${entryId}`
  navigator.clipboard.writeText(url)
  alert('Link kopyalandı!')
}

function formatCount(n) {
  return n >= 1000 ? Math.floor(n/1000) + 'b' : n
}

function truncate(text, len) {
  return text.length > len ? text.substring(0, len) + '...' : text
}

function formatContent(content) {
  return content
    .replace(/@(\w+)/g, '<a href="/biri/$1">@$1</a>')
    .replace(/\(bkz: ([^)]+)\)/g, '<a href="/baslik/$1">(bkz: $1)</a>')
}

function formatDate(date) {
  const d = new Date(date)
  const diff = Date.now() - d.getTime()
  const mins = Math.floor(diff / 60000)
  if (mins < 60) return `${mins} dk önce`
  const hours = Math.floor(mins / 60)
  if (hours < 24) return `${hours} saat önce`
  return d.toLocaleDateString('tr-TR')
}

function submitEntry() {
  if (newEntry.value.length < 10 || !selectedTopic.value) return
  entriesStore.addEntry({
    id: Date.now(),
    content: newEntry.value,
    author: { username: authStore.username },
    topicId: selectedTopic.value.id,
    favoriteCount: 0,
    createdAt: new Date().toISOString(),
    isFavorited: false,
  })
  newEntry.value = ''
}

onMounted(() => {
  // Fetch lists
  topicsStore.fetchSidebarTopics(0, 50) // Sidebar is independent
  topicsStore.fetchTrendingTopics(0, 10) // Gundem view
  topicsStore.fetchPopularTopics(0, 10)
  
  if (route.params.slug) {
    // fetchData inside watch/onMounted will handle it
  }
})
</script>

<style scoped>
.app {
  min-height: 100vh;
  background: #0f0f1a;
  color: #e0e0e0;
}

.page-container {
  max-width: 1100px;
  margin: 0 auto;
  padding-top: 50px;
}

/* Desktop */
.desktop-layout {
  display: flex;
}

.mobile-layout {
  display: none;
}

/* Sidebar */
.sidebar {
  width: 280px;
  min-height: calc(100vh - 50px);
  background: #0a0a14;
  border-right: 1px solid #1a1a2e;
}

.sidebar-header {
  padding: 0.75rem 1rem;
  font-size: 0.7rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: #666;
  border-bottom: 1px solid #1a1a2e;
}

.topic-list {
  padding: 0.25rem 0;
}

.topic-item {
  display: flex;
  justify-content: space-between;
  width: 100%;
  padding: 0.5rem 1rem;
  background: none;
  border: none;
  border-left: 2px solid transparent;
  color: #d4c84a;
  font-size: 0.85rem;
  text-align: left;
  cursor: pointer;
}

.topic-item:hover {
  background: rgba(255, 237, 0, 0.03);
  color: #d4c84a;
}

.topic-item.active {
  background: rgba(255, 237, 0, 0.05);
  color: #d4c84a;
  border-left-color: #d4c84a;
}

.topic-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.topic-count {
  font-size: 0.7rem;
  color: #444;
}

/* Main Content */
.main-content {
  flex: 1;
  padding: 1.5rem;
}

/* Popular Grid */
.popular-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
}

.popular-card {
  background: #1a1a2e;
  border: 1px solid #2a2a4a;
  border-radius: 8px;
  padding: 1rem;
  cursor: pointer;
  transition: all 0.15s;
}

.popular-card:hover {
  border-color: #d4c84a;
  transform: translateY(-2px);
}

.popular-card h3 {
  font-size: 0.95rem;
  color: #d4c84a;
  margin: 0 0 0.5rem;
}

.popular-card p {
  font-size: 0.85rem;
  color: #aaa;
  line-height: 1.5;
  margin: 0 0 0.75rem;
}

.card-meta {
  display: flex;
  justify-content: space-between;
  font-size: 0.75rem;
}

.card-meta .author { color: #6fbf6f; }
.card-meta .likes { display: flex; align-items: center; gap: 0.25rem; color: #666; }
.icon { width: 14px; height: 14px; }

/* Entries Section */
.topic-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
}

.back-btn {
  padding: 0.5rem;
  background: none;
  border: none;
  color: #888;
  cursor: pointer;
  border-radius: 4px;
}

.back-btn:hover { background: rgba(255,255,255,0.05); color: #d4c84a; }

.topic-header h1 {
  font-size: 1.1rem;
  font-weight: 400;
  color: #d4c84a;
  margin: 0;
}

.entry {
  padding: 1rem 0;
  border-bottom: 1px solid #1a1a2e;
}

.entry-body {
  font-size: 0.9rem;
  line-height: 1.7;
  color: #ccc;
  margin-bottom: 0.75rem;
}

.entry-body :deep(a) { color: #58a6ff; text-decoration: none; }

.entry-footer {
  display: flex;
  justify-content: space-between;
}

.icon-sm {
  width: 14px;
  height: 14px;
}

.actions {
  display: flex;
  gap: 0.25rem;
}

.actions button {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.4rem 0.6rem;
  background: none;
  border: none;
  color: #555;
  font-size: 0.8rem;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.15s;
}

.actions button:hover {
  background: rgba(255, 255, 255, 0.05);
  color: #888;
}

.actions button.liked {
  color: #3fb950;
}

.actions button.disliked {
  color: #f85149;
}

.actions button.favorited {
  color: #d4c84a;
}

.meta { display: flex; gap: 0.75rem; font-size: 0.75rem; }
.meta .author { color: #6fbf6f; }
.meta .date { color: #555; }

.entry-form {
  margin-top: 1.5rem;
  padding-top: 1rem;
  border-top: 1px solid #1a1a2e;
}

.entry-form textarea {
  width: 100%;
  padding: 0.75rem;
  background: #0a0a14;
  border: 1px solid #2a2a4a;
  border-radius: 6px;
  color: #ccc;
  font-size: 0.85rem;
  resize: vertical;
  margin-bottom: 0.75rem;
}

.entry-form textarea:focus { outline: none; border-color: #d4c84a; }

.entry-form button {
  padding: 0.5rem 1.25rem;
  background: #d4c84a;
  color: #0f0f1a;
  font-size: 0.8rem;
  font-weight: 600;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.entry-form button:disabled { opacity: 0.5; cursor: not-allowed; }

/* Empty State */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 2rem;
  text-align: center;
  background: rgba(26, 26, 46, 0.5);
  border: 1px dashed #2a2a4a;
  border-radius: 12px;
  margin: 2rem 0;
}

.empty-icon {
  width: 48px;
  height: 48px;
  color: #d4c84a;
  opacity: 0.6;
  margin-bottom: 1rem;
}

.empty-title {
  font-size: 1rem;
  color: #ccc;
  margin: 0 0 0.5rem;
}

.empty-hint {
  font-size: 0.85rem;
  color: #6fbf6f;
}

/* Mobile */
@media (max-width: 768px) {
  .desktop-layout { display: none; }
  .mobile-layout { display: block; padding: 1rem; }

  .mobile-popular, .mobile-topics {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
  }

  .mobile-card {
    background: #1a1a2e;
    border: 1px solid #2a2a4a;
    border-radius: 8px;
    padding: 1rem;
  }

  .mobile-card h3 {
    font-size: 0.95rem;
    color: #d4c84a;
    margin: 0 0 0.5rem;
  }

  .mobile-card p {
    font-size: 0.85rem;
    color: #aaa;
    margin: 0 0 0.75rem;
    line-height: 1.5;
  }

  .mobile-card .card-meta {
    display: flex;
    justify-content: space-between;
    font-size: 0.75rem;
    color: #6fbf6f;
  }

  .icon-sm { width: 12px; height: 12px; }

  .mobile-section-header {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.75rem 0;
    margin-bottom: 0.5rem;
    border-bottom: 1px solid #2a2a4a;
    font-size: 0.9rem;
    color: #d4c84a;
  }

  .mobile-section-header button {
    padding: 0.5rem;
    background: none;
    border: none;
    color: #888;
  }

  .mobile-topic-item {
    display: flex;
    justify-content: space-between;
    width: 100%;
    padding: 0.75rem 0.5rem;
    background: none;
    border: none;
    border-bottom: 1px solid #1a1a2e;
    color: #6fbf6f;
    font-size: 0.9rem;
    text-align: left;
  }

  .mobile-topic-item .count {
    color: #444;
    font-size: 0.75rem;
  }

  .mobile-entry {
    padding: 1rem 0;
    border-bottom: 1px solid #1a1a2e;
  }

  .mobile-entry p {
    font-size: 0.9rem;
    line-height: 1.6;
    color: #ccc;
    margin: 0 0 0.75rem;
  }

  .mobile-entry footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .mobile-entry .actions {
    display: flex;
    gap: 0.5rem;
  }

  .mobile-entry .actions button {
    padding: 0.25rem;
    background: none;
    border: none;
    color: #555;
  }

  .mobile-entry .actions button.liked { color: #3fb950; }

  .mobile-entry .info { font-size: 0.75rem; }
  .mobile-entry .info .author { color: #6fbf6f; }
  .mobile-entry .info .time { color: #555; margin-left: 0.5rem; }
}
.entry-topic-ref {
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
  color: #888;
}

.ref-link {
  color: #d4c84a;
  text-decoration: none;
}

.ref-link:hover { text-decoration: underline; }

.badge {
  display: inline-block;
  padding: 0.1rem 0.35rem;
  background: #2a2a4a;
  color: #d4c84a;
  font-size: 0.7rem;
  border-radius: 4px;
  margin-right: 0.4rem;
  vertical-align: middle;
  font-weight: normal;
}
</style>
