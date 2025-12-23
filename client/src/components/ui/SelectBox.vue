<template>
  <div class="select-box" ref="containerRef">
    <div 
      class="selected-option" 
      :class="{ 'placeholder': !modelValue, 'open': isOpen, 'has-error': error }"
      @click="toggle"
    >
      <span>{{ selectedLabel }}</span>
      <ChevronDown class="icon" :class="{ 'rotate': isOpen }" />
    </div>

    <transition name="fade">
      <div v-if="isOpen" class="options-container">
        <div 
          v-for="option in options" 
          :key="option.id || option.value"
          class="option-item"
          :class="{ 'active': isSelected(option) }"
          @click="select(option)"
        >
          {{ option.name || option.label }}
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ChevronDown } from 'lucide-vue-next'

const props = defineProps({
  modelValue: {
    type: [String, Number, Object],
    default: null
  },
  options: {
    type: Array,
    required: true
  },
  placeholder: {
    type: String,
    default: 'Seçiniz'
  },
  error: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])
const containerRef = ref(null)
const isOpen = ref(false)

const selectedLabel = computed(() => {
  if (!props.modelValue) return props.placeholder
  const selected = props.options.find(opt => (opt.id || opt.value) === props.modelValue)
  return selected ? (selected.name || selected.label) : props.placeholder
})

function toggle() {
  isOpen.value = !isOpen.value
}

function select(option) {
  emit('update:modelValue', option.id || option.value)
  isOpen.value = false
}

function isSelected(option) {
  return props.modelValue === (option.id || option.value)
}

function handleClickOutside(e) {
  if (containerRef.value && !containerRef.value.contains(e.target)) {
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
.select-box {
  position: relative;
  width: 100%;
}

.selected-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  background: #0a0a14;
  border: 1px solid #2a2a4a;
  border-radius: 4px;
  cursor: pointer;
  color: #e0e0e0;
  font-size: 0.9rem;
  transition: all 0.2s;
}

.selected-option:hover {
  border-color: #d4c84a;
}

.selected-option.has-error {
  border-color: #e74c3c !important;
}

.selected-option.placeholder {
  color: #666;
}

.selected-option.open {
  border-color: #d4c84a;
  border-bottom-color: transparent;
  border-radius: 4px 4px 0 0;
}

.icon {
  width: 16px;
  height: 16px;
  color: #666;
  transition: transform 0.2s;
}

.icon.rotate {
  transform: rotate(180deg);
}

.options-container {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: #1a1a2e;
  border: 1px solid #d4c84a;
  border-top: none;
  border-radius: 0 0 4px 4px;
  max-height: 200px;
  overflow-y: auto;
  z-index: 50;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.5);
}

.option-item {
  padding: 0.75rem 1rem;
  color: #ccc;
  font-size: 0.9rem;
  cursor: pointer;
  transition: background 0.15s;
}

.option-item:hover {
  background: rgba(255, 237, 0, 0.1);
  color: #d4c84a;
}

.option-item.active {
  background: rgba(255, 237, 0, 0.05);
  color: #d4c84a;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
