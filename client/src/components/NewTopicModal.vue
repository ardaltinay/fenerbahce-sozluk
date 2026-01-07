<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content">
      <div class="modal-header">
        <h3>yeni başlık</h3>
        <button class="close-btn" @click="$emit('close')">
          <X class="icon" />
        </button>
      </div>

      <div class="modal-body">
        <form @submit.prevent="handleSubmit">
          <div class="form-group">
            <label>başlık</label>
            <input 
              v-model="form.title" 
              type="text" 
              placeholder="başlık giriniz..."
              maxlength="50"
              @blur="v$.title.$touch"
              :class="{ 'has-error': v$.title.$error }"
            />
            <span class="error" v-if="v$.title.$error">
              {{ v$.title.$errors[0].$message }}
            </span>
          </div>

      <div class="form-group">
        <label>İçerik</label>
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
              @keyup.enter.prevent="insertBkz"
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
            />
            <input 
              v-model="linkText" 
              type="text" 
              placeholder="görünecek isim"
              @keyup.enter.prevent="insertLink"
            />
            <button type="button" class="popover-btn" @click="insertLink">ekle</button>
          </div>
        </div>

        <textarea 
          ref="contentTextarea"
          v-model="form.content" 
          placeholder="ilk entry içeriği..."
          rows="6"
          @blur="v$.content.$touch"
          :class="{ 'has-error': v$.content.$error }"
        ></textarea>
        <span class="error" v-if="v$.content.$error">
          {{ v$.content.$errors[0].$message }}
        </span>
      </div>

      <div class="modal-actions">
        <button type="button" class="btn-cancel" @click="$emit('close')">iptal</button>
        <button type="submit" class="btn-submit" :disabled="loading">
          {{ loading ? 'oluşturuluyor...' : 'oluştur' }}
        </button>
      </div>

      <div v-if="error" class="error-msg">{{ error }}</div>
    </form>
  </div>
    </div>

    <ConfirmModal 
      :show="showDuplicateConfirm"
      title="başlık zaten var"
      message="bu başlık zaten mevcut. başlığa gitmek ister misiniz?"
      @confirm="handleDuplicateConfirm"
      @cancel="showDuplicateConfirm = false"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, nextTick } from 'vue'
import { X } from 'lucide-vue-next'
import { useVuelidate } from '@vuelidate/core'
import { required, minLength, helpers } from '@vuelidate/validators'
import { entriesApi } from '@/services/api'
import { useTopicsStore } from '@/stores/topics'
import { useRouter } from 'vue-router'
import ConfirmModal from '@/components/ui/ConfirmModal.vue'

const emit = defineEmits(['close', 'created'])
const router = useRouter()
const topicsStore = useTopicsStore()

const loading = ref(false)
const error = ref(null)
const showDuplicateConfirm = ref(false)
const existingTopicId = ref(null)

const form = reactive({
  title: '',
  content: ''
})

// Bkz/Link toolbar state
const contentTextarea = ref(null)
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
  
  const linkStr = `[${linkText.value.trim()}](${linkUrl.value.trim()})`
  insertAtCursor(linkStr)
  
  linkUrl.value = ''
  linkText.value = ''
  showLinkPopover.value = false
}

function insertAtCursor(text) {
  const textarea = contentTextarea.value
  if (!textarea) {
    form.content += text
    return
  }
  
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const before = form.content.substring(0, start)
  const after = form.content.substring(end)
  
  form.content = before + text + after
  
  nextTick(() => {
    textarea.focus()
    textarea.selectionStart = textarea.selectionEnd = start + text.length
  })
}

const rules = computed(() => ({
  title: {
    required: helpers.withMessage('Başlık alanı zorunludur.', required),
    minLength: helpers.withMessage(({ $params }) => `Başlık en az ${$params.min} karakter olmalıdır.`, minLength(3)),
    maxLength: helpers.withMessage(({ $params }) => `Başlık en fazla 50 karakter olabilir.`, (val) => val.length <= 50)
  },
  content: {
    required: helpers.withMessage('İçerik alanı zorunludur.', required),
    minLength: helpers.withMessage(({ $params }) => `İçerik en az ${$params.min} karakter olmalıdır.`, minLength(10))
  }
}))

const v$ = useVuelidate(rules, form)

async function handleSubmit() {
  const isFormValid = await v$.value.$validate()
  if (!isFormValid) return
  
  loading.value = true
  error.value = null

  try {
    const newTopic = await topicsStore.createTopic({
      title: form.title
    })
    
    await entriesApi.create({
      topicId: newTopic.id,
      content: form.content
    })
    
    emit('created')
    router.push(`/baslik/${newTopic.id}`)
    emit('close')
    
  } catch (err) {
    const errorMsg = err.response?.data?.message || err.message || ''
    
    if (errorMsg.includes('duplicate_topic:')) {
      existingTopicId.value = errorMsg.split('duplicate_topic:')[1]
      showDuplicateConfirm.value = true
      return
    }

    error.value = err.response?.data?.message || err.message || 'Bir hata oluştu'
  } finally {
    loading.value = false
  }
}

