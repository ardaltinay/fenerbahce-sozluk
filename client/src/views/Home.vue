<template>
  <div class="app">
    <Header ref="headerRef" @tab-change="handleTabChange" />

    <div class="page-container">
      <!-- Desktop Layout -->
      <div class="desktop-layout">
        <!-- Sidebar -->
        <Sidebar />

        <!-- Main Content -->
        <main class="main-content">
          <div class="topic-header" v-if="getTabTitle()">
            <h1>{{ getTabTitle() }}</h1>
          </div>

          <!-- Feed Content -->
          <div :class="{ 'popular-grid': activeTab !== 'son' && activeTab !== 'random' && activeTab !== 'popular' }">
            <template v-if="activeTab === 'gundem'">
              <router-link 
                v-for="topic in topicsStore.topics" 
                :key="topic.id" 
                class="popular-card"
                :to="`/baslik/${topic.id}`"
              >
                <div class="card-content">
                  
                  <h3>{{ topic.title }}</h3>
                </div>
                <div class="card-meta">
                  <span>{{ formatCount(topic.entryCount) }} entry</span>
                </div>
              </router-link>
            </template>

            <template v-else-if="activeTab === 'popular'">
              <!-- Single random popular entry like rastgele -->
              <div class="entries-feed">
                <div class="random-actions top-actions" style="margin-bottom: 2rem;">
                  <button class="refresh-btn slim" @click="refreshPopularEntry">
                    <RefreshCw class="icon" /> yenile
                  </button>
                </div>
                
                <article v-if="popularEntry" class="entry">
                  <div class="entry-topic-ref">
                    <span class="ref-text">(bkz: </span>
                    <router-link :to="`/baslik/${popularEntry.topicId}`" class="ref-link">
                      {{ popularEntry.topicTitle }}
                    </router-link>
                    <span class="ref-text">)</span>
                  </div>
                  <div class="entry-body" v-html="formatContent(popularEntry.content)"></div>
                  <footer class="entry-footer">
                    <div class="actions">
                      <button :class="{ 'liked': popularEntry.currentUserVote === 'LIKE' }" @click="vote(popularEntry.id, 'LIKE')">
                        <ThumbsUp class="icon-sm" /> <span>{{ popularEntry.likeCount || 0 }}</span>
                      </button>
                      <button :class="{ 'disliked': popularEntry.currentUserVote === 'DISLIKE' }" @click="vote(popularEntry.id, 'DISLIKE')">
                        <ThumbsDown class="icon-sm" /> <span>{{ popularEntry.dislikeCount || 0 }}</span>
                      </button>
                      <button :class="{ 'favorited': popularEntry.currentUserVote === 'FAVORITE' }" @click="vote(popularEntry.id, 'FAVORITE')">
                        <Star class="icon-sm" /> <span>{{ popularEntry.favoriteCount || 0 }}</span>
                      </button>
                      <button @click="shareEntry(popularEntry.id)">
                        <Share2 class="icon-sm" />
                      </button>
                    </div>
                    <div class="meta">
                      <router-link :to="`/biri/${popularEntry.authorUsername || popularEntry.author?.username}`" class="author">
                        {{ popularEntry.authorUsername || popularEntry.author?.username || '-' }}
                      </router-link>
                      <span class="date">{{ formatDate(popularEntry.createdAt) }}</span>
                    </div>
                  </footer>
                </article>
                
                <div v-else class="empty-state">
                  <p>popüler entry bulunamadı</p>
                </div>
              </div>
            </template>
          </div>

          <!-- Entries Feed (Son / Random) -->
          <div v-if="activeTab === 'son' || activeTab === 'random'" class="entries-feed">
            <div v-if="activeTab === 'random'" class="random-actions top-actions" style="margin-bottom: 2rem;">
              <button class="refresh-btn slim" @click="refreshRandom">
                <RefreshCw class="icon" /> yenile
              </button>
            </div>

            <article v-for="entry in entriesStore.entries" :key="entry.id" class="entry">
              <div class="entry-topic-ref">
                 <span class="ref-text">(bkz: </span>
                 <router-link :to="`/baslik/${entry.topicId}`" class="ref-link">
                   {{ entry.topicTitle }}
                 </router-link>
                 <span class="ref-text">)</span>
              </div>
              <div class="entry-body" v-html="formatContent(entry.content)"></div>
              <footer class="entry-footer">
                <div class="actions">
                  <button :class="{ 'liked': entry.currentUserVote === 'LIKE' }" @click="vote(entry.id, 'LIKE')">
                    <ThumbsUp class="icon-sm" /> <span>{{ entry.likeCount || 0 }}</span>
                  </button>
                  <button :class="{ 'disliked': entry.currentUserVote === 'DISLIKE' }" @click="vote(entry.id, 'DISLIKE')">
                    <ThumbsDown class="icon-sm" /> <span>{{ entry.dislikeCount || 0 }}</span>
                  </button>
                  <button :class="{ 'favorited': entry.currentUserVote === 'FAVORITE' }" @click="vote(entry.id, 'FAVORITE')">
                    <Star class="icon-sm" /> <span>{{ entry.favoriteCount || 0 }}</span>
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
          </div>
        </main>
      </div>

      <!-- Mobile Layout -->
      <div class="mobile-layout">
        <!-- Varsayılan: Popüler Entry (tek entry) -->
        <div v-if="mobileView === 'home'" class="mobile-entries">
          <div class="mobile-section-header">
            <span>popüler</span>
            <button class="refresh-btn slim" @click="refreshPopularEntry">
              <RefreshCw class="icon" /> yenile
            </button>
          </div>
          
          <article v-if="popularEntry" class="mobile-entry">
            <div class="entry-topic-ref">
              <span class="ref-text">(bkz: </span>
              <router-link :to="`/baslik/${popularEntry.topicId}`" class="ref-link">
                {{ popularEntry.topicTitle }}
              </router-link>
              <span class="ref-text">)</span>
            </div>
            <div class="entry-body" v-html="formatContent(popularEntry.content)"></div>
            <footer class="mobile-entry-footer">
              <div class="actions">
                <button :class="{ 'liked': popularEntry.currentUserVote === 'LIKE' }" @click="vote(popularEntry.id, 'LIKE')">
                  <ThumbsUp class="icon-sm" /> <span>{{ popularEntry.likeCount || 0 }}</span>
                </button>
                <button :class="{ 'disliked': popularEntry.currentUserVote === 'DISLIKE' }" @click="vote(popularEntry.id, 'DISLIKE')">
                  <ThumbsDown class="icon-sm" /> <span>{{ popularEntry.dislikeCount || 0 }}</span>
                </button>
                <button :class="{ 'favorited': popularEntry.currentUserVote === 'FAVORITE' }" @click="vote(popularEntry.id, 'FAVORITE')">
                  <Star class="icon-sm" /> <span>{{ popularEntry.favoriteCount || 0 }}</span>
                </button>
              </div>
              <div class="info">
                <router-link :to="`/biri/${popularEntry.authorUsername}`" class="author">
                  {{ popularEntry.authorUsername || '-' }}
                </router-link>
                <span class="time">{{ formatDate(popularEntry.createdAt) }}</span>
              </div>
            </footer>
          </article>
          
          <div v-else class="empty-state">
            <p>popüler entry bulunamadı</p>
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
              {{ topic.title }}
            </span>
            <span class="count">{{ formatCount(topic.entryCount) }}</span>
          </button>
        </div>

        <!-- Entry Listesi -->
        <div v-else-if="mobileView === 'entries'" class="mobile-entries">
          <div class="mobile-section-header">
            <button @click="mobileView = previousMobileView"><ArrowLeft class="icon" /></button>
            <span>entryler</span>
          </div>

          <div v-if="activeTab === 'random'" class="random-actions top-actions" style="margin-bottom: 1.5rem;">
            <button class="refresh-btn slim" @click="refreshRandom">
              <RefreshCw class="icon" /> yenile
            </button>
          </div>

          <!-- Boş Durum - Mobile -->
          <div v-if="currentEntries.length === 0" class="empty-state">
            <MessageSquare class="empty-icon" />
            <p class="empty-title">bu başlıkta henüz entry girilmemiş</p>
            <span class="empty-hint">ilk entry'yi sen yazabilirsin!</span>
          </div>

          <template v-else>
            <article v-for="entry in currentEntries" :key="entry.id" class="mobile-entry stable-card">
              <div class="entry-topic-ref" style="margin-bottom: 0.5rem; font-size: 0.8rem;">
                 <span class="ref-text" style="color: #666;">(bkz: </span>
                 <router-link :to="`/baslik/${entry.topicId}`" class="ref-link" style="color: #d4c84a; font-weight: 500;">
                   {{ entry.topicTitle }}
                 </router-link>
                 <span class="ref-text" style="color: #666;">)</span>
              </div>
              <p v-html="formatContent(entry.content)" style="margin-top: 0;"></p>
              <footer class="mobile-entry-footer">
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
import { useRoute, useRouter } from 'vue-router'
import { Star, Share2, ArrowLeft, ThumbsUp, ThumbsDown, MessageSquare, RefreshCw } from 'lucide-vue-next'
import Sidebar from '@/components/layout/Sidebar.vue'
import Header from '@/components/layout/Header.vue'
import { useTopicsStore } from '@/stores/topics'
import { useEntriesStore } from '@/stores/entries'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import { entriesApi, votesApi } from '@/services/api'

