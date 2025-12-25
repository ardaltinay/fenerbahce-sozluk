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
  padding: 4rem 3rem;
  text-align: center;
  max-width: 450px;
  width: 100%;
}

.error-code {
  font-size: 5rem;
  font-weight: 800;
  color: rgba(212, 200, 74, 0.15);
  line-height: 1;
  margin-bottom: 0.5rem;
}

.error-icon {
  display: flex;
  justify-content: center;
  margin-bottom: 1.5rem;
}

.icon-lg {
  width: 56px;
  height: 56px;
  color: #d4c84a;
}

h2 {
  font-size: 1.5rem;
  color: #d4c84a;
  margin: 0 0 0.75rem;
}

p {
  color: #888;
  margin: 0 0 2rem;
  line-height: 1.6;
  font-size: 0.95rem;
}

.home-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.75rem;
  background: #d4c84a;
  color: #1a1a2e;
  text-decoration: none;
  font-weight: 600;
  border-radius: 10px;
  font-size: 0.9rem;
  transition: all 0.2s;
}

.home-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 32px rgba(212, 200, 74, 0.25);
}

.icon-sm {
  width: 16px;
  height: 16px;
}

/* Mobile responsive */
@media (max-width: 480px) {
  .not-found-page {
    padding: 1rem;
  }
  
  .glass-container {
    padding: 2.5rem 1.5rem;
    border-radius: 16px;
  }
  
  .error-code {
    font-size: 4rem;
  }
  
  .icon-lg {
    width: 48px;
    height: 48px;
  }
  
  h2 {
    font-size: 1.25rem;
  }
  
  p {
    font-size: 0.875rem;
  }
  
  .home-btn {
    padding: 0.65rem 1.5rem;
    font-size: 0.85rem;
  }
}
</style>
