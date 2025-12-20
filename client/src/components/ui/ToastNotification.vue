<template>
  <Teleport to="body">
    <div class="toast-container">
      <TransitionGroup name="toast">
        <div 
          v-for="toast in toasts" 
          :key="toast.id" 
          class="toast"
          :class="toast.type"
          @click="remove(toast.id)"
        >
          <span class="toast-icon">
            <template v-if="toast.type === 'success'">✓</template>
            <template v-else-if="toast.type === 'error'">✕</template>
            <template v-else-if="toast.type === 'warning'">⚠</template>
            <template v-else>ℹ</template>
          </span>
          <span class="toast-message">{{ toast.message }}</span>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<script setup>
import { useToast } from '@/composables/useToast'

const { toasts, remove } = useToast()
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 70px;
  right: 20px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  pointer-events: none;
}

.toast {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.85rem 1.25rem;
  border-radius: 8px;
  background: #1a1a2e;
  border: 1px solid #2a2a4a;
  color: #e0e0e0;
  font-size: 0.9rem;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
  cursor: pointer;
  pointer-events: auto;
  min-width: 250px;
  max-width: 400px;
}

.toast.success {
  border-color: #3fb950;
  background: linear-gradient(135deg, #1a2e1a 0%, #1a1a2e 100%);
}

.toast.success .toast-icon {
  color: #3fb950;
}

.toast.error {
  border-color: #f85149;
  background: linear-gradient(135deg, #2e1a1a 0%, #1a1a2e 100%);
}

.toast.error .toast-icon {
  color: #f85149;
}

.toast.warning {
  border-color: #d4c84a;
  background: linear-gradient(135deg, #2e2a1a 0%, #1a1a2e 100%);
}

.toast.warning .toast-icon {
  color: #d4c84a;
}

.toast.info {
  border-color: #58a6ff;
  background: linear-gradient(135deg, #1a1a2e 0%, #1a2a3e 100%);
}

.toast.info .toast-icon {
  color: #58a6ff;
}

.toast-icon {
  font-size: 1.1rem;
  font-weight: bold;
}

.toast-message {
  flex: 1;
}

/* Animations */
.toast-enter-active {
  animation: slideIn 0.3s ease;
}

.toast-leave-active {
  animation: slideOut 0.25s ease;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(100%);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes slideOut {
  from {
    opacity: 1;
    transform: translateX(0);
  }
  to {
    opacity: 0;
    transform: translateX(100%);
  }
}
</style>
