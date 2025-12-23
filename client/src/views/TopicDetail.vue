<template>
  <div class="topic-page">
    <Header />

    <div class="page-container">
      <!-- Desktop Layout with Sidebar -->
      <div class="desktop-layout">
        <Sidebar />
        
        <main class="main-content">
          <div class="topic-container">
            <!-- Topic Header -->
            <div class="topic-header">
              <button class="back-link" @click="goBack">
                <ArrowLeft class="icon" />
                <span>geri</span>
              </button>
              <div v-if="topic?.categoryName" class="badge-lg" style="margin-bottom: 0.5rem; display: inline-block;">{{ topic.categoryName }}</div>
              <h1>
                {{ topic?.title || slug }}
              </h1>
              <div class="topic-meta">
                <span><MessageSquare class="icon-sm" /> {{ topic?.entryCount || 0 }} entry</span>
              </div>
              <!-- Topic Actions -->
              <div class="topic-actions" v-if="topic && authStore.canDeleteTopic(topic)">
                <button class="topic-delete-btn" @click="openTopicDeleteModal" title="başlığı sil">
                  <Trash2 class="icon-sm" />
                  <span>sil</span>
                </button>
              </div>
            </div>
      
            <!-- Entries -->
            <div class="entries-container">
              <!-- Loading (Only on initial load if no data exists) -->
              <div v-if="loading && entries.length === 0" class="loading-state">
                <Loader2 class="spin" />
                <span>yükleniyor...</span>
              </div>
      
              <!-- Empty State -->
              <div v-else-if="entries.length === 0" class="empty-state">
                <MessageSquare class="empty-icon" />
                <p>bu başlıkta henüz entry girilmemiş</p>
                <span class="empty-hint">ilk entry'yi sen yazabilirsin!</span>
              </div>
      
              <!-- Entry List -->
              <template v-else>
                <article v-for="(entry, index) in entries" :key="entry.id" class="entry-card">
                  <div class="entry-number">#{{ index + 1 }}</div>
                  
                  <!-- Edit Mode -->
                  <div v-if="editingEntryId === entry.id" class="edit-mode">
                    <textarea 
                      v-model="editContent"
                      class="edit-textarea"
                      rows="4"
                      placeholder="entry içeriği..."
                    ></textarea>
                    <div class="edit-actions">
                      <button class="btn-cancel" @click="cancelEdit">iptal</button>
                      <button class="btn-save" @click="saveEdit(entry.id)" :disabled="!editContent.trim()">
                        kaydet
                      </button>
                    </div>
                  </div>
                  
                  <!-- View Mode -->
                  <template v-else>
                    <div class="entry-content" v-html="formatContent(entry.content)"></div>
                    <footer class="entry-footer">
                      <div class="actions">
                        <button 
                          :class="{ 'liked': entry.currentUserVote === 'LIKE' }"
                          @click="vote(entry.id, 'LIKE')"
                        >
                          <ThumbsUp class="icon-sm" />
                          <span>{{ entry.likeCount || 0 }}</span>
                        </button>
                        <button 
                          :class="{ 'disliked': entry.currentUserVote === 'DISLIKE' }"
                          @click="vote(entry.id, 'DISLIKE')"
                        >
                          <ThumbsDown class="icon-sm" />
                          <span>{{ entry.dislikeCount || 0 }}</span>
                        </button>
                        <button 
                          :class="{ 'favorited': entry.currentUserVote === 'FAVORITE' }"
                          @click="vote(entry.id, 'FAVORITE')"
                        >
                          <Star class="icon-sm" />
                          <span>{{ entry.favoriteCount || 0 }}</span>
                        </button>
                        <button @click="shareEntry(entry.id)">
                          <Share2 class="icon" />
                        </button>
                        <!-- Edit/Delete buttons for authorized users -->
                        <button 
                          v-if="authStore.canEditEntry(entry)"
                          class="edit-btn"
                          @click="startEdit(entry)"
                          title="düzenle"
                        >
                          <Edit2 class="icon" />
                        </button>
                        <button 
                          v-if="authStore.canDeleteEntry(entry)"
                          class="delete-btn"
                          @click="openDeleteModal(entry)"
                          title="sil"
                        >
                          <Trash2 class="icon" />
                        </button>
                      </div>
                      <div class="meta">
                        <router-link :to="`/biri/${entry.authorUsername || entry.author?.username}`" class="author">
                          {{ entry.authorUsername || entry.author?.username || '-' }}
                        </router-link>
                        <span class="date">{{ formatDate(entry.createdAt) }}</span>
                      </div>
                    </footer>
                  </template>
                </article>
              </template>
              
              <!-- Pagination -->
              <div v-if="entriesStore.totalPages > 1" class="pagination">
                <button 
                  :disabled="entriesStore.currentPage === 0" 
                  @click="changePage(entriesStore.currentPage - 1)"
                  class="page-btn"
                >
                  ←
                </button>
                <span class="page-info">{{ entriesStore.currentPage + 1 }} / {{ entriesStore.totalPages }}</span>
                <button 
                  :disabled="entriesStore.currentPage >= entriesStore.totalPages - 1" 
                  @click="changePage(entriesStore.currentPage + 1)"
                  class="page-btn"
                >
                  →
                </button>
              </div>
            </div>
      
            <!-- Entry Form -->
            <div v-if="authStore.isAuthenticated" class="entry-form-container">
              <div class="form-header">
                <span>entry yaz</span>
              </div>
              <textarea 
                v-model="newEntry" 
                placeholder="düşüncelerini paylaş..."
                rows="4"
              ></textarea>
              <div class="form-footer">
                <span class="hint">en az 10 karakter</span>
                <button 
                  class="submit-btn"
                  :disabled="newEntry.length < 10 || submitting"
                  @click="submitEntry"
                >
                  <Loader2 v-if="submitting" class="spin-sm" />
                  {{ submitting ? 'gönderiliyor...' : 'yolla' }}
                </button>
              </div>
            </div>
      
            <!-- Login prompt -->
            <div v-else class="login-prompt">
              <PenSquare class="prompt-icon" />
              <p>entry yazmak için giriş yapın</p>
              <div class="prompt-actions">
                <router-link to="/giris" class="btn-secondary">giriş yap</router-link>
                <router-link to="/kayit" class="btn-primary">kayıt ol</router-link>
              </div>
            </div>
          </div>
        </main>
      </div>
    
      <!-- Mobile Layout -->
      <div class="mobile-layout">
          <div class="topic-container">
            <!-- Topic Header -->
            <div class="topic-header">
              <button class="back-link" @click="goBack">
                <ArrowLeft class="icon" />
                <span>geri</span>
              </button>
              <div v-if="topic?.categoryName" class="badge-lg" style="margin-bottom: 0.5rem; display: inline-block;">{{ topic.categoryName }}</div>
              <h1>
                {{ topic?.title || slug }}
              </h1>
              <div class="topic-meta">
                <span><MessageSquare class="icon-sm" /> {{ topic?.entryCount || entries.length }} entry</span>
              </div>
             </div>

             <!-- Mobile entries reuse the same logic, simplified view if needed or just same content -->
              <div class="entries-container">
                 <div v-if="loading" class="loading-state">
                    <Loader2 class="spin" />
                 </div>
                 <div v-else-if="entries.length === 0" class="empty-state">
                    <MessageSquare class="empty-icon" />
                    <p>henüz entry yok</p>
                 </div>
                 <template v-else>
                    <article v-for="(entry, index) in entries" :key="entry.id" class="entry-card">
                        <!-- Reuse entry card structure or simplify for mobile if needed. 
                             For now, reusing entry-card styles which are responsive enough. -->
                        <div class="entry-content" v-html="formatContent(entry.content)"></div>
                         <footer class="entry-footer">
                            <div class="actions">
                                <button :class="{ 'liked': entry.currentUserVote === 'LIKE' }" @click="vote(entry.id, 'LIKE')">
                                    <ThumbsUp class="icon-sm" /> <span>{{ entry.likeCount || 0 }}</span>
                                </button>
                                <button :class="{ 'disliked': entry.currentUserVote === 'DISLIKE' }" @click="vote(entry.id, 'DISLIKE')">
                                    <ThumbsDown class="icon-sm" /> <span>{{ entry.dislikeCount || 0 }}</span>
                                </button>
                                <button :class="{ 'favorited': entry.currentUserVote === 'FAVORITE' }" @click="vote(entry.id, 'FAVORITE')">
                                    <Star class="icon-sm" /> <span>{{ entry.favoriteCount || 0 }}</span>
                                </button>
                            </div>
                            <div class="meta">
                                <router-link :to="`/biri/${entry.authorUsername || entry.author?.username}`" class="author">
                                  {{ entry.authorUsername || entry.author?.username || '-' }}
                                </router-link>
                            </div>
                         </footer>
                    </article>
                 </template>
              </div>

              <!-- Entry Form - Mobile -->
              <div v-if="authStore.isAuthenticated" class="entry-form-container" style="margin-top: 2rem;">
                <div class="form-header">
                  <span>entry yaz</span>
                </div>
                <textarea 
                  v-model="newEntry" 
                  placeholder="düşüncelerini paylaş..."
                  rows="4"
                ></textarea>
                <div class="form-footer">
                  <span class="hint">en az 10 karakter</span>
                  <button 
                    class="submit-btn"
                    :disabled="newEntry.length < 10 || submitting"
                    @click="submitEntry"
                  >
                    <Loader2 v-if="submitting" class="spin-sm" />
                    {{ submitting ? 'gönderiliyor...' : 'yolla' }}
                  </button>
                </div>
              </div>
        
              <!-- Login prompt - Mobile -->
              <div v-else class="login-prompt" style="margin-top: 2rem;">
                <PenSquare class="prompt-icon" />
                <p>entry yazmak için giriş yapın</p>
                <div class="prompt-actions">
                  <router-link to="/giris" class="btn-secondary">giriş yap</router-link>
                  <router-link to="/kayit" class="btn-primary">kayıt ol</router-link>
                </div>
              </div>
          </div>
      </div>
    </div>

    <!-- Modals -->
    <!-- Delete Confirmation Modal -->
    <div v-if="deleteModalEntry" class="modal-overlay" @click.self="closeDeleteModal">
      <div class="delete-modal">
        <div class="modal-header">
          <h3>Entry'yi Sil</h3>
        </div>
        <div class="modal-body">
          <p>Bu entry'yi silmek istediğinizden emin misiniz?</p>
          <div class="entry-preview">
            {{ truncateText(deleteModalEntry.content, 100) }}
          </div>
          <div class="form-group" style="margin-top: 1rem;">
             <label>Silme Sebebi (Opsiyonel)</label>
             <textarea v-model="deleteEntryReason" class="form-textarea" placeholder="Neden siliyorsunuz?" rows="2" style="min-height: 60px"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="closeDeleteModal">İptal</button>
          <button class="btn-delete" @click="confirmDeleteEntry">Sil</button>
        </div>
      </div>
    </div>

    <!-- Topic Delete Modal -->
    <div v-if="showTopicDeleteModal" class="modal-overlay" @click.self="closeTopicDeleteModal">
      <div class="delete-modal">
        <div class="modal-header">
          <h3>Başlığı Sil</h3>
        </div>
        <div class="modal-body">
          <p>Bu başlığı ve tüm entry'leri silmek istediğinizden emin misiniz?</p>
          <div class="entry-preview">
            {{ topic?.title }}
          </div>
          <div class="form-group" style="margin-top: 1rem;">
             <label>Silme Sebebi (Opsiyonel)</label>
             <textarea v-model="deleteTopicReason" class="form-textarea" placeholder="Neden siliyorsunuz?" rows="2" style="min-height: 60px"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="closeTopicDeleteModal">İptal</button>
          <button class="btn-delete" @click="confirmDeleteTopic">Sil</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  ArrowLeft, MessageSquare, Eye, ThumbsUp, ThumbsDown, 
  Star, Share2, Loader2, PenSquare, Edit2, Trash2
} from 'lucide-vue-next'
import Sidebar from '@/components/layout/Sidebar.vue'
import Header from '@/components/layout/Header.vue'
import { useTopicsStore } from '@/stores/topics'
import { useEntriesStore } from '@/stores/entries'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const router = useRouter()
const topicsStore = useTopicsStore()
const entriesStore = useEntriesStore()
const authStore = useAuthStore()
const toast = useToast()

