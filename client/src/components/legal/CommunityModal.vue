<template>
  <Teleport to="body">
    <div v-if="modelValue" class="legal-modal-overlay" @click.self="close">
      <div class="legal-modal-content">
        <div class="legal-modal-header">
          <h2>Topluluk Yapısı</h2>
          <button class="modal-close" @click="close">
            <X class="icon text-white" />
          </button>
        </div>
        <div class="legal-modal-body">
           <div class="legal-content">
            <h3>Topluluk Yapısı ve Katılım Kuralı</h3>
            <p>Bu sözlük, Fenerbahçe Spor Kulübü taraftarları tarafından, taraftarlarının kendi aralarında paylaşım yapabilmesi amacıyla kurulmuştur. İçeriklerin, Fenerbahçe taraftarlık kültürünü bilen ve bu kimlikle katkı sunan kullanıcılar tarafından oluşturulması beklenir.</p>
            <p>Farklı takım taraftarlarının katılımı ya da bu yönde yapılan paylaşımlar, sözlüğün amacını korumak adına yayınlanmayacaktır.</p>
            <p class="thanks">Anlayışınız için teşekkür ederiz.</p>
          </div>
        </div>
        <div class="legal-modal-footer">
          <button class="modal-btn secondary" @click="close">Kapat</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { X } from 'lucide-vue-next'

defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue'])

function close() {
  emit('update:modelValue', false)
}
</script>

<style scoped>
.legal-modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2100;
  padding: 1rem;
  backdrop-filter: blur(2px);
}
.legal-modal-content {
  background: #1a1a2e;
  border: 1px solid #2a2a4a;
  border-radius: 12px;
  width: 100%;
  max-width: 500px;
  max-height: 70vh;
  display: flex;
  flex-direction: column;
}
.legal-modal-header {
  display: flex; justify-content: space-between; align-items: center; padding: 1rem 1.25rem; border-bottom: 1px solid #2a2a4a;
}
.legal-modal-header h2 { font-size: 1rem; color: #d4c84a; margin: 0; }
.legal-modal-body { padding: 1.25rem; overflow-y: auto; }
.legal-content h3 { color: #e0e0e0; font-size: 0.9rem; margin: 0 0 1rem; }
.legal-content p { color: #b0b0b0; font-size: 0.8rem; margin-bottom: 0.75rem; line-height: 1.6; }
.legal-content p.thanks { color: #d4c84a; font-style: italic; margin-top: 1rem; }
.legal-modal-footer {
  padding: 1rem 1.25rem; border-top: 1px solid #2a2a4a; display: flex; justify-content: flex-end; gap: 0.75rem;
}
.modal-btn {
  padding: 0.5rem 1rem; border-radius: 6px; font-size: 0.8rem; cursor: pointer; border: none;
}
.modal-btn.secondary { background: transparent; color: #888; border: 1px solid #444; }
.modal-btn.secondary:hover { border-color: #888; color: #ccc; }

.icon.text-white { color: #fff; width: 20px; height: 20px; }
.modal-close { background: none; border: none; cursor: pointer; color: #888; display: flex; align-items: center; }
.modal-close:hover { color: #fff; }
</style>
