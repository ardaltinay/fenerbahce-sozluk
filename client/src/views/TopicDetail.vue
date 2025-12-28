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
              <div class="header-row">
                <h1>{{ topic?.title }}</h1>
              </div>
              <div class="topic-meta">
                <span><MessageSquare class="icon-sm" /> {{ topic?.entryCount || 0 }} entry</span>
              </div>
              <!-- Topic Actions -->
              <div class="topic-actions" v-if="topic && (authStore.canDeleteTopic(topic) || authStore.isModeratorOrAdmin)">
                <!-- Add Transfermarkt Button (only if no transfermarkt exists) -->
                <button 
                  v-if="authStore.isModeratorOrAdmin && !topic.transfermarktId" 
                  class="topic-edit-btn" 
                  @click="showTransfermarktModal = true"
                  title="transfermarkt künyesi ekle"
                >
                  <Edit3 class="icon-sm" />
                  <span>künye ekle</span>
                </button>
                <button 
                  v-if="authStore.canDeleteTopic(topic)" 
                  class="topic-delete-btn" 
                  @click="openTopicDeleteModal" 
                  title="başlığı sil"
                >
                  <Trash2 class="icon-sm" />
                  <span>sil</span>
                </button>
              </div>
            </div>

            <!-- Transfermarkt Info Card -->
            <TransfermarktCard 
              v-if="topic?.transfermarktId && topic?.topicType"
              :type="topic.topicType"
              :transfermarkt-id="topic.transfermarktId"
            />

            <!-- Transfermarkt Edit Modal -->
            <div v-if="showTransfermarktModal" class="modal-overlay" @click.self="showTransfermarktModal = false">
              <div class="modal">
                <div class="modal-header">
                  <h3>Transfermarkt Künyesi Ekle</h3>
                  <button class="close-btn" @click="showTransfermarktModal = false">
                    <X class="icon" />
                  </button>
                </div>
                <div class="modal-body">
                  <div class="form-group">
                    <label>Künye Tipi</label>
                    <!-- Custom Dropdown with Tailwind -->
                    <div class="relative">
                      <button 
                        type="button"
                        @click="dropdownOpen = !dropdownOpen"
                        class="w-full flex items-center justify-between px-4 py-3 bg-[#0d0d1a] border border-[#2a2a4a] rounded-lg text-[#e0e0e0] text-sm cursor-pointer hover:border-[#58a6ff] transition-colors"
                      >
                        <span :class="transfermarktForm.topicType ? 'text-[#e0e0e0]' : 'text-[#666]'">
                          {{ topicTypeLabel }}
                        </span>
                        <ChevronDown class="w-4 h-4 text-[#888] transition-transform" :class="{ 'rotate-180': dropdownOpen }" />
                      </button>
                      
                      <!-- Dropdown Menu -->
                      <div 
                        v-if="dropdownOpen" 
                        class="absolute z-50 w-full mt-1 bg-[#0d0d1a] border border-[#2a2a4a] rounded-lg shadow-xl overflow-hidden"
                      >
                        <button
                          type="button"
                          @click="selectTopicType('')"
                          class="w-full px-4 py-3 text-left text-sm text-[#666] hover:bg-[#1a1a2e] transition-colors"
                        >
                          Seçiniz...
                        </button>
                        <button
                          type="button"
                          @click="selectTopicType('player')"
                          class="w-full px-4 py-3 text-left text-sm text-[#e0e0e0] hover:bg-[#1a1a2e] transition-colors"
                          :class="{ 'bg-[#1a1a2e]': transfermarktForm.topicType === 'player' }"
                        >
                          Oyuncu
                        </button>
                        <button
                          type="button"
                          @click="selectTopicType('club')"
                          class="w-full px-4 py-3 text-left text-sm text-[#e0e0e0] hover:bg-[#1a1a2e] transition-colors"
                          :class="{ 'bg-[#1a1a2e]': transfermarktForm.topicType === 'club' }"
                        >
                          Kulüp
                        </button>
                      </div>
                    </div>
                  </div>
                  <div class="form-group">
                    <label>Transfermarkt id</label>
                    <input 
                      v-model="transfermarktForm.transfermarktId" 
                      type="text" 
                      placeholder="örn: 8263 (oyuncu ID)"
                    />
                    <small>transfermarkt.com URL'sinden id'yi alın</small>
                  </div>
                </div>
                <div class="modal-footer">
                  <button class="btn-secondary" @click="showTransfermarktModal = false">İptal</button>
                  <button class="btn-primary" @click="saveTransfermarkt" :disabled="!transfermarktForm.topicType || !transfermarktForm.transfermarktId">
                    Kaydet
                  </button>
                </div>
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
              <Pagination 
                :current-page="entriesStore.currentPage"
                :total-pages="entriesStore.totalPages"
                @change="changePage"
              />
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
              <div class="header-row">
                <h1>{{ topic?.title || 'başlık' }}</h1>
              </div>
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
                        <div class="entry-content" v-html="formatContent(entry.content)"></div>
                          <footer class="mobile-entry-footer">
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
                                 <button @click="shareEntry(entry.id)">
                                   <Share2 class="icon-sm" />
                                 </button>
                                 <!-- Edit/Delete for authorized users -->
                                 <button v-if="authStore.canEditEntry(entry)" class="edit-btn" @click="startEdit(entry)" title="düzenle">
                                   <Edit2 class="icon-sm" />
                                 </button>
                                 <button v-if="authStore.canDeleteEntry(entry)" class="delete-btn" @click="openDeleteModal(entry)" title="sil">
                                   <Trash2 class="icon-sm" />
                                 </button>
                             </div>
                             <div class="meta">
                                 <router-link :to="`/biri/${entry.authorUsername || entry.author?.username}`" class="author">
                                   {{ entry.authorUsername || entry.author?.username || '-' }}
                                 </router-link>
                                 <span class="date">{{ formatDate(entry.createdAt) }}</span>
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
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  ArrowLeft, MessageSquare, Eye, ThumbsUp, ThumbsDown, 
  Star, Share2, Loader2, PenSquare, Edit2, Edit3, Trash2, X, ChevronDown
} from 'lucide-vue-next'
import Sidebar from '@/components/layout/Sidebar.vue'
import Header from '@/components/layout/Header.vue'
import TransfermarktCard from '@/components/TransfermarktCard.vue'
import Pagination from '@/components/ui/Pagination.vue'
import { useTopicsStore } from '@/stores/topics'
import { useEntriesStore } from '@/stores/entries'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import { useWebSocket } from '@/composables/useWebSocket'
import { topicsApi } from '@/services/api'