function goBack() {
  if (window.history.length > 2) {
    router.back()
  } else {
    router.push('/')
  }
}

const loading = ref(true)
const newEntry = ref('')
const submitting = ref(false)

const topicId = computed(() => route.params.id)
const topic = computed(() => topicsStore.currentTopic)
const entries = computed(() => entriesStore.entries)

function formatContent(content) {
  if (!content) return ''
  return content
    .replace(/@(\w+)/g, '<a href="/biri/$1">@$1</a>')
    .replace(/\(bkz: ([^)]+)\)/g, '<a href="/arama?q=$1">(bkz: $1)</a>')
}

function formatDate(date) {
  if (!date) return ''
  const d = new Date(date)
  const now = new Date()
  const diff = now - d
  const mins = Math.floor(diff / 60000)
  
  if (mins < 60) return `${mins} dakika önce`
  const hours = Math.floor(mins / 60)
  if (hours < 24) return `${hours} saat önce`
  
  return d.toLocaleDateString('tr-TR', { day: 'numeric', month: 'long', year: 'numeric' })
}

async function vote(entryId, voteType) {
  if (!authStore.isAuthenticated) {
    toast.warning('Oy vermek için giriş yapmalısınız')
    return
  }
  await entriesStore.vote(entryId, voteType)
}

