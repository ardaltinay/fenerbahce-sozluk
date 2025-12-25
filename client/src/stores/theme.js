import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const theme = ref(localStorage.getItem('theme') || 'dark')

  function toggleTheme() {
    theme.value = theme.value === 'dark' ? 'light' : 'dark'
    localStorage.setItem('theme', theme.value)
    applyTheme()
  }

  function applyTheme() {
    document.documentElement.setAttribute('data-theme', theme.value)
  }

  // Apply theme on init
  applyTheme()

  return {
    theme,
    toggleTheme,
    applyTheme,
  }
})
