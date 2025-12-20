<template>
  <aside class="sidebar">
    <!-- Başlık Listesi -->
    <nav class="topic-list">
      <button
        v-for="topic in topics"
        :key="topic.id"
        class="topic-item"
        :class="{ 'active': selectedTopicId === topic.id }"
        @click="selectTopic(topic)"
      >
        <span class="topic-title">{{ topic.title }}</span>
        <span class="topic-count">{{ formatCount(topic.entryCount) }}</span>
      </button>
    </nav>
  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { useTopicsStore } from '@/stores/topics'

const emit = defineEmits(['select-topic'])

const topicsStore = useTopicsStore()

const topics = computed(() => topicsStore.topics)
const selectedTopicId = computed(() => topicsStore.currentTopic?.id)

function selectTopic(topic) {
  topicsStore.setCurrentTopic(topic)
  emit('select-topic', topic)
}

function formatCount(num) {
  if (num >= 1000) {
    return Math.floor(num / 1000) + 'b'
  }
  return num.toString()
}
</script>

<style scoped>
.sidebar {
  position: fixed;
  left: 0;
  top: 56px;
  bottom: 0;
  width: 300px;
  background: #001232;
  border-right: 1px solid rgba(255, 237, 0, 0.1);
  overflow-y: auto;
  z-index: 40;
}

.topic-list {
  padding: 0.5rem 0;
}

.topic-item {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.5rem 1rem;
  background: none;
  border: none;
  text-align: left;
  cursor: pointer;
  transition: all 0.1s ease;
  font-size: 0.875rem;
  color: #81C784;
  border-left: 2px solid transparent;
}

.topic-item:hover {
  background: rgba(255, 237, 0, 0.05);
  color: #FFED00;
}

.topic-item.active {
  background: rgba(255, 237, 0, 0.08);
  color: #FFED00;
  border-left-color: #FFED00;
}

.topic-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 0.5rem;
}

.topic-count {
  font-size: 0.75rem;
  color: #4B5563;
  flex-shrink: 0;
}

.topic-item:hover .topic-count,
.topic-item.active .topic-count {
  color: #94A3B8;
}

/* Scrollbar */
.sidebar::-webkit-scrollbar {
  width: 8px;
}

.sidebar::-webkit-scrollbar-track {
  background: transparent;
}

.sidebar::-webkit-scrollbar-thumb {
  background: rgba(255, 237, 0, 0.15);
  border-radius: 4px;
}

.sidebar::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 237, 0, 0.3);
}

/* Mobile */
@media (max-width: 1024px) {
  .sidebar {
    display: none;
  }
}
</style>
