<template>
  <div class="contact-page">
    <Header />

    <div class="contact-container">
      <div class="contact-card">
        <div class="card-header">
          <h1>
            <MessageSquare class="header-icon" />
            İletişim
          </h1>
          <p class="subtitle">Görüş, öneri ve şikayetlerinizi bize iletebilirsiniz.</p>
        </div>

        <form v-if="!submitted" @submit.prevent="handleSubmit" class="contact-form">
          <div class="form-row">
            <div class="form-group">
              <label>Adınız</label>
              <div class="input-group">
                <User class="input-icon" />
                <input 
                  v-model="form.name" 
                  type="text" 
                  placeholder="Adınız Soyadınız"
                  required 
                />
              </div>
            </div>

            <div class="form-group">
              <label>E-posta</label>
              <div class="input-group">
                <Mail class="input-icon" />
                <input 
                  v-model="form.email" 
                  type="email" 
                  placeholder="ornek@email.com"
                  required 
                />
              </div>
            </div>
          </div>

          <div class="form-group">
            <label>Konu</label>
            <div class="input-group">
              <Hash class="input-icon" />
              <input 
                v-model="form.subject" 
                type="text" 
                placeholder="Mesajınızın konusu"
                required 
              />
            </div>
          </div>

          <div class="form-group">
            <label>Mesajınız</label>
            <textarea 
              v-model="form.message" 
              placeholder="Mesajınızı buraya yazın..."
              rows="6"
              required
            ></textarea>
            <span class="char-count" :class="{ 'warning': form.message.length > 4500 }">
              {{ form.message.length }} / 5000
            </span>
          </div>

          <div v-if="error" class="error-box">{{ error }}</div>

          <button type="submit" class="submit-btn" :disabled="loading || !isFormValid">
            <Loader2 v-if="loading" class="spin" />
            <Send v-else class="btn-icon" />
            {{ loading ? 'Gönderiliyor...' : 'Mesajı Gönder' }}
          </button>
        </form>

        <!-- Success State -->
        <div v-else class="success-state">
          <div class="success-icon">
            <CheckCircle class="icon" />
          </div>
          <h2>Mesajınız Gönderildi!</h2>
          <p>
            Mesajınızı aldık. En kısa sürede size dönüş yapacağız.
            Teşekkür ederiz!
          </p>
          
          <button @click="resetForm" class="another-btn">
            <MessageSquare class="icon-sm" />
            Başka Bir Mesaj Gönder
          </button>
        </div>
      </div>

      <!-- Contact Info -->
      <div class="contact-info">
        <div class="info-card">
          <Mail class="info-icon" />
          <div>
            <h3>E-posta</h3>
            <a href="mailto:admin@fenerbahcesozluk.net">admin@fenerbahcesozluk.net</a>
          </div>
        </div>
        
        <div class="info-card">
          <Clock class="info-icon" />
          <div>
            <h3>Yanıt Süresi</h3>
            <p>Genellikle 12-48 saat içinde yanıt veriyoruz.</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { MessageSquare, User, Mail, Hash, Send, Loader2, CheckCircle, Clock } from 'lucide-vue-next'
import Header from '@/components/layout/Header.vue'
import { contactApi } from '@/services/api'
import { useToast } from '@/composables/useToast'
import { useAuthStore } from '@/stores/auth'

const toast = useToast()
const authStore = useAuthStore()

const form = reactive({
  name: authStore.user?.username || '',
  email: authStore.user?.email || '',
  subject: '',
  message: '',
})

const loading = ref(false)
const error = ref('')
const submitted = ref(false)

const isFormValid = computed(() => {
  return form.name.length >= 2 && 
         form.email.includes('@') && 
         form.subject.length >= 3 && 
         form.message.length >= 10 &&
         form.message.length <= 5000
})

async function handleSubmit() {
  loading.value = true
  error.value = ''

  try {
    await contactApi.send(form)
    submitted.value = true
    toast.success('Mesajınız başarıyla gönderildi!')
  } catch (err) {
    error.value = err.response?.data?.message || 'Mesaj gönderilemedi. Lütfen tekrar deneyin.'
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.subject = ''
  form.message = ''
  submitted.value = false
  error.value = ''
}
</script>

<style scoped>
.contact-page {
  min-height: 100vh;
  background: transparent;
  color: #e0e0e0;
}

.contact-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 70px 1rem 2rem;
  display: grid;
  gap: 1.5rem;
}

