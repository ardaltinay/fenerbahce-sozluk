<template>
  <div class="profile-page">
    <Header />

    <div class="profile-container">
      <!-- Profile Header -->
      <div class="profile-header">
        <div class="profile-info">
          <div class="avatar">{{ username.charAt(0).toUpperCase() }}</div>
          <div class="info">
            <h1>{{ username }}</h1>
            <span class="badge" :class="userRole?.toLowerCase()">
              {{ getRoleBadge(userRole) }}
            </span>
          </div>
        </div>

        <div class="profile-stats">
          <div class="stat">
            <span class="value">{{ author.entryCount || 0 }}</span>
            <span class="label">entry</span>
          </div>
          <div class="stat">
            <span class="value">{{ author.likeCount || 0 }}</span>
            <span class="label">beğeni</span>
          </div>
          <div class="stat">
            <span class="value">{{ author.favoriteCount || 0 }}</span>
            <span class="label">favori</span>
          </div>
          <div class="stat">
            <span class="value">{{ author.createdAt ? formatDate(author.createdAt) : '-' }}</span>
            <span class="label">üyelik</span>
          </div>
        </div>

        <!-- Actions -->
        <div class="profile-actions" v-if="!isOwnProfile && authStore.isAuthenticated">
          <button 
            class="action-btn secondary disabled"
            disabled
            title="yakında"
          >
            <UserPlus class="icon" />
            takip et
          </button>
          <!-- Admin: Remove User Button -->
          <button 
            v-if="authStore.canDeleteUser(username)"
            class="action-btn danger" 
            @click="confirmBanUser"
          >
            <UserX class="icon" />
            kullanıcıyı yasakla
          </button>
        </div>

        <!-- Edit Profile (own profile) -->
        <div class="profile-actions" v-if="isOwnProfile">
          <button class="action-btn secondary" @click="showSettings = true">
            <Settings class="icon" />
            ayarlar
          </button>
        </div>
      </div>

      <!-- Tabs -->
      <div class="profile-tabs">
        <button 
          v-for="tab in tabs" 
          :key="tab.id"
          class="tab"
          :class="{ 'active': activeTab === tab.id }"
          @click="changeTab(tab.id)"
        >
          {{ tab.label }}
          <span v-if="tab.count" class="count">{{ tab.count }}</span>
        </button>
      </div>

      <!-- Content -->
      <div class="profile-content">
        <!-- Loading -->
        <div v-if="loading" class="loading-state">
          <Loader2 class="spin" />
          <span>yükleniyor...</span>
        </div>

        <!-- Entries Tab -->
        <div v-else-if="activeTab === 'entries'" class="entries-list">
          <div v-if="userEntries.length === 0" class="empty-state">
            <FileText class="empty-icon" />
            <p>henüz entry yazılmamış</p>
          </div>
          <article v-for="entry in userEntries" :key="entry.id" class="entry-card">
            <router-link :to="getTopicLink(entry)" class="entry-topic">
              {{ entry.topicTitle }}
            </router-link>
            
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
              <p class="entry-content" v-html="formatContent(entry.content)"></p>
              <footer class="entry-footer">
                <div class="actions">
                  <button :class="{ 'liked': entry.currentUserVote === 'LIKE' }">
                    <ThumbsUp class="icon-sm" />
                    <span>{{ entry.likeCount || 0 }}</span>
                  </button>
                  <button :class="{ 'disliked': entry.currentUserVote === 'DISLIKE' }">
                    <ThumbsDown class="icon-sm" />
                    <span>{{ entry.dislikeCount || 0 }}</span>
                  </button>
                  <button :class="{ 'favorited': entry.currentUserVote === 'FAVORITE' }">
                    <Star class="icon-sm" />
                    <span>{{ entry.favoriteCount || 0 }}</span>
                  </button>
                  <!-- Edit/Delete for authorized users -->
                  <button 
                    v-if="authStore.canEditEntry(entry)"
                    class="edit-btn"
                    @click="startEditEntry(entry)"
                    title="düzenle"
                  >
                    <Edit2 class="icon-sm" />
                  </button>
                  <button 
                    v-if="authStore.canDeleteEntry(entry)"
                    class="delete-btn"
                    @click="openDeleteModal(entry)"
                    title="sil"
                  >
                    <Trash2 class="icon-sm" />
                  </button>
                </div>
                <span class="date">{{ formatDateTime(entry.createdAt) }}</span>
              </footer>
            </template>
          </article>
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

        <!-- Favorites Tab -->
        <div v-else-if="activeTab === 'favorites'" class="entries-list">
          <div class="empty-state">
            <Star class="empty-icon" />
            <p>favori entry yok</p>
          </div>
        </div>

        <!-- Topics Tab -->
        <div v-else-if="activeTab === 'topics'" class="topics-list">
          <div v-if="userTopics.length === 0" class="empty-state">
            <Hash class="empty-icon" />
            <p>henüz başlık açılmamış</p>
            <span class="empty-hint">ilk başlığı açmak için entry yaz</span>
          </div>
          <router-link 
            v-for="topic in userTopics" 
            :key="topic.id"
            :to="`/baslik/${topic.id}`"
            class="topic-card"
          >
            <div class="topic-info">
              <Hash class="topic-icon" />
              <div class="topic-details">
                <span class="topic-title">{{ topic.title }}</span>
                <span class="topic-meta">{{ topic.entryCount || 0 }} entry</span>
              </div>
            </div>
            <ChevronRight class="arrow-icon" />
          </router-link>
        </div>
      </div>
    </div>

    <!-- Settings Modal -->
    <div v-if="showSettings" class="modal-overlay" @click.self="showSettings = false">
      <div class="modal">
        <div class="modal-header">
          <h2>Ayarlar</h2>
          <button class="close-btn" @click="showSettings = false">
            <X class="icon" />
          </button>
        </div>
        <div class="modal-body">
          <div class="settings-section">
            <h3>Profil Bilgileri</h3>
            <div class="form-group">
              <label>Kullanıcı Adı</label>
              <input type="text" :value="authStore.username" disabled />
              <span class="hint">Kullanıcı adı değiştirilemez</span>
            </div>
            <div class="form-group">
              <label>E-posta</label>
              <input type="email" :value="authStore.user?.email" disabled />
            </div>
          </div>
          
          <div class="settings-section">
            <h3>Güvenlik</h3>
            <button class="settings-btn" @click="openChangePasswordModal">Şifre Değiştir</button>
          </div>
          
          <div class="settings-section danger">
            <h3>Tehlikeli Bölge</h3>
            <button class="settings-btn danger" @click="openDeleteAccountModal">Hesabı Sil</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Change Password Modal -->
    <Teleport to="body">
      <div v-if="showChangePasswordModal" class="modal-overlay" @click.self="closeChangePasswordModal">
        <div class="modal">
          <div class="modal-header">
            <h2>Şifre Değiştir</h2>
            <button class="close-btn" @click="closeChangePasswordModal">
              <X class="icon" />
            </button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="handleChangePassword">
              <div class="form-group">
                <label>Mevcut Şifre</label>
                <input 
                  v-model="passwordForm.currentPassword" 
                  type="password" 
                  placeholder="••••••••"
                  required 
                />
              </div>
              <div class="form-group">
                <label>Yeni Şifre</label>
                <input 
                  v-model="passwordForm.newPassword" 
                  type="password" 
                  placeholder="••••••••"
                  required 
                />
                <!-- Password strength -->
                <div class="password-strength" v-if="passwordForm.newPassword">
                  <div class="strength-bar">
                    <div 
                      class="strength-fill" 
                      :class="`level-${newPasswordStrength}`"
                      :style="{ width: (newPasswordStrength * 25) + '%' }"
                    ></div>
                  </div>
                  <span class="strength-text">{{ strengthLabels[newPasswordStrength] }}</span>
                </div>
              </div>
              <div class="form-group">
                <label>Yeni Şifre (Tekrar)</label>
                <input 
                  v-model="passwordForm.confirmPassword" 
                  type="password" 
                  placeholder="••••••••"
                  required 
                />
                <span v-if="passwordForm.confirmPassword && passwordForm.newPassword !== passwordForm.confirmPassword" class="error-hint">
                  Şifreler eşleşmiyor
                </span>
              </div>
              <div v-if="passwordError" class="error-box">{{ passwordError }}</div>
              <div class="form-actions right">
                <button type="button" class="btn-cancel" @click="closeChangePasswordModal">İptal</button>
                <button 
                  type="submit" 
                  class="btn-save" 
                  :disabled="passwordLoading || !isPasswordFormValid"
                >
                  {{ passwordLoading ? 'Güncelleniyor...' : 'Şifreyi Güncelle' }}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Delete Account Modal -->
    <Teleport to="body">
      <div v-if="showDeleteAccountModal" class="modal-overlay" @click.self="closeDeleteAccountModal">
        <div class="modal">
          <div class="modal-header danger-header">
            <h2>Hesabı Sil</h2>
            <button class="close-btn" @click="closeDeleteAccountModal">
              <X class="icon" />
            </button>
          </div>
          <div class="modal-body">
            <div class="warning-box">
              <AlertTriangle class="warning-icon" />
              <div>
                <strong>Bu işlem geri alınamaz!</strong>
                <p>Hesabınızı sildiğinizde tüm verileriniz kalıcı olarak kaldırılacaktır.</p>
              </div>
            </div>
            <form @submit.prevent="handleDeleteAccount">
              <div class="form-group">
                <label>Şifrenizi Girin</label>
                <input 
                  v-model="deleteAccountPassword" 
                  type="password" 
                  placeholder="Şifrenizi onaylayın"
                  required 
                />
              </div>
              <div v-if="deleteAccountError" class="error-box">{{ deleteAccountError }}</div>
              <div class="form-actions right">
                <button type="button" class="btn-cancel" @click="closeDeleteAccountModal">Vazgeç</button>
                <button 
                  type="submit" 
                  class="btn-delete" 
                  :disabled="deleteAccountLoading || !deleteAccountPassword"
                >
                  {{ deleteAccountLoading ? 'Siliniyor...' : 'Hesabımı Sil' }}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Delete Confirmation Modal -->
    <div v-if="deleteModalEntry" class="modal-overlay delete-modal-overlay" @click.self="closeDeleteModal">
      <div class="delete-modal">
        <div class="delete-modal-header">
          <h3>Entry'yi Sil</h3>
        </div>
        <div class="delete-modal-body">
          <p>Bu entry'yi silmek istediğinizden emin misiniz?</p>
          <div class="entry-preview">
            {{ truncateText(deleteModalEntry.content, 100) }}
          </div>
          <div class="form-group" style="margin-top: 1rem;">
             <label>Silme Sebebi (Opsiyonel)</label>
             <textarea v-model="deleteReasonInput" class="form-textarea" placeholder="Neden siliyorsunuz?" rows="2" style="min-height: 60px"></textarea>
          </div>
        </div>
        <div class="delete-modal-footer">
          <button class="btn-cancel" @click="closeDeleteModal">İptal</button>
          <button class="btn-delete" @click="confirmDeleteEntry">Sil</button>
        </div>
      </div>
    </div>

    <!-- Ban User Modal -->
    <div v-if="showBanModal" class="modal-overlay" @click.self="closeBanModal">
      <div class="modal">
        <div class="modal-header">
          <h2>Kullanıcıyı Yasakla (Çaylak)</h2>
          <button class="close-btn" @click="closeBanModal">
            <X class="icon" />
          </button>
        </div>
        <div class="modal-body">
          <div class="form-group">
             <label>Süre</label>
             <select v-model="banDuration" class="form-select">
               <option :value="3600">1 Saat</option>
               <option :value="86400">1 Gün</option>
               <option :value="604800">1 Hafta</option>
               <option :value="2592000">1 Ay</option>
               <option :value="3153600000">Süresiz (100 Yıl)</option>
             </select>
          </div>
          <div class="form-group">
             <label>Sebep</label>
             <textarea v-model="banReason" class="form-textarea" placeholder="Yasaklama sebebini belirtin..."></textarea>
          </div>
          <div class="form-actions right">
             <button class="btn-cancel" @click="closeBanModal">İptal</button>
             <button class="btn-delete" @click="executeBan" :disabled="!banReason">Yasakla</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  UserPlus, UserCheck, MessageCircle, Settings, Loader2,
  FileText, Star, Hash, ThumbsUp, ThumbsDown, ChevronRight, X, UserX, Edit2, Trash2, AlertTriangle
} from 'lucide-vue-next'
import Header from '@/components/layout/Header.vue'
import { useAuthStore } from '@/stores/auth'
import { useEntriesStore } from '@/stores/entries'
import { useTopicsStore } from '@/stores/topics'
import { usersApi } from '@/services/api'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const authStore = useAuthStore()
const entriesStore = useEntriesStore()
const topicsStore = useTopicsStore()
const toast = useToast()

