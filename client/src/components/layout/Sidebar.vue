<template>
  <aside class="sidebar">
    <div class="sidebar-header">bugünün konuları</div>
    <nav class="topic-list">
      <button
        v-for="topic in topicsStore.sidebarTopics"
        :key="topic.id"
        class="topic-item"
        :class="{ 'active': isTopicActive(topic) }"
        @click="navigateToTopic(topic)"
      >
        <span class="topic-title">{{ topic.title }}</span>
        <span class="topic-count">{{ formatCount(topic.entryCount) }}</span>
      </button>
    </nav>
  </aside>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useTopicsStore } from '@/stores/topics'

const router = useRouter()
const route = useRoute()
const topicsStore = useTopicsStore()
const refreshInterval = ref(null)

function navigateToTopic(topic) {
  // Always navigate to the topic detail page
  router.push(`/baslik/${topic.id}`)
}

function isTopicActive(topic) {
  // Check if current route is topic detail and slug matches
  if (route.name === 'TopicDetail') {
    const currentId = route.params.id
    return currentId === topic.id
  }
  return false
}

function formatCount(n) {
  return n >= 1000 ? Math.floor(n/1000) + 'b' : n
}

function fetchTopics() {
  topicsStore.fetchSidebarTopics(0, 50)
}

onMounted(() => {
  // Ensure sidebar topics are loaded
  if (topicsStore.sidebarTopics.length === 0) {
    fetchTopics()
  }

  // Auto Refresh every 60 seconds
  refreshInterval.value = setInterval(() => {
    fetchTopics()
  }, 60000)
})

onUnmounted(() => {
  if (refreshInterval.value) {
    clearInterval(refreshInterval.value)
  }
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
  white-space: nowrap;
}

.topic-count {
  font-size: 0.7rem;
  color: #444;
}
</style>
