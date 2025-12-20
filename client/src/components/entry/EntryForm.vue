<template>
  <form @submit.prevent="handleSubmit" class="card">
    <div class="flex items-center gap-2 mb-4">
      <PenSquare class="w-5 h-5 text-fb-yellow" />
      <h3 class="font-semibold text-text-primary">entry yaz</h3>
    </div>
    
    <div class="relative">
      <textarea
        v-model="content"
        placeholder="düşüncelerini paylaş..."
        class="input min-h-32 resize-none"
        :class="{ 'border-accent-error': error }"
        @focus="focused = true"
        @blur="focused = false"
      ></textarea>
      
      <!-- Character Count -->
      <div class="absolute bottom-3 right-3 text-xs" :class="characterCountClass">
        {{ content.length }} / {{ maxLength }}
      </div>
    </div>

    <p v-if="error" class="text-sm text-accent-error mt-2">{{ error }}</p>

    <!-- Toolbar -->
    <div class="flex items-center justify-between mt-4 pt-4 border-t border-fb-yellow/10">
      <div class="flex items-center gap-2">
        <button type="button" class="btn btn-ghost p-2" title="bkz ekle">
          <Link class="w-4 h-4" />
        </button>
        <button type="button" class="btn btn-ghost p-2" title="spoiler">
          <EyeOff class="w-4 h-4" />
        </button>
        <button type="button" class="btn btn-ghost p-2" title="resim">
          <Image class="w-4 h-4" />
        </button>
      </div>

      <button 
        type="submit" 
        class="btn btn-primary"
        :disabled="!isValid || submitting"
      >
        <Send class="w-4 h-4" v-if="!submitting" />
        <Loader2 class="w-4 h-4 animate-spin" v-else />
        <span>{{ submitting ? 'gönderiliyor...' : 'gönder' }}</span>
      </button>
    </div>
  </form>
</template>

<script setup>
import { ref, computed } from 'vue'
import { PenSquare, Link, EyeOff, Image, Send, Loader2 } from 'lucide-vue-next'
import { useEntriesStore } from '@/stores/entries'
import { useAuthStore } from '@/stores/auth'

const props = defineProps({
  topicId: {
    type: Number,
    required: true,
  },
  topicTitle: {
    type: String,
    required: true,
  },
  topicSlug: {
    type: String,
    required: true,
  }
})

const emit = defineEmits(['submitted'])

const entriesStore = useEntriesStore()
const authStore = useAuthStore()

const content = ref('')
const focused = ref(false)
const submitting = ref(false)
const error = ref('')
const maxLength = 2000

const isValid = computed(() => {
  return content.value.trim().length >= 10 && content.value.length <= maxLength
})

const characterCountClass = computed(() => {
  if (content.value.length > maxLength) return 'text-accent-error'
  if (content.value.length > maxLength * 0.9) return 'text-accent-warning'
  return 'text-text-muted'
})

async function handleSubmit() {
  if (!isValid.value) {
    error.value = 'entry en az 10 karakter olmalıdır.'
    return
  }

  if (!authStore.isAuthenticated) {
    error.value = 'entry yazmak için giriş yapmalısınız.'
    return
  }

  submitting.value = true
  error.value = ''

  try {
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    const newEntry = {
      id: Date.now(),
      content: content.value,
      author: {
        username: authStore.username,
        avatar: null,
      },
      topicId: props.topicId,
      topicTitle: props.topicTitle,
      topicSlug: props.topicSlug,
      createdAt: new Date().toISOString(),
      favoriteCount: 0,
      isFavorited: false,
    }

    entriesStore.addEntry(newEntry)
    content.value = ''
    emit('submitted', newEntry)
  } catch (e) {
    error.value = 'bir hata oluştu, tekrar deneyin.'
  } finally {
    submitting.value = false
  }
}
</script>