const route = useRoute()
const topicsStore = useTopicsStore()
const entriesStore = useEntriesStore()
const authStore = useAuthStore()
const toast = useToast()

// Popular entry state
const popularEntry = ref(null)

async function fetchPopularEntry() {
  try {
    const response = await entriesApi.getRandomPopular()
    popularEntry.value = response.data
  } catch (e) {
    popularEntry.value = null
  }
}

function refreshPopularEntry() {
  fetchPopularEntry()
}

const headerRef = ref(null)

// Mobile view state: 'home', 'gundem', 'entries'
const mobileView = ref('home')
const previousMobileView = ref('home')

const currentEntries = computed(() => entriesStore.entries)

// Header'dan tab değişikliği (mobil için)
const activeTab = ref('popular') // Default tab
const router = useRouter() // Need router

function handleTabChange(tab) {
  activeTab.value = tab === 'home' ? 'popular' : tab
  
  if (tab === 'popular' || tab === 'home') {
    mobileView.value = 'home'
    fetchPopularEntry()
    // If home (logo click), we also want to populate Sidebar (Trending)
    if (tab === 'home') {
      topicsStore.fetchSidebarTopics(0, 50)
    }
  } else if (tab === 'son') {
    mobileView.value = 'entries'
    entriesStore.fetchLatestEntries()
  } else if (tab === 'gundem') {
    mobileView.value = 'gundem'
    topicsStore.fetchTrendingTopics(0, 10)
  } else if (tab === 'random') {
    mobileView.value = 'entries'
    entriesStore.fetchRandomEntries(4)
  }
}


