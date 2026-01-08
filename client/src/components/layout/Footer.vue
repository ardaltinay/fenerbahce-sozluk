<template>
  <footer class="footer">
    <div class="footer-container">
      <!-- Left Side: Brand & Copyright -->
      <div class="footer-left">
        <div class="logo-area">
          <img src="/icon.png" alt="Fenerbahçe Sözlük" class="footer-logo" />
        </div>
        <span class="divider">|</span>
        <span class="copyright-text">
          &copy; {{ new Date().getFullYear() }} tüm hakları saklıdır.
        </span>
      </div>

      <!-- Right Side: Links & Social -->
      <div class="footer-right">
        <nav class="footer-nav">
          <router-link to="/iletisim">iletişim</router-link>
          <a href="#" @click.prevent="showTerms = true">kurallar</a>
          <a href="#" @click.prevent="showPrivacy = true">gizlilik</a>
        </nav>
        
        <div class="separator"></div>

        <div class="social-icons">
          <a href="#" @click.prevent="showComingSoon('x (twitter)')" class="social-link" title="x (twitter)">
            <!-- X Logo SVG -->
            <svg viewBox="0 0 24 24" aria-hidden="true" class="icon fill-current">
              <path d="M18.244 2.25h3.308l-7.227 8.26 8.502 11.24H16.17l-5.214-6.817L4.99 21.75H1.68l7.73-8.835L1.254 2.25H8.08l4.713 6.231zm-1.161 17.52h1.833L7.084 4.126H5.117z"></path>
            </svg>
          </a>
          <a href="#" @click.prevent="showComingSoon('instagram')" class="social-link instagram">
            <Instagram class="icon" />
          </a>
        </div>
      </div>
    </div>

    <!-- Modals -->
    <TermsModal v-model="showTerms" />
    <PrivacyModal v-model="showPrivacy" />
  </footer>
</template>

<script setup>
import { ref } from 'vue'
import { Instagram } from 'lucide-vue-next'
import TermsModal from '@/components/legal/TermsModal.vue'
import PrivacyModal from '@/components/legal/PrivacyModal.vue'

import { useToast } from '@/composables/useToast'

const showTerms = ref(false)
const showPrivacy = ref(false)
const toast = useToast()

function showComingSoon(platform) {
  toast.info(`${platform} hesabımız yakında açılacaktır`)
}
</script>

<style scoped>
.footer {
  background: rgba(13, 13, 26, 0.6);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  padding: 10px 0;
  margin-top: auto;
  width: 100%;
  z-index: 10;
}

.footer-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* Left Side */
.footer-left {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.footer-logo {
  width: 20px;
  height: 20px;
  opacity: 0.9;
}

.divider {
  color: rgba(255, 255, 255, 0.2);
  font-size: 0.8rem;
}

.copyright-text {
  font-size: 0.75rem;
  color: #888;
}

/* Right Side */
.footer-right {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.footer-nav {
  display: flex;
  gap: 1.25rem;
}

.footer-nav a {
  color: #888;
  text-decoration: none;
  font-size: 0.8rem;
  transition: color 0.2s;
}

.footer-nav a:hover {
  color: #d4c84a;
}

.separator {
  width: 1px;
  height: 20px;
  background: rgba(255, 255, 255, 0.1);
}

.social-icons {
  display: flex;
  gap: 0.75rem;
}

.social-link {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 50%;
  color: #ccc;
  transition: all 0.2s;
}

.social-link:hover {
  background: #d4c84a;
  color: #1a1a2e;
  transform: translateY(-2px);
}

.icon { width: 16px; height: 16px; }
.fill-current { fill: currentColor; }


/* Mobile */
@media (max-width: 768px) {
  .footer-container {
    flex-direction: column;
    gap: 1.5rem;
    text-align: center;
  }
  
  .footer-left {
    align-items: center;
  }
  
  .copyright-text {
    margin-left: 0;
  }
  
  .footer-right {
    flex-direction: column;
    gap: 1rem;
  }
  
  .separator { display: none; }
  
  .footer-nav {
    gap: 1rem;
  }
}
</style>
