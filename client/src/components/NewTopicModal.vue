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

      <!-- Topic Type -->
      <div class="form-group">
        <label>Başlık Türü <span class="optional">(opsiyonel)</span></label>
        <div class="type-buttons">
          <button 
            type="button" 
            class="type-btn" 
            :class="{ active: form.topicType === 'general' }"
            @click="selectTopicType('general')"
          >
            Genel
          </button>
          <button 
            type="button" 
            class="type-btn" 
            :class="{ active: form.topicType === 'player' }"
            @click="selectTopicType('player')"
          >
            <User class="btn-icon" /> Oyuncu
          </button>
          <button 
            type="button" 
            class="type-btn" 
            :class="{ active: form.topicType === 'club' }"
            @click="selectTopicType('club')"
          >
            <Shield class="btn-icon" /> Kulüp
          </button>
        </div>
      </div>

      <!-- Transfermarkt Search -->
      <div v-if="form.topicType === 'player' || form.topicType === 'club'" class="form-group">
        <label>Transfermarkt Eşleştirme</label>
        <div class="tm-search-box">
          <input 
            v-model="tmSearchQuery" 
            type="text" 
            :placeholder="form.topicType === 'player' ? 'Oyuncu ara...' : 'Kulüp ara...'"
            @keyup.enter="searchTransfermarkt"
          />
          <button type="button" class="search-btn" @click="searchTransfermarkt" :disabled="tmSearching">
            <Search v-if="!tmSearching" class="btn-icon" />
            <Loader2 v-else class="btn-icon spin" />
          </button>
        </div>

        <!-- Search Results -->
        <div v-if="tmResults.length > 0" class="tm-results">
          <div 
            v-for="result in tmResults" 
            :key="result.id" 
            class="tm-result-item"
            :class="{ selected: form.transfermarktId === result.id }"
            @click="selectTransfermarkt(result)"
          >
            <span class="result-name">{{ result.name }}</span>
            <span v-if="result.club" class="result-club">{{ result.club.name }}</span>
            <span v-if="result.country" class="result-club">{{ result.country }}</span>
            <Check v-if="form.transfermarktId === result.id" class="check-icon" />
          </div>
        </div>

        <div v-if="form.transfermarktId" class="tm-selected">
          <Check class="check-icon" />
          <span>Seçili: {{ selectedTmName }}</span>
          <button type="button" class="clear-btn" @click="clearTransfermarkt">
            <X class="btn-icon" />
          </button>
        </div>
      </div>

      <div class="form-group">
        <label>İçerik</label>
        <textarea 
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
import { ref, reactive, computed, onMounted } from 'vue'
import { X, User, Shield, Search, Loader2, Check } from 'lucide-vue-next'
import { useVuelidate } from '@vuelidate/core'
import { required, minLength, helpers } from '@vuelidate/validators'
import { entriesApi, transfermarktApi } from '@/services/api'
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
  content: '',
  topicType: 'general',
  transfermarktId: null
})

// Transfermarkt state
const tmSearchQuery = ref('')
const tmSearching = ref(false)
const tmResults = ref([])
const selectedTmName = ref('')

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
      title: form.title,
      topicType: form.topicType !== 'general' ? form.topicType : null,
      transfermarktId: form.transfermarktId
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

// Transfermarkt functions
function selectTopicType(type) {
  form.topicType = type
  if (type === 'general') {
    form.transfermarktId = null
    tmResults.value = []
    selectedTmName.value = ''
  }
}

async function searchTransfermarkt() {
  if (!tmSearchQuery.value.trim()) return
  
  tmSearching.value = true
  tmResults.value = []
  
  try {
    let response
    if (form.topicType === 'player') {
      response = await transfermarktApi.searchPlayers(tmSearchQuery.value)
    } else {
      response = await transfermarktApi.searchClubs(tmSearchQuery.value)
    }
    tmResults.value = response.data.results || []
  } catch (err) {
    console.error('Transfermarkt search error:', err)
  } finally {
    tmSearching.value = false
  }
}

function selectTransfermarkt(result) {
  form.transfermarktId = result.id
  selectedTmName.value = result.name
  tmResults.value = []
}

function clearTransfermarkt() {
  form.transfermarktId = null
  selectedTmName.value = ''
  tmSearchQuery.value = ''
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

/* Transfermarkt Search */
.tm-search-box {
  display: flex;
  gap: 0.5rem;
}

.tm-search-box input {
  flex: 1;
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
</style>
