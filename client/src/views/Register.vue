<template>
  <div class="auth-page">
    <div class="auth-container">
      <!-- Logo -->
      <router-link to="/" class="logo">
        <img src="/icon.png" alt="Fenerbahçe Sözlük" class="logo-icon" />
        <div class="logo-text">
          <span class="logo-title">fenerbahçe sözlük</span>
          <span class="logo-subtitle">kutlu adın yüreklerde</span>
        </div>
      </router-link>

      <!-- Register Form -->
      <div class="auth-card">
        <h1>kayıt ol</h1>

        <form @submit.prevent="handleRegister">
          <div class="form-group">
            <label>kullanıcı adı</label>
            <div class="input-group">
              <User class="input-icon" />
              <input
                v-model="form.username"
                type="text"
                placeholder="sari_kanarya"
                required
              />
            </div>
            <span class="hint">sadece harf, rakam ve alt çizgi</span>
          </div>

          <div class="form-group">
            <label>e-posta</label>
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

          <div class="form-group">
            <label>şifre</label>
            <div class="input-group">
              <Lock class="input-icon" />
              <input
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="••••••••"
                required
              />
              <button type="button" class="toggle-password" @click="showPassword = !showPassword">
                <EyeOff v-if="showPassword" />
                <Eye v-else />
              </button>
            </div>
            <!-- Şifre Gücü -->
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

          <label class="checkbox">
            <input type="checkbox" v-model="form.acceptTerms" required />
            <span>
              <router-link to="/kosullar">kullanım koşullarını</router-link> ve 
              <router-link to="/gizlilik">gizlilik politikasını</router-link> kabul ediyorum
            </span>
          </label>

          <div v-if="error" class="error-box">{{ error }}</div>

          <button type="submit" class="submit-btn" :disabled="loading || !isFormValid">
            <Loader2 v-if="loading" class="spin" />
            {{ loading ? 'kayıt yapılıyor...' : 'kayıt ol' }}
          </button>
        </form>

        <div class="divider"><span>veya</span></div>

        <button class="social-btn">
          <img src="https://www.google.com/favicon.ico" alt="Google" />
          google ile kayıt ol
        </button>

        <p class="switch-link">
          zaten hesabın var mı? <router-link to="/giris">giriş yap</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { User, Mail, Lock, Eye, EyeOff, Loader2 } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const form = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  acceptTerms: false,
})

const showPassword = ref(false)
const loading = ref(false)
const error = ref('')

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
  return form.username && 
         form.email && 
         form.password && 
         form.password === form.confirmPassword && 
         form.acceptTerms && 
         passwordStrength.value >= 2
})

async function handleRegister() {
  loading.value = true
  error.value = ''

  try {
    const result = await authStore.register({
      username: form.username,
      email: form.email,
      password: form.password,
    })
    
    if (result.success) {
      router.push('/')
    } else {
      // Handle validation errors object or string message
      if (typeof result.message === 'object') {
        error.value = Object.values(result.message).join(', ')
      } else {
        error.value = result.message || 'Kayıt yapılamadı'
      }
    }
  } catch (e) {
    error.value = e.message || 'Kayıt yapılamadı'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem 1rem;
  background: #0f0f1a;
}

.auth-container {
  width: 100%;
  max-width: 400px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  text-decoration: none;
  margin-bottom: 2rem;
  justify-content: center;
}

.logo-icon {
  width: 48px;
  height: 48px;
  object-fit: contain;
}

.logo-text {
  display: flex;
  flex-direction: column;
}

.logo-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: #d4c84a;
}

.logo-subtitle {
  font-size: 0.75rem;
  color: #666;
}

.auth-card {
  background: #1a1a2e;
  border: 1px solid #2a2a4a;
  border-radius: 16px;
  padding: 2rem;
}

.auth-card h1 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #e0e0e0;
  text-align: center;
  margin: 0 0 1.5rem;
}

.form-group {
  margin-bottom: 1.25rem;
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

.hint {
  display: block;
  font-size: 0.7rem;
  color: #555;
  margin-top: 0.375rem;
}

.error-hint {
  display: block;
  font-size: 0.7rem;
  color: #ef4444;
  margin-top: 0.375rem;
}

.password-strength {
  margin-top: 0.5rem;
}

.strength-bar {
  height: 4px;
  background: #2a2a4a;
  border-radius: 2px;
  overflow: hidden;
  margin-bottom: 0.25rem;
}

.strength-fill {
  height: 100%;
  transition: all 0.2s;
}

.strength-fill.level-1 { background: #ef4444; }
.strength-fill.level-2 { background: #f59e0b; }
.strength-fill.level-3 { background: #d4c84a; }
.strength-fill.level-4 { background: #10b981; }

.strength-text {
  font-size: 0.7rem;
  color: #666;
}

.checkbox {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  font-size: 0.8rem;
  color: #888;
  margin-bottom: 1.5rem;
  line-height: 1.4;
}

.checkbox input {
  margin-top: 2px;
  accent-color: #d4c84a;
}

.checkbox a {
  color: #d4c84a;
  text-decoration: none;
}

.checkbox a:hover {
  text-decoration: underline;
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

.divider {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin: 1.5rem 0;
  color: #555;
  font-size: 0.8rem;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #2a2a4a;
}

.social-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  padding: 0.875rem;
  background: transparent;
  border: 1px solid #2a2a4a;
  border-radius: 8px;
  color: #e0e0e0;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.15s;
}

.social-btn:hover {
  border-color: #d4c84a;
  color: #d4c84a;
}

.social-btn img {
  width: 18px;
  height: 18px;
}

.switch-link {
  text-align: center;
  margin-top: 1.5rem;
  font-size: 0.85rem;
  color: #888;
}

.switch-link a {
  color: #d4c84a;
  text-decoration: none;
  font-weight: 500;
}

.switch-link a:hover {
  text-decoration: underline;
}

@media (max-width: 480px) {
  .auth-card {
    padding: 1.5rem;
  }
}
</style>