// Watch for tab query changes - only trigger if tab actually changed
watch(() => route.query.tab, (newTab, oldTab) => {
  if (newTab && newTab !== activeTab.value) {
    handleTabChange(newTab)
  } else if (!newTab && activeTab.value !== 'popular') {
    handleTabChange('popular') // Default
  }
})

function getTabTitle() {
  if (activeTab.value === 'son') return 'son entryler'
  if (activeTab.value === 'random') return 'rastgele'
  if (activeTab.value === 'tarihte') return 'tarihte bugün'
  if (activeTab.value === 'gundem') return 'bugünün konuları'
  return 'popüler'
}



function openMobileEntries(topic) {
  router.push(`/baslik/${topic.id}`)
}

function refreshRandom() {
  entriesStore.fetchRandomEntries(4)
}

async function vote(entryId, voteType) {
  if (!authStore.isAuthenticated) {
    toast.warning('Oy vermek için giriş yapmalısınız')
    return
  }
  
  // Check if this is the popularEntry
  if (popularEntry.value && popularEntry.value.id === entryId) {
    try {
      await votesApi.vote({ entryId, voteType })
      
      // Locally update popularEntry
      const entry = popularEntry.value
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
    } catch (e) {
      toast.error('Oy verilemedi')
    }
  } else {
    // Use entriesStore for other entries
    await entriesStore.vote(entryId, voteType)
  }
}

