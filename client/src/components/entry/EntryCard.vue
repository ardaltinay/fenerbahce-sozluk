<template>
  <article class="card hover:border-fb-yellow/30 transition-all duration-200 animate-fade-in">
    <!-- Entry Header -->
    <div class="flex items-start justify-between mb-3">
      <router-link 
        :to="`/baslik/${entry.topicId}`"
        class="text-fb-yellow hover:text-fb-yellow-light font-medium text-sm"
      >
        {{ entry.topicTitle }}
      </router-link>
      <span class="text-xs text-text-muted">
        #{{ entry.id }}
      </span>
    </div>

    <!-- Entry Content -->
    <div class="text-text-primary text-sm leading-relaxed mb-4">
      <p v-html="formatContent(entry.content)"></p>
    </div>

    <!-- Entry Footer -->
    <div class="entry-card-footer mt-4 pt-3 border-t border-fb-yellow/10">
      <!-- Actions -->
      <div class="actions flex items-center gap-1">
        <button 
          class="btn btn-ghost p-2"
          :class="{ 'text-fb-yellow': entry.isFavorited }"
          @click="handleFavorite"
          :title="entry.isFavorited ? 'favorilerden çıkar' : 'favorile'"
        >
          <Heart class="w-4 h-4" :class="{ 'fill-current': entry.isFavorited }" />
          <span class="text-xs ml-1" v-if="entry.favoriteCount > 0">{{ entry.favoriteCount }}</span>
        </button>
        
        <button class="btn btn-ghost p-2" title="paylaş">
          <Share2 class="w-4 h-4" />
        </button>
        
        <button class="btn btn-ghost p-2" @click="showMore = !showMore">
          <MoreHorizontal class="w-4 h-4" />
        </button>

        <!-- More Menu -->
        <div 
          v-if="showMore"
          class="absolute right-4 mt-32 w-40 glass rounded-lg py-2 animate-fade-in z-10"
        >
          <button class="w-full text-left px-4 py-2 text-sm hover:bg-fb-yellow/10 flex items-center gap-2">
            <Flag class="w-4 h-4" />
            <span>şikayet et</span>
          </button>
          <button class="w-full text-left px-4 py-2 text-sm hover:bg-fb-yellow/10 flex items-center gap-2">
            <Link class="w-4 h-4" />
            <span>bağlantıyı kopyala</span>
          </button>
        </div>
      </div>

      <!-- Author Info -->
      <router-link 
        :to="`/biri/${entry.author.username}`"
        class="author-info flex items-center gap-2 group mt-2"
      >
        <div class="avatar avatar-sm">
          {{ entry.author.username.charAt(0).toUpperCase() }}
        </div>
        <div class="flex items-center gap-2">
          <span class="text-sm text-fb-yellow group-hover:text-fb-yellow-light transition-colors">
            {{ entry.author.username }}
          </span>
          <span class="text-xs text-text-muted">
            {{ formatDate(entry.createdAt) }}
          </span>
        </div>
      </router-link>
    </div>
  </article>
</template>

<script setup>
import { ref } from 'vue'
import { useEntriesStore } from '@/stores/entries'
import { Heart, Share2, MoreHorizontal, Flag, Link } from 'lucide-vue-next'

const props = defineProps({
  entry: {
    type: Object,
    required: true,
  }
})

const entriesStore = useEntriesStore()
const showMore = ref(false)

function handleFavorite() {
  entriesStore.toggleFavorite(props.entry.id)
}

function formatContent(content) {
  // Add link formatting for mentions and hashtags
  let formatted = content
    .replace(/@(\w+)/g, '<a href="/biri/$1" class="text-fb-yellow hover:underline">@$1</a>')
    .replace(/#(\d+)/g, '<a href="/entry/$1" class="text-fb-yellow hover:underline">#$1</a>')
    .replace(/\(bkz: ([^)]+)\)/g, '<a href="/baslik/$1" class="text-fb-yellow hover:underline italic">(bkz: $1)</a>')
  return formatted
}

function formatDate(dateString) {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now - date
  
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 60) return `${minutes} dakika önce`
  if (hours < 24) return `${hours} saat önce`
  if (days < 7) return `${days} gün önce`
  
  return date.toLocaleDateString('tr-TR', { 
    day: 'numeric', 
    month: 'long', 
    year: 'numeric' 
  })
}
</script>

<style scoped>
.entry-card-footer {
  display: flex;
  flex-direction: column;
}

.author-info {
  justify-content: space-between;
}

@media (min-width: 768px) {
  .entry-card-footer {
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
  }
  
  .author-info {
    order: -1;
    margin-top: 0;
  }
}
</style>
