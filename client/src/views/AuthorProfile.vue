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
            <!-- Admin/Mod: Ban User Button -->
          <button 
            v-if="authStore.canBanUser(userRole) && !isBanned"
            class="action-btn danger" 
            @click="openBanModal"
          >
            <UserX class="icon" />
            kullanıcıyı yasakla
          </button>
          <!-- Admin/Mod: Unban User Button -->
          <button 
            v-if="authStore.canBanUser(userRole) && isBanned"
            class="action-btn success" 
            @click="openUnbanModal"
          >
            <UserCheck class="icon" />
            yasağı kaldır
          </button>
          <!-- Admin: Promote to Moderator -->
          <button 
            v-if="authStore.isAdmin && author?.role === 'USER'"
            class="action-btn primary" 
            @click="promoteToModerator"
          >
            <Shield class="icon" />
            moderatör yap
          </button>
          <!-- Admin: Demote from Moderator -->
          <button 
            v-if="authStore.isAdmin && author?.role === 'MODERATOR'"
            class="action-btn warning" 
            @click="demoteToUser"
          >
            <ShieldOff class="icon" />
            moderatörlükten çıkar
          </button>
        </div>

        <div v-if="isBanned && (authStore.isAdmin || authStore.isModerator)" class="ban-info-box">
           <AlertTriangle class="icon-sm" />
           <div class="ban-details">
             <span class="ban-status">Kullanıcı Yasaklı</span>
             <span class="ban-date">Bitiş: {{ formatBanDate(author.bannedUntil) }}</span>
             <span class="ban-reason">Sebep: {{ author.banReason }}</span>
           </div>
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
            <Pagination 
              :current-page="entriesStore.currentPage"
              :total-pages="entriesStore.totalPages"
              @change="changePage"
            />
          </div>

        <!-- Favorites Tab -->
        <div v-else-if="activeTab === 'favorites'" class="entries-list">
          <div v-if="userFavorites.length === 0" class="empty-state">
            <Star class="empty-icon" />
            <p>favori entry yok</p>
          </div>
          <article v-for="entry in userFavorites" :key="entry.id" class="entry-card">
            <router-link :to="`/baslik/${entry.topicId}`" class="entry-topic">
              {{ entry.topicTitle }}
            </router-link>
            <p class="entry-content" v-html="formatContent(entry.content)"></p>
            <footer class="entry-footer">
              <div class="actions">
                <button :class="{ 'liked': entry.currentUserVote === 'LIKE' }">
                  <ThumbsUp class="icon-sm" />
                  <span>{{ entry.likeCount || 0 }}</span>
                </button>
                <button :class="{ 'favorited': entry.currentUserVote === 'FAVORITE' }">
                  <Star class="icon-sm" />
                  <span>{{ entry.favoriteCount || 0 }}</span>
                </button>
              </div>
              <span class="date">{{ formatDateTime(entry.createdAt) }}</span>
            </footer>
          </article>
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

        <!-- Top Liked Tab -->
        <div v-else-if="activeTab === 'topLiked'" class="entries-list">
          <div v-if="topLikedEntries.length === 0" class="empty-state">
            <ThumbsUp class="empty-icon" />
            <p>henüz beğenilen entry yok</p>
          </div>
          <article v-for="entry in topLikedEntries" :key="entry.id" class="entry-card">
            <router-link :to="`/baslik/${entry.topicId}`" class="entry-topic">
              {{ entry.topicTitle }}
            </router-link>
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
              </div>
              <span class="date">{{ formatDateTime(entry.createdAt) }}</span>
            </footer>
          </article>
        </div>

        <!-- Top Favorited Tab -->
        <div v-else-if="activeTab === 'topFavorited'" class="entries-list">
          <div v-if="topFavoritedEntries.length === 0" class="empty-state">
            <Star class="empty-icon" />
            <p>henüz favlanan entry yok</p>
          </div>
          <article v-for="entry in topFavoritedEntries" :key="entry.id" class="entry-card">
            <router-link :to="`/baslik/${entry.topicId}`" class="entry-topic">
              {{ entry.topicTitle }}
            </router-link>
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
              </div>
              <span class="date">{{ formatDateTime(entry.createdAt) }}</span>
            </footer>
          </article>
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
            <button class="settings-btn danger" @click="openSuspendAccountModal">Hesabı Askıya Al</button>
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

    <!-- Suspend Account Modal -->
    <Teleport to="body">
      <div v-if="showSuspendAccountModal" class="modal-overlay" @click.self="closeSuspendAccountModal">
        <div class="modal">
          <div class="modal-header danger-header">
            <h2>Hesabı Askıya Al</h2>
            <button class="close-btn" @click="closeSuspendAccountModal">
              <X class="icon" />
            </button>
          </div>
          <div class="modal-body">
            <div class="warning-box">
              <AlertTriangle class="warning-icon" />
              <div>
                <strong>Bu işlem hesabınızı donduracaktır!</strong>
                <p>Hesabınız askıya alınacak ve giriş yapamayacaksınız. Tekrar aktif etmek için yönetici ile iletişime geçmeniz gerekebilir.</p>
              </div>
            </div>
            <form @submit.prevent="handleSuspendAccount">
              <div class="form-group">
                <label>Şifrenizi Girin</label>
                <input 
                  v-model="suspendAccountPassword" 
                  type="password" 
                  placeholder="Şifrenizi onaylayın"
                  required 
                />
              </div>
              <div v-if="suspendAccountError" class="error-box">{{ suspendAccountError }}</div>
              <div class="form-actions right">
                <button type="button" class="btn-cancel" @click="closeSuspendAccountModal">Vazgeç</button>
                <button 
                  type="submit" 
                  class="btn-delete" 
                  :disabled="suspendAccountLoading || !suspendAccountPassword"
                >
                  {{ suspendAccountLoading ? 'Askıya Alınıyor...' : 'Hesabımı Askıya Al' }}
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
             <SelectBox 
               v-model="banDuration" 
               :options="banOptions"
               placeholder="Süre seçiniz"
             />
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
    <!-- Unban User Modal -->
    <div v-if="showUnbanModal" class="modal-overlay" @click.self="closeUnbanModal">
      <div class="modal">
        <div class="modal-header">
          <h2>Yasağı Kaldır</h2>
          <button class="close-btn" @click="closeUnbanModal">
            <X class="icon" />
          </button>
        </div>
        <div class="modal-body">
          <p>Bu kullanıcının yasağını kaldırmak istediğinize emin misiniz?</p>
          <div class="ban-info-summary">
             <div class="info-row">
               <span class="label">Mevcut Yasak:</span>
               <span class="value">{{ formatBanDate(author.bannedUntil) }}</span>
             </div>
             <div class="info-row">
               <span class="label">Sebep:</span>
               <span class="value">{{ author.banReason }}</span>
             </div>
          </div>
          <div class="form-actions right">
             <button class="btn-cancel" @click="closeUnbanModal">İptal</button>
             <button class="btn-save" @click="executeUnban">Yasağı Kaldır</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import SelectBox from '@/components/ui/SelectBox.vue'
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  UserPlus, UserCheck, Settings, Loader2,
  FileText, Star, Hash, ThumbsUp, ThumbsDown, ChevronRight, X, UserX, Edit2, Trash2, AlertTriangle, Shield, ShieldOff
} from 'lucide-vue-next'
import Header from '@/components/layout/Header.vue'
import Pagination from '@/components/ui/Pagination.vue'
import { useAuthStore } from '@/stores/auth'
import { useEntriesStore } from '@/stores/entries'
import { useTopicsStore } from '@/stores/topics'
import { usersApi, entriesApi } from '@/services/api'
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
const userFavorites = ref([])
const topLikedEntries = ref([])
const topFavoritedEntries = ref([])

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
  isActive: true,
  bannedUntil: null,
  banReason: null
})

