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
              required
              minlength="3"
            />
          </div>

          <div class="form-group">
        <label>Kategori</label>
        <SelectBox 
          v-model="form.categoryId" 
          :options="categories" 
          placeholder="Kategori seçiniz"
        />
        <span class="error" v-if="v$.categoryId.$error">
          {{ v$.categoryId.$errors[0].$message }}
        </span>
      </div>

      <div class="form-group">
        <label>İçerik</label>
        <textarea 
          v-model="form.content" 
          placeholder="ilk entry içeriği..."
          rows="6"
          @blur="v$.content.$touch"
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
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { X } from 'lucide-vue-next'
import { useVuelidate } from '@vuelidate/core'
import { required, minLength, helpers } from '@vuelidate/validators'
import { categoriesApi } from '@/services/api'
import { useTopicsStore } from '@/stores/topics'
import { useRouter } from 'vue-router'
import SelectBox from '@/components/ui/SelectBox.vue'

const emit = defineEmits(['close', 'created'])
const router = useRouter()
const topicsStore = useTopicsStore()

const loading = ref(false)
const error = ref(null)
const categories = ref([])

const form = reactive({
  title: '',
  categoryId: null,
  content: ''
})

const rules = computed(() => ({
  title: {
    required: helpers.withMessage('Başlık alanı zorunludur.', required),
    minLength: helpers.withMessage(({ $params }) => `Başlık en az ${$params.min} karakter olmalıdır.`, minLength(3))
  },
  categoryId: {
    required: helpers.withMessage('Kategori seçimi zorunludur.', required)
  },
  content: {
    required: helpers.withMessage('İçerik alanı zorunludur.', required),
    minLength: helpers.withMessage(({ $params }) => `İçerik en az ${$params.min} karakter olmalıdır.`, minLength(3))
  }
}))

const v$ = useVuelidate(rules, form)

const loadingCategories = ref(false)

const isValid = computed(() => {
  return form.value.title.length >= 3 && 
         form.value.categoryId && 
         form.value.content.length > 0
})

onMounted(async () => {
  loadingCategories.value = true
  try {
    const res = await categoriesApi.getAll()
    categories.value = res.data
  } catch (err) {
    console.error('Categories fetch error:', err)
  } finally {
    loadingCategories.value = false
  }
})

async function handleSubmit() {
  if (!isValid.value) return
  
  loading.value = true
  error.value = null

  try {
    // 1. Create Topic
    const newTopic = await topicsStore.createTopic({
      title: form.value.title,
      categoryId: form.value.categoryId
    })

    // 2. Create First Entry handled by backend? No, createTopic usually just creates topic?
    // User request implies creating topic AND entry.
    // If backend verify: TopicService.createTopic takes TopicRequest (title, categoryId).
    // It creates TOPIC. It does NOT create entry.
    // So we need to create Entry separately?
    // Or update Backend to accept initial entry?
    // Standard Sozluk flow: Create Topic -> Redirect to Topic -> User enters entry?
    // OR Atomic: Create Topic + Entry.
    // User said "yeni topic ekleme kısmını da implemente edelim... bizim için bu değer category olacak".
    // I will assume simple: Create Topic, then Create Entry.
    // BUT if I create topic without entry, it might be empty/deleted?
    // Actually, Topic usually needs an entry to exist in lists.
    // I should create entry immediately.
    
    // I will use store action if available, or API calls.
        
    // Create Entry
    const { entriesApi } = await import('@/services/api') // Dynamic import to avoid circular dep if any
    await entriesApi.create({
      topicId: newTopic.id,
      content: form.value.content
    })
    
    emit('created')
    router.push(`/baslik/${newTopic.slug}`)
    emit('close')
    
  } catch (err) {
    error.value = err.response?.data?.message || 'Bir hata oluştu'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
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
}

.modal-content {
  background: #1a1a2e;
  width: 90%;
  max-width: 500px;
  border-radius: 8px;
  border: 1px solid #2a2a4a;
  box-shadow: 0 10px 40px rgba(0,0,0,0.5);
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
</style>