function handleDuplicateConfirm() {
  showDuplicateConfirm.value = false
  emit('close')
  router.push({
    path: `/baslik/${existingTopicId.value}`,
    query: form.content ? { draft: encodeURIComponent(form.content) } : {}
  })
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.75);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}

.modal-content {
  background: rgba(26, 26, 46, 0.95);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  width: 90%;
  max-width: 500px;
  border-radius: 12px;
  border: 1px solid rgba(212, 200, 74, 0.2);
  box-shadow: 0 10px 40px rgba(0,0,0,0.6);
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  border-bottom: 1px solid #2a2a4a;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.1rem;
  color: #d4c84a;
}

.close-btn {
  background: none;
  border: none;
  color: #888;
  cursor: pointer;
}
.close-btn:hover { color: #fff; }

.modal-body {
  padding: 1.5rem;
}

.form-group {
  margin-bottom: 1.2rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
  color: #ccc;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 0.75rem;
  background: #0d0d1a;
  border: 1px solid #2a2a4a;
  border-radius: 4px;
  color: #fff;
  font-size: 0.95rem;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #d4c84a;
}

.has-error {
  border-color: #e74c3c !important;
}

.error {
  color: #e74c3c;
  font-size: 0.8rem;
  margin-top: 0.25rem;
  display: block;
}

.modal-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1.5rem;
}

.btn-cancel {
  flex: 1;
  padding: 0.75rem;
  background: transparent;
  color: #888;
  border: 1px solid #2a2a4a;
  border-radius: 4px;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel:hover {
  border-color: #888;
  color: #fff;
}

.btn-submit {
  flex: 1;
  padding: 0.75rem;
  background: #d4c84a;
  color: #1a1a2e;
  border: none;
  border-radius: 4px;
  font-size: 0.95rem;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-submit:hover {
  background: #e0d45a;
}

.btn-submit:disabled {
  background: #444;
  color: #888;
  cursor: not-allowed;
}

.submit-btn {
  width: 100%;
  padding: 0.75rem;
  background: #d4c84a;
  color: #1a1a2e;
  border: none;
  border-radius: 4px;
  font-weight: bold;
  cursor: pointer;
  margin-top: 1rem;
}

.submit-btn:disabled {
  background: #444;
  color: #888;
  cursor: not-allowed;
}

.error-msg {
  color: #e74c3c;
  font-size: 0.9rem;
  margin-bottom: 1rem;
}

.hint {
  font-size: 0.8rem;
  color: #666;
  margin-top: 0.25rem;
}

/* Topic Type Buttons */
.type-buttons {
  display: flex;
  gap: 0.5rem;
}

.type-btn {
  flex: 1;
  padding: 0.5rem 0.75rem;
  background: rgba(26, 26, 46, 0.6);
  border: 1px solid rgba(255, 237, 0, 0.1);
  border-radius: 6px;
  color: #888;
  font-size: 0.8rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.35rem;
  transition: all 0.15s;
}

.type-btn:hover {
  border-color: rgba(255, 237, 0, 0.3);
  color: #ccc;
}

.type-btn.active {
  background: rgba(212, 200, 74, 0.15);
  border-color: #d4c84a;
  color: #d4c84a;
}

.btn-icon {
  width: 14px;
  height: 14px;
}

.optional {
  font-size: 0.7rem;
  color: #666;
  font-weight: normal;
}

.search-btn {
  padding: 0.5rem 0.75rem;
  background: rgba(212, 200, 74, 0.2);
  border: 1px solid rgba(255, 237, 0, 0.2);
  border-radius: 6px;
  color: #d4c84a;
  cursor: pointer;
  display: flex;
  align-items: center;
}

.search-btn:hover:not(:disabled) {
  background: rgba(212, 200, 74, 0.3);
}

.search-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.tm-results {
  margin-top: 0.5rem;
  background: rgba(13, 13, 26, 0.8);
  border: 1px solid rgba(255, 237, 0, 0.1);
  border-radius: 6px;
  max-height: 150px;
  overflow-y: auto;
}

.tm-result-item {
  padding: 0.5rem 0.75rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 237, 0, 0.05);
  transition: background 0.15s;
}

.tm-result-item:hover {
  background: rgba(212, 200, 74, 0.1);
}

.tm-result-item.selected {
  background: rgba(212, 200, 74, 0.15);
}

.tm-result-item:last-child {
  border-bottom: none;
}

.result-name {
  font-size: 0.85rem;
  color: #e0e0e0;
}

.result-club {
  font-size: 0.75rem;
  color: #888;
}

.check-icon {
  width: 14px;
  height: 14px;
  color: #4ade80;
  margin-left: auto;
}

.tm-selected {
  margin-top: 0.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  background: rgba(74, 222, 128, 0.1);
  border: 1px solid rgba(74, 222, 128, 0.2);
  border-radius: 6px;
  font-size: 0.8rem;
  color: #4ade80;
}

.clear-btn {
  margin-left: auto;
  background: none;
  border: none;
  color: #888;
  cursor: pointer;
  padding: 0.25rem;
  display: flex;
}

.clear-btn:hover {
  color: #ef4444;
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
  min-width: 120px;
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
</style>
