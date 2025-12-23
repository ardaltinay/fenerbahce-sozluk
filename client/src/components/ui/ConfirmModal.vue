<template>
  <div v-if="show" class="confirm-modal-overlay" @click.self="handleCancel">
    <div class="confirm-modal-content">
      <div class="confirm-icon" :class="type">
        <HelpCircle v-if="type === 'confirm'" />
        <AlertTriangle v-if="type === 'warning' || type === 'danger'" />
        <Info v-if="type === 'info'" />
      </div>
      
      <h3>{{ title }}</h3>
      <p>{{ message }}</p>
      
      <div class="confirm-actions">
        <button class="btn-cancel" @click="handleCancel">{{ cancelText }}</button>
        <button class="btn-confirm" :class="type" @click="handleConfirm">{{ confirmText }}</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { HelpCircle, AlertTriangle, Info } from 'lucide-vue-next'

const props = defineProps({
  show: Boolean,
  title: {
    type: String,
    default: 'emin misiniz?'
  },
  message: String,
  confirmText: {
    type: String,
    default: 'evet'
  },
  cancelText: {
    type: String,
    default: 'iptal'
  },
  type: {
    type: String,
    default: 'confirm' // confirm, warning, danger, info
  }
})

const emit = defineEmits(['confirm', 'cancel'])

function handleConfirm() {
  emit('confirm')
}

function handleCancel() {
  emit('cancel')
}
</script>

<style scoped>
.confirm-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3000;
  backdrop-filter: blur(8px);
}

.confirm-modal-content {
  background: rgba(26, 26, 46, 0.95);
  border: 1px solid rgba(212, 200, 74, 0.2);
  border-radius: 20px;
  padding: 2.5rem;
  max-width: 400px;
  width: 90%;
  text-align: center;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.8);
}

.confirm-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 1.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(212, 200, 74, 0.1);
  color: #d4c84a;
}

.confirm-icon :deep(svg) {
  width: 32px;
  height: 32px;
}

.confirm-icon.danger {
  background: rgba(248, 81, 73, 0.1);
  color: #f85149;
}

.confirm-icon.warning {
  background: rgba(212, 200, 74, 0.1);
  color: #d4c84a;
}

h3 {
  color: #fff;
  font-size: 1.25rem;
  margin-bottom: 0.75rem;
}

p {
  color: #888;
  font-size: 1rem;
  line-height: 1.5;
  margin-bottom: 2rem;
}

.confirm-actions {
  display: flex;
  gap: 1rem;
}

.btn-cancel, .btn-confirm {
  flex: 1;
  padding: 0.85rem;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 0.95rem;
}

.btn-cancel {
  background: rgba(255, 255, 255, 0.05);
  color: #888;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.btn-cancel:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.btn-confirm {
  background: #d4c84a;
  color: #1a1a2e;
  border: none;
}

.btn-confirm:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(212, 200, 74, 0.3);
}

.btn-confirm.danger {
  background: #f85149;
  color: #fff;
}

.btn-confirm.danger:hover {
  box-shadow: 0 4px 15px rgba(248, 81, 73, 0.3);
}
</style>
