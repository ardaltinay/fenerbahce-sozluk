<template>
  <router-view />
  <ToastNotification />
</template>

<script setup>
import ToastNotification from '@/components/ui/ToastNotification.vue'
import { useThemeStore } from '@/stores/theme'

// Initialize theme on app start
const themeStore = useThemeStore()
</script>