const loading = ref(true)
const isFollowing = ref(false)
const activeTab = ref('entries')
const showSettings = ref(false)

const username = computed(() => route.params.username)
const isOwnProfile = computed(() => authStore.username === username.value)

// Get user role - from authStore if own profile, otherwise from API
const userRole = computed(() => {
  if (isOwnProfile.value && authStore.user?.role) {
    return authStore.user.role
  }
  return author.value.role || 'USER'
})

const author = ref({
  entryCount: 0,
  likeCount: 0,
  role: 'USER',
  createdAt: null,
})

// Filter entries by username
const userEntries = computed(() => {
  return entriesStore.entries.filter(e => 
    e.authorUsername === username.value || e.author?.username === username.value
  )
})

// User topics from API - filtered by username
const userTopics = computed(() => {
  return topicsStore.topics.filter(t => 
    t.authorUsername === username.value || t.author?.username === username.value
  )
})

// Sync tabs with filtered data
const tabs = computed(() => [
  { id: 'entries', label: 'entryler', count: userEntries.value.length },
  { id: 'favorites', label: 'favoriler' },
  { id: 'topics', label: 'başlıklar', count: userTopics.value.length },
])

function getRoleBadge(role) {
  const badges = {
    'ADMIN': 'yönetici',
    'MODERATOR': 'moderatör',
    'USER': 'yazar',
  }
  return badges[role] || 'yazar'
}