function shareEntry(entryId) {
  const url = `${window.location.origin}/entry/${entryId}`
  navigator.clipboard.writeText(url)
  toast.success('Link kopyalandı!')
}

// Edit state
const editingEntryId = ref(null)
const editContent = ref('')

function startEdit(entry) {
  editingEntryId.value = entry.id
  editContent.value = entry.content
}

function cancelEdit() {
  editingEntryId.value = null
  editContent.value = ''
}

async function saveEdit(entryId) {
  if (!editContent.value.trim()) return
  
  const result = await entriesStore.updateEntry(entryId, editContent.value.trim())
  if (result.success) {
    editingEntryId.value = null
    editContent.value = ''
  } else {
    toast.error(result.message || 'Entry güncellenemedi')
  }
}

// Delete modal state
const deleteModalEntry = ref(null)
const deleteEntryReason = ref('')

function openDeleteModal(entry) {
  deleteModalEntry.value = entry
  deleteEntryReason.value = ''
}

function closeDeleteModal() {
  deleteModalEntry.value = null
  deleteEntryReason.value = ''
}

async function confirmDeleteEntry() {
  if (!deleteModalEntry.value) return
  
  const result = await entriesStore.deleteEntry(deleteModalEntry.value.id, deleteEntryReason.value)
  if (result.success) {
    closeDeleteModal()
  } else {
    toast.error(result.message || 'Entry silinemedi')
  }
}

