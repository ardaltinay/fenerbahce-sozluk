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

      <!-- Login Form -->
      <div class="auth-card">
        <h1>giriş yap</h1>

        <form @submit.prevent="handleLogin">
          <div class="form-group">
            <label>e-posta veya kullanıcı adı</label>
            <div class="input-group">
              <User class="input-icon" />
              <input
                v-model="form.email"
                type="text"
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
          </div>

          <div class="form-row">
            <label class="checkbox">
              <input type="checkbox" v-model="form.remember" />
              <span>beni hatırla</span>
            </label>
            <router-link to="/sifremi-unuttum" class="forgot-link">şifremi unuttum</router-link>
          </div>

          <div v-if="error" class="error-box">{{ error }}</div>

          <button type="submit" class="submit-btn" :disabled="loading">
            <Loader2 v-if="loading" class="spin" />
            {{ loading ? 'giriş yapılıyor...' : 'giriş yap' }}
          </button>
        </form>

        <div class="divider"><span>veya</span></div>

        <button class="social-btn">
          <img src="https://www.google.com/favicon.ico" alt="Google" />
          google ile devam et
        </button>

        <p class="switch-link">
          hesabın yok mu? <router-link to="/kayit">kayıt ol</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Eye, EyeOff, Loader2 } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const form = reactive({
  email: '',
  password: '',
  remember: false,
})

const showPassword = ref(false)
const loading = ref(false)
const error = ref('')

async function handleLogin() {
  loading.value = true
  error.value = ''

  try {
    const result = await authStore.login({
      username: form.email,
      password: form.password,
    })
    
    if (result.success) {
      router.push('/')
    } else {
      error.value = typeof result.message === 'string' 
        ? result.message 
        : 'Giriş yapılamadı'
    }
  } catch (e) {
    error.value = e.message || 'Giriş yapılamadı'
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

.form-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.checkbox {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.8rem;
  color: #888;
  cursor: pointer;
}

.checkbox input {
  accent-color: #d4c84a;
}

.forgot-link {
  font-size: 0.8rem;
  color: #d4c84a;
  text-decoration: none;
}

.forgot-link:hover {
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
