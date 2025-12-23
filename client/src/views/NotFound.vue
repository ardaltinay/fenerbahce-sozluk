<template>
  <div class="not-found-page">
    <div class="glass-container">
      <div class="error-code">404</div>
      <div class="error-icon">
        <AlertCircle class="icon-lg" />
      </div>
      <h2>sayfa bulunamadı</h2>
      <p>aradığınız sayfa kaldırılmış veya hiç var olmamış olabilir.</p>
      
      <router-link to="/" class="home-btn">
        <Home class="icon-sm" />
        ana sayfaya dön
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { AlertCircle, Home } from 'lucide-vue-next'
</script>

<style scoped>
.not-found-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  background: radial-gradient(circle at center, #1a1a2e 0%, #0d0d1a 100%);
}

.glass-container {
  background: rgba(26, 26, 46, 0.4);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(212, 200, 74, 0.1);
  border-radius: 24px;
  padding: 4rem 2rem;
  text-align: center;
  max-width: 450px;
  width: 100%;
}

.error-code {
  font-size: 6rem;
  font-weight: 800;
  color: rgba(212, 200, 74, 0.1);
  line-height: 1;
  margin-bottom: -2rem;
}

.error-icon {
  margin-bottom: 2rem;
}

.icon-lg {
  width: 64px;
  height: 64px;
  color: #d4c84a;
}

h2 {
  font-size: 1.75rem;
  color: #d4c84a;
  margin-bottom: 1rem;
}

p {
  color: #888;
  margin-bottom: 2.5rem;
  line-height: 1.6;
}

.home-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 2rem;
  background: #d4c84a;
  color: #1a1a2e;
  text-decoration: none;
  font-weight: 600;
  border-radius: 12px;
  transition: all 0.2s;
}

.home-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 32px rgba(212, 200, 74, 0.25);
}

.icon-sm {
  width: 18px;
  height: 18px;
}
</style>
