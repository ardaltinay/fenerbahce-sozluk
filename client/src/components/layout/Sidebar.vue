<template>
  <aside class="sidebar">
    <!-- bugün section -->
    <template v-if="topicsStore.todayTopics.length > 0">
      <div class="sidebar-header">bugün</div>
      <nav class="topic-list">
        <button
          v-for="topic in topicsStore.todayTopics"
          :key="topic.id"
          class="topic-item"
          :class="{ 'active': isTopicActive(topic, 'today') }"
          @click="navigateToTopic(topic, 'today')"
        >
          <span class="topic-title">{{ topic.title }}</span>
          <span class="topic-count">{{ formatCount(topic.todayEntryCount || topic.entryCount) }}</span>
        </button>
      </nav>
    </template>

    <!-- dün section -->
    <template v-if="topicsStore.yesterdayTopics.length > 0">
      <div class="sidebar-header">dün</div>
      <nav class="topic-list">
        <button
          v-for="topic in topicsStore.yesterdayTopics"
          :key="topic.id"
          class="topic-item"
          :class="{ 'active': isTopicActive(topic, 'yesterday') }"
          @click="navigateToTopic(topic, 'yesterday')"
        >
          <span class="topic-title">{{ topic.title }}</span>
          <span class="topic-count">{{ formatCount(topic.todayEntryCount || topic.entryCount) }}</span>
        </button>
      </nav>
    </template>

    <!-- önceki günler section -->
    <template v-if="topicsStore.olderTopics.length > 0">
      <div class="sidebar-header">önceki günler</div>
      <nav class="topic-list">
        <button
          v-for="topic in topicsStore.olderTopics"
          :key="topic.id"
          class="topic-item"
          :class="{ 'active': isTopicActive(topic, 'older') }"
          @click="navigateToTopic(topic, 'older')"
        >
          <span class="topic-title">{{ topic.title }}</span>
          <span class="topic-count">{{ formatCount(topic.olderEntryCount || topic.entryCount) }}</span>
        </button>
      </nav>
    </template>

    <!-- Fallback if no topics at all -->
    <template v-if="topicsStore.todayTopics.length === 0 && topicsStore.yesterdayTopics.length === 0 && topicsStore.olderTopics.length === 0">
      <div class="sidebar-header">bugünün konuları</div>
      <div class="empty-sidebar">henüz konu yok</div>
    </template>
  </aside>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useTopicsStore } from '@/stores/topics'
import { useWebSocket } from '@/composables/useWebSocket'

const router = useRouter()
const route = useRoute()
const topicsStore = useTopicsStore()
const { connect, subscribeToSidebar } = useWebSocket()

function navigateToTopic(topic, dateFilter = null) {
  if (dateFilter) {
    router.push({ path: `/baslik/${topic.id}`, query: { dateFilter } })
  } else {
    router.push(`/baslik/${topic.id}`)
  }
}

function isTopicActive(topic, sectionDateFilter) {
  if (route.name === 'TopicDetail') {
    const currentId = route.params.id
    const currentDateFilter = route.query.dateFilter || null
    return currentId === topic.id && currentDateFilter === sectionDateFilter
  }
  return false
}

function formatCount(n) {
  if (!n) return 0
  return n >= 1000 ? Math.floor(n/1000) + 'b' : n
}

onMounted(() => {
  // Fetch date-based topics for sidebar
  topicsStore.fetchAllSidebarTopicsByDate()
  
  connect()
  subscribeToSidebar(() => {
    // Force refresh to bypass cache when WebSocket notifies of changes
    topicsStore.fetchAllSidebarTopicsByDate(true)
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