function shareEntry(entryId) {
  const url = `${window.location.origin}/entry/${entryId}`
  navigator.clipboard.writeText(url)
  toast.success('Link kopyalandı!')
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



onMounted(() => {
  // Sidebar independent fetch
  topicsStore.fetchSidebarTopics(0, 50) 
  
  // Initial tab check
  const initialTab = route.query.tab
  
  if (initialTab) {
    handleTabChange(initialTab)
  } else {
    handleTabChange('popular')
  }
})
</script>

<style scoped>
.app {
  min-height: 100vh;
  background: transparent;
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

/* Main Content */
.main-content {
  flex: 1;
  padding: 1.5rem;
}

/* Popular Grid */
.popular-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.25rem;
}

.popular-card {
  background: rgba(26, 26, 46, 0.45);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 237, 0, 0.05);
  border-radius: 12px;
  padding: 1.25rem;
  cursor: pointer;
  transition: all 0.2s ease;
  height: 140px;
  width: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.popular-card:hover {
  background: rgba(26, 26, 46, 0.6);
  border-color: #d4c84a;
  transform: translateY(-4px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

.popular-card h3 {
  font-size: 1rem;
  color: #d4c84a;
  margin: 0.5rem 0 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.4;
  font-weight: 500;
}

.card-content {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.card-meta {
  display: flex;
  justify-content: space-between;
  font-size: 0.75rem;
  margin-top: 0.75rem;
}

.card-meta .author { color: #6fbf6f; }
.card-meta .likes { display: flex; align-items: center; gap: 0.25rem; color: #666; }
.icon { width: 14px; height: 14px; }

/* Topic Header */
.topic-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
}

.topic-header h1 {
  font-size: 1.1rem;
  font-weight: 400;
  color: #d4c84a;
  margin: 0;
}

/* Entries */
.entry {
  padding: 1.5rem;
  background: var(--entry-bg, rgba(26, 26, 46, 0.45));
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border: 1px solid var(--border, rgba(255, 237, 0, 0.05));
  border-radius: 12px;
  margin-bottom: 1rem;
  position: relative;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
}

.entry:hover {
  background: var(--entry-hover, rgba(26, 26, 46, 0.55));
  border-color: var(--border-hover, rgba(255, 237, 0, 0.15));
  transform: translateY(-2px);
  box-shadow: 0 5px 15px var(--card-shadow, rgba(0, 0, 0, 0.2));
}

.entry-body {
  font-size: 0.9rem;
  line-height: 1.7;
  color: #ccc;
  margin-bottom: 1.25rem;
  flex: 1;
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
  padding: 0.35rem 0.6rem;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.05);
  color: #999;
  font-size: 0.75rem;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.actions button:hover {
  background: rgba(212, 200, 74, 0.1);
  border-color: rgba(212, 200, 74, 0.2);
  color: #d4c84a;
}

.actions button.liked { color: #3fb950; }
.actions button.disliked { color: #f85149; }
.actions button.favorited { color: #d4c84a; }

.meta { display: flex; gap: 0.75rem; font-size: 0.75rem; }
.meta .author { color: #6fbf6f; }
.meta .date { color: #555; }

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
    background: rgba(26, 26, 46, 0.45);
    backdrop-filter: blur(8px);
    -webkit-backdrop-filter: blur(8px);
    border: 1px solid rgba(255, 237, 0, 0.05);
    border-radius: 12px;
    padding: 1.25rem;
  }

  .mobile-card h3 {
    font-size: 0.95rem;
    color: #d4c84a;
    margin: 0;
  }

  .mobile-card .card-meta {
    display: flex;
    justify-content: space-between;
    font-size: 0.75rem;
    margin-top: 0.75rem;
  }

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
    padding: 1.25rem 1rem;
    background: rgba(26, 26, 46, 0.45);
    backdrop-filter: blur(8px);
    -webkit-backdrop-filter: blur(8px);
    border-radius: 12px;
    border: 1px solid rgba(255, 237, 0, 0.05);
    margin-bottom: 1rem;
    display: flex;
    flex-direction: column;
  }

  .mobile-entry p {
    font-size: 0.9rem;
    line-height: 1.6;
    color: #ccc;
    margin: 0 0 1rem;
    flex: 1;
  }

  .mobile-entry footer.entry-footer {
    display: none; /* Hide default footer, but not mobile-entry-footer */
  }

  .mobile-entry-footer {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
    margin-top: 0.5rem;
  }

  .mobile-entry-footer .actions {
    display: flex;
    gap: 0.5rem;
  }

  .mobile-entry-footer .info {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 0.75rem;
    color: #666;
  }

  .mobile-entry-footer .info .author {
    color: #6fbf6f;
  }

  .mobile-entry .actions {
    display: flex;
    gap: 0.25rem;
  }

  .mobile-entry .actions button {
    display: flex;
    align-items: center;
    gap: 0.25rem;
    padding: 0.35rem 0.6rem;
    background: rgba(255, 255, 255, 0.03);
    border: 1px solid rgba(255, 255, 255, 0.05);
    color: #999;
    font-size: 0.75rem;
    cursor: pointer;
    border-radius: 6px;
    transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  }

  .mobile-entry .actions button:hover {
    background: rgba(212, 200, 74, 0.1);
    border-color: rgba(212, 200, 74, 0.2);
    color: #d4c84a;
  }

  .mobile-entry .actions button.liked { color: #3fb950; }
  .mobile-entry .actions button.disliked { color: #f85149; }
  .mobile-entry .actions button.favorited { color: #d4c84a; }
  .mobile-entry .actions button.edit-btn:hover { color: #58a6ff; }
  .mobile-entry .actions button.delete-btn:hover { color: #f85149; }

  .mobile-entry .info { font-size: 0.75rem; }
  .mobile-entry .info .author { color: #d4c84a; }
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
  padding: 0.2rem 0.5rem;
  background: rgba(88, 166, 255, 0.1);
  color: #58a6ff;
  font-size: 0.65rem;
  border-radius: 4px;
  border: 1px solid rgba(88, 166, 255, 0.2);
  vertical-align: middle;
  font-weight: 500;
  text-transform: lowercase;
  width: fit-content;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1.25rem;
  background: #d4c84a;
  color: #1a1a2e;
  border: 1px solid #d4c84a;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.8rem;
  font-weight: 600;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.refresh-btn.slim {
  padding: 0.35rem 0.8rem;
  font-size: 0.75rem;
  background: rgba(212, 200, 74, 0.1);
  color: #d4c84a;
  border-color: rgba(212, 200, 74, 0.3);
}

.refresh-btn.slim:hover {
  background: #d4c84a;
  color: #1a1a2e;
}

.header-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .header-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.4rem;
  }
}
</style>

