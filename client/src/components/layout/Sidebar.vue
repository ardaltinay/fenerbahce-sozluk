<template>
  <aside class="sidebar">
    <!-- bugün section -->
    <template v-if="todayTopics.length > 0">
      <div class="sidebar-header">bugün</div>
      <nav class="topic-list">
        <button
          v-for="topic in todayTopics"
          :key="topic.id"
          class="topic-item"
          :class="{ 'active': isTopicActive(topic) }"
          @click="navigateToTopic(topic)"
        >
          <span class="topic-title">{{ topic.title }}</span>
          <span class="topic-count">{{ formatCount(topic.entryCount) }}</span>
        </button>
      </nav>
    </template>

    <!-- dün section -->
    <template v-if="yesterdayTopics.length > 0">
      <div class="sidebar-header">dün</div>
      <nav class="topic-list">
        <button
          v-for="topic in yesterdayTopics"
          :key="topic.id"
          class="topic-item"
          :class="{ 'active': isTopicActive(topic) }"
          @click="navigateToTopic(topic)"
        >
          <span class="topic-title">{{ topic.title }}</span>
          <span class="topic-count">{{ formatCount(topic.entryCount) }}</span>
        </button>
      </nav>
    </template>

    <!-- önceki günler section -->
    <template v-if="olderTopics.length > 0">
      <div class="sidebar-header">önceki günler</div>
      <nav class="topic-list">
        <button
          v-for="topic in olderTopics"
          :key="topic.id"
          class="topic-item"
          :class="{ 'active': isTopicActive(topic) }"
          @click="navigateToTopic(topic)"
        >
          <span class="topic-title">{{ topic.title }}</span>
          <span class="topic-count">{{ formatCount(topic.entryCount) }}</span>
        </button>
      </nav>
    </template>

    <!-- Fallback if no topics at all -->
    <template v-if="topicsStore.sidebarTopics.length === 0">
      <div class="sidebar-header">bugünün konuları</div>
      <div class="empty-sidebar">henüz konu yok</div>
    </template>
  </aside>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useTopicsStore } from '@/stores/topics'
import { useWebSocket } from '@/composables/useWebSocket'

const router = useRouter()
const route = useRoute()
const topicsStore = useTopicsStore()
const { connect, subscribeToSidebar } = useWebSocket()

// Date helpers
function isToday(date) {
  if (!date) return false
  const today = new Date()
  const d = new Date(date)
  return d.getDate() === today.getDate() &&
         d.getMonth() === today.getMonth() &&
         d.getFullYear() === today.getFullYear()
}

function isYesterday(date) {
  if (!date) return false
  const yesterday = new Date()
  yesterday.setDate(yesterday.getDate() - 1)
  const d = new Date(date)
  return d.getDate() === yesterday.getDate() &&
         d.getMonth() === yesterday.getMonth() &&
         d.getFullYear() === yesterday.getFullYear()
}

// Grouped topics computed
const todayTopics = computed(() => 
  topicsStore.sidebarTopics.filter(t => isToday(t.lastActivityAt || t.updatedAt))
)

const yesterdayTopics = computed(() => 
  topicsStore.sidebarTopics.filter(t => isYesterday(t.lastActivityAt || t.updatedAt))
)

const olderTopics = computed(() => 
  topicsStore.sidebarTopics.filter(t => {
    const date = t.lastActivityAt || t.updatedAt
    return date && !isToday(date) && !isYesterday(date)
  })
)

function navigateToTopic(topic) {
  router.push(`/baslik/${topic.id}`)
}

function isTopicActive(topic) {
  if (route.name === 'TopicDetail') {
    const currentId = route.params.id
    return currentId === topic.id
  }
  return false
}

function formatCount(n) {
  return n >= 1000 ? Math.floor(n/1000) + 'b' : n
}

onMounted(() => {
  if (topicsStore.sidebarTopics.length === 0) {
    topicsStore.fetchSidebarTopics(0, 50)
  }
  
  connect()
  subscribeToSidebar(() => {
    topicsStore.fetchSidebarTopics(0, 50)
  })
})
</script>

<style scoped>
.sidebar {
  width: 280px;
  min-height: calc(100vh - 50px);
  background: rgba(10, 10, 25, 0.4);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-right: 1px solid rgba(212, 200, 74, 0.1);
  box-shadow: 4px 0 15px rgba(0, 0, 0, 0.15);
}

.sidebar-header {
  padding: 0.75rem 1rem;
  font-size: 0.7rem;
  letter-spacing: 0.05em;
  color: #666;
  border-bottom: 1px solid #1a1a2e;
  text-transform: lowercase;
  font-weight: 600;
}

.topic-list {
  padding: 0.25rem 0;
}

.topic-item {
  display: flex;
  justify-content: space-between;
  width: 100%;
  padding: 0.65rem 1rem;
  background: none;
  border: none;
  border-left: 3px solid transparent;
  color: #d4c84a;
  font-size: 0.875rem;
  font-weight: 500;
  text-align: left;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
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
}

.topic-count {
  font-size: 0.7rem;
  color: #444;
}

.empty-sidebar {
  padding: 2rem 1rem;
  text-align: center;
  color: #666;
  font-size: 0.8rem;
}
</style>
