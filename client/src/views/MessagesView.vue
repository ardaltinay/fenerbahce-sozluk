<template>
  <div class="app">
    <Header />
    <div class="messages-container">
      <div class="messages-layout">
        <!-- Conversations List -->
        <aside class="conversations-panel">
          <div class="panel-header">
            <h2>mesajlar</h2>
            <span class="retention-info" title="Mesajlar 1 ay süreyle saklanır">ⓘ 1 ay</span>
          </div>
          
          <div v-if="loading" class="loading-state">
            <Loader2 class="spin" />
          </div>
          
          <div v-else-if="conversations.length === 0" class="empty-conversations">
            <MessageSquare class="empty-icon" />
            <p>henüz mesajınız yok</p>
            <span>bir yazarın profilinden mesaj başlatabilirsiniz</span>
          </div>
          
          <div v-else class="conversation-list">
            <div
              v-for="conv in conversations"
              :key="conv.username"
              class="conversation-item-wrapper"
            >
              <button 
                class="delete-conv-btn" 
                @click.stop="openDeleteModal(conv.username)"
                title="Konuşmayı sil"
              >
                <X class="delete-icon" />
              </button>
              <button
                class="conversation-item"
                :class="{ active: selectedUsername === conv.username }"
                @click="selectConversation(conv.username)"
              >
                <div class="conv-avatar">
                  <User class="avatar-icon" />
                </div>
                <div class="conv-info">
                  <div class="conv-header">
                    <span class="conv-username">{{ conv.username }}</span>
                    <span class="conv-time">{{ formatTime(conv.lastMessageAt) }}</span>
                  </div>
                  <p class="conv-preview">{{ conv.lastMessage }}</p>
                </div>
                <span v-if="conv.unreadCount > 0" class="unread-badge">
                  {{ conv.unreadCount }}
                </span>
              </button>
            </div>
          </div>
        </aside>

        <!-- Chat Window -->
        <main class="chat-panel">
          <template v-if="selectedUsername">
            <div class="chat-header">
              <button class="back-btn mobile-only" @click="selectedUsername = null">
                <ArrowLeft class="icon" />
              </button>
              <router-link :to="`/biri/${selectedUsername}`" class="chat-user">
                <User class="avatar-icon" />
                <span>{{ selectedUsername }}</span>
              </router-link>
            </div>

            <div class="messages-list" ref="messagesList">
              <div v-if="messagesLoading" class="loading-state">
                <Loader2 class="spin" />
              </div>
              
              <div
                v-for="msg in messages"
                :key="msg.id"
                class="message-bubble"
                :class="{ own: msg.isOwn }"
              >
                <p class="message-content">{{ msg.content }}</p>
                <div class="message-meta">
                  <span class="message-time">{{ formatTime(msg.createdAt) }}</span>
                  <Check v-if="msg.isOwn && msg.isRead" class="read-icon" />
                </div>
              </div>
            </div>

            <form class="message-input" @submit.prevent="handleSend">
              <div class="input-wrapper">
                <textarea
                  v-model="newMessage"
                  placeholder="mesajınızı yazın..."
                  maxlength="2000"
                  :disabled="sending"
                  rows="3"
                  @keydown.enter.exact.prevent="handleSend"
                ></textarea>
                <button type="button" class="emoji-btn" @click="toggleEmojiPicker" title="Emoji ekle">
                  <Smile class="icon" />
                </button>
              </div>
              <button type="submit" class="send-btn" :disabled="!newMessage.trim() || sending">
                <Send class="icon" />
              </button>
            </form>
            
            <!-- Emoji Picker Overlay -->
            <div v-if="showEmojiPicker" class="emoji-overlay" @click="showEmojiPicker = false"></div>
            <div v-if="showEmojiPicker" class="emoji-picker">
              <div class="emoji-grid">
                <button 
                  v-for="emoji in emojis" 
                  :key="emoji" 
                  class="emoji-item"
                  @click="insertEmoji(emoji)"
                >{{ emoji }}</button>
              </div>
            </div>
          </template>

          <div v-else class="no-chat-selected">
            <MessageCircle class="empty-icon" />
            <p>bir konuşma seçin</p>
          </div>
        </main>
      </div>
    </div>
    
    <!-- Delete Confirmation Modal -->
    <Teleport to="body">
      <div v-if="showDeleteModal" class="delete-modal-overlay" @click.self="showDeleteModal = false">
        <div class="delete-modal">
          <h3>Konuşmayı Sil</h3>
          <p><strong>{{ deleteTargetUsername }}</strong> ile olan konuşmayı silmek istediğinize emin misiniz?</p>
          <p class="delete-note">Bu işlem geri alınamaz.</p>
          <div class="delete-modal-actions">
            <button class="cancel-btn" @click="showDeleteModal = false">Vazgeç</button>
            <button class="confirm-btn" @click="executeDeleteConversation">Sil</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { User, MessageSquare, MessageCircle, ArrowLeft, Send, Loader2, Check, X, Smile } from 'lucide-vue-next'