function changeTab(tabId) {
  activeTab.value = tabId
}

function toggleFollow() {
  isFollowing.value = !isFollowing.value
}

function formatContent(content) {
  return content
    .replace(/@(\w+)/g, '<a href="/biri/$1">@$1</a>')
    .replace(/\(bkz: ([^)]+)\)/g, '<a href="/baslik/$1">(bkz: $1)</a>')
}

function formatDate(date) {
  if (!date) return 'yeni'
  const d = new Date(date)
  const months = ['ocak', 'şubat', 'mart', 'nisan', 'mayıs', 'haziran',
                  'temmuz', 'ağustos', 'eylül', 'ekim', 'kasım', 'aralık']
  return `${months[d.getMonth()]} ${d.getFullYear()}`
}

function formatDateTime(date) {
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

// Inline Edit State
const editingEntryId = ref(null)
const editContent = ref('')

function startEditEntry(entry) {
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

// Delete Modal State
const deleteModalEntry = ref(null)

const deleteReasonInput = ref('')

function openDeleteModal(entry) {
  deleteModalEntry.value = entry
  deleteReasonInput.value = ''
}

function closeDeleteModal() {
  deleteModalEntry.value = null
  deleteReasonInput.value = ''
}

async function confirmDeleteEntry() {
  if (!deleteModalEntry.value) return
  
  const result = await entriesStore.deleteEntry(deleteModalEntry.value.id, deleteReasonInput.value)
  if (result.success) {
    closeDeleteModal()
  } else {
    toast.error(result.message || 'Entry silinemedi')
  }
}

function truncateText(text, maxLength) {
  if (!text) return ''
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}

const showBanModal = ref(false)
const banDuration = ref(3600)
const banReason = ref('')

function openBanModal() {
  showBanModal.value = true
}

function closeBanModal() {
  showBanModal.value = false
  banReason.value = ''
  banDuration.value = 3600
}

async function executeBan() {
  if (!author.value.id) return

  try {
    // API expects id, duration(seconds), reason
    await usersApi.ban(author.value.id, banDuration.value, banReason.value)
    toast.success('Kullanıcı yasaklandı')
    closeBanModal()
    // Reload or redirect
    loadUserData()
  } catch (err) {
    toast.error(err.response?.data?.message || 'İşlem başarısız')
  }
}

function confirmBanUser() {
  openBanModal()
}

function getTopicLink(entry) {
  return `/baslik/${entry.topicId}`
}

async function loadUserData() {
  loading.value = true
  
  try {
    // Fetch user profile from API
    const response = await usersApi.getByUsername(username.value)
    author.value = {
      id: response.data.id,
      entryCount: response.data.entryCount || 0,
      likeCount: response.data.likeCount || 0,
      favoriteCount: response.data.favoriteCount || 0,
      role: response.data.role || 'USER',
      createdAt: response.data.createdAt,
    }
    
    // Fetch user's entries using the author ID
    if (author.value.id) {
       await entriesStore.fetchEntriesByAuthor(author.value.id, 0, 10)
    }
    
  } catch (err) {
    console.error('User profile fetch error:', err)
    // Use defaults on error
    author.value = {
      entryCount: 0,
      likeCount: 0,
      favoriteCount: 0,
      role: 'USER',
      createdAt: null,
    }
  }
  
  // Create safe link helper availability in template (not needed if I use it in template directly, but I need to expose it or use computed)
  // Actually, I can just use a method or inline logic. I'll use a method in template.
  
  // Topics: Current API doesn't support fetching topics by author easily, so we rely on global trends for now or need a new endpoint.
  // We'll leave topics as 'trends' but this is technically incorrect for "User's Topics". 
  // However, without backend changes, we can't fix "My Topics" list if it requires a new endpoint.
  // We will simply fetch trends to populate sidebar/cache if needed, but not user specific topics.
  // userTopics computed property filters topicsStore.topics. If we don't fetch anything specific, it might be empty.
  // For now let's just fetch popular/trends to have SOMETHING in the store, although it won't be user specific unless they are trending.
  await topicsStore.fetchTrendingTopics()
  
  loading.value = false
}

function changePage(page) {
  if (page < 0 || page >= entriesStore.totalPages) return
  if (author.value.id) {
    entriesStore.fetchEntriesByAuthor(author.value.id, page)
  }
  window.scrollTo(0, 0)
}

watch(() => route.params.username, () => {
  loadUserData()
})

// ===== CHANGE PASSWORD =====
const showChangePasswordModal = ref(false)
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
})
const passwordLoading = ref(false)
const passwordError = ref('')

