<template>
  <div class="news-page">
    <Header />
    
    <div class="news-container">
      <div class="page-header">
        <div class="header-icon">
          <Newspaper class="icon" />
        </div>
        <div class="header-text">
          <h1>Haber Merkezi</h1>
          <p>Fenerbahçe dünyasından en güncel gelişmeler</p>
        </div>
      </div>

      <div v-if="loading && news.length === 0" class="loading-state">
        <div class="loader"></div>
        <span>Gelişmeler taranıyor...</span>
      </div>

      <div v-else-if="news.length === 0" class="empty-state">
        <Newspaper class="empty-icon" />
        <p>Henüz haber bulunamadı.</p>
        <p class="sub-text">Haberler taranıyor, birazdan burası dolacak.</p>
      </div>

      <div v-else class="news-grid">
        <NewsCard 
          v-for="item in news" 
          :key="item.id" 
          :news="item" 
        />
      </div>

      <div v-if="totalPages > currentPage + 1" class="load-more-container">
        <button class="load-more-btn" @click="loadMore" :disabled="loading">
          {{ loading ? 'Yükleniyor...' : 'Daha Fazla Göster' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Newspaper } from 'lucide-vue-next'
import Header from '@/components/layout/Header.vue'
import NewsCard from '@/components/news/NewsCard.vue'
import { newsApi } from '@/services/api'
import { useToast } from '@/composables/useToast'

const news = ref([])
const loading = ref(true)
const currentPage = ref(0)
const totalPages = ref(0)
const toast = useToast()

async function fetchNews(page = 0) {
  try {
    loading.value = true
    const response = await newsApi.getAll(page)
    if (page === 0) {
      news.value = response.data.content
    } else {
      news.value.push(...response.data.content)
    }
    totalPages.value = response.data.totalPages
    currentPage.value = response.data.number
  } catch (error) {
    console.error(error)
    toast.error('Haberler yüklenirken bir sorun oluştu')
  } finally {
    loading.value = false
  }
}

function loadMore() {
  fetchNews(currentPage.value + 1)
}

onMounted(() => {
  fetchNews()
})
</script>

<style scoped>
.news-page {
  min-height: 100vh;
  padding-top: 20px;
}

.news-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 60px 20px 40px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  margin-bottom: 2.5rem;
  padding: 2rem;
  background: linear-gradient(135deg, rgba(26, 26, 46, 0.8), rgba(212, 200, 74, 0.1));
  border: 1px solid rgba(255, 237, 0, 0.1);
  border-radius: 16px;
  backdrop-filter: blur(10px);
}

.header-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  background: rgba(212, 200, 74, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #d4c84a;
}

.header-icon .icon {
  width: 32px;
  height: 32px;
}

.header-text h1 {
  font-size: 2rem;
  font-weight: 700;
  color: #fff;
  margin: 0 0 0.5rem;
  letter-spacing: -0.5px;
}

.header-text p {
  color: #aaa;
  font-size: 1.1rem;
  margin: 0;
}

.news-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

/* Loading & Empty States */
.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem;
  text-align: center;
  color: #888;
}

.loader {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(255, 237, 0, 0.1);
  border-top-color: #d4c84a;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

.empty-icon {
  width: 64px;
  height: 64px;
  margin-bottom: 1rem;
  opacity: 0.5;
}

.sub-text {
  font-size: 0.9rem;
  color: #666;
  margin-top: 0.5rem;
}

.load-more-container {
  display: flex;
  justify-content: center;
  margin-top: 3rem;
}

.load-more-btn {
  padding: 0.8rem 2rem;
  background: transparent;
  border: 1px solid #d4c84a;
  color: #d4c84a;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.load-more-btn:hover:not(:disabled) {
  background: rgba(212, 200, 74, 0.1);
  transform: translateY(-2px);
}

.load-more-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .news-container {
    padding: 0;
  }
  
  .page-header {
    border-radius: 0;
    margin-bottom: 1rem;
    padding: 3rem 1.5rem 1.5rem; /* More top padding for mobile header */
  }

  .news-grid {
    grid-template-columns: 1fr;
    padding: 0 1rem;
    gap: 16px;
  }
}
</style>