import Header from '@/components/layout/Header.vue'
import { useMessagesStore } from '@/stores/messages'
import { useAuthStore } from '@/stores/auth'
import { useWebSocket } from '@/composables/useWebSocket'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const router = useRouter()
const messagesStore = useMessagesStore()
const authStore = useAuthStore()
const { connect, subscribeToUserMessages, unsubscribeFromUserMessages } = useWebSocket()
const toast = useToast()

const selectedUsername = ref(null)
const newMessage = ref('')
const sending = ref(false)
const messagesLoading = ref(false)
const messagesList = ref(null)
const showEmojiPicker = ref(false)
const showDeleteModal = ref(false)
const deleteTargetUsername = ref('')

// Emoji listesi
const emojis = [
  '😀', '😂', '😍', '🥰', '😎', '🤔', '😢', '😡', '👍', '👎',
  '❤️', '💛', '💙', '🔥', '⚽', '🏆', '🎉', '💪', '🙏', '👏',
  '😊', '😉', '🤣', '😅', '😇', '🥳', '😤', '🤝', '✌️', '🤞'
]

const conversations = computed(() => messagesStore.conversations)
const messages = computed(() => messagesStore.currentMessages)
const loading = computed(() => messagesStore.loading)

// URL'den username parametresini al
watch(() => route.params.username, (username) => {
  if (username) {
    selectConversation(username)
  }
}, { immediate: true })

async function selectConversation(username) {
  if (selectedUsername.value === username) return // Aynı konuşmayı tekrar seçme
  
  selectedUsername.value = username
  messagesLoading.value = true
  
  await messagesStore.fetchMessages(username)
  
  // Okundu işaretlemeyi arka planda yap (await yok)
  messagesStore.markConversationAsRead(username)
  
  messagesLoading.value = false
  
  // URL'i güncelle
  if (route.params.username !== username) {
    router.replace(`/mesajlar/${username}`)
  }
  
  // Scroll to bottom
  await nextTick()
  scrollToBottom()
}

async function handleSend() {
  if (!newMessage.value.trim() || sending.value) return
  
  sending.value = true
  const result = await messagesStore.sendMessage(selectedUsername.value, newMessage.value)
  
  if (result.success) {
    newMessage.value = ''
    await nextTick()
    scrollToBottom()
  } else {
    toast.error(result.message)
  }
  
  sending.value = false
}

function scrollToBottom() {
  if (messagesList.value) {
    messagesList.value.scrollTop = messagesList.value.scrollHeight
  }
}

function toggleEmojiPicker() {
  showEmojiPicker.value = !showEmojiPicker.value
}

function insertEmoji(emoji) {
  newMessage.value += emoji
  showEmojiPicker.value = false
}

function openDeleteModal(username) {
  deleteTargetUsername.value = username
  showDeleteModal.value = true
}

