<template>
  <router-link 
    :to="`/baslik/${topic.id}`"
    class="block card hover:border-fb-yellow/40 transition-all duration-200 group"
  >
    <div class="flex items-start justify-between">
      <div class="flex-1 min-w-0">
        <h3 class="text-text-primary font-medium group-hover:text-fb-yellow transition-colors truncate">
          {{ topic.title }}
        </h3>
        <div class="flex items-center gap-3 mt-1.5">
          <span class="text-xs text-text-muted flex items-center gap-1">
            <MessageSquare class="w-3 h-3" />
            {{ formatNumber(topic.entryCount) }} entry
          </span>
          <span class="text-xs text-text-muted">
            {{ topic.lastEntry }}
          </span>
        </div>
      </div>
      
      <ChevronRight class="w-5 h-5 text-text-muted group-hover:text-fb-yellow group-hover:translate-x-1 transition-all flex-shrink-0" />
    </div>
  </router-link>
</template>

<script setup>
import { MessageSquare, ChevronRight } from 'lucide-vue-next'

defineProps({
  topic: {
    type: Object,
    required: true,
  }
})

function formatNumber(num) {
  if (num >= 1000000) {
    return (num / 1000000).toFixed(1) + 'm'
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}
</script>