const route = useRoute()
const router = useRouter()
const topicsStore = useTopicsStore()
const entriesStore = useEntriesStore()
const authStore = useAuthStore()
const toast = useToast()
const { connect, subscribeToTopic, unsubscribeFromTopic } = useWebSocket()

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

// Transfermarkt modal
const showTransfermarktModal = ref(false)
const dropdownOpen = ref(false)
const transfermarktForm = ref({
  topicType: '',
  transfermarktId: ''
})

const topicTypeLabel = computed(() => {
  const labels = {
    'player': 'Oyuncu',
    'club': 'Kulüp'
  }
  return labels[transfermarktForm.value.topicType] || 'Seçiniz...'
})

function selectTopicType(value) {
  transfermarktForm.value.topicType = value
  dropdownOpen.value = false
}

async function saveTransfermarkt() {
  try {
    await topicsApi.updateTransfermarkt(
      topicId.value, 
      transfermarktForm.value.transfermarktId, 
      transfermarktForm.value.topicType
    )
    toast.success('Künye eklendi!')
    showTransfermarktModal.value = false
    // Refresh topic data
    await topicsStore.fetchTopicById(topicId.value)
  } catch (error) {
    toast.error('Künye eklenirken bir hata oluştu')
  }
}

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
    topicsStore.fetchSidebarTopics(0, 50)
    // Refresh topic info for entry count
    await topicsStore.fetchTopicById(topic.value.id)
    
    // Calculate last page
    const entryCount = topic.value.entryCount
    const lastPage = Math.max(0, Math.ceil(entryCount / 10) - 1)
    
    // Fetch and go to last page
    await entriesStore.fetchEntriesByTopic(topic.value.id, lastPage, 10, true)
    
    // Scroll to the new entry (bottom of page usually)
    setTimeout(() => {
        const entriesContainer = document.querySelector('.entries-container')
        if (entriesContainer) {
             // Scroll to bottom of entries container or just page bottom
             window.scrollTo({ top: document.body.scrollHeight, behavior: 'smooth' })
        }
    }, 100)
    
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
  await topicsStore.fetchTopicById(id)
  if (topic.value?.id) {
    await entriesStore.fetchEntriesByTopic(topic.value.id, 0, 10)
  } else {
    // Topic not found or invalid ID, redirect to 404
    router.push('/404')
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
  
  // Check for draft content from NewTopicModal redirect
  if (route.query.draft) {
    try {
      newEntry.value = decodeURIComponent(route.query.draft)
      toast.info('Taslak içeriğiniz entry formuna eklendi. Göndermek için "yolla" butonuna basın.')
      // Remove draft from URL without reloading
      router.replace({ path: route.path, query: {} })
    } catch (e) {
      console.error('Draft decode error:', e)
    }
  }
})