const strengthLabels = {
  0: '',
  1: 'çok zayıf',
  2: 'zayıf',
  3: 'orta',
  4: 'güçlü',
}

const newPasswordStrength = computed(() => {
  const p = passwordForm.newPassword
  if (!p) return 0
  let s = 0
  if (p.length >= 8) s++
  if (/[a-z]/.test(p) && /[A-Z]/.test(p)) s++
  if (/\d/.test(p)) s++
  if (/[^a-zA-Z0-9]/.test(p)) s++
  return s
})

const isPasswordFormValid = computed(() => {
  return passwordForm.currentPassword && 
         passwordForm.newPassword && 
         passwordForm.newPassword === passwordForm.confirmPassword && 
         newPasswordStrength.value >= 2
})

function openChangePasswordModal() {
  showChangePasswordModal.value = true
  passwordForm.currentPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordError.value = ''
}

function closeChangePasswordModal() {
  showChangePasswordModal.value = false
}

async function handleChangePassword() {
  passwordLoading.value = true
  passwordError.value = ''
  
  try {
    await usersApi.changePassword(passwordForm.currentPassword, passwordForm.newPassword)
    toast.success('Şifreniz başarıyla güncellendi!')
    closeChangePasswordModal()
    showSettings.value = false
  } catch (err) {
    passwordError.value = err.response?.data?.message || 'Şifre güncellenemedi'
  } finally {
    passwordLoading.value = false
  }
}

