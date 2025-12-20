<template>
  <footer class="glass border-t border-fb-yellow/20 mt-auto">
    <div class="max-w-7xl mx-auto px-4 py-8">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-8">
        <!-- Brand -->
        <div class="md:col-span-1">
          <div class="flex items-center gap-3 mb-4">
            <div class="w-10 h-10 rounded-lg bg-fb-yellow flex items-center justify-center">
              <span class="text-fb-navy font-bold text-xl">FB</span>
            </div>
            <div>
              <h3 class="text-lg font-bold text-fb-yellow">fenerbahçe sözlük</h3>
            </div>
          </div>
          <p class="text-sm text-text-muted mb-4">
            fenerbahçeliler için fenerbahçeliler tarafından oluşturulan özgür sözlük platformu.
          </p>
          <div class="flex gap-3">
            <a href="#" class="btn btn-ghost p-2">
              <Twitter class="w-5 h-5" />
            </a>
            <a href="#" class="btn btn-ghost p-2">
              <Instagram class="w-5 h-5" />
            </a>
            <a href="#" class="btn btn-ghost p-2">
              <Youtube class="w-5 h-5" />
            </a>
          </div>
        </div>

        <!-- Links -->
        <div>
          <h4 class="text-sm font-semibold text-fb-yellow mb-4">keşfet</h4>
          <nav class="space-y-2">
            <router-link to="/" class="block text-sm text-text-muted hover:text-fb-yellow transition-colors">
              gündem
            </router-link>
            <router-link to="/debe" class="block text-sm text-text-muted hover:text-fb-yellow transition-colors">
              debe
            </router-link>
            <router-link to="/kanallar" class="block text-sm text-text-muted hover:text-fb-yellow transition-colors">
              kanallar
            </router-link>
            <router-link to="/rastgele" class="block text-sm text-text-muted hover:text-fb-yellow transition-colors">
              rastgele
            </router-link>
          </nav>
        </div>

        <div>
          <h4 class="text-sm font-semibold text-fb-yellow mb-4">topluluk</h4>
          <nav class="space-y-2">
            <router-link to="/kurallar" class="block text-sm text-text-muted hover:text-fb-yellow transition-colors">
              kurallar
            </router-link>
            <router-link to="/yardim" class="block text-sm text-text-muted hover:text-fb-yellow transition-colors">
              yardım
            </router-link>
            <router-link to="/iletisim" class="block text-sm text-text-muted hover:text-fb-yellow transition-colors">
              iletişim
            </router-link>
            <router-link to="/moderatorler" class="block text-sm text-text-muted hover:text-fb-yellow transition-colors">
              moderatörler
            </router-link>
          </nav>
        </div>

        <div>
          <h4 class="text-sm font-semibold text-fb-yellow mb-4">yasal</h4>
          <nav class="space-y-2">
            <router-link to="/gizlilik" class="block text-sm text-text-muted hover:text-fb-yellow transition-colors">
              gizlilik politikası
            </router-link>
            <router-link to="/kullanim-kosullari" class="block text-sm text-text-muted hover:text-fb-yellow transition-colors">
              kullanım koşulları
            </router-link>
            <router-link to="/cerez-politikasi" class="block text-sm text-text-muted hover:text-fb-yellow transition-colors">
              çerez politikası
            </router-link>
          </nav>
        </div>
      </div>

      <!-- Bottom Bar -->
      <div class="mt-8 pt-8 border-t border-fb-yellow/10">
        <div class="flex flex-col md:flex-row justify-between items-center gap-4">
          <p class="text-sm text-text-muted">
            © {{ currentYear }} fenerbahçe sözlük. tüm hakları saklıdır.
          </p>
          <p class="text-sm text-text-muted flex items-center gap-2">
            <Heart class="w-4 h-4 text-fb-yellow" />
            <span>sarı lacivert sevgisiyle yapıldı</span>
          </p>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { computed } from 'vue'
import { Twitter, Instagram, Youtube, Heart } from 'lucide-vue-next'

const currentYear = computed(() => new Date().getFullYear())
</script>