// Topic delete modal
const showTopicDeleteModal = ref(false)
const deleteTopicReason = ref('')

function openTopicDeleteModal() {
  showTopicDeleteModal.value = true
  deleteTopicReason.value = ''
}

function closeTopicDeleteModal() {
  showTopicDeleteModal.value = false
  deleteTopicReason.value = ''
}

async function confirmDeleteTopic() {
  if (!topic.value) return
  
  const result = await topicsStore.deleteTopic(topic.value.id, deleteTopicReason.value)
  if (result.success) {
    closeTopicDeleteModal()
    router.push('/')
  } else {
    toast.error(result.message || 'Başlık silinemedi')
  }
}

function truncateText(text, maxLength) {
  if (!text) return ''
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}

async function submitEntry() {
  if (newEntry.value.length < 10 || !topic.value) return
  
  submitting.value = true
  const result = await entriesStore.createEntry({
    topicId: topic.value.id,
    content: newEntry.value,
    author: { username: authStore.username }, 
  })
  
  if (result.success) {
    newEntry.value = ''
  } else {
    toast.error(result.message || 'Entry gönderilemedi')
  }
  submitting.value = false
}

function changePage(page) {
  if (page < 0 || page >= entriesStore.totalPages) return
  entriesStore.fetchEntriesByTopic(topic.value.id, page, 10)
  window.scrollTo(0, 0)
}
async function fetchData(id) {
  loading.value = true
  // Flicker'ı önlemek için veriler gelene kadar bekleyelim, store'u hemen boşaltmayalım
  await topicsStore.fetchTopicById(id)
  if (topic.value?.id) {
    await entriesStore.fetchEntriesByTopic(topic.value.id, 0, 10)
  }
  loading.value = false
}

watch(() => route.params.id, (newId) => {
  if (newId) {
    fetchData(newId)
  }
})

onMounted(() => {
  if (route.params.id) {
    fetchData(route.params.id)
  }
})
</script>

<style scoped>
.topic-page {
  min-height: 100vh;
  background: transparent;
  color: #e0e0e0;
}

.page-container {
  max-width: 1100px;
  margin: 0 auto;
  padding-top: 50px;
}

.desktop-layout {
  display: flex;
}

.main-content {
  flex: 1;
  padding: 1.5rem;
}

/* Hide Sidebar on mobile */
@media (max-width: 768px) {
  .desktop-layout {
    display: none;
  }
}

.mobile-layout {
    display: none;
    padding: 1rem 0.75rem;
}