async function executeDeleteConversation() {
  const username = deleteTargetUsername.value
  showDeleteModal.value = false
  
  const result = await messagesStore.deleteConversation(username)
  if (result.success) {
    toast.success('Konuşma silindi')
    if (selectedUsername.value === username) {
      selectedUsername.value = null
      router.replace('/mesajlar')
    }
  } else {
    toast.error(result.message || 'Konuşma silinemedi')
  }
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return 'şimdi'
  if (diff < 3600000) return Math.floor(diff / 60000) + ' dk'
  if (diff < 86400000) return Math.floor(diff / 3600000) + ' sa'
  
  return date.toLocaleDateString('tr-TR', { day: 'numeric', month: 'short' })
}

// WebSocket ile yeni mesaj dinle
function handleNewMessage(message) {
  messagesStore.handleIncomingMessage(message)
  
  // Eğer şu an açık olan konuşmadaysak, otomatik okundu işaretle ve scroll
  if (selectedUsername.value === message.senderUsername) {
    messagesStore.markConversationAsRead(message.senderUsername)
    nextTick(() => scrollToBottom())
  }
}

function handleUnreadUpdate(count) {
  messagesStore.updateUnreadCount(count)
}

onMounted(async () => {
  if (!authStore.isAuthenticated) {
    router.push('/giris')
    return
  }
  
  await messagesStore.fetchConversations()
  
  // Entry'den gelen ön-doldurulmuş mesajı kontrol et
  const prefill = localStorage.getItem('prefillMessage')
  if (prefill) {
    newMessage.value = prefill
    localStorage.removeItem('prefillMessage')
  }
  
  // WebSocket bağlantısı
  connect()
  subscribeToUserMessages(
    authStore.user.username,
    handleNewMessage,
    handleUnreadUpdate
  )
})

onUnmounted(() => {
  if (authStore.user?.username) {
    unsubscribeFromUserMessages(authStore.user.username)
  }
  messagesStore.clearCurrentConversation()
})
</script>

<style scoped>
.app {
  min-height: 100vh;
  background: transparent;
  color: #e0e0e0;
}

.messages-container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 60px 1rem 1rem;
  height: calc(100vh - 60px);
}

.messages-layout {
  display: flex;
  height: 100%;
  background: rgba(26, 26, 46, 0.6);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 237, 0, 0.05);
  border-radius: 12px;
  overflow: hidden;
}

/* Conversations Panel */
.conversations-panel {
  width: 320px;
  border-right: 1px solid rgba(255, 255, 255, 0.05);
  display: flex;
  flex-direction: column;
}

.panel-header {
  padding: 1.25rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.panel-header h2 {
  font-size: 1.1rem;
  font-weight: 600;
  color: #d4c84a;
  margin: 0;
}

.retention-info {
  font-size: 0.7rem;
  color: #666;
  cursor: help;
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
}

.conversation-item-wrapper {
  display: flex;
  align-items: stretch;
  border-bottom: 1px solid rgba(255, 255, 255, 0.03);
  position: relative;
}

.conversation-item-wrapper:hover .delete-conv-btn {
  opacity: 1;
}

.delete-conv-btn {
  position: absolute;
  left: 2px;
  top: 4px;
  background: none;
  border: none;
  border-radius: 3px;
  padding: 2px;
  cursor: pointer;
  opacity: 0;
  transition: all 0.2s;
  z-index: 2;
}

.delete-conv-btn:hover {
  background: rgba(255, 80, 80, 0.2);
}

.delete-icon {
  width: 10px;
  height: 10px;
  color: #ff6b6b;
}

.conversation-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex: 1;
  padding: 0.75rem 1rem 0.75rem 1.25rem;
  background: none;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
  text-align: left;
}

.conversation-item:hover {
  background: rgba(255, 255, 255, 0.03);
}

.conversation-item.active {
  background: rgba(212, 200, 74, 0.1);
  border-left: 3px solid #d4c84a;
}

