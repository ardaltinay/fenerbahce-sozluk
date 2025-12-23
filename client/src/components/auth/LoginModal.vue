<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content">
      <div class="modal-header">
        <h1>giriş yap</h1>
        <button class="close-btn" @click="$emit('close')"><X class="icon text-white" /></button>
      </div>

      <div class="modal-body">
        <form @submit.prevent="handleLogin">
          <div class="form-group">
            <label>kullanıcı adı</label>
            <div class="input-group">
              <User class="input-icon" />
              <input
                v-model="form.email"
                type="text"
                placeholder="kullanıcı adınızı giriniz"
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
            <router-link to="/sifremi-unuttum" class="forgot-link" @click="$emit('close')">şifremi unuttum</router-link>
          </div>

          <div v-if="error" class="error-box">{{ error }}</div>

          <button type="submit" class="submit-btn" :disabled="loading">
            <Loader2 v-if="loading" class="spin" />
            {{ loading ? 'giriş yapılıyor...' : 'giriş yap' }}
          </button>
        </form>

        <div class="divider"><span>veya</span></div>

        <button class="social-btn" @click="handleGoogleClick">
          <img src="https://www.google.com/favicon.ico" alt="Google" />
          google ile devam et
        </button>

        <p class="switch-link">
          hesabın yok mu? <a href="#" @click.prevent="$emit('switch-to-register')">kayıt ol</a>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Eye, EyeOff, Loader2, X } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'

const emit = defineEmits(['close', 'switch-to-register'])
const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()

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
      toast.success('Giriş başarılı!')
      emit('close')
      // router.push('/') // No need to push if already on page, or reload?
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

function handleGoogleClick() {
  toast.info('Google ile giriş yakında aktif olacak!')
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
  padding: 1rem;
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}

.modal-content {
  background: rgba(26, 26, 46, 0.95);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  width: 100%;
  max-width: 400px;
  border-radius: 16px;
  border: 1px solid rgba(212, 200, 74, 0.2);
  box-shadow: 0 10px 40px rgba(0,0,0,0.6);
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 1.5rem 0;
}

.modal-header h1 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #e0e0e0;
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  color: #888;
  cursor: pointer;
  padding: 0;
}
.close-btn:hover { color: #fff; }

.modal-body {
  padding: 1.5rem;
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
</style>
