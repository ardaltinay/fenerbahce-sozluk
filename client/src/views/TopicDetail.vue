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

              <!-- Date Filter Tabs -->
              <div v-if="topic" class="date-filter-tabs">
                <button 
                  :class="{ active: !activeDateFilter }"
                  @click="setDateFilter(null)"
                >
                  tümü ({{ topic.entryCount || 0 }})
                </button>
                <button 
                  v-if="topic.todayEntryCount > 0"
                  :class="{ active: activeDateFilter === 'today' }"
                  @click="setDateFilter('today')"
                >
                  bugün ({{ topic.todayEntryCount }})
                </button>
                <button 
                  v-if="topic.yesterdayEntryCount > 0"
                  :class="{ active: activeDateFilter === 'yesterday' }"
                  @click="setDateFilter('yesterday')"
                >
                  dün ({{ topic.yesterdayEntryCount }})
                </button>
                <button 
                  v-if="topic.olderEntryCount > 0"
                  :class="{ active: activeDateFilter === 'older' }"
                  @click="setDateFilter('older')"
                >
                  önceki günler ({{ topic.olderEntryCount }})
                </button>
              </div>

              <!-- Topic Actions -->
              <div class="topic-actions" v-if="topic && (authStore.canDeleteTopic(topic) || authStore.isModeratorOrAdmin)">
                <!-- Add Transfermarkt Button (only if no transfermarkt exists) -->
                <button 
                  v-if="authStore.isModeratorOrAdmin" 
                  class="topic-edit-btn" 
                  @click="openKunyeModal"
                  :title="hasKunye ? 'künyeyi düzenle' : 'künye ekle'"
                >
                  <Edit3 class="icon-sm" />
                  <span>{{ hasKunye ? 'künye düzenle' : 'künye ekle' }}</span>
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

            <!-- Künye Card -->
            <div v-if="hasKunye" class="transfermarkt-card">
              <div class="player-card">
                <div class="player-image">
                  <img v-if="topic.kunyeImageUrl" :src="topic.kunyeImageUrl" :alt="topic.title" />
                  <div v-else class="placeholder-image">
                    <User class="icon" />
                  </div>
                </div>
                <div class="player-info">
                  <h3 class="player-name">{{ topic.title }}</h3>
                  <div class="player-details">
                    <template v-if="parsedKunyeData">
                      <div v-for="(value, key) in parsedKunyeData" :key="key" class="detail-row">
                        <span class="label">{{ key }}:</span>
                        <span class="value">{{ value }}</span>
                      </div>
                    </template>
                  </div>
                </div>
              </div>
              <div class="tm-badge">
                <span>Künye</span>
              </div>
            </div>

            <!-- Künye Edit Modal -->
            <div v-if="showKunyeModal" class="modal-overlay" @click.self="showKunyeModal = false">
              <div class="modal">
                <div class="modal-header">
                  <h3>{{ hasKunye ? 'Künye Düzenle' : 'Künye Ekle' }}</h3>
                  <button class="close-btn" @click="showKunyeModal = false">
                    <X class="icon" />
                  </button>
                </div>
                <div class="modal-body">
                  <div class="form-group">
                    <label>Resim URL</label>
                    <input 
                      v-model="kunyeForm.imageUrl" 
                      type="text" 
                      placeholder="https://example.com/image.jpg"
                    />
                  </div>
                  <div class="form-group">
                    <label>Künye Bilgileri (JSON)</label>
                    <textarea 
                      v-model="kunyeForm.kunyeData" 
                      rows="6"
                      class="form-textarea"
                      placeholder='{"Doğum Tarihi": "1990-01-01", "Mevki": "Forvet", "Kulüp": "Fenerbahçe"}'
                    ></textarea>
                    <small>JSON formatında key-value çiftleri girin</small>
                  </div>
                </div>
                <div class="modal-footer">
                  <button class="btn-secondary" @click="showKunyeModal = false">İptal</button>
                  <button 
                    class="btn-primary" 
                    @click="saveKunye"
                  >
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
              <!-- Toolbar -->
              <div class="entry-toolbar">
                <button 
                  type="button" 
                  class="toolbar-btn" 
                  @click="showBkzPopover = !showBkzPopover; showLinkPopover = false"
                  :class="{ active: showBkzPopover }"
                  title="referans ekle (bkz)"
                >
                  bkz
                </button>
                <button 
                  type="button" 
                  class="toolbar-btn" 
                  @click="showLinkPopover = !showLinkPopover; showBkzPopover = false"
                  :class="{ active: showLinkPopover }"
                  title="link ekle"
                >
                  link
                </button>
              </div>
              
              <!-- Bkz Popover -->
              <div v-if="showBkzPopover" class="toolbar-popover">
                <div class="popover-content">
                  <input 
                    v-model="bkzInput" 
                    type="text" 
                    placeholder="başlık adı girin..."
                    @keyup.enter="insertBkz"
                    ref="bkzInputRef"
                  />
                  <button type="button" class="popover-btn" @click="insertBkz">ekle</button>
                </div>
              </div>
              
              <!-- Link Popover -->
              <div v-if="showLinkPopover" class="toolbar-popover">
                <div class="popover-content">
                  <input 
                    v-model="linkUrl" 
                    type="text" 
                    placeholder="link URL (https://...)"
                    ref="linkInputRef"
                  />
                  <input 
                    v-model="linkText" 
                    type="text" 
                    placeholder="görünecek isim"
                    @keyup.enter="insertLink"
                  />
                  <button type="button" class="popover-btn" @click="insertLink">ekle</button>
                </div>
              </div>
              
              <textarea 
                ref="entryTextarea"
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
              
              <!-- Date Filter Tabs - Mobile -->
              <div v-if="topic" class="date-filter-tabs">
                <button 
                  :class="{ active: !activeDateFilter }"
                  @click="setDateFilter(null)"
                >
                  tümü ({{ topic.entryCount || 0 }})
                </button>
                <button 
                  v-if="topic.todayEntryCount > 0"
                  :class="{ active: activeDateFilter === 'today' }"
                  @click="setDateFilter('today')"
                >
                  bugün ({{ topic.todayEntryCount }})
                </button>
                <button 
                  v-if="topic.yesterdayEntryCount > 0"
                  :class="{ active: activeDateFilter === 'yesterday' }"
                  @click="setDateFilter('yesterday')"
                >
                  dün ({{ topic.yesterdayEntryCount }})
                </button>
                <button 
                  v-if="topic.olderEntryCount > 0"
                  :class="{ active: activeDateFilter === 'older' }"
                  @click="setDateFilter('older')"
                >
                  önceki günler ({{ topic.olderEntryCount }})
                </button>
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
                 
                 <!-- Pagination - Mobile -->
                 <Pagination 
                   :current-page="entriesStore.currentPage"
                   :total-pages="entriesStore.totalPages"
                   @change="changePage"
                 />
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
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  ArrowLeft, MessageSquare, Eye, ThumbsUp, ThumbsDown, 
  Star, Share2, Loader2, PenSquare, Edit2, Edit3, Trash2, X, User
} from 'lucide-vue-next'
import Sidebar from '@/components/layout/Sidebar.vue'
import Header from '@/components/layout/Header.vue'
import Pagination from '@/components/ui/Pagination.vue'
import { useTopicsStore } from '@/stores/topics'
import { useEntriesStore } from '@/stores/entries'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import { useWebSocket } from '@/composables/useWebSocket'
import api from '@/services/api'

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
const activeDateFilter = ref(null)

