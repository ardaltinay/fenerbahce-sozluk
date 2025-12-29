<template>
  <a :href="news.link" target="_blank" rel="noopener noreferrer" class="news-card">
    <div class="image-container">
      <img 
        :src="news.imageUrl || defaultImage" 
        :alt="news.title" 
        class="news-image"
        @error="handleImageError"
      />
      <div class="overlay"></div>
      <span class="source-badge">{{ news.source }}</span>
    </div>
    
    <div class="content">
      <div class="meta">
        <span class="date">{{ formatTimeAgo(news.pubDate) }}</span>
      </div>
      <h3 class="title" :title="news.title">{{ news.title }}</h3>
      <p class="description">{{ truncateText(news.description, 100) }}</p>
    </div>
  </a>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  news: {
    type: Object,
    required: true
  }
})

// Fallback image if RSS item has no image
const defaultImage = 'https://upload.wikimedia.org/wikipedia/tr/8/86/Fenerbah%C3%A7e_SK.png' // Or local asset

function handleImageError(e) {
  e.target.src = defaultImage
}

function truncateText(text, length) {
  if (!text) return ''
  return text.length > length ? text.substring(0, length) + '...' : text
}

function formatTimeAgo(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return new Intl.DateTimeFormat('tr-TR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}
</script>

<style scoped>
.news-card {
  display: flex;
  flex-direction: column;
  background: rgba(26, 26, 46, 0.6);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 237, 0, 0.05);
  border-radius: 12px;
  overflow: hidden;
  text-decoration: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  height: 100%;
}

.news-card:hover {
  transform: translateY(-5px);
  border-color: rgba(212, 200, 74, 0.3);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.image-container {
  position: relative;
  width: 100%;
  padding-top: 56.25%; /* 16:9 Aspect Ratio */
  overflow: hidden;
  background: #0d0d1a;
}

.news-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s;
}

.news-card:hover .news-image {
  transform: scale(1.05);
}

.overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(13, 13, 26, 0.8) 0%, transparent 60%);
  pointer-events: none;
}

.source-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(212, 200, 74, 0.9);
  color: #1a1a2e;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.7rem;
  font-weight: 700;
  text-transform: uppercase;
  z-index: 2;
}

.content {
  padding: 1rem;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.meta {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 0.5rem;
}

.date {
  font-size: 0.75rem;
  color: #888;
  display: flex;
  align-items: center;
}

.date::before {
  content: '';
  display: block;
  width: 6px;
  height: 6px;
  background-color: #d4c84a;
  border-radius: 50%;
  margin-right: 6px;
}

.title {
  font-size: 1rem;
  font-weight: 600;
  color: #e0e0e0;
  margin: 0 0 0.5rem;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-card:hover .title {
  color: #d4c84a;
}

.description {
  font-size: 0.85rem;
  color: #aaa;
  line-height: 1.5;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
