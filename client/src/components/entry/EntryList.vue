<template>
  <div class="space-y-4">
    <div v-if="loading" class="space-y-4">
      <div v-for="i in 3" :key="i" class="card animate-pulse">
        <div class="h-4 bg-fb-navy-light rounded w-1/4 mb-4"></div>
        <div class="space-y-2">
          <div class="h-3 bg-fb-navy-light rounded w-full"></div>
          <div class="h-3 bg-fb-navy-light rounded w-5/6"></div>
          <div class="h-3 bg-fb-navy-light rounded w-4/6"></div>
        </div>
        <div class="flex items-center gap-2 mt-4 pt-3 border-t border-fb-yellow/10">
          <div class="w-8 h-8 bg-fb-navy-light rounded-full"></div>
          <div class="h-3 bg-fb-navy-light rounded w-24"></div>
        </div>
      </div>
    </div>

    <template v-else>
      <EntryCard 
        v-for="entry in entries" 
        :key="entry.id" 
        :entry="entry" 
      />

      <div v-if="entries.length === 0" class="card text-center py-8">
        <MessageSquare class="w-12 h-12 text-text-muted mx-auto mb-4" />
        <p class="text-text-muted">henüz entry yok.</p>
        <p class="text-sm text-text-muted mt-1">ilk entry'yi sen yaz!</p>
      </div>
    </template>
  </div>
</template>

<script setup>
import { MessageSquare } from 'lucide-vue-next'
import EntryCard from './EntryCard.vue'

defineProps({
  entries: {
    type: Array,
    default: () => [],
  },
  loading: {
    type: Boolean,
    default: false,
  }
})
</script>