// Entry toolbar refs and state
const entryTextarea = ref(null)
const showBkzPopover = ref(false)
const showLinkPopover = ref(false)
const bkzInput = ref('')
const linkUrl = ref('')
const linkText = ref('')

function insertBkz() {
  if (!bkzInput.value.trim()) return
  
  const bkzStr = `(bkz: ${bkzInput.value.trim()})`
  insertAtCursor(bkzStr)
  
  bkzInput.value = ''
  showBkzPopover.value = false
}

function insertLink() {
  if (!linkUrl.value.trim() || !linkText.value.trim()) return
  
  // Format: [link text](url)
  const linkStr = `[${linkText.value.trim()}](${linkUrl.value.trim()})`
  insertAtCursor(linkStr)
  
  linkUrl.value = ''
  linkText.value = ''
  showLinkPopover.value = false
}

function insertAtCursor(text) {
  const textarea = entryTextarea.value
  if (!textarea) {
    newEntry.value += text
    return
  }
  
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const before = newEntry.value.substring(0, start)
  const after = newEntry.value.substring(end)
  
  newEntry.value = before + text + after
  
  // Set cursor after inserted text
  nextTick(() => {
    textarea.focus()
    textarea.selectionStart = textarea.selectionEnd = start + text.length
  })
}

// Künye modal
const showKunyeModal = ref(false)
const kunyeForm = ref({
  imageUrl: '',
  kunyeData: ''
})

