<template>
  <div class="stats-page">
    <Header />

    <div class="stats-container">
      <div class="stats-header">
        <h1>İstatistikler</h1>
        <p>Fenerbahçe Sözlük'ün güncel verileri</p>
      </div>

      <!-- Main Stats Grid -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon entry-icon">
            <MessageSquare />
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ formatNumber(stats.totalEntries) }}</span>
            <span class="stat-label">Toplam Entry</span>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon topic-icon">
            <FileText />
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ formatNumber(stats.totalTopics) }}</span>
            <span class="stat-label">Toplam Başlık</span>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon author-icon">
            <Users />
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ formatNumber(stats.totalAuthors) }}</span>
            <span class="stat-label">Toplam Yazar</span>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon like-icon">
            <ThumbsUp />
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ formatNumber(stats.totalLikes) }}</span>
            <span class="stat-label">Beğeni</span>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon dislike-icon">
            <ThumbsDown />
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ formatNumber(stats.totalDislikes) }}</span>
            <span class="stat-label">Beğenmeme</span>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon favorite-icon">
            <Star />
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ formatNumber(stats.totalFavorites) }}</span>
            <span class="stat-label">Favori</span>
          </div>
        </div>
      </div>

      <!-- Rankings -->
      <div class="rankings-section">
        <div class="ranking-card">
          <div class="ranking-header">
            <Trophy class="ranking-icon" />
            <h2>En Aktif Yazarlar</h2>
          </div>
          <div class="ranking-list">
            <router-link 
              v-for="(author, index) in stats.topAuthors" 
              :key="author.username"
              :to="`/biri/${author.username}`"
              class="ranking-item"
            >
              <span class="rank" :class="`rank-${index + 1}`">{{ index + 1 }}</span>
              <span class="name">{{ author.username }}</span>
              <span class="count">{{ author.entryCount }} entry</span>
            </router-link>
          </div>
        </div>

        <div class="ranking-card">
          <div class="ranking-header">
            <TrendingUp class="ranking-icon" />
            <h2>En Popüler Başlıklar</h2>
          </div>
          <div class="ranking-list">
            <router-link 
              v-for="(topic, index) in stats.topTopics" 
              :key="topic.id"
              :to="`/baslik/${topic.id}`"
              class="ranking-item"
            >
              <span class="rank" :class="`rank-${index + 1}`">{{ index + 1 }}</span>
              <span class="name">{{ topic.title }}</span>
              <span class="count">{{ topic.entryCount }} entry</span>
            </router-link>
          </div>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="loading-overlay">
        <Loader2 class="spin" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { BarChart3, MessageSquare, FileText, Users, ThumbsUp, ThumbsDown, Star, Trophy, TrendingUp, Loader2 } from 'lucide-vue-next'
import Header from '@/components/layout/Header.vue'
import { statsApi } from '@/services/api'

const loading = ref(true)
const stats = ref({
  totalEntries: 0,
  totalAuthors: 0,
  totalTopics: 0,
  totalLikes: 0,
  totalDislikes: 0,
  totalFavorites: 0,
  topAuthors: [],
  topTopics: [],
})

function formatNumber(num) {
  if (!num) return '0'
  if (num >= 1000000) return (num / 1000000).toFixed(1) + 'M'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'K'
  return num.toString()
}

onMounted(async () => {
  try {
    const res = await statsApi.get()
    stats.value = res.data
  } catch (err) {
    console.error('Stats fetch error:', err)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.stats-page {
  min-height: 100vh;
  background: transparent;
  color: var(--text-primary, #e0e0e0);
}

.stats-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 70px 1rem 2rem;
  position: relative;
}

.stats-header {
  text-align: center;
  margin-bottom: 2rem;
}

.header-icon {
  width: 40px;
  height: 40px;
  color: var(--accent, #d4c84a);
  margin-bottom: 0.5rem;
}

.stats-header h1 {
  font-size: 1.75rem;
  color: var(--accent, #d4c84a);
  margin: 0 0 0.5rem;
}

.stats-header p {
  color: var(--text-secondary, #888);
  font-size: 0.95rem;
  margin: 0;
}

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  margin-bottom: 2rem;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}

.stat-card {
  background: rgba(26, 26, 46, 0.45);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 237, 0, 0.05);
  border-radius: 12px;
  padding: 1.25rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: all 0.2s;
}

.stat-card:hover {
  border-color: rgba(255, 237, 0, 0.15);
  transform: translateY(-2px);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon svg {
  width: 24px;
  height: 24px;
}

.entry-icon { background: rgba(88, 166, 255, 0.15); color: #58a6ff; }
.topic-icon { background: rgba(212, 200, 74, 0.15); color: #d4c84a; }
.author-icon { background: rgba(111, 191, 111, 0.15); color: #6fbf6f; }
.like-icon { background: rgba(63, 185, 80, 0.15); color: #3fb950; }
.dislike-icon { background: rgba(248, 81, 73, 0.15); color: #f85149; }
.favorite-icon { background: rgba(212, 200, 74, 0.15); color: #d4c84a; }

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary, #e0e0e0);
}

.stat-label {
  font-size: 0.8rem;
  color: var(--text-secondary, #888);
}

/* Rankings */
.rankings-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

@media (max-width: 768px) {
  .rankings-section {
    grid-template-columns: 1fr;
  }
}

.ranking-card {
  background: rgba(26, 26, 46, 0.45);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 237, 0, 0.05);
  border-radius: 12px;
  overflow: hidden;
}

.ranking-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.ranking-icon {
  width: 20px;
  height: 20px;
  color: var(--accent, #d4c84a);
}

.ranking-header h2 {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary, #e0e0e0);
  margin: 0;
}

.ranking-list {
  display: flex;
  flex-direction: column;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.875rem 1.25rem;
  text-decoration: none;
  border-bottom: 1px solid rgba(255, 255, 255, 0.03);
  transition: background 0.15s;
}

.ranking-item:last-child {
  border-bottom: none;
}

.ranking-item:hover {
  background: rgba(255, 255, 255, 0.03);
}

.rank {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-secondary, #888);
}

.rank-1 { background: linear-gradient(135deg, #ffd700, #b8860b); color: #0f0f1a; }
.rank-2 { background: linear-gradient(135deg, #c0c0c0, #808080); color: #0f0f1a; }
.rank-3 { background: linear-gradient(135deg, #cd7f32, #8b4513); color: #0f0f1a; }

.name {
  flex: 1;
  font-size: 0.9rem;
  color: var(--accent, #d4c84a);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.count {
  font-size: 0.75rem;
  color: var(--text-secondary, #888);
}

/* Loading */
.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(13, 13, 26, 0.8);
  border-radius: 12px;
}

.spin {
  animation: spin 1s linear infinite;
  width: 32px;
  height: 32px;
  color: var(--accent, #d4c84a);
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
