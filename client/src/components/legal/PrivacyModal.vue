<template>
  <Teleport to="body">
    <div v-if="modelValue" class="legal-modal-overlay" @click.self="close">
      <div class="legal-modal-content">
        <div class="legal-modal-header">
          <h2>Gizlilik Politikası</h2>
          <button class="modal-close" @click="close">
            <X class="icon text-white" />
          </button>
        </div>
        <div class="legal-modal-body">
          <div class="legal-content">
            <h3>1. TOPLANAN VERİLER</h3>
            <p>Platform'a kayıt olurken verilen e-posta adresi, kullanıcı adı ve şifre bilgileri ile site içindeki aktiviteleriniz (entryler, oylamalar) kayıt altına alınır.</p>

            <h3>2. VERİLERİN KULLANIMI</h3>
            <p>Toplanan bilgiler, üyelik işlemlerinin yürütülmesi, Platform güvenliğinin sağlanması ve yasal yükümlülüklerin yerine getirilmesi amacıyla kullanılır.</p>

            <h3>3. VERİ GÜVENLİĞİ</h3>
            <p>Kullanıcı bilgileri 3. şahıslarla paylaşılmaz (yasal zorunluluklar hariç) ve şifreleriniz veritabanında "hash"lenerek (şifrelenerek) saklanır.</p>

            <h3>4. ÇEREZLER (COOKIES)</h3>
            <p>Platform deneyimini iyileştirmek amacıyla çerezler kullanılabilir.</p>

            <h3>5. İLETİŞİM</h3>
            <p>Verilerinizle ilgili talepleriniz için yönetimle iletişime geçebilirsiniz.</p>
          </div>
        </div>
        <div class="legal-modal-footer">
          <button class="modal-btn secondary" @click="close">Kapat</button>
          <button v-if="canAccept" class="modal-btn primary" @click="accept">
              <Check class="icon-sm" /> Okudum, Kabul Ediyorum
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { X, Check } from 'lucide-vue-next'

const props = defineProps({
  modelValue: Boolean,
  canAccept: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'accept'])

function close() {
  emit('update:modelValue', false)
}

function accept() {
  emit('accept')
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
.legal-content h3 { color: #e0e0e0; font-size: 0.9rem; margin: 1rem 0 0.5rem; }
.legal-content p { color: #b0b0b0; font-size: 0.8rem; margin-bottom: 0.5rem; }
.legal-modal-footer {
  padding: 1rem 1.25rem; border-top: 1px solid #2a2a4a; display: flex; justify-content: flex-end; gap: 0.75rem;
}
.modal-btn {
  padding: 0.5rem 1rem; border-radius: 6px; font-size: 0.8rem; cursor: pointer; border: none; display: flex; align-items: center; gap: 0.4rem;
}
.modal-btn.secondary { background: transparent; color: #888; border: 1px solid #444; }
.modal-btn.secondary:hover { border-color: #888; color: #ccc; }
.modal-btn.primary { background: #d4c84a; color: #000; font-weight: 600; }
.modal-btn.primary:hover { background: #e0d454; }

.icon-sm { width: 14px; height: 14px; }
.icon.text-white { color: #fff; width: 20px; height: 20px; }
.modal-close { background: none; border: none; cursor: pointer; color: #888; display: flex; align-items: center; }
.modal-close:hover { color: #fff; }
</style>