// ===== DELETE ACCOUNT =====
const router = useRouter()
const showDeleteAccountModal = ref(false)
const deleteAccountPassword = ref('')
const deleteAccountLoading = ref(false)
const deleteAccountError = ref('')

function openDeleteAccountModal() {
  showDeleteAccountModal.value = true
  deleteAccountPassword.value = ''
  deleteAccountError.value = ''
}

function closeDeleteAccountModal() {
  showDeleteAccountModal.value = false
}

async function handleDeleteAccount() {
  deleteAccountLoading.value = true
  deleteAccountError.value = ''
  
  try {
    await usersApi.deleteAccount(deleteAccountPassword.value)
    toast.success('Hesabınız silindi. Hoşça kalın!')
    authStore.logout()
    router.push('/')
  } catch (err) {
    deleteAccountError.value = err.response?.data?.message || 'Hesap silinemedi'
  } finally {
    deleteAccountLoading.value = false
  }
}

onMounted(() => {
  loadUserData()
})
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: transparent;
  color: #e0e0e0;
}

.profile-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 70px 1rem 2rem;
}

/* Profile Header */
.profile-header {
  background: rgba(26, 26, 46, 0.45);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 237, 0, 0.05);
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 1rem;
}

.profile-info {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.avatar {
  width: 72px;
  height: 72px;
  background: linear-gradient(135deg, #d4c84a, #b5a93d);
  color: #1a1a2e;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  font-weight: 700;
  text-transform: uppercase;
}

.info h1 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #d4c84a;
  margin: 0 0 0.25rem;
}

.badge {
  display: inline-block;
  padding: 0.2rem 0.6rem;
  background: rgba(212, 200, 74, 0.15);
  color: #d4c84a;
  font-size: 0.7rem;
  border-radius: 4px;
  text-transform: lowercase;
}

.badge.admin {
  background: rgba(239, 68, 68, 0.15);
  color: #ef4444;
}

.badge.moderator {
  background: rgba(59, 130, 246, 0.15);
  color: #3b82f6;
}

/* Stats */
.profile-stats {
  display: flex;
  gap: 2rem;
  padding: 1rem 0;
  border-top: 1px solid #2a2a4a;
  border-bottom: 1px solid #2a2a4a;
  margin-bottom: 1rem;
}

.stat {
  display: flex;
  flex-direction: column;
}

.stat .value {
  font-size: 1.25rem;
  font-weight: 600;
  color: #e0e0e0;
}

.stat .label {
  font-size: 0.75rem;
  color: #666;
}

/* Actions */
.profile-actions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.profile-actions.admin-actions {
  margin-top: 0.5rem;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.6rem 1rem;
  background: #d4c84a;
  color: #1a1a2e;
  border: none;
  border-radius: 6px;
  font-size: 0.85rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
}

.action-btn:hover {
  background: #e0d454;
}

.action-btn.danger {
  background: transparent;
  border: 1px solid #f85149;
  color: #f85149;
}

.action-btn.danger:hover {
  background: rgba(248, 81, 73, 0.15);
}

.action-btn.secondary {
  background: transparent;
  border: 1px solid #2a2a4a;
  color: #888;
}

.action-btn.secondary:hover:not(:disabled) {
  border-color: #d4c84a;
  color: #d4c84a;
}

.action-btn.disabled,
.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.action-btn.following {
  background: #3fb950;
}

.action-btn .icon {
  width: 16px;
  height: 16px;
}

/* Tabs */
.profile-tabs {
  display: flex;
  gap: 0.5rem;
  background: rgba(26, 26, 46, 0.45);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 237, 0, 0.05);
  border-radius: 8px;
  padding: 0.5rem;
  margin-bottom: 1rem;
}