// Check if topic has künye data
const hasKunye = computed(() => {
  return !!(topic.value?.kunyeImageUrl || topic.value?.kunyeData)
})

function openKunyeModal() {
  // Pre-fill form if künye exists
  if (hasKunye.value) {
    kunyeForm.value.imageUrl = topic.value.kunyeImageUrl || ''
    kunyeForm.value.kunyeData = topic.value.kunyeData || ''
  } else {
    kunyeForm.value.imageUrl = ''
    kunyeForm.value.kunyeData = ''
  }
  showKunyeModal.value = true
}

async function saveKunye() {
  try {
    await api.post(`/api/kunye/topics/${topicId.value}`, {
      imageUrl: kunyeForm.value.imageUrl,
      kunyeData: kunyeForm.value.kunyeData
    })
    
    toast.success(hasKunye.value ? 'Künye güncellendi!' : 'Künye eklendi!')
    showKunyeModal.value = false
    // Refresh topic data
    await topicsStore.fetchTopicById(topicId.value)
  } catch (error) {
    toast.error('Künye kaydedilirken bir hata oluştu')
  }
}

const topicId = computed(() => route.params.id)
const topic = computed(() => topicsStore.currentTopic)
const entries = computed(() => entriesStore.entries)

// Parse künye JSON data
const parsedKunyeData = computed(() => {
  if (!topic.value?.kunyeData) return null
  try {
    return JSON.parse(topic.value.kunyeData)
  } catch (e) {
    console.error('Failed to parse kunyeData:', e)
    return null
  }
})