// WebSocket for real-time updates
let currentSubscribedTopicId = null
const PAGE_SIZE = 10

function setupWebSocket(topicId) {
  if (currentSubscribedTopicId) {
    unsubscribeFromTopic(currentSubscribedTopicId)
  }
  
  connect()
  subscribeToTopic(topicId, (newEntry) => {
    // Only add if not already in list (avoid duplicates from own entries)
    const exists = entriesStore.entries.some(e => e.id === newEntry.id)
    if (!exists) {
      // Update topic entry count
      if (topic.value) {
        topic.value.entryCount = (topic.value.entryCount || 0) + 1
      }
      
      // Check if we're on the last page
      const isLastPage = entriesStore.currentPage >= entriesStore.totalPages - 1
      
      // If on last page and current entries are less than page size, add directly
      if (isLastPage && entriesStore.entries.length < PAGE_SIZE) {
        entriesStore.entries.push(newEntry)
      } else if (isLastPage) {
        // Last page but full - need to create new page, refresh to update pagination
        entriesStore.fetchEntriesByTopic(topic.value.id, entriesStore.currentPage, PAGE_SIZE)
      }
      // If not on last page, don't add - user will see it when they navigate to last page
    }
  })
  currentSubscribedTopicId = topicId
}

watch(() => route.params.id, (newId, oldId) => {
  if (newId && newId !== oldId) {
    setupWebSocket(newId)
  }
})

onMounted(() => {
  if (route.params.id) {
    setupWebSocket(route.params.id)
  }
})

