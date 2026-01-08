<template>
  <header class="header">
    <div class="header-container">
      <!-- Logo -->
      <router-link to="/" class="logo" @click="emitTabChange('home')">
        <img src="/logo.png" alt="Fenerbahçe Sözlük" class="logo-icon" />
        <span class="logo-text">fenerbahçe sözlük</span>
      </router-link>

      <!-- Navigation Tabs -->
      <nav class="nav-tabs">
        <button 
          class="nav-tab" 
          :class="{ 'active': activeTab === 'gundem' }"
          @click="setTab('gundem')"
        >
          gündem
        </button>
        <button 
          class="nav-tab"
          :class="{ 'active': activeTab === 'popular' }"
          @click="setTab('popular')"
        >
          popüler
        </button>
        <button 
          class="nav-tab"
          :class="{ 'active': activeTab === 'random' }"
          @click="setTab('random')"
        >
          rastgele
        </button>
        <button 
          class="nav-tab"
          :class="{ 'active': activeTab === 'son' }"
          @click="setTab('son')"
        >
          son
        </button>
        <button 
          class="nav-tab"
          :class="{ 'active': activeTab === 'news' }"
          @click="setTab('news')"
        >
          haberler
        </button>
        
        <!-- Stats Link -->
        <router-link to="/istatistikler" class="nav-tab">
          istatistikler
        </router-link>
      </nav>

      <!-- Search with Dropdown -->
      <div class="search-container desktop-only" ref="searchContainerRef">
        <div class="search-box">
          <input
            type="text"
            v-model="searchQuery"
            placeholder="ara"
            @focus="showSearchDropdown = true"
            @input="handleSearchInput"
            @keyup.enter="navigateToSearch"
          />
          <button @click="navigateToSearch">
            <Search class="icon" />
          </button>
        </div>

        <!-- Search Dropdown -->
        <div v-if="showSearchDropdown && searchQuery.length >= 2" class="search-dropdown">
          <div v-if="searchResults.length === 0" class="dropdown-empty">
            sonuç bulunamadı
          </div>
          <template v-else>
            <!-- Topics -->
            <div v-if="topicResults.length > 0" class="dropdown-section">
              <div class="section-title">başlıklar</div>
              <router-link 
                v-for="topic in topicResults.slice(0, 5)" 
                :key="topic.id"
                :to="`/baslik/${topic.id}`"
                class="dropdown-item"
                @click="closeSearch"
              >
                <FileText class="item-icon" />
                {{ topic.title }}
              </router-link>
            </div>

            <!-- Authors -->
            <div v-if="authorResults.length > 0" class="dropdown-section">
              <div class="section-title">yazarlar</div>
              <router-link 
                v-for="author in authorResults.slice(0, 3)" 
                :key="author"
                :to="`/biri/${author}`"
                class="dropdown-item"
                @click="closeSearch"
              >
                <User class="item-icon" />
                {{ author }}
              </router-link>
            </div>

            <!-- View All -->
            <router-link 
              :to="{ path: '/arama', query: { q: searchQuery } }"
              class="dropdown-footer"
              @click="closeSearch"
            >
              tüm sonuçları gör
            </router-link>
          </template>
        </div>
      </div>

      <!-- Create Topic Button -->
      <button v-if="authStore.isAuthenticated" class="create-topic-btn desktop-only" @click="showTopicModal = true">
        <Edit3 class="icon" />
        <span>başlık aç</span>
      </button>

      <!-- Messages Icon -->
      <router-link v-if="authStore.isAuthenticated" to="/mesajlar" class="message-icon-btn desktop-only">
        <MessageCircle class="icon" />
        <span v-if="messagesStore.unreadCount > 0" class="message-badge">{{ messagesStore.unreadCount > 99 ? '99+' : messagesStore.unreadCount }}</span>
      </router-link>

      <!-- Auth -->
      <div class="auth-area desktop-only">


        <template v-if="!authStore.isAuthenticated">
          <button class="auth-btn secondary" @click="showLoginModal = true">giriş</button>
          <button class="auth-btn primary" @click="showRegisterModal = true">kayıt</button>
        </template>
        <template v-else>
          <button class="user-btn" @click="showMenu = !showMenu">
            <Avatar :username="authStore.username" size="sm" />
          </button>
          <div v-if="showMenu" class="dropdown">
            <router-link :to="`/biri/${authStore.username}`" @click="showMenu = false">profilim</router-link>
            <router-link to="/iletisim" @click="showMenu = false">iletişim</router-link>
            <button @click="logout">çıkış</button>
          </div>
        </template>
      </div>

      <!-- Mobile Actions (Order: new topic - search - theme - avatar) -->
      <div class="mobile-auth">
        <button v-if="authStore.isAuthenticated" class="mobile-auth-icon" @click="showTopicModal = true" title="yeni başlık">
          <Edit3 class="icon" />
        </button>

        <button class="mobile-auth-icon mobile-search-btn" @click.stop="showMobileSearch = !showMobileSearch" title="ara">
          <Search class="icon" />
        </button>

        <router-link v-if="authStore.isAuthenticated" to="/mesajlar" class="mobile-auth-icon message-link">
          <MessageCircle class="icon" />
          <span v-if="messagesStore.unreadCount > 0" class="message-badge mobile">{{ messagesStore.unreadCount > 9 ? '9+' : messagesStore.unreadCount }}</span>
        </router-link>

        <template v-if="!authStore.isAuthenticated">
          <button class="mobile-auth-icon" @click="showLoginModal = true" title="giriş">
            <LogIn class="icon" />
          </button>
          <button class="mobile-auth-icon highlight" @click="showRegisterModal = true" title="kayıt">
            <UserPlus class="icon" />
          </button>
        </template>
        <template v-else>

          <button class="user-btn" @click="showMenu = !showMenu">
            <Avatar :username="authStore.username" size="sm" />
          </button>
          <div v-if="showMenu" class="dropdown mobile-dropdown">
            <router-link :to="`/biri/${authStore.username}`" @click="showMenu = false">profilim</router-link>
            <router-link to="/iletisim" @click="showMenu = false">iletişim</router-link>
            <button @click="logout">çıkış</button>
          </div>
        </template>
      </div>
    </div>

    <!-- Mobile Search Bar -->
    <div v-if="showMobileSearch" class="mobile-search">
      <input
        type="text"
        v-model="searchQuery"
        placeholder="ara..."
        @keyup.enter="navigateToSearch"
      />
    </div>

    <!-- Login Modal -->
    <Teleport to="body">
      <LoginModal
        v-if="showLoginModal"
        @close="showLoginModal = false"
        @switch-to-register="openRegisterModal"
      />
    </Teleport>

    <!-- Register Modal -->
    <Teleport to="body">
      <RegisterModal
        v-if="showRegisterModal"
        @close="showRegisterModal = false"
        @switch-to-login="openLoginModal"
      />
    </Teleport>

    <!-- New Topic Modal -->
    <Teleport to="body">
      <NewTopicModal 
        v-if="showTopicModal" 
        @close="showTopicModal = false" 
        @created="handleTopicCreated"
      />
    </Teleport>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search, FileText, User, LogIn, UserPlus, Edit3, MessageCircle } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import { useMessagesStore } from '@/stores/messages'
