<template>
  <div class="relative w-full" ref="containerRef">
    <div 
      class="select-trigger" 
      :class="{ 'is-open': isOpen }"
      @click="toggle"
    >
      <span v-if="selectedLabel" class="selected-text">{{ selectedLabel }}</span>
      <span v-else class="placeholder">{{ placeholder || 'Seçiniz' }}</span>
      
      <svg 
        xmlns="http://www.w3.org/2000/svg" 
        width="16" 
        height="16" 
        viewBox="0 0 24 24" 
        fill="none" 
        stroke="currentColor" 
        stroke-width="2" 
        stroke-linecap="round" 
        stroke-linejoin="round"
        class="chevron"
        :class="{ 'rotate-180': isOpen }"
      >
        <path d="m6 9 6 6 6-6"/>
      </svg>
    </div>

    <div v-show="isOpen" class="select-dropdown">
      <div 
        v-for="option in options" 
        :key="option.value"
        class="select-option"
        :class="{ 'is-selected': modelValue === option.value }"
        @click="select(option)"
      >
        {{ option.label }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  modelValue: {
    type: [String, Number],
    default: null
  },
  options: {
    type: Array,
    required: true,
    // Expected format: [{ label: 'Option 1', value: 1 }]
  },
  placeholder: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue'])

const isOpen = ref(false)
const containerRef = ref(null)

const selectedLabel = computed(() => {
  const selected = props.options.find(opt => opt.value === props.modelValue)
  return selected ? selected.label : null
})

function toggle() {
  isOpen.value = !isOpen.value
}

function select(option) {
  emit('update:modelValue', option.value)
  isOpen.value = false
}

function handleClickOutside(event) {
  if (containerRef.value && !containerRef.value.contains(event.target)) {
    isOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.select-trigger {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 0.75rem;
  background: #0d0d1a;
  border: 1px solid #2a2a4a;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.select-trigger:hover {
  border-color: #d4c84a;
}

.select-trigger.is-open {
  border-color: #d4c84a;
  box-shadow: 0 0 0 3px rgba(212, 200, 74, 0.1);
}

.selected-text {
  color: #e0e0e0;
  font-size: 0.9rem;
}

.placeholder {
  color: #666;
  font-size: 0.9rem;
}

.chevron {
  color: #888;
  transition: transform 0.2s;
}

.rotate-180 {
  transform: rotate(180deg);
}

.select-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 0.5rem;
  background: #1a1a2e;
  border: 1px solid #2a2a4a;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  z-index: 50;
  max-height: 200px;
  overflow-y: auto;
}

.select-option {
  padding: 0.75rem;
  color: #bbb;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.15s;
}

.select-option:hover {
  background: rgba(212, 200, 74, 0.1);
  color: #fff;
}

.select-option.is-selected {
  background: rgba(212, 200, 74, 0.2);
  color: #d4c84a;
  font-weight: 600;
}
</style>
