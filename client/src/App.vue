<template>
  <div class="layout-wrapper">
    <div class="content-wrapper">
      <router-view />
    </div>
    <Footer />
  </div>
  <ToastNotification />
</template>

<script setup>
import ToastNotification from '@/components/ui/ToastNotification.vue'
import Footer from '@/components/layout/Footer.vue'
import { useThemeStore } from '@/stores/theme'

// Initialize theme on app start
const themeStore = useThemeStore()
</script>

<style>
/* Global Layout styles for Sticky Footer */
.layout-wrapper {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.content-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
}
</style>

