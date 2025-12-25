<template>
  <div class="reset-password-page">
    <div class="form-container">
      <div class="form-header">
        <router-link to="/" class="logo">
          <span class="logo-icon">⚽</span>
          <span class="logo-text">fenerbahçe sözlük</span>
        </router-link>
        
        <!-- Loading state while validating token -->
        <div v-if="validating" class="validating-state">
          <Loader2 class="spin loading-icon" />
          <p>Token doğrulanıyor...</p>
        </div>

        <!-- Invalid token state -->
        <div v-else-if="!tokenValid" class="invalid-token-state">
          <div class="error-icon">
            <XCircle class="icon" />
          </div>
          <h1>Geçersiz veya Süresi Dolmuş Bağlantı</h1>
          <p>Bu şifre sıfırlama bağlantısı artık geçerli değil. Bağlantı süresi dolmuş veya daha önce kullanılmış olabilir.</p>
          <router-link to="/sifremi-unuttum" class="retry-link">
            <RefreshCw class="icon-sm" />
            Yeni bağlantı iste
          </router-link>
        </div>

        <!-- Valid token - show form -->
        <template v-else>
          <h1>yeni şifre belirle</h1>
          <p class="subtitle">Hesabınız için yeni bir şifre oluşturun.</p>
        </template>
      </div>

      <!-- Password reset form -->
      <form v-if="tokenValid && !validating && !success" @submit.prevent="handleSubmit">
        <div class="form-group">
          <label>yeni şifre</label>
          <div class="input-group">
            <Lock class="input-icon" />
            <input
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="••••••••"
              required
              autofocus
            />
            <button type="button" class="toggle-password" @click="showPassword = !showPassword">
              <EyeOff v-if="showPassword" />
              <Eye v-else />
            </button>
          </div>
          <!-- Password strength -->
          <div class="password-strength">
            <div class="strength-bar">
              <div 
                class="strength-fill" 
                :class="`level-${passwordStrength}`"
                :style="{ width: (passwordStrength * 25) + '%' }"
              ></div>
            </div>
            <span class="strength-text">{{ strengthLabels[passwordStrength] }}</span>
          </div>
        </div>

        <div class="form-group">
          <label>şifre tekrar</label>
          <div class="input-group">
            <Lock class="input-icon" />
            <input
              v-model="form.confirmPassword"
              :type="showPassword ? 'text' : 'password'"
              placeholder="••••••••"
              required
            />
          </div>
          <span v-if="form.confirmPassword && form.password !== form.confirmPassword" class="error-hint">
            şifreler eşleşmiyor
          </span>
        </div>

        <div v-if="error" class="error-box">{{ error }}</div>

        <button type="submit" class="submit-btn" :disabled="loading || !isFormValid">
          <Loader2 v-if="loading" class="spin" />
          {{ loading ? 'güncelleniyor...' : 'şifremi güncelle' }}
        </button>
      </form>

      <!-- Success state -->
      <div v-if="success" class="success-state">
        <div class="success-icon">
          <CheckCircle class="icon" />
        </div>
        <h2>Şifreniz Güncellendi!</h2>
        <p>Şifreniz başarıyla değiştirildi. Artık yeni şifrenizle giriş yapabilirsiniz.</p>
        
        <router-link to="/" class="login-btn">
          Giriş Yap
        </router-link>
      </div>

      <div v-if="!success" class="back-link">
        <router-link to="/">
          <ArrowLeft class="icon-sm" />
          ana sayfaya dön
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Lock, Eye, EyeOff, Loader2, CheckCircle, XCircle, ArrowLeft, RefreshCw } from 'lucide-vue-next'
import { authApi } from '@/services/api'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const toast = useToast()

const form = reactive({
  password: '',
  confirmPassword: '',
})

const showPassword = ref(false)
const loading = ref(false)
const validating = ref(true)
const tokenValid = ref(false)
const error = ref('')
const success = ref(false)

const strengthLabels = {
  0: '',
  1: 'çok zayıf',
  2: 'zayıf',
  3: 'orta',
  4: 'güçlü',
}

