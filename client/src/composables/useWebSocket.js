import { ref, onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

const client = ref(null)
const connected = ref(false)
const subscriptions = ref(new Map())

export function useWebSocket() {

  function connect() {
    if (client.value?.connected) return

    client.value = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8081/ws'),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,

      onConnect: () => {
        connected.value = true
        console.log('WebSocket connected')
      },

      onDisconnect: () => {
        connected.value = false
        console.log('WebSocket disconnected')
      },

      onStompError: (frame) => {
        console.error('WebSocket error:', frame.headers.message)
      }
    })

    client.value.activate()
  }

  function disconnect() {
    if (client.value) {
      client.value.deactivate()
      subscriptions.value.clear()
    }
  }

  function subscribe(destination, callback) {
    if (!client.value?.connected) {
      // Queue subscription for when connected
      const checkConnection = setInterval(() => {
        if (client.value?.connected) {
          clearInterval(checkConnection)
          doSubscribe(destination, callback)
        }
      }, 100)

      // Timeout after 10 seconds
      setTimeout(() => clearInterval(checkConnection), 10000)
      return
    }

    doSubscribe(destination, callback)
  }

  function doSubscribe(destination, callback) {
    if (subscriptions.value.has(destination)) {
      // Already subscribed
      return
    }

    const subscription = client.value.subscribe(destination, (message) => {
      try {
        const data = JSON.parse(message.body)
        callback(data)
      } catch {
        callback(message.body)
      }
    })

    subscriptions.value.set(destination, subscription)
  }

  function unsubscribe(destination) {
    const subscription = subscriptions.value.get(destination)
    if (subscription) {
      subscription.unsubscribe()
      subscriptions.value.delete(destination)
    }
  }

  function subscribeToTopic(topicId, callback) {
    subscribe(`/topic/entries/${topicId}`, callback)
  }

  function subscribeToSidebar(callback) {
    subscribe('/topic/sidebar', callback)
  }

  function unsubscribeFromTopic(topicId) {
    unsubscribe(`/topic/entries/${topicId}`)
  }

  /**
   * Kullanıcıya gelen özel mesajlara abone ol
   */
  function subscribeToUserMessages(username, onMessage, onUnreadCount, onMessagesRead) {
    // Yeni mesaj bildirimi
    subscribe(`/user/${username}/queue/messages`, onMessage)
    // Okunmamış sayı güncellemesi
    subscribe(`/user/${username}/queue/unread-count`, onUnreadCount)
    // Mesajların okunduğu bildirimi
    if (onMessagesRead) {
      subscribe(`/user/${username}/queue/messages-read`, onMessagesRead)
    }
  }

  function unsubscribeFromUserMessages(username) {
    unsubscribe(`/user/${username}/queue/messages`)
    unsubscribe(`/user/${username}/queue/unread-count`)
    unsubscribe(`/user/${username}/queue/messages-read`)
  }

  onUnmounted(() => {
    // Clean up subscriptions when component unmounts
    subscriptions.value.forEach((sub) => sub.unsubscribe())
    subscriptions.value.clear()
  })

  return {
    connected,
    connect,
    disconnect,
    subscribe,
    unsubscribe,
    subscribeToTopic,
    subscribeToSidebar,
    unsubscribeFromTopic,
    subscribeToUserMessages,
    unsubscribeFromUserMessages
  }
}