@media (max-width: 768px) {
  .mobile-layout {
    display: block;
  }
  
  .entry-card {
    padding: 1.25rem 1rem;
    margin-bottom: 0.75rem;
    background: rgba(26, 26, 46, 0.45);
    backdrop-filter: blur(8px);
    -webkit-backdrop-filter: blur(8px);
    border: 1px solid rgba(255, 237, 0, 0.05);
  }
}

.topic-container {
  width: 100%; 
}

/* Topic Header */
.topic-header {
  background: rgba(26, 26, 46, 0.7);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(212, 200, 74, 0.15);
  border-radius: 12px;
  padding: 1.25rem;
  margin-bottom: 1rem;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  color: #888;
  text-decoration: none;
  font-size: 0.85rem;
  margin-bottom: 0.75rem;
  transition: color 0.15s;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
}

.back-link:hover {
  color: #d4c84a;
}

.topic-header h1 {
  font-size: 1.5rem;
  color: #d4c84a;
  margin: 0;
}

.badge-lg {
  display: inline-block;
  font-size: 0.9rem;
  background: #2a2a4a;
  color: #d4c84a;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  font-weight: normal;
}

@media (max-width: 768px) {
  .badge-lg {
    display: block !important;
    width: fit-content;
    margin: 0.5rem 0 0.8rem 0 !important;
  }
}

.topic-meta {
  display: flex;
  gap: 1.5rem;
  font-size: 0.85rem;
  color: #666;
  margin-top: 0.5rem;
}

.topic-meta span {
  display: flex;
  align-items: center;
  gap: 0.35rem;
}

.icon {
  width: 16px;
  height: 16px;
}

.icon-sm {
  width: 14px;
  height: 14px;
}

/* Topic Actions */
.topic-actions {
  margin-top: 0.75rem;
}

.topic-delete-btn {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.4rem 0.75rem;
  background: transparent;
  border: 1px solid #f85149;
  border-radius: 6px;
  color: #f85149;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.15s;
}

.topic-delete-btn:hover {
  background: rgba(248, 81, 73, 0.15);
}

/* Entries Container */
.entries-container {
  background: #1a1a2e;
  border: 1px solid #2a2a4a;
  border-radius: 12px;
  min-height: 200px;
  margin-bottom: 1rem;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  gap: 1rem;
  color: #666;
}

.spin {
  animation: spin 1s linear infinite;
  width: 24px;
  height: 24px;
}

.spin-sm {
  animation: spin 1s linear infinite;
  width: 16px;
  height: 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  text-align: center;
}

.empty-icon {
  width: 48px;
  height: 48px;
  color: #d4c84a;
  opacity: 0.5;
  margin-bottom: 1rem;
}

.empty-state p {
  color: #888;
  margin: 0 0 0.25rem;
}

.empty-hint {
  font-size: 0.85rem;
  color: #6fbf6f;
}

/* Entry Card */
.entry-card {
  padding: 1.5rem;
  background: rgba(26, 26, 46, 0.45);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 237, 0, 0.05);
  border-radius: 12px;
  margin-bottom: 1rem;
  position: relative;
  transition: transform 0.2s;
}

.entry-card:hover {
  background: rgba(26, 26, 46, 0.55);
  border-color: rgba(255, 237, 0, 0.15);
}

.entry-number {
  position: absolute;
  top: 1rem;
  right: 1rem;
  font-size: 0.7rem;
  color: #444;
}

.entry-content {
  font-size: 0.95rem;
  line-height: 1.7;
  color: #ccc;
  margin-bottom: 1rem;
  padding-right: 2rem;
}

.entry-content :deep(a) {
  color: #58a6ff;
  text-decoration: none;
}

.entry-content :deep(a:hover) {
  text-decoration: underline;
}

.entry-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.actions {
  display: flex;
  gap: 0.25rem;
}

.actions button {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.35rem 0.6rem;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.05);
  color: #999;
  font-size: 0.75rem;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.actions button:hover {
  background: rgba(212, 200, 74, 0.1);
  border-color: rgba(212, 200, 74, 0.2);
  color: #d4c84a;
}

.actions button.liked {
  color: #3fb950;
}

.actions button.disliked {
  color: #f85149;
}

.actions button.edit-btn:hover {
  color: #58a6ff;
}

.actions button.delete-btn:hover {
  color: #f85149;
}

.actions button.favorited {
  color: #d4c84a;
}

.meta {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 0.8rem;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 2rem;
  padding-top: 1rem;
  border-top: 1px solid #2a2a4a;
}