const passwordStrength = computed(() => {
  const p = form.password
  if (!p) return 0
  let s = 0
  if (p.length >= 8) s++
  if (/[a-z]/.test(p) && /[A-Z]/.test(p)) s++
  if (/\d/.test(p)) s++
  if (/[^a-zA-Z0-9]/.test(p)) s++
  return s
})

const isFormValid = computed(() => {
  return form.password && 
         form.password === form.confirmPassword && 
         passwordStrength.value >= 2
})

onMounted(async () => {
  const token = route.params.token
  
  if (!token) {
    tokenValid.value = false
    validating.value = false
    return
  }

  try {
    const response = await authApi.validateResetToken(token)
    tokenValid.value = response.data.valid
  } catch (err) {
    tokenValid.value = false
  } finally {
    validating.value = false
  }
})

async function handleSubmit() {
  loading.value = true
  error.value = ''

  try {
    await authApi.resetPassword(route.params.token, form.password)
    success.value = true
    toast.success('Şifreniz başarıyla güncellendi!')
  } catch (err) {
    error.value = err.response?.data?.message || 'Şifre güncellenemedi. Lütfen tekrar deneyin.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.reset-password-page {
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

/* Validating State */
.validating-state {
  padding: 2rem 0;
}

.loading-icon {
  width: 48px;
  height: 48px;
  color: #d4c84a;
  margin: 0 auto 1rem;
  display: block;
}

.validating-state p {
  color: #888;
  font-size: 0.9rem;
}

/* Invalid Token State */
.invalid-token-state {
  padding: 1rem 0;
}

.error-icon .icon {
  width: 64px;
  height: 64px;
  color: #ef4444;
  display: block;
  margin: 0 auto 1rem;
}

.invalid-token-state h1 {
  font-size: 1.25rem;
  margin-bottom: 1rem;
}

.invalid-token-state p {
  font-size: 0.9rem;
  color: #888;
  line-height: 1.6;
  margin-bottom: 1.5rem;
}

.retry-link {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.25rem;
  background: #d4c84a;
  color: #0f0f1a;
  font-size: 0.9rem;
  font-weight: 600;
  text-decoration: none;
  border-radius: 8px;
  transition: all 0.15s;
}

.retry-link:hover {
  background: #e0d454;
}

/* Form Styles */
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

.toggle-password {
  padding: 0.5rem 0.75rem;
  background: none;
  border: none;
  color: #555;
  cursor: pointer;
}

.toggle-password:hover {
  color: #d4c84a;
}

.toggle-password svg {
  width: 18px;
  height: 18px;
}

/* Password Strength */
.password-strength { margin-top: 0.5rem; }
.strength-bar {
  height: 4px;
  background: #2a2a4a;
  border-radius: 2px;
  overflow: hidden;
  margin-bottom: 0.25rem;
}
.strength-fill { height: 100%; transition: all 0.2s; }
.strength-fill.level-1 { background: #ef4444; }
.strength-fill.level-2 { background: #f59e0b; }
.strength-fill.level-3 { background: #d4c84a; }
.strength-fill.level-4 { background: #10b981; }
.strength-text { font-size: 0.7rem; color: #666; }

.error-hint {
  display: block;
  font-size: 0.7rem;
  color: #ef4444;
  margin-top: 0.375rem;
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

.success-icon .icon {
  width: 64px;
  height: 64px;
  color: #10b981;
  display: block;
  margin: 0 auto 1rem;
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
  margin: 0 0 1.5rem;
}

.login-btn {
  display: inline-block;
  padding: 0.75rem 2rem;
  background: #d4c84a;
  color: #0f0f1a;
  font-size: 0.9rem;
  font-weight: 600;
  text-decoration: none;
  border-radius: 8px;
  transition: all 0.15s;
}

.login-btn:hover {
  background: #e0d454;
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
  .reset-password-page {
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
  
  .loading-icon {
    width: 40px;
    height: 40px;
  }
  
  .error-icon .icon {
    width: 56px;
    height: 56px;
  }
  
  .invalid-token-state h1 {
    font-size: 1.1rem;
  }
  
  .invalid-token-state p {
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
