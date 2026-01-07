<template>
  <div class="avatar" :class="[size, { 'has-shadow': shadow }]">
    <User class="avatar-icon" />
  </div>
</template>

<script setup>
import { User } from 'lucide-vue-next'

const props = defineProps({
  username: {
    type: String,
    default: ''
  },
  size: {
    type: String,
    default: 'md',
    validator: (value) => ['sm', 'md', 'lg', 'xl'].includes(value)
  },
  shadow: {
    type: Boolean,
    default: true
  }
})
</script>

<style scoped>
.avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(212, 200, 74, 0.1);
  color: #d4c84a;
  border: 1px solid rgba(212, 200, 74, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}

.has-shadow {
  box-shadow: 0 0 15px rgba(212, 200, 74, 0.1);
}

.avatar:hover {
  background: rgba(212, 200, 74, 0.2);
  border-color: #d4c84a;
  box-shadow: 0 0 20px rgba(212, 200, 74, 0.25);
  color: #fff;
  transform: scale(1.05);
}

.avatar-icon {
  width: 60%;
  height: 60%;
  stroke-width: 2px;
}

/* Sizes */
.sm { width: 32px; height: 32px; }
.md { width: 40px; height: 40px; }
.lg { width: 56px; height: 56px; }
.xl { width: 80px; height: 80px; border-width: 2px; }
.xl .avatar-icon { width: 50%; height: 50%; stroke-width: 1.5px; }

</style>