.contact-card {
  background: rgba(26, 26, 46, 0.45);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 237, 0, 0.05);
  border-radius: 12px;
  padding: 2rem;
}

.card-header {
  margin-bottom: 2rem;
}

.card-header h1 {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 1.5rem;
  font-weight: 600;
  color: #d4c84a;
  margin: 0 0 0.5rem;
}

.header-icon {
  width: 28px;
  height: 28px;
}

.subtitle {
  color: #888;
  font-size: 0.9rem;
  margin: 0;
}

/* Form */
.contact-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

@media (max-width: 600px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}

.form-group label {
  display: block;
  font-size: 0.8rem;
  color: #888;
  margin-bottom: 0.5rem;
}

.input-group {
  display: flex;
  align-items: center;
  background: #0d0d1a;
  border: 1px solid #2a2a4a;
  border-radius: 8px;
  overflow: hidden;
}

.input-group:focus-within {
  border-color: #d4c84a;
}

.input-icon {
  width: 18px;
  height: 18px;
  color: #555;
  margin-left: 0.75rem;
  flex-shrink: 0;
}

.input-group input {
  flex: 1;
  padding: 0.875rem 0.75rem;
  background: transparent;
  border: none;
  color: #e0e0e0;
  font-size: 0.9rem;
}

.input-group input:focus {
  outline: none;
}

.input-group input::placeholder {
  color: #555;
}

textarea {
  width: 100%;
  padding: 1rem;
  background: #0d0d1a;
  border: 1px solid #2a2a4a;
  border-radius: 8px;
  color: #e0e0e0;
  font-size: 0.9rem;
  font-family: inherit;
  resize: vertical;
  min-height: 150px;
}

textarea:focus {
  outline: none;
  border-color: #d4c84a;
}

textarea::placeholder {
  color: #555;
}

.char-count {
  display: block;
  text-align: right;
  font-size: 0.75rem;
  color: #555;
  margin-top: 0.25rem;
}

.char-count.warning {
  color: #f59e0b;
}

.error-box {
  padding: 0.75rem;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: 8px;
  color: #ef4444;
  font-size: 0.8rem;
}

.submit-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 1rem;
  background: #d4c84a;
  color: #0f0f1a;
  font-size: 0.95rem;
  font-weight: 600;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;
}

.submit-btn:hover:not(:disabled) {
  background: #e0d454;
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-icon {
  width: 18px;
  height: 18px;
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Success State */
.success-state {
  text-align: center;
  padding: 2rem 0;
}

.success-icon .icon {
  width: 72px;
  height: 72px;
  color: #10b981;
  margin-bottom: 1.5rem;
}

.success-state h2 {
  font-size: 1.5rem;
  color: #e0e0e0;
  margin: 0 0 1rem;
}

.success-state p {
  font-size: 0.95rem;
  color: #b0b0b0;
  line-height: 1.6;
  margin: 0 0 2rem;
}

.another-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  background: transparent;
  border: 1px solid #2a2a4a;
  border-radius: 8px;
  color: #888;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.15s;
}

.another-btn:hover {
  border-color: #d4c84a;
  color: #d4c84a;
}

.icon-sm {
  width: 18px;
  height: 18px;
}

/* Contact Info */
.contact-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
}

.info-card {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  background: rgba(26, 26, 46, 0.45);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 237, 0, 0.05);
  border-radius: 12px;
  padding: 1.25rem;
}

.info-icon {
  width: 24px;
  height: 24px;
  color: #d4c84a;
  flex-shrink: 0;
  margin-top: 2px;
}

.info-card h3 {
  font-size: 0.9rem;
  font-weight: 600;
  color: #e0e0e0;
  margin: 0 0 0.25rem;
}

.info-card p {
  font-size: 0.85rem;
  color: #888;
  margin: 0;
}

.info-card a {
  font-size: 0.85rem;
  color: #d4c84a;
  text-decoration: none;
}

.info-card a:hover {
  text-decoration: underline;
}
</style>
