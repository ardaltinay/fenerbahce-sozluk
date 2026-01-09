<template>
  <Teleport to="body">
    <div v-if="modelValue" class="legal-modal-overlay" @click.self="close">
      <div class="legal-modal-content">
        <div class="legal-modal-header">
          <h2>Sözlük Formatı</h2>
          <button class="modal-close" @click="close">
            <X class="icon text-white" />
          </button>
        </div>
        <div class="legal-modal-body">
           <div class="legal-content">
            <h3>Sözlük Tanımı</h3>
            <p>Bu platform, Fenerbahçe taraftarlarının kulüp, tarih, kültür ve güncel gelişmeler hakkında bilgi, görüş ve yorumlarını sözlük formatında paylaşmaları amacıyla oluşturulmuştur. İçerikler kişisel görüş içerebilir; ancak sözlük yapısına, genel ahlaka ve platform kurallarına uygun olmalıdır.</p>

            <h3>Yazım Kuralları</h3>
            <ul>
              <li>entry'ler açık, anlaşılır ve türkçe dil bilgisi kurallarına uygun şekilde yazılmalıdır.</li>
              <li>Küfür, hakaret, aşağılayıcı ifadeler ve provokatif söylemler kesinlikle yasaktır.</li>
              <li>Kişilere, kurumlara veya topluluklara yönelik hedef gösterici içeriklere izin verilmez.</li>
              <li>spam ve tekrar eden içerikler yayınlanmaz.</li>
              <li>yazarlar tarafından herhangi bir reklam faaliyetine izin verilmez.</li>
            </ul>

            <h3>Yazım Formatı</h3>
            <ul>
              <li>Entry'ler başlıkla doğrudan ilgili olmalı, konu dışına çıkılmamalıdır.</li>
              <li>girilen entry'ler yorum yapılabilecek başlıklar hariç, tanım cümleleri içermelidir.</li>
              <li>sözlük formatına uygun anlatım tercih edilmelidir.</li>
              <li>Başlık tanımı yapılırken kişisel diyalog dili yerine açıklayıcı ve tarafsız bir anlatım esas alınır.</li>
            </ul>
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
.legal-content h3 { color: #e0e0e0; font-size: 0.9rem; margin: 1.25rem 0 0.75rem; }
.legal-content h3:first-child { margin-top: 0; }
.legal-content p { color: #b0b0b0; font-size: 0.8rem; margin-bottom: 0.75rem; line-height: 1.6; }
.legal-content ul { 
  color: #b0b0b0; 
  font-size: 0.8rem; 
  margin: 0; 
  padding-left: 1.25rem; 
  line-height: 1.8;
}
.legal-content li { margin-bottom: 0.5rem; }
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
