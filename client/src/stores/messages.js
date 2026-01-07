import { defineStore } from 'pinia'
import { ref } from 'vue'
import { messagesApi } from '@/services/api'
import { useAuthStore } from '@/stores/auth'

export const useMessagesStore = defineStore('messages', () => {
  const conversations = ref([])
  const currentMessages = ref([])
  const currentPartner = ref(null)
  const unreadCount = ref(0)
  const loading = ref(false)
  const error = ref(null)

  /**
   * Konuşma listesini yükle
   */
  async function fetchConversations() {
    loading.value = true
    error.value = null
    try {
      const response = await messagesApi.getConversations()
      conversations.value = response.data
    } catch (err) {
      console.error('Conversations fetch error:', err)
      error.value = 'Konuşmalar yüklenemedi'
    } finally {
      loading.value = false
    }
  }

  /**
   * Belirli bir kullanıcıyla olan mesajları yükle
   */
  async function fetchMessages(username, page = 0) {
    error.value = null
    try {
      const authStore = useAuthStore()
      const response = await messagesApi.getMessages(username, page, 50)
      // Mesajlar tersten geliyor (en yeni en önce), UI için ters çevir
      // isOwn'ı frontend'de hesapla (daha güvenilir)
      const messages = (response.data.content || []).map(msg => ({
        ...msg,
        isOwn: msg.senderUsername === authStore.username
      }))
      currentMessages.value = messages.reverse()
      currentPartner.value = username
    } catch (err) {
      console.error('Messages fetch error:', err)
      error.value = 'Mesajlar yüklenemedi'
    }
  }

  /**
   * Mesaj gönder
   */
  async function sendMessage(receiverUsername, content) {
    try {
      const response = await messagesApi.send({
        receiverUsername,
        content
      })
      // Mesajı listeye ekle (kendi mesajımız olduğu için isOwn: true)
      currentMessages.value.push({
        ...response.data,
        isOwn: true
      })
      return { success: true, data: response.data }
    } catch (err) {
      console.error('Send message error:', err)
      return { success: false, message: err.response?.data?.message || 'Mesaj gönderilemedi' }
    }
  }

  /**
   * Konuşmayı okundu olarak işaretle (arka planda çalışır, loading göstermez)
   */
  async function markConversationAsRead(username) {
    try {
      // Önce UI'ı güncelle (optimistic update)
      const conv = conversations.value.find(c => c.username === username)
      const previousUnread = conv?.unreadCount || 0
      if (conv) {
        conv.unreadCount = 0
      }
      // Toplam okunmamıştan düş
      unreadCount.value = Math.max(0, unreadCount.value - previousUnread)

      // Arka planda API çağrısı yap
      await messagesApi.markConversationAsRead(username)
    } catch (err) {
      console.error('Mark as read error:', err)
    }
  }

  /**
   * Okunmamış mesaj sayısını yükle
   */
  async function fetchUnreadCount() {
    try {
      const response = await messagesApi.getUnreadCount()
      unreadCount.value = response.data.count || 0
    } catch (err) {
      console.error('Unread count fetch error:', err)
    }
  }

  /**
   * WebSocket'ten gelen yeni mesajı işle
   */
  function handleIncomingMessage(message) {
    // Eğer şu an açık olan konuşmadaysak mesajı ekle
    if (currentPartner.value === message.senderUsername) {
      currentMessages.value.push(message)
    }

    // Konuşma listesini güncelle
    const existingConv = conversations.value.find(
      c => c.username === message.senderUsername
    )

    if (existingConv) {
      existingConv.lastMessage = message.content.substring(0, 50)
      existingConv.lastMessageAt = message.createdAt
      existingConv.unreadCount++
      // En üste taşı
      conversations.value = [
        existingConv,
        ...conversations.value.filter(c => c.username !== message.senderUsername)
      ]
    } else {
      // Yeni konuşma ekle
      conversations.value.unshift({
        username: message.senderUsername,
        lastMessage: message.content.substring(0, 50),
        lastMessageAt: message.createdAt,
        unreadCount: 1
      })
    }
  }

  /**
   * WebSocket'ten gelen okunmamış sayı güncellemesini işle
   */
  function updateUnreadCount(count) {
    unreadCount.value = count
  }

  /**
   * State'i temizle
   */
  function clearCurrentConversation() {
    currentMessages.value = []
    currentPartner.value = null
  }

  return {
    conversations,
    currentMessages,
    currentPartner,
    unreadCount,
    loading,
    error,
    fetchConversations,
    fetchMessages,
    sendMessage,
    markConversationAsRead,
    fetchUnreadCount,
    handleIncomingMessage,
    updateUnreadCount,
    clearCurrentConversation
  }
})