import { useWebSocket } from '@/composables/useWebSocket'
import { useToast } from '@/composables/useToast'

import { useTopicsStore } from '@/stores/topics'
import { useEntriesStore } from '@/stores/entries'
import { useUsersStore } from '@/stores/users'
import NewTopicModal from '@/components/NewTopicModal.vue'
import LoginModal from '@/components/auth/LoginModal.vue'
import RegisterModal from '@/components/auth/RegisterModal.vue'
import Avatar from '@/components/ui/Avatar.vue'

const emit = defineEmits(['tab-change'])

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const topicsStore = useTopicsStore()
const entriesStore = useEntriesStore()
const usersStore = useUsersStore()
const messagesStore = useMessagesStore()
const { connect, subscribeToUserMessages } = useWebSocket()



const searchQuery = ref('')
const showMenu = ref(false)
const showMobileSearch = ref(false)
const showSearchDropdown = ref(false)
const activeTab = ref('')
const searchContainerRef = ref(null)
const showTopicModal = ref(false)
const showLoginModal = ref(false)
const showRegisterModal = ref(false)

// Route değişince tab'ı temizle
watch(() => route.path, (newPath) => {
  if (newPath !== '/') {
    activeTab.value = ''
  }
})

// Search results
const topicResults = computed(() => {
  if (searchQuery.value.length < 2) return []
  return topicsStore.topics.filter(t => 
    t.title.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

// Author search from Store
const apiAuthorResults = ref([])

const authorResults = computed(() => {
  return apiAuthorResults.value.map(u => u.username)
})

// Fetch authors from Store when searchQuery changes
watch(searchQuery, async (newQuery) => {
  if (newQuery.length < 2) {
    apiAuthorResults.value = []
    return
  }
  const results = await usersStore.search(newQuery)
  apiAuthorResults.value = results || []
}, { immediate: true })

const searchResults = computed(() => [...topicResults.value, ...authorResults.value])

function setTab(tab) {
  activeTab.value = tab
  router.push({ path: '/', query: { tab: tab } })
  emit('tab-change', tab)
}

function emitTabChange(tab) {
  activeTab.value = ''
  emit('tab-change', tab)
  router.push('/')
}

function handleSearchInput() {
  showSearchDropdown.value = true
}

function navigateToSearch() {
  if (searchQuery.value.trim()) {
    router.push({ path: '/arama', query: { q: searchQuery.value } })
    closeSearch()
  }
}

function closeSearch() {
  showSearchDropdown.value = false
  searchQuery.value = ''
  showMobileSearch.value = false
}

function logout() {
  authStore.logout()
  showMenu.value = false
  router.push('/')
}

// Click outside to close
function handleClickOutside(e) {
  if (searchContainerRef.value && !searchContainerRef.value.contains(e.target)) {
    showSearchDropdown.value = false
  }
  
  const isUserBtnClick = e.target.closest('.user-btn')
  const userDropdown = e.target.closest('.dropdown')
  if (showMenu.value && !isUserBtnClick && !userDropdown) {
    showMenu.value = false
  }

  const isSearchBtnClick = e.target.closest('.mobile-search-btn')
  const mobileSearchArea = e.target.closest('.mobile-search')
  if (showMobileSearch.value && !isSearchBtnClick && !mobileSearchArea) {
    showMobileSearch.value = false
  }
}

onMounted(async () => {
  document.addEventListener('click', handleClickOutside)
  topicsStore.fetchTopics()
  
  // Session expired kontrolü
  if (localStorage.getItem('sessionExpired') === 'true') {
    localStorage.removeItem('sessionExpired')
    const toast = useToast()
    toast.warning('Oturumunuz sona erdi, lütfen tekrar giriş yapın')
    showLoginModal.value = true
  }
  
  // Mesaj bildirimleri için WebSocket
  if (authStore.isAuthenticated && authStore.user?.username) {
    messagesStore.fetchUnreadCount()
    connect()
    subscribeToUserMessages(
      authStore.user.username,
      (msg) => messagesStore.handleIncomingMessage(msg),
      (count) => messagesStore.updateUnreadCount(count)
    )
  }
})

// Kullanıcı giriş yaptığında okunmamış mesaj sayısını fetch et
watch(() => authStore.isAuthenticated, (isAuth) => {
  if (isAuth && authStore.user?.username) {
    messagesStore.fetchUnreadCount()
    connect()
    subscribeToUserMessages(
      authStore.user.username,
      (msg) => messagesStore.handleIncomingMessage(msg),
      (count) => messagesStore.updateUnreadCount(count)
    )
  }
})

function handleTopicCreated() {
  topicsStore.fetchTopics() 
}


function openLoginModal() {
  showRegisterModal.value = false
  showLoginModal.value = true
}

function openRegisterModal() {
  showLoginModal.value = false
  showRegisterModal.value = true
}

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

function clearActiveTab() {
  activeTab.value = ''
}

defineExpose({
  clearActiveTab
})
</script>

<style scoped>
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: rgba(26, 26, 46, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(255, 237, 0, 0.4);
  box-shadow: 0 1px 15px rgba(0, 0, 0, 0.2);
}

.header-container {
  display: flex;
  align-items: center;
  gap: 1rem;
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 1rem;
  height: 50px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  text-decoration: none;
  flex-shrink: 0;
}

.logo-icon {
  width: 34px;
  height: 34px;
  object-fit: contain;
}

.logo-text {
  font-size: 0.9rem;
  font-weight: 600;
  color: #d4c84a;
}

.nav-tabs {
  display: flex;
  align-items: center;
  height: 100%;
  overflow-x: auto;
  scrollbar-width: none;
}

@media (min-width: 769px) {
  .nav-tabs {
    overflow: visible; 
  }
}

.nav-tabs::-webkit-scrollbar { display: none; }

.nav-tab {
  height: 100%;
  padding: 0 0.75rem;
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  font-size: 0.8rem;
  color: #888;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.15s;
  text-decoration: none;
  display: flex;
  align-items: center;
}

.nav-tab:hover { color: #d4c84a; }
.nav-tab.active { color: #d4c84a; border-bottom-color: #d4c84a; }
.nav-tab.router-link-active { color: #d4c84a; border-bottom-color: #d4c84a; }

.nav-channels {
  position: relative;
  height: 100%;
}

.channels-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  background: #1a1a2e;
  border: 1px solid rgba(255, 237, 0, 0.15);
  border-radius: 0 0 6px 6px;
  padding: 0.5rem 0;
  min-width: 150px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.5);
  display: flex;
  flex-direction: column;
}

.channel-item {
  text-align: left;
  padding: 0.5rem 1rem;
  background: none;
  border: none;
  color: #ccc;
  font-size: 0.85rem;
  cursor: pointer;
  white-space: nowrap;
}

.channel-item:hover {
  background: rgba(255, 237, 0, 0.1);
  color: #d4c84a;
}

.create-topic-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  padding: 0.3rem 0.6rem;
  background: #d4c84a;
  color: #1a1a2e;
  border: none;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 600;
  cursor: pointer;
  margin-left: 0.5rem;
  flex-shrink: 0;
  width: auto;
}

.create-topic-btn .icon {
  width: 12px;
  height: 12px;
  flex-shrink: 0;
}

.create-topic-btn:hover {
  opacity: 0.9;
}

/* Search Container */
.search-container {
  position: relative;
  margin-left: auto;
}

.search-box {
  display: flex;
  background: rgba(13, 13, 26, 0.4);
  border: 1px solid rgba(255, 237, 0, 0.05);
  border-radius: 4px;
  overflow: hidden;
}

.search-box input {
  width: 180px;
  padding: 0.4rem 0.75rem;
  background: transparent;
  border: none;
  color: #ccc;
  font-size: 0.8rem;
}

.search-box input:focus { outline: none; }
.search-box input::placeholder { color: #555; }

.search-box button {
  padding: 0.4rem 0.6rem;
  background: none;
  border: none;
  color: #555;
  cursor: pointer;
}

.search-box button:hover { color: #d4c84a; }
.icon { width: 14px; height: 14px; }

/* Search Dropdown */
.search-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 0.5rem;
  background: #1a1a2e;
  border: 1px solid rgba(255, 237, 0, 0.2);
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.4);
  min-width: 280px;
}

.dropdown-empty {
  padding: 1rem;
  text-align: center;
  font-size: 0.8rem;
  color: #666;
}

.dropdown-section {
  border-bottom: 1px solid rgba(255, 237, 0, 0.05);
}

.dropdown-section:last-of-type {
  border-bottom: none;
}

.section-title {
  padding: 0.5rem 0.75rem;
  font-size: 0.7rem;
  text-transform: uppercase;
  color: #555;
  background: #0d0d1a;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.6rem 0.75rem;
  color: #ccc;
  text-decoration: none;
  font-size: 0.85rem;
  transition: background 0.1s;
}

.dropdown-item:hover {
  background: rgba(255, 237, 0, 0.1);
  color: #d4c84a;
}

.item-icon {
  width: 14px;
  height: 14px;
  color: #555;
}

.dropdown-footer {
  display: block;
  padding: 0.75rem;
  text-align: center;
  font-size: 0.8rem;
  color: #d4c84a;
  text-decoration: none;
  background: #0d0d1a;
  border-top: 1px solid rgba(255, 237, 0, 0.1);
}

.dropdown-footer:hover {
  background: rgba(255, 237, 0, 0.1);
}

/* Auth */
.auth-area {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  position: relative;
}

.auth-btn {
  padding: 0.35rem 0.75rem;
  font-size: 0.75rem;
  font-weight: 600;
  text-decoration: none;
  border-radius: 4px;
  border: none;
  cursor: pointer;
}

.auth-btn.primary {
  background: #d4c84a;
  color: #1a1a2e;
}

.auth-btn.secondary {
  background: transparent;
  color: #d4c84a;
  border: 1px solid #d4c84a;
  margin-right: 0.5rem;
}

.auth-btn:hover {
  opacity: 0.9;
}

.user-btn {
  padding: 0.25rem;
  background: none;
  border: none;
  cursor: pointer;
}

/* Message Icon */
.message-icon-btn {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0.5rem;
  color: #888;
  transition: color 0.2s;
}

.message-icon-btn:hover {
  color: #d4c84a;
}

.message-link {
  position: relative;
}

.message-badge {
  position: absolute;
  top: -2px;
  right: -4px;
  background: #e53935;
  color: white;
  font-size: 0.65rem;
  font-weight: 600;
  padding: 0.1rem 0.35rem;
  border-radius: 10px;
  min-width: 16px;
  text-align: center;
  line-height: 1.2;
}

.message-badge.mobile {
  top: -4px;
  right: -6px;
  font-size: 0.6rem;
  padding: 0.05rem 0.25rem;
  min-width: 14px;
}

.desktop-user-btn {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  padding: 0.25rem 0.5rem;
  border-radius: 30px;
  transition: background 0.2s;
}

.desktop-user-btn:hover {
  background: rgba(255, 255, 255, 0.03);
}

.user-name {
  color: #ccc;
  font-size: 0.85rem;
  font-weight: 500;
}

.desktop-user-btn:hover .user-name {
  color: #d4c84a;
}



.dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 0.5rem;
  background: #1a1a2e;
  border: 1px solid rgba(255, 237, 0, 0.15);
  border-radius: 6px;
  padding: 0.5rem 0;
  min-width: 120px;
}

.dropdown a,
.dropdown button {
  display: block;
  width: 100%;
  padding: 0.5rem 1rem;
  background: none;
  border: none;
  color: #ccc;
  font-size: 0.8rem;
  text-decoration: none;
  text-align: left;
  cursor: pointer;
}

.dropdown a:hover,
.dropdown button:hover {
  background: rgba(255, 237, 0, 0.1);
  color: #d4c84a;
}



/* Nav Icon */
.nav-icon {
  width: 14px;
  height: 14px;
  margin-right: 0.25rem;
}

/* Mobile */
.mobile-only { display: none; }
.mobile-auth { display: none; }

.mobile-search-btn {
  padding: 0.5rem;
  background: none;
  border: none;
  color: #888;
  cursor: pointer;
}

.mobile-search-btn .icon { width: 20px; height: 20px; }

.mobile-search {
  padding: 0.5rem 1rem;
  background: rgba(13, 13, 26, 0.4);
  border-top: 1px solid rgba(255, 237, 0, 0.05);
}

.mobile-search input {
  width: 100%;
  padding: 0.6rem 0.75rem;
  background: rgba(26, 26, 46, 0.45); backdrop-filter: blur(12px); -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 237, 0, 0.05);
  border-radius: 4px;
  color: #ccc;
  font-size: 0.875rem;
}

.mobile-search input:focus { outline: none; border-color: #d4c84a; }

/* Mobile Auth - base styles */
.mobile-auth {
  display: none;
  align-items: center;
  gap: 0.25rem;
  position: relative;
}

.mobile-auth-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0.4rem;
  color: #888;
  text-decoration: none;
  border-radius: 4px;
  transition: all 0.15s;
}

.mobile-auth-icon:hover {
  color: #d4c84a;
  background: rgba(255, 237, 0, 0.1);
}

.mobile-auth-icon.highlight {
  color: #d4c84a;
}

.mobile-auth-icon .icon {
  width: 18px;
  height: 18px;
}

.mobile-dropdown {
  right: 0;
  min-width: 100px;
}

@media (max-width: 768px) {
  .desktop-only { display: none; }
  .mobile-only { display: flex; }
  .mobile-auth { display: flex; }
  .logo-text { display: none; }
  .nav-tabs { 
    flex: 1; 
    justify-content: flex-start; 
    padding-left: 0.5rem;
    -webkit-overflow-scrolling: touch;
  }
  .nav-tab { padding: 0 0.6rem; font-size: 0.75rem; }
}


</style>