.page-btn {
  padding: 0.5rem 1rem;
  background: #1a1a2e;
  border: 1px solid #2a2a4a;
  color: #d4c84a;
  border-radius: 4px;
  cursor: pointer;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  color: #666;
}

.page-info {
  font-size: 0.9rem;
  color: #888;
}

.author {
  color: #6fbf6f;
  text-decoration: none;
}

.author:hover {
  text-decoration: underline;
}

.date {
  color: #555;
}

/* Entry Form */
.entry-form-container {
  background: #1a1a2e;
  border: 1px solid #2a2a4a;
  border-radius: 12px;
  overflow: hidden;
}

.form-header {
  padding: 0.75rem 1rem;
  background: #0d0d1a;
  border-bottom: 1px solid #2a2a4a;
  font-size: 0.85rem;
  color: #888;
}

.entry-form-container textarea {
  width: 100%;
  padding: 1rem;
  background: transparent;
  border: none;
  color: #e0e0e0;
  font-size: 0.95rem;
  line-height: 1.6;
  resize: vertical;
  min-height: 100px;
}

.entry-form-container textarea:focus {
  outline: none;
}

.entry-form-container textarea::placeholder {
  color: #555;
}

.form-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  border-top: 1px solid #2a2a4a;
}

.hint {
  font-size: 0.75rem;
  color: #555;
}

.submit-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.6rem 1.25rem;
  background: #d4c84a;
  color: #0f0f1a;
  border: none;
  border-radius: 6px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
}

.submit-btn:hover:not(:disabled) {
  background: #e0d454;
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Login Prompt */
.login-prompt {
  background: #1a1a2e;
  border: 1px solid #2a2a4a;
  border-radius: 12px;
  padding: 2rem;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.prompt-icon {
  width: 32px;
  height: 32px;
  color: #666;
}

.login-prompt p {
  color: #aaa;
  margin: 0;
}

.prompt-actions {
  display: flex;
  gap: 1rem;
  margin-top: 0.5rem;
}

.btn-primary {
  padding: 0.5rem 1.25rem;
  background: #d4c84a;
  color: #0f0f1a;
  border-radius: 6px;
  text-decoration: none;
  font-size: 0.9rem;
  font-weight: 500;
}

.btn-secondary {
  padding: 0.5rem 1.25rem;
  background: transparent;
  border: 1px solid #2a2a4a;
  color: #ccc;
  border-radius: 6px;
  text-decoration: none;
  font-size: 0.9rem;
}

/* Modals */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(4px);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.delete-modal {
  background: #1a1a2e;
  border: 1px solid #2a2a4a;
  border-radius: 12px;
  width: 90%;
  max-width: 400px;
  overflow: hidden;
}

.modal-header {
  padding: 1rem;
  border-bottom: 1px solid #2a2a4a;
}

.modal-header h3 {
  margin: 0;
  color: #e0e0e0;
  font-size: 1.1rem;
}

.modal-body {
  padding: 1.5rem;
}

.modal-body p {
  color: #ccc;
  margin: 0 0 1rem;
}

.entry-preview {
  background: #0f0f1a;
  padding: 1rem;
  border-radius: 8px;
  color: #888;
  font-size: 0.9rem;
  font-style: italic;
  border-left: 3px solid #f85149;
}

.modal-footer {
  padding: 1rem;
  background: #0d0d1a;
  border-top: 1px solid #2a2a4a;
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
}

.btn-cancel {
  padding: 0.5rem 1rem;
  background: transparent;
  border: 1px solid #2a2a4a;
  color: #ccc;
  border-radius: 6px;
  cursor: pointer;
}

.btn-delete {
  padding: 0.5rem 1rem;
  background: #f85149;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.edit-mode {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.edit-textarea {
  width: 100%;
  background: #0a0a14;
  border: 1px solid #2a2a4a;
  padding: 1rem;
  color: #e0e0e0;
  border-radius: 8px;
  resize: vertical;
}

.edit-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}

.btn-save {
  padding: 0.5rem 1rem;
  background: #3fb950;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.btn-save:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  font-size: 0.85rem;
  color: #aaa;
  margin-bottom: 0.5rem;
}

.form-textarea {
  width: 100%;
  padding: 0.75rem;
  background: #0d0d1a;
  border: 1px solid #2a2a4a;
  border-radius: 6px;
  color: #e0e0e0;
  font-size: 0.9rem;
  resize: vertical;
}

.form-textarea:focus {
  outline: none;
  border-color: #d4c84a;
}
</style>