.conv-avatar {
  width: 40px;
  height: 40px;
  background: rgba(212, 200, 74, 0.1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.avatar-icon {
  width: 20px;
  height: 20px;
  color: #d4c84a;
}

.conv-info {
  flex: 1;
  min-width: 0;
}

.conv-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.25rem;
}

.conv-username {
  font-weight: 500;
  color: #e0e0e0;
  font-size: 0.9rem;
}

.conv-time {
  font-size: 0.75rem;
  color: #666;
}

.conv-preview {
  font-size: 0.8rem;
  color: #888;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin: 0;
}

.unread-badge {
  background: #d4c84a;
  color: #0d0d1a;
  font-size: 0.7rem;
  font-weight: 600;
  padding: 0.15rem 0.5rem;
  border-radius: 10px;
  min-width: 20px;
  text-align: center;
}

/* Chat Panel */
.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.chat-user {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  color: #e0e0e0;
  text-decoration: none;
  font-weight: 500;
}

.chat-user:hover {
  color: #d4c84a;
}

.messages-list {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.message-bubble {
  max-width: 70%;
  padding: 0.75rem 1rem;
  border-radius: 16px;
  background: rgba(80, 80, 95, 0.5);
  color: #ccc;
  align-self: flex-start;
  position: relative;
  margin-left: 12px;
}

/* Sol baloncuk kıvrımı (karşıdan gelen) */
.message-bubble::before {
  content: '';
  position: absolute;
  left: -8px;
  top: 12px;
  width: 0;
  height: 0;
  border-top: 6px solid transparent;
  border-bottom: 6px solid transparent;
  border-right: 8px solid rgba(80, 80, 95, 0.5);
}

.message-bubble.own {
  background: rgba(212, 200, 74, 0.25);
  color: #e8e8e8;
  align-self: flex-end;
  margin-left: 0;
  margin-right: 12px;
}

/* Sağ baloncuk kıvrımı (kendi mesajı) */
.message-bubble.own::before {
  left: auto;
  right: -8px;
  border-right: none;
  border-left: 8px solid rgba(212, 200, 74, 0.25);
}

.message-content {
  margin: 0;
  font-size: 0.9rem;
  line-height: 1.5;
  word-wrap: break-word;
}

.message-meta {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  justify-content: flex-end;
  margin-top: 0.25rem;
}

.message-time {
  font-size: 0.7rem;
  color: #666;
}

.read-icon {
  width: 12px;
  height: 12px;
  color: #d4c84a;
}

.message-input {
  display: flex;
  gap: 0.75rem;
  padding: 1rem;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  align-items: flex-end;
  position: relative;
}

.input-wrapper {
  flex: 1;
  position: relative;
  display: flex;
  align-items: flex-end;
}

.input-wrapper textarea {
  flex: 1;
  padding: 0.75rem 2.5rem 0.75rem 1rem;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  color: #e0e0e0;
  font-size: 0.9rem;
  resize: none;
  font-family: inherit;
  line-height: 1.4;
}

.input-wrapper textarea:focus {
  outline: none;
  border-color: #d4c84a;
}

.input-wrapper textarea::placeholder {
  color: #666;
}

.emoji-btn {
  position: absolute;
  right: 8px;
  bottom: 8px;
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  opacity: 0.6;
  transition: opacity 0.2s;
}

.emoji-btn:hover {
  opacity: 1;
}

.emoji-btn .icon {
  width: 18px;
  height: 18px;
  color: #888;
}

.send-btn {
  padding: 0.75rem;
  background: #d4c84a;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}

.send-btn:hover:not(:disabled) {
  background: #e0d454;
  transform: scale(1.05);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.send-btn .icon {
  width: 20px;
  height: 20px;
  color: #0d0d1a;
}

/* Emoji Picker */
.emoji-picker {
  position: absolute;
  bottom: 80px;
  left: 1rem;
  right: 1rem;
  background: #1a1a2e;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 0.75rem;
  z-index: 100;
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(10, 1fr);
  gap: 4px;
}

.emoji-item {
  background: none;
  border: none;
  font-size: 1.25rem;
  cursor: pointer;
  padding: 6px;
  border-radius: 6px;
  transition: background 0.2s;
}

.emoji-item:hover {
  background: rgba(255, 255, 255, 0.1);
}

/* Emoji Overlay - dışına tıklanınca kapaması için */
.emoji-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 99;
}

/* Delete Confirmation Modal */
.delete-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 1rem;
}

.delete-modal {
  background: #1a1a2e;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 1.5rem;
  max-width: 350px;
  width: 100%;
  text-align: center;
}

.delete-modal h3 {
  color: #ff6b6b;
  font-size: 1.1rem;
  margin: 0 0 1rem;
}

.delete-modal p {
  color: #ccc;
  font-size: 0.9rem;
  margin: 0 0 0.5rem;
}

.delete-modal .delete-note {
  color: #888;
  font-size: 0.8rem;
  margin-bottom: 1.5rem;
}

.delete-modal-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: center;
}

.delete-modal-actions button {
  padding: 0.6rem 1.5rem;
  border-radius: 8px;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s;
}

.cancel-btn {
  background: transparent;
  border: 1px solid #555;
  color: #aaa;
}

.cancel-btn:hover {
  border-color: #888;
  color: #fff;
}

.confirm-btn {
  background: #ff6b6b;
  border: none;
  color: #fff;
  font-weight: 500;
}

.confirm-btn:hover {
  background: #ff5252;
}

/* Empty States */
.empty-conversations, .no-chat-selected {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #666;
  text-align: center;
  padding: 2rem;
}

.empty-icon {
  width: 48px;
  height: 48px;
  color: #444;
  margin-bottom: 1rem;
}

.empty-conversations p, .no-chat-selected p {
  margin: 0 0 0.5rem;
  font-size: 0.9rem;
}

.empty-conversations span {
  font-size: 0.8rem;
  color: #555;
}

.loading-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #d4c84a;
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.back-btn {
  background: none;
  border: none;
  color: #888;
  cursor: pointer;
  padding: 0.5rem;
}

.mobile-only {
  display: none;
}

.icon {
  width: 20px;
  height: 20px;
}

/* Mobile */
@media (max-width: 768px) {
  .messages-container {
    padding: 60px 0.5rem 0.5rem;
    height: calc(100vh - 60px);
  }
  
  .messages-layout {
    flex-direction: column;
    height: 100%;
  }
  
  .conversations-panel {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    max-height: 100%;
  }
  
  .conversations-panel:has(~ .chat-panel .chat-header) {
    display: none;
  }
  
  .chat-panel {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-height: 0;
  }
  
  .chat-panel:not(:has(.chat-header)) {
    display: none;
  }
  
  .mobile-only {
    display: block;
  }
  
  .messages-list {
    flex: 1;
    min-height: 0;
  }
  
  .message-bubble {
    max-width: 85%;
  }
  
  .message-input {
    padding: 0.75rem;
    gap: 0.5rem;
  }
  
  .input-wrapper textarea {
    font-size: 16px; /* Prevent iOS zoom */
    padding: 0.6rem 2.5rem 0.6rem 0.75rem;
  }
  
  .emoji-picker {
    bottom: 70px;
    left: 0.5rem;
    right: 0.5rem;
  }
  
  .emoji-grid {
    grid-template-columns: repeat(6, 1fr);
  }
}

/* Custom Scrollbar */
.conversation-list::-webkit-scrollbar,
.messages-list::-webkit-scrollbar {
  width: 6px;
}

.conversation-list::-webkit-scrollbar-track,
.messages-list::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.1);
}

.conversation-list::-webkit-scrollbar-thumb,
.messages-list::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
}

.conversation-list::-webkit-scrollbar-thumb:hover,
.messages-list::-webkit-scrollbar-thumb:hover {
  background: rgba(212, 200, 74, 0.3);
}
</style>
