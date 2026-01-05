<template>
  <div v-if="totalPages > 1" class="pagination">
    <button 
      class="page-btn nav-btn"
      :disabled="currentPage === 0"
      @click="$emit('change', 0)"
      title="ilk sayfa"
    >
      <ChevronFirst class="icon" />
    </button>
    
    <button 
      class="page-btn nav-btn"
      :disabled="currentPage === 0"
      @click="$emit('change', currentPage - 1)"
      title="önceki"
    >
      <ChevronLeft class="icon" />
    </button>
    
    <div class="page-numbers">
      <button 
        v-for="page in visiblePages" 
        :key="page"
        class="page-btn number-btn"
        :class="{ active: page === currentPage }"
        @click="$emit('change', page)"
      >
        {{ page + 1 }}
      </button>
    </div>
    
    <button 
      class="page-btn nav-btn"
      :disabled="currentPage >= totalPages - 1"
      @click="$emit('change', currentPage + 1)"
      title="sonraki"
    >
      <ChevronRight class="icon" />
    </button>
    
    <button 
      class="page-btn nav-btn"
      :disabled="currentPage >= totalPages - 1"
      @click="$emit('change', totalPages - 1)"
      title="son sayfa"
    >
      <ChevronLast class="icon" />
    </button>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ChevronLeft, ChevronRight, ChevronFirst, ChevronLast } from 'lucide-vue-next'

const props = defineProps({
  currentPage: {
    type: Number,
    required: true
  },
  totalPages: {
    type: Number,
    required: true
  },
  maxVisible: {
    type: Number,
    default: 5
  }
})

defineEmits(['change'])

const visiblePages = computed(() => {
  const pages = []
  const half = Math.floor(props.maxVisible / 2)
  
  let start = Math.max(0, props.currentPage - half)
  let end = Math.min(props.totalPages - 1, start + props.maxVisible - 1)
  
  // Adjust start if we're near the end
  if (end - start < props.maxVisible - 1) {
    start = Math.max(0, end - props.maxVisible + 1)
  }
  
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  
  return pages
})
</script>

<style scoped>
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 1rem 0;
}

.page-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 36px;
  height: 36px;
  padding: 0 0.5rem;
  border: 1px solid rgba(255, 237, 0, 0.15);
  border-radius: 8px;
  background: rgba(26, 26, 46, 0.6);
  color: var(--text-secondary, #888);
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.page-btn:hover:not(:disabled) {
  background: rgba(212, 200, 74, 0.15);
  border-color: rgba(255, 237, 0, 0.3);
  color: var(--accent, #d4c84a);
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-btn.active {
  background: linear-gradient(135deg, var(--accent, #d4c84a), #b8a832);
  border-color: var(--accent, #d4c84a);
  color: #0d0d1a;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(212, 200, 74, 0.3);
}

.nav-btn .icon {
  width: 16px;
  height: 16px;
}

.page-numbers {
  display: flex;
  gap: 0.25rem;
}

/* Mobile responsive */
@media (max-width: 480px) {
  .pagination {
    gap: 0.25rem;
  }
  
  .page-btn {
    min-width: 32px;
    height: 32px;
    font-size: 0.75rem;
  }
  
  .nav-btn .icon {
    width: 14px;
    height: 14px;
  }
  
  .page-numbers {
    display: flex;
    gap: 0.15rem;
  }
  
  .number-btn {
    min-width: 28px;
    height: 28px;
  }
}
</style>
