import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUiStore = defineStore('ui', () => {
  const sidebarOpen = ref(true)
  const mobileMenuOpen = ref(false)
  const searchModalOpen = ref(false)
  const theme = ref('dark')

  function toggleSidebar() {
    sidebarOpen.value = !sidebarOpen.value
  }

  function toggleMobileMenu() {
    mobileMenuOpen.value = !mobileMenuOpen.value
  }

  function openSearchModal() {
    searchModalOpen.value = true
  }

  function closeSearchModal() {
    searchModalOpen.value = false
  }

  function setTheme(newTheme) {
    theme.value = newTheme
  }

  return {
    sidebarOpen,
    mobileMenuOpen,
    searchModalOpen,
    theme,
    toggleSidebar,
    toggleMobileMenu,
    openSearchModal,
    closeSearchModal,
    setTheme,
  }
})