.tab {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.6rem 1rem;
  background: transparent;
  border: none;
  border-radius: 6px;
  color: #888;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.15s;
}

.tab:hover {
  background: rgba(255, 255, 255, 0.05);
  color: #ccc;
}

.tab.active {
  background: rgba(212, 200, 74, 0.15);
  color: #d4c84a;
}

.tab .count {
  font-size: 0.7rem;
  padding: 0.15rem 0.4rem;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
}

/* Content */
.profile-content {
  background: rgba(26, 26, 46, 0.45);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 237, 0, 0.05);
  border-radius: 12px;
  min-height: 300px;
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
  color: #666;
}

.empty-icon {
  width: 48px;
  height: 48px;
  margin-bottom: 1rem;
  opacity: 0.5;
}

/* Entries */
.entries-list {
  padding: 0.5rem;
}

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

.entry-topic {
  font-size: 0.85rem;
  color: #d4c84a;
  text-decoration: none;
  display: block;
  margin-bottom: 0.5rem;
}

.entry-topic:hover {
  text-decoration: underline;
}

.entry-content {
  font-size: 0.9rem;
  line-height: 1.6;
  color: #ccc;
  margin: 0 0 0.75rem;
}

.entry-content :deep(a) {
  color: #58a6ff;
  text-decoration: none;
}

