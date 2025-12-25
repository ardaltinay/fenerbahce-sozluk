<template>
  <div class="forgot-password-page">
    <div class="form-container">
      <div class="form-header">
        <router-link to="/" class="logo">
          <span class="logo-icon">⚽</span>
          <span class="logo-text">fenerbahçe sözlük</span>
        </router-link>
        <h1>şifremi unuttum</h1>
        <p class="subtitle">Kayıtlı email adresinizi girin, size şifre sıfırlama bağlantısı gönderelim.</p>
      </div>

      <form v-if="!submitted" @submit.prevent="handleSubmit">
        <div class="form-group">
          <label>e-posta adresi</label>
          <div class="input-group">
            <Mail class="input-icon" />
            <input
              v-model="email"
              type="email"
              placeholder="ornek@email.com"
              required
              autofocus
            />
          </div>
        </div>

        <div v-if="error" class="error-box">{{ error }}</div>

        <button type="submit" class="submit-btn" :disabled="loading">
          <Loader2 v-if="loading" class="spin" />
          {{ loading ? 'gönderiliyor...' : 'sıfırlama bağlantısı gönder' }}
        </button>
      </form>

      <div v-else class="success-state">
        <div class="success-icon">
          <CheckCircle class="icon" />
        </div>
        <h2>Email Gönderildi!</h2>
        <p>
          Eğer <strong>{{ email }}</strong> adresi sistemimizde kayıtlıysa, 
          şifre sıfırlama bağlantısı içeren bir email gönderdik.
        </p>
        <p class="hint">Email gelmedi mi? Spam klasörünü kontrol edin veya birkaç dakika bekleyin.</p>
        
        <button @click="submitted = false" class="retry-btn">
          <RefreshCw class="icon-sm" />
          Tekrar Dene
        </button>
      </div>

      <div class="back-link">
        <router-link to="/">
          <ArrowLeft class="icon-sm" />
          ana sayfaya dön
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Mail, Loader2, CheckCircle, ArrowLeft, RefreshCw } from 'lucide-vue-next'
import { authApi } from '@/services/api'
import { useToast } from '@/composables/useToast'

const toast = useToast()

const email = ref('')
const loading = ref(false)
const error = ref('')
const submitted = ref(false)

async function handleSubmit() {
  loading.value = true
  error.value = ''

  try {
    await authApi.forgotPassword(email.value)
    submitted.value = true
    toast.success('Şifre sıfırlama bağlantısı gönderildi!')
  } catch (err) {
    // Always show success to prevent email enumeration
    // But in case of network/server errors, show error
    if (err.response?.status >= 500) {
      error.value = 'Bir hata oluştu. Lütfen daha sonra tekrar deneyin.'
    } else if (err.response?.status === 429) {
      error.value = err.response?.data?.message || 'Çok fazla deneme. Lütfen bekleyin.'
    } else {
      // For other cases, still show success (security best practice)
      submitted.value = true
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.forgot-password-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #0d0d1a 0%, #1a1a2e 50%, #0f0f23 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem 1rem;
}

.form-container {
  background: rgba(26, 26, 46, 0.95);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  width: 100%;
  max-width: 420px;
  border-radius: 16px;
  border: 1px solid rgba(212, 200, 74, 0.2);
  box-shadow: 0 10px 40px rgba(0,0,0,0.6);
  padding: 2rem;
}

.form-header {
  text-align: center;
  margin-bottom: 2rem;
}

.logo {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  text-decoration: none;
  margin-bottom: 1.5rem;
}

.logo-icon {
  font-size: 1.5rem;
}

.logo-text {
  font-size: 1.25rem;
  font-weight: 700;
  color: #d4c84a;
}

.form-header h1 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #e0e0e0;
  margin: 0 0 0.5rem;
}

.subtitle {
  font-size: 0.9rem;
  color: #888;
  margin: 0;
  line-height: 1.5;
}

.form-group {
  margin-bottom: 1.5rem;
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

.error-box {
  padding: 0.75rem;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: 8px;
  color: #ef4444;
  font-size: 0.8rem;
  margin-bottom: 1rem;
}

.submit-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.875rem;
  background: #d4c84a;
  color: #0f0f1a;
  font-size: 0.9rem;
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
  opacity: 0.6;
  cursor: not-allowed;
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
  padding: 1rem 0;
}

.success-icon {
  display: flex;
  justify-content: center;
  margin-bottom: 1rem;
}

.success-icon .icon {
  width: 64px;
  height: 64px;
  color: #10b981;
}

.success-state h2 {
  font-size: 1.25rem;
  color: #e0e0e0;
  margin: 0 0 1rem;
}

.success-state p {
  font-size: 0.9rem;
  color: #b0b0b0;
  line-height: 1.6;
  margin: 0 0 0.75rem;
}

.success-state strong {
  color: #d4c84a;
}

.hint {
  font-size: 0.8rem;
  color: #666;
}

.retry-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 1.5rem;
  padding: 0.5rem 1rem;
  background: transparent;
  border: 1px solid #2a2a4a;
  border-radius: 6px;
  color: #888;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.15s;
}

.retry-btn:hover {
  border-color: #d4c84a;
  color: #d4c84a;
}

.icon-sm {
  width: 16px;
  height: 16px;
}

.back-link {
  text-align: center;
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid #2a2a4a;
}

.back-link a {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  color: #888;
  text-decoration: none;
  font-size: 0.85rem;
  transition: color 0.15s;
}

.back-link a:hover {
  color: #d4c84a;
}

/* Mobile responsive */
@media (max-width: 480px) {
  .forgot-password-page {
    padding: 1rem;
  }
  
  .form-container {
    padding: 1.5rem;
    border-radius: 12px;
  }
  
  .logo-icon {
    font-size: 1.25rem;
  }
  
  .logo-text {
    font-size: 1.1rem;
  }
  
  .form-header h1 {
    font-size: 1.25rem;
  }
  
  .subtitle {
    font-size: 0.85rem;
  }
  
  .success-icon .icon {
    width: 56px;
    height: 56px;
  }
  
  .success-state h2 {
    font-size: 1.1rem;
  }
  
  .success-state p {
    font-size: 0.85rem;
  }
}
</style>