const isBanned = computed(() => {
  if (!author.value.bannedUntil) return false
  return new Date(author.value.bannedUntil) > new Date()
})

const banOptions = [
  { label: '1 Saat', value: 3600 },
  { label: '1 Gün', value: 86400 },
  { label: '1 Hafta', value: 604800 },
  { label: '1 Ay', value: 2592000 },
  { label: 'Süresiz (100 Yıl)', value: 3153600000 }
]

// ... (existing code)

function formatBanDate(date) {
  if (!date) return ''
  const d = new Date(date)
  if (d.getFullYear() > new Date().getFullYear() + 50) {
    return 'Süresiz'
  }
  return d.toLocaleDateString('tr-TR', { day: 'numeric', month: 'long', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}

const showUnbanModal = ref(false)

function openUnbanModal() {
  showUnbanModal.value = true
}

function closeUnbanModal() {
  showUnbanModal.value = false
}

async function executeUnban() {
  try {
    await usersApi.unban(author.value.id)
    toast.success('Kullanıcı yasağı kaldırıldı')
    closeUnbanModal()
    await loadUserData()
  } catch (err) {
    toast.error(err.response?.data?.message || 'İşlem başarısız')
  }
}



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

// Sync tabs with filtered data - always show topLiked and topFavorited
const tabs = computed(() => {
  const result = []
  
  // Always show these tabs
  result.push({ id: 'entries', label: 'entryler', count: author.value.entryCount || 0 })
  result.push({ id: 'topics', label: 'başlıklar', count: userTopics.value.length || 0 })
  result.push({ id: 'favorites', label: 'favoriler', count: userFavorites.value.length || 0 })
  result.push({ id: 'topLiked', label: 'en beğenilen', count: topLikedEntries.value.length || 0 })
  result.push({ id: 'topFavorited', label: 'en favlanan', count: topFavoritedEntries.value.length || 0 })
  
  return result
})

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
    .replace(/\(bkz: ([^)]+)\)/g, '<a href="/arama?q=$1" class="bkz-link">(bkz: <span class="bkz-topic">$1</span>)</a>')
    .replace(/\[([^\]]+)\]\((https?:\/\/[^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener noreferrer" class="external-link">$1</a>')
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

async function promoteToModerator() {
  if (!author.value?.id) return
  try {
    await usersApi.promote(author.value.id)
    toast.success('Kullanıcı moderatör yapıldı')
    // Refresh profile data
    await loadUserData()
  } catch (error) {
    toast.error(error.response?.data?.message || 'Bir hata oluştu')
  }
}

async function demoteToUser() {
  if (!author.value?.id) return
  try {
    await usersApi.demote(author.value.id)
    toast.success('Kullanıcı normal kullanıcı yapıldı')
    // Refresh profile data
    await loadUserData()
  } catch (error) {
    toast.error(error.response?.data?.message || 'Bir hata oluştu')
  }
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
      isActive: response.data.isActive,
      bannedUntil: response.data.bannedUntil,
      banReason: response.data.banReason
    }
    
    // Fetch user's entries using the author ID
    if (author.value.id) {
       await entriesStore.fetchEntriesByAuthor(author.value.id, 0, 10)
       
       // Fetch user's favorite entries
       try {
         const favResponse = await usersApi.getFavorites(username.value)
         userFavorites.value = favResponse.data || []
       } catch (e) {
         userFavorites.value = []
       }
       
       // Fetch top liked entries
       try {
         const likedResponse = await entriesApi.getTopLikedByAuthor(author.value.id, 5)
         topLikedEntries.value = likedResponse.data || []
       } catch (e) {
         topLikedEntries.value = []
       }
       
       // Fetch top favorited entries
       try {
         const favoritedResponse = await entriesApi.getTopFavoritedByAuthor(author.value.id, 5)
         topFavoritedEntries.value = favoritedResponse.data || []
       } catch (e) {
         topFavoritedEntries.value = []
       }
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
// ===== SUSPEND ACCOUNT =====
const router = useRouter()
const showSuspendAccountModal = ref(false)
const suspendAccountPassword = ref('')
const suspendAccountLoading = ref(false)
const suspendAccountError = ref('')

function openSuspendAccountModal() {
  showSuspendAccountModal.value = true
  suspendAccountPassword.value = ''
  suspendAccountError.value = ''
}

function closeSuspendAccountModal() {
  showSuspendAccountModal.value = false
}

async function handleSuspendAccount() {
  suspendAccountLoading.value = true
  suspendAccountError.value = ''
  
  try {
    await usersApi.suspendAccount(suspendAccountPassword.value)
    toast.success('Hesabınız askıya alındı. Hoşça kalın!')
    authStore.logout()
    router.push('/')
  } catch (err) {
    suspendAccountError.value = err.response?.data?.message || 'Hesap askıya alınamadı'
  } finally {
    suspendAccountLoading.value = false
  }
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

.action-btn.primary {
  background: #58a6ff;
  color: #fff;
}

.action-btn.primary:hover {
  background: #79b8ff;
}

.action-btn.warning {
  background: transparent;
  border: 1px solid #f0883e;
  color: #f0883e;
}

.action-btn.warning:hover {
  background: rgba(240, 136, 62, 0.15);
}

.action-btn .icon {
  width: 16px;
  height: 16px;
}

/* Mobile profile actions */
@media (max-width: 768px) {
  .profile-actions {
    flex-wrap: wrap;
    gap: 0.5rem;
  }
  
  .profile-actions .action-btn {
    flex: 1;
    min-width: 120px;
    justify-content: center;
    padding: 0.5rem 0.75rem;
    font-size: 0.8rem;
  }
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

/* Entry content link styles */
.entry-content :deep(a) { text-decoration: none; color: inherit; }

.entry-content :deep(.bkz-link) { color: #888; text-decoration: none; }
.entry-content :deep(.bkz-link .bkz-topic) { color: #d4c84a; font-weight: 500; }
.entry-content :deep(.bkz-link:hover .bkz-topic) { text-decoration: underline; }

.entry-content :deep(.external-link) { color: #58a6ff; }
.entry-content :deep(.external-link:hover) { text-decoration: underline; }
.entry-content :deep(.external-link::after) {
  content: '↗';
  font-size: 0.7em;
  margin-left: 2px;
  vertical-align: super;
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
    overflow-x: hidden;
  }

  .profile-header {
    padding: 1rem;
    overflow: hidden;
  }

  .profile-info {
    flex-direction: column;
    text-align: center;
  }

  .avatar {
    width: 60px;
    height: 60px;
    font-size: 1.5rem;
  }

  .profile-stats {
    justify-content: center;
    flex-wrap: wrap;
  }

  .profile-actions {
    justify-content: center;
    flex-wrap: wrap;
  }

  /* Horizontal scrolling tabs */
  .profile-tabs {
    overflow-x: auto;
    overflow-y: hidden;
    -webkit-overflow-scrolling: touch;
    scrollbar-width: none;
    -ms-overflow-style: none;
    flex-wrap: nowrap;
  }

  .profile-tabs::-webkit-scrollbar {
    display: none;
  }

  .tab {
    flex: 0 0 auto;
    padding: 0.5rem 0.75rem;
    font-size: 0.75rem;
    white-space: nowrap;
  }

  .tab .count {
    font-size: 0.65rem;
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

/* Ban Info Box */
.ban-info-box {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 1rem;
  background: rgba(248, 81, 73, 0.1);
  border: 1px solid rgba(248, 81, 73, 0.3);
  border-radius: 8px;
  margin-top: 1.5rem;
  margin-bottom: 1.5rem;
}

.ban-info-box .icon-sm {
  color: #f85149;
  width: 20px;
  height: 20px;
  margin-top: 2px;
}

.ban-details {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.ban-status {
  font-weight: 600;
  color: #f85149;
  font-size: 0.95rem;
  display: block;
}

.ban-date, .ban-reason {
  font-size: 0.9rem;
  color: #ccc;
  display: block;
}

/* Ban Modal Summary */
.ban-info-summary {
  background: rgba(0, 0, 0, 0.2);
  padding: 1rem;
  border-radius: 6px;
  margin: 1rem 0;
  border: 1px solid #2a2a4a;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-row .label {
  color: #888;
}

.info-row .value {
  color: #e0e0e0;
  font-weight: 500;
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