.entry-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.entry-footer .actions {
  display: flex;
  gap: 0.5rem;
}

.entry-footer .actions button {
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

.entry-footer .actions button:hover {
  background: rgba(212, 200, 74, 0.1);
  border-color: rgba(212, 200, 74, 0.2);
  color: #d4c84a;
}

.entry-footer .actions button.liked {
  color: #3fb950;
}

.entry-footer .actions button.favorited {
  color: #d4c84a;
}

.entry-footer .actions button.edit-btn:hover {
  color: #58a6ff;
}

.entry-footer .actions button.delete-btn:hover {
  color: #f85149;
}

.icon-sm {
  width: 14px;
  height: 14px;
}

.entry-footer .date {
  font-size: 0.75rem;
  color: #555;
}

/* Topics */
.topics-list {
  padding: 0.5rem;
}

.topic-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem;
  text-decoration: none;
  border-bottom: 1px solid #2a2a4a;
  transition: all 0.15s;
}

.topic-card:last-child {
  border-bottom: none;
}

.topic-card:hover {
  background: rgba(255, 237, 0, 0.03);
}

.topic-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.topic-icon {
  width: 20px;
  height: 20px;
  color: #d4c84a;
  opacity: 0.7;
}

.topic-details {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.topic-title {
  font-size: 0.95rem;
  color: #d4c84a;
}

.topic-meta {
  font-size: 0.75rem;
  color: #666;
}

.arrow-icon {
  width: 16px;
  height: 16px;
  color: #444;
}

.topic-card:hover .arrow-icon {
  color: #d4c84a;
}

.empty-hint {
  font-size: 0.8rem;
  color: #6fbf6f;
  margin-top: 0.25rem;
}

/* Inline Edit Mode */
.edit-mode {
  padding: 0.5rem 0;
}

.edit-textarea {
  width: 100%;
  padding: 0.75rem;
  background: #0d0d1a;
  border: 1px solid #d4c84a;
  border-radius: 6px;
  color: #e0e0e0;
  font-size: 0.9rem;
  line-height: 1.6;
  resize: vertical;
  min-height: 80px;
}

.edit-textarea:focus {
  outline: none;
  border-color: #e0d454;
}

.edit-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 0.75rem;
}

.btn-cancel {
  padding: 0.5rem 1rem;
  background: transparent;
  border: 1px solid #2a2a4a;
  border-radius: 6px;
  color: #888;
  font-size: 0.85rem;
  cursor: pointer;
}

.btn-cancel:hover {
  border-color: #888;
  color: #aaa;
}

.btn-save {
  padding: 0.5rem 1rem;
  background: #d4c84a;
  border: none;
  border-radius: 6px;
  color: #0f0f1a;
  font-size: 0.85rem;
  font-weight: 500;
  cursor: pointer;
}

.btn-save:hover:not(:disabled) {
  background: #e0d454;
}

.btn-save:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Delete Modal */
.delete-modal-overlay {
  z-index: 1100;
}

.delete-modal {
  background: #1a1a2e;
  border: 1px solid #2a2a4a;
  border-radius: 12px;
  width: 100%;
  max-width: 400px;
  overflow: hidden;
}

.delete-modal-header {
  padding: 1rem 1.25rem;
  border-bottom: 1px solid #2a2a4a;
}

.delete-modal-header h3 {
  margin: 0;
  font-size: 1rem;
  font-weight: 500;
  color: #f85149;
}

.delete-modal-body {
  padding: 1.25rem;
}

.delete-modal-body p {
  margin: 0 0 1rem;
  color: #aaa;
  font-size: 0.9rem;
}

.entry-preview {
  padding: 0.75rem;
  background: #0d0d1a;
  border-radius: 6px;
  font-size: 0.85rem;
  color: #888;
  font-style: italic;
}

.delete-modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  padding: 1rem 1.25rem;
  border-top: 1px solid #2a2a4a;
}

.btn-delete {
  padding: 0.5rem 1rem;
  background: #f85149;
  border: none;
  border-radius: 6px;
  color: white;
  font-size: 0.85rem;
  font-weight: 500;
  cursor: pointer;
}