onUnmounted(() => {
  if (currentSubscribedTopicId) {
    unsubscribeFromTopic(currentSubscribedTopicId)
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

@media (max-width: 768px) {
  .desktop-layout { display: none; }
}

.mobile-layout {
    display: none;
    padding: 1rem 0.75rem;
}

@media (max-width: 768px) {
  .mobile-layout {
    display: block;
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

.header-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .header-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }
  
  .badge-lg {
    margin-bottom: 0.5rem;
  }
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
  display: flex;
  gap: 0.5rem;
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

.topic-edit-btn {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.4rem 0.75rem;
  background: transparent;
  border: 1px solid #58a6ff;
  border-radius: 6px;
  color: #58a6ff;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.15s;
}

.topic-edit-btn:hover {
  background: rgba(88, 166, 255, 0.15);
}

/* Entries Container */
.entries-container {
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
}

.entry-content :deep(a) {
  color: #58a6ff;
  text-decoration: none;
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

.actions button.liked { color: #3fb950; }
.actions button.disliked { color: #f85149; }
.actions button.favorited { color: #d4c84a; }
.actions button.edit-btn:hover { color: #58a6ff; }
.actions button.delete-btn:hover { color: #f85149; }

.meta {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 0.8rem;
}

/* Mobile Specific Footer */
.mobile-entry-footer {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-top: 0.5rem;
}

.mobile-entry-footer .actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.mobile-entry-footer .meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.75rem;
  color: #666;
}

.mobile-entry-footer .meta .author {
  color: #d4c84a;
}

/* Entry Form */
.entry-form-container {
  margin-top: 2rem;
  padding: 1.5rem;
  background: rgba(26, 26, 46, 0.6);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(212, 200, 74, 0.15);
  border-radius: 12px;
}

.form-header {
  margin-bottom: 1rem;
}

.form-header span {
  font-size: 1.1rem;
  font-weight: 600;
  color: #d4c84a;
}

textarea {
  width: 100%;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 237, 0, 0.1);
  border-radius: 8px;
  padding: 1rem;
  color: #e0e0e0;
  font-family: inherit;
  font-size: 0.95rem;
  resize: vertical;
  min-height: 120px;
  transition: all 0.2s;
}

textarea:focus {
  outline: none;
  border-color: rgba(212, 200, 74, 0.4);
  background: rgba(0, 0, 0, 0.3);
  box-shadow: 0 0 0 4px rgba(212, 200, 74, 0.05);
}

.form-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 1rem;
}

.hint {
  font-size: 0.8rem;
  color: #666;
}

.submit-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.6rem 1.5rem;
  background: #d4c84a;
  color: #1a1a2e;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(212, 200, 74, 0.2);
}

.submit-btn:active:not(:disabled) {
  transform: translateY(0);
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.spin-sm {
  width: 16px;
  height: 16px;
  animation: spin 1s linear infinite;
}

/* Login Prompt */
.login-prompt {
  margin-top: 2rem;
  padding: 2.5rem;
  background: rgba(26, 26, 46, 0.4);
  border: 1px dashed rgba(212, 200, 74, 0.2);
  border-radius: 12px;
  text-align: center;
}

.prompt-icon {
  width: 40px;
  height: 40px;
  color: #d4c84a;
  margin-bottom: 1rem;
  opacity: 0.8;
}

.prompt-actions {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-top: 1.5rem;
}

.btn-primary, .btn-secondary {
  padding: 0.6rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.2s;
}

.btn-primary {
  background: #d4c84a;
  color: #1a1a2e;
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.05);
  color: #e0e0e0;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.btn-primary:hover {
  opacity: 0.9;
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.1);
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
}

.modal {
  background: #1a1a2e;
  border: 1px solid #2a2a4a;
  border-radius: 12px;
  width: 100%;
  max-width: 450px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid #2a2a4a;
}

.modal-header h3 {
  margin: 0;
  font-size: 1rem;
  color: #e0e0e0;
}

.modal-header .close-btn {
  background: transparent;
  border: none;
  color: #666;
  cursor: pointer;
  padding: 0.25rem;
}

.modal-header .close-btn:hover {
  color: #fff;
}

.modal-body {
  padding: 1.25rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-size: 0.85rem;
  color: #888;
}

.form-group input {
  width: 100%;
  padding: 0.75rem;
  background: #0d0d1a;
  border: 1px solid #2a2a4a;
  border-radius: 6px;
  color: #e0e0e0;
  font-size: 0.9rem;
}

.form-group select,
.form-select {
  width: 100%;
  padding: 0.75rem;
  padding-right: 2.5rem;
  background: #0d0d1a;
  border: 1px solid #2a2a4a;
  border-radius: 6px;
  color: #e0e0e0;
  font-size: 0.9rem;
  cursor: pointer;
  appearance: none;
  -webkit-appearance: none;
  -moz-appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12' fill='%23888'%3E%3Cpath d='M6 8L1 3h10z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 0.75rem center;
}

.form-group select:focus,
.form-group input:focus,
.form-select:focus {
  outline: none;
  border-color: #58a6ff;
}

.form-group select option,
.form-select option {
  background: #0d0d1a;
  color: #e0e0e0;
  padding: 0.5rem;
}

.form-group small {
  display: block;
  margin-top: 0.35rem;
  font-size: 0.75rem;
  color: #666;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  padding: 1rem 1.25rem;
  border-top: 1px solid #2a2a4a;
}

.modal-footer .btn-primary,
.modal-footer .btn-secondary {
  padding: 0.5rem 1rem;
  border-radius: 6px;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.15s;
}

.modal-footer .btn-primary {
  background: #58a6ff;
  border: none;
  color: #fff;
}

.modal-footer .btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.modal-footer .btn-secondary {
  background: transparent;
  border: 1px solid #444;
  color: #888;
}
</style>

<style scoped>
/* Edit Mode & Buttons */
.edit-mode {
  padding: 0.5rem 0;
  background: transparent;
  border: none;
  margin-bottom: 1rem;
}

.edit-textarea {
  width: 100%;
  padding: 0.75rem;
  background: #0d0d1a;
  border: 1px solid #2a2a4a;
  border-radius: 6px;
  color: #e0e0e0;
  font-size: 0.95rem;
  line-height: 1.5;
  resize: vertical;
  margin-bottom: 0.75rem;
}

.edit-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}

.btn-cancel {
  padding: 0.5rem 1rem;
  background: transparent;
  border: 1px solid #444;
  color: #888;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-cancel:hover {
  border-color: #666;
  color: #aaa;
}

.btn-save {
  padding: 0.5rem 1rem;
  background: #d4c84a;
  color: #1a1a2e;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-save:hover:not(:disabled) {
  background: #e6da5c;
  transform: translateY(-1px);
}
.btn-save:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-delete {
  padding: 0.5rem 1rem;
  background: #ef4444;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-delete:hover {
  background: #dc2626;
}

/* Delete Modal Specifics */
.delete-modal {
  background: #1a1a2e;
  border: 1px solid #2a2a4a;
  border-radius: 12px;
  width: 100%;
  max-width: 450px;
  overflow: hidden;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.5);
}

.entry-preview {
  padding: 1rem;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 6px;
  font-style: italic;
  font-size: 0.9rem;
  color: #bbb;
  border-left: 3px solid #d4c84a;
  margin: 1rem 0;
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
</style>