function formatContent(content) {
  if (!content) return ''
  return content
    // @mentions
    .replace(/@(\w+)/g, '<a href="/biri/$1">@$1</a>')
    // (bkz: topic) - link to search page
    .replace(/\(bkz: ([^)]+)\)/g, '<a href="/arama?q=$1" class="bkz-link">(bkz: <span class="bkz-topic">$1</span>)</a>')
    // [text](url) - markdown style links
    .replace(/\[([^\]]+)\]\((https?:\/\/[^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener noreferrer" class="external-link">$1</a>')
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
    // Refresh sidebar to remove deleted topic
    topicsStore.fetchAllSidebarTopicsByDate(true)
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
    topicsStore.fetchAllSidebarTopicsByDate(true)
    // Refresh topic info for entry count
    await topicsStore.fetchTopicById(topic.value.id)
    
    // New entry is created today, so switch to "today" filter or "all" to see it
    activeDateFilter.value = 'today'
    router.replace({ path: route.path, query: { dateFilter: 'today' } })
    
    // Calculate last page for today's entries
    const todayEntryCount = topic.value.todayEntryCount || 1
    const lastPage = Math.max(0, Math.ceil(todayEntryCount / 10) - 1)
    
    // Fetch and go to last page of today's entries
    await entriesStore.fetchEntriesByTopic(topic.value.id, lastPage, 10, true, 'today')
    
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
  entriesStore.fetchEntriesByTopic(topic.value.id, page, 10, false, activeDateFilter.value)
  window.scrollTo(0, 0)
}

function setDateFilter(filter) {
  activeDateFilter.value = filter
  if (filter) {
    router.replace({ path: route.path, query: { dateFilter: filter } })
  } else {
    router.replace({ path: route.path, query: {} })
  }
  entriesStore.fetchEntriesByTopic(topic.value.id, 0, 10, true, filter)
}

async function fetchData(id, dateFilter = null) {
  loading.value = true
  await topicsStore.fetchTopicById(id)
  if (topic.value?.id) {
    activeDateFilter.value = dateFilter
    await entriesStore.fetchEntriesByTopic(topic.value.id, 0, 10, false, dateFilter)
  } else {
    // Topic not found or invalid ID, redirect to 404
    router.push('/404')
  }
  loading.value = false
}

watch(() => route.params.id, (newId) => {
  if (newId) {
    const dateFilter = route.query.dateFilter || null
    fetchData(newId, dateFilter)
  }
})

// Watch for dateFilter query changes (same topic, different date section)
watch(() => route.query.dateFilter, (newDateFilter, oldDateFilter) => {
  // Only react if we're on the same topic (id didn't change)
  if (route.params.id && newDateFilter !== oldDateFilter) {
    const dateFilter = newDateFilter || null
    activeDateFilter.value = dateFilter
    entriesStore.fetchEntriesByTopic(topic.value.id, 0, 10, true, dateFilter)
  }
})

onMounted(() => {
  if (route.params.id) {
    const dateFilter = route.query.dateFilter || null
    fetchData(route.params.id, dateFilter)
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

.date-filter-tabs {
  display: flex;
  gap: 0.5rem;
  margin-top: 1rem;
  flex-wrap: wrap;
}

.date-filter-tabs button {
  padding: 0.4rem 0.75rem;
  border: 1px solid rgba(212, 200, 74, 0.3);
  border-radius: 16px;
  background: rgba(26, 26, 46, 0.6);
  color: #999;
  font-size: 0.75rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.date-filter-tabs button:hover {
  border-color: #d4c84a;
  color: #d4c84a;
}

.date-filter-tabs button.active {
  background: rgba(212, 200, 74, 0.15);
  border-color: #d4c84a;
  color: #d4c84a;
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
  padding: 1rem;
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

/* Transfermarkt Card Styles (used for custom künye too) */
.transfermarkt-card {
  background: var(--entry-bg, rgba(26, 26, 46, 0.45));
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border: 1px solid var(--border, rgba(255, 237, 0, 0.1));
  border-radius: 12px;
  padding: 1rem;
  margin-bottom: 1rem;
  position: relative;
}

.player-card {
  display: flex;
  gap: 1rem;
  align-items: flex-start;
}

.player-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  background: rgba(0, 0, 0, 0.2);
}

.player-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.placeholder-image {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.3);
}

.placeholder-image .icon {
  width: 32px;
  height: 32px;
  color: #555;
}

.player-info {
  flex: 1;
  min-width: 0;
}

.player-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--accent, #d4c84a);
  margin: 0 0 0.5rem;
}

.player-details {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0.25rem 1rem;
}

@media (max-width: 480px) {
  .player-details {
    grid-template-columns: 1fr;
  }
  
  .player-image {
    width: 60px;
    height: 60px;
  }
}

.detail-row {
  display: flex;
  gap: 0.5rem;
  font-size: 0.8rem;
}

.detail-row .label {
  color: var(--text-secondary, #888);
}

.detail-row .value {
  color: var(--text-primary, #e0e0e0);
}

.tm-badge {
  position: absolute;
  bottom: 0.5rem;
  right: 0.5rem;
  font-size: 0.65rem;
  color: var(--text-muted, #666);
  opacity: 0.6;
}

/* Entry Toolbar */
.entry-toolbar {
  display: flex;
  gap: 0.5rem;
  padding: 0.5rem;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px 8px 0 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.toolbar-btn {
  padding: 0.25rem 0.75rem;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  color: #888;
  font-size: 0.75rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.toolbar-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #e0e0e0;
}

.toolbar-btn.active {
  background: var(--accent, #d4c84a);
  color: #000;
  border-color: var(--accent, #d4c84a);
}

.toolbar-popover {
  background: rgba(13, 13, 26, 0.98);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 0;
  padding: 0.75rem;
  margin-bottom: -1px;
}

.popover-content {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.popover-content input {
  flex: 1;
  min-width: 150px;
  padding: 0.5rem 0.75rem;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  color: #e0e0e0;
  font-size: 0.85rem;
}

.popover-content input::placeholder {
  color: #666;
}

.popover-btn {
  padding: 0.5rem 1rem;
  background: var(--accent, #d4c84a);
  border: none;
  border-radius: 4px;
  color: #000;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.popover-btn:hover {
  filter: brightness(1.1);
}

/* Link Styles in Entry Content - use :deep for v-html */
.entry-content :deep(.bkz-link) {
  color: #888;
  text-decoration: none;
}

.entry-content :deep(.bkz-link .bkz-topic) {
  color: #d4c84a;
  font-weight: 500;
}

.entry-content :deep(.bkz-link:hover) {
  text-decoration: none;
}

.entry-content :deep(.bkz-link:hover .bkz-topic) {
  text-decoration: underline;
}

.entry-content :deep(.external-link) {
  color: #58a6ff;
  text-decoration: none;
}

.entry-content :deep(.external-link:hover) {
  text-decoration: underline;
}

.entry-content :deep(.external-link::after) {
  content: '↗';
  font-size: 0.7em;
  margin-left: 2px;
  vertical-align: super;
}
</style>