.btn-delete:hover {
  background: #ff6b63;
}

/* Mobile */
@media (max-width: 640px) {
  .profile-container {
    padding: 60px 0.75rem 1rem;
  }

  .profile-header {
    padding: 1rem;
  }

  .profile-info {
    flex-direction: column;
    text-align: center;
  }

  .profile-stats {
    justify-content: center;
  }

  .profile-actions {
    justify-content: center;
  }

  .tab {
    padding: 0.5rem;
    font-size: 0.8rem;
  }
}

/* Modal */
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
  max-width: 480px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid #2a2a4a;
}

.modal-header h2 {
  font-size: 1.1rem;
  font-weight: 500;
  color: #e0e0e0;
  margin: 0;
}

.close-btn {
  padding: 0.5rem;
  background: none;
  border: none;
  color: #888;
  cursor: pointer;
  border-radius: 4px;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.05);
  color: #e0e0e0;
}

.modal-body {
  padding: 1.5rem;
}

.settings-section {
  margin-bottom: 1.5rem;
}

.settings-section:last-child {
  margin-bottom: 0;
}

.settings-section h3 {
  font-size: 0.85rem;
  font-weight: 500;
  color: #888;
  margin: 0 0 1rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.settings-section.danger h3 {
  color: #f85149;
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

.form-group input {
  width: 100%;
  padding: 0.75rem;
  background: #0d0d1a;
  border: 1px solid #2a2a4a;
  border-radius: 6px;
  color: #e0e0e0;
  font-size: 0.9rem;
}

.form-group input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.form-group .hint {
  display: block;
  font-size: 0.75rem;
  color: #666;
  margin-top: 0.35rem;
}

.settings-btn {
  padding: 0.75rem 1.25rem;
  background: transparent;
  border: 1px solid #2a2a4a;
  border-radius: 6px;
  color: #aaa;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.15s;
}

.settings-btn:hover {
  border-color: #d4c84a;
  color: #d4c84a;
}

.settings-btn.danger {
  border-color: #f85149;
  color: #f85149;
}

.settings-btn.danger:hover {
  background: rgba(248, 81, 73, 0.1);
}

.form-select, .form-textarea {
  width: 100%;
  padding: 0.75rem;
  background: #0d0d1a;
  border: 1px solid #2a2a4a;
  border-radius: 6px;
  color: #e0e0e0;
  font-size: 0.9rem;
}

.form-textarea {
  min-height: 100px;
  resize: vertical;
}

.form-actions.right {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 1rem;
}

/* Password Strength */
.password-strength { margin-top: 0.5rem; }
.strength-bar {
  height: 4px;
  background: #2a2a4a;
  border-radius: 2px;
  overflow: hidden;
  margin-bottom: 0.25rem;
}
.strength-fill { height: 100%; transition: all 0.2s; }
.strength-fill.level-1 { background: #ef4444; }
.strength-fill.level-2 { background: #f59e0b; }
.strength-fill.level-3 { background: #d4c84a; }
.strength-fill.level-4 { background: #10b981; }
.strength-text { font-size: 0.7rem; color: #666; }

.error-hint {
  display: block;
  font-size: 0.7rem;
  color: #ef4444;
  margin-top: 0.375rem;
}

.error-box {
  padding: 0.75rem;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: 8px;
  color: #ef4444;
  font-size: 0.8rem;
  margin-bottom: 1rem;
}

/* Warning Box for Delete Account */
.warning-box {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: 8px;
  margin-bottom: 1.5rem;
}

.warning-box .warning-icon {
  width: 24px;
  height: 24px;
  color: #ef4444;
  flex-shrink: 0;
}

.warning-box strong {
  color: #ef4444;
  display: block;
  margin-bottom: 0.25rem;
}

.warning-box p {
  color: #b0b0b0;
  font-size: 0.85rem;
  margin: 0;
}

.danger-header {
  border-bottom-color: rgba(239, 68, 68, 0.3);
}

.danger-header h2 {
  color: #ef4444;
}

.btn-save {
  padding: 0.6rem 1.25rem;
  background: #d4c84a;
  color: #0f0f1a;
  border: none;
  border-radius: 6px;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
}

.btn-save:hover:not(:disabled) {
  background: #e0d454;
}

.btn-save:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
