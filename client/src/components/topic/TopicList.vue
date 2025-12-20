<template>
  <div class="space-y-2">
    <div v-if="loading" class="space-y-2">
      <div v-for="i in 5" :key="i" class="card animate-pulse">
        <div class="h-4 bg-fb-navy-light rounded w-3/4 mb-2"></div>
        <div class="h-3 bg-fb-navy-light rounded w-1/4"></div>
      </div>
    </div>

    <template v-else>
      <TopicCard 
        v-for="topic in topics" 
        :key="topic.id" 
        :topic="topic" 
      />

      <div v-if="topics.length === 0" class="card text-center py-8">
        <FileQuestion class="w-12 h-12 text-text-muted mx-auto mb-4" />
        <p class="text-text-muted">başlık bulunamadı.</p>
      </div>
    </template>
  </div>
</template>

<script setup>
import { FileQuestion } from 'lucide-vue-next'
import TopicCard from './TopicCard.vue'

defineProps({
  topics: {
    type: Array,
    default: () => [],
  },
  loading: {
    type: Boolean,
    default: false,
  }
})
</script>
