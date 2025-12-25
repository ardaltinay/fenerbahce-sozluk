<template>
  <div v-if="data" class="transfermarkt-card">
    <!-- Player Card -->
    <template v-if="type === 'player'">
      <div class="player-card">
        <div class="player-image">
          <img v-if="data.imageUrl" :src="data.imageUrl" :alt="data.name" />
          <div v-else class="placeholder-image">
            <User class="icon" />
          </div>
        </div>
        <div class="player-info">
          <h3 class="player-name">{{ data.name }}</h3>
          <div class="player-details">
            <div class="detail-row">
              <span class="label">Takım:</span>
              <span class="value">{{ data.club?.name || '-' }}</span>
            </div>
            <div class="detail-row">
              <span class="label">Mevki:</span>
              <span class="value">{{ data.position?.main || '-' }}</span>
            </div>
            <div class="detail-row">
              <span class="label">Yaş:</span>
              <span class="value">{{ getAge(data) || '-' }}</span>
            </div>
            <div class="detail-row">
              <span class="label">Boy:</span>
              <span class="value">{{ data.height ? (data.height / 100).toFixed(2) + ' m' : '-' }}</span>
            </div>
            <div class="detail-row">
              <span class="label">Uyruk:</span>
              <span class="value">{{ data.citizenship?.join(', ') || '-' }}</span>
            </div>
            <div v-if="data.marketValue" class="detail-row market-value">
              <span class="label">Piyasa Değeri:</span>
              <span class="value accent">{{ formatMarketValue(data.marketValue) }}</span>
            </div>
          </div>
        </div>
        <a 
          :href="getTransfermarktUrl(data.url)" 
          target="_blank" 
          class="tm-link"
          title="Transfermarkt'ta görüntüle"
        >
          <ExternalLink class="icon-sm" />
        </a>
      </div>
    </template>

    <!-- Club Card -->
    <template v-else-if="type === 'club'">
      <div class="club-card">
        <div class="club-image">
          <img v-if="data.image" :src="data.image" :alt="data.name" />
        </div>
        <div class="club-info">
          <h3 class="club-name">{{ data.name }}</h3>
          <div class="club-details">
            <div class="detail-row">
              <span class="label">Stadyum:</span>
              <span class="value">{{ data.stadiumName || '-' }}</span>
            </div>
            <div class="detail-row">
              <span class="label">Kapasite:</span>
              <span class="value">{{ data.stadiumSeats?.toLocaleString() || '-' }}</span>
            </div>
            <div class="detail-row">
              <span class="label">Kadro:</span>
              <span class="value">{{ data.squad?.size || '-' }} oyuncu</span>
            </div>
            <div v-if="data.currentMarketValue" class="detail-row market-value">
              <span class="label">Toplam Değer:</span>
              <span class="value accent">{{ formatMarketValue(data.currentMarketValue) }}</span>
            </div>
          </div>
        </div>
        <a 
          :href="getTransfermarktUrl(data.url)" 
          target="_blank" 
          class="tm-link"
        >
          <ExternalLink class="icon-sm" />
        </a>
      </div>
    </template>

    <div class="tm-badge">
      <span>Transfermarkt</span>
    </div>
  </div>

  <!-- Loading State -->
  <div v-else-if="loading" class="transfermarkt-card loading">
    <Loader2 class="spin" />
    <span>Bilgi yükleniyor...</span>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { User, ExternalLink, Loader2 } from 'lucide-vue-next'
import { transfermarktApi } from '@/services/api'

const props = defineProps({
  type: {
    type: String,
    required: true,
    validator: (v) => ['player', 'club'].includes(v)
  },
  transfermarktId: {
    type: String,
    required: true
  }
})

const data = ref(null)
const loading = ref(true)

function formatMarketValue(value) {
  if (!value) return '-'
  if (value >= 1000000) {
    return '€' + (value / 1000000).toFixed(1) + 'M'
  }
  if (value >= 1000) {
    return '€' + (value / 1000).toFixed(0) + 'K'
  }
  return '€' + value
}

function getTransfermarktUrl(url) {
  if (!url) return 'https://www.transfermarkt.com'
  if (url.startsWith('http')) return url
  return `https://www.transfermarkt.com${url}`
}

function getAge(player) {
  if (!player) return null
  if (player.age) return player.age
  // Parse age from description: "Name, 31, from Country..."
  if (player.description) {
    const match = player.description.match(/,\s*(\d{1,2}),\s*from/)
    if (match) return match[1]
  }
  return null
}

async function fetchData() {
  data.value = null  // Clear old data immediately
  loading.value = true
  try {
    let response
    if (props.type === 'player') {
      response = await transfermarktApi.getPlayer(props.transfermarktId)
    } else {
      response = await transfermarktApi.getClub(props.transfermarktId)
    }
    data.value = response.data
  } catch (err) {
    console.error('Transfermarkt fetch error:', err)
    data.value = null
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)

watch(() => props.transfermarktId, (newId, oldId) => {
  if (newId && newId !== oldId) {
    fetchData()
  }
})
</script>

<style scoped>
.transfermarkt-card {
  background: var(--entry-bg, rgba(26, 26, 46, 0.45));
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border: 1px solid var(--border, rgba(255, 237, 0, 0.1));
  border-radius: 12px;
  padding: 1rem;
  margin-bottom: 1rem;
  position: relative;
}

.transfermarkt-card.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 2rem;
  color: var(--text-secondary, #888);
}

.spin {
  animation: spin 1s linear infinite;
  width: 20px;
  height: 20px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.player-card, .club-card {
  display: flex;
  gap: 1rem;
  align-items: flex-start;
}

.player-image, .club-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  background: rgba(0, 0, 0, 0.2);
}

.player-image img, .club-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.placeholder-image {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.3);
}

.placeholder-image .icon {
  width: 32px;
  height: 32px;
  color: #555;
}

.player-info, .club-info {
  flex: 1;
  min-width: 0;
}

.player-name, .club-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--accent, #d4c84a);
  margin: 0 0 0.5rem;
}

.player-details, .club-details {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0.25rem 1rem;
}

@media (max-width: 480px) {
  .player-details, .club-details {
    grid-template-columns: 1fr;
  }
  
  .player-image, .club-image {
    width: 60px;
    height: 60px;
  }
}

.detail-row {
  display: flex;
  gap: 0.5rem;
  font-size: 0.8rem;
}

.detail-row .label {
  color: var(--text-secondary, #888);
}

.detail-row .value {
  color: var(--text-primary, #e0e0e0);
}

.detail-row .value.accent {
  color: var(--accent, #d4c84a);
  font-weight: 600;
}

.market-value {
  grid-column: 1 / -1;
  margin-top: 0.25rem;
}

.tm-link {
  position: absolute;
  top: 1rem;
  right: 1rem;
  color: var(--text-secondary, #888);
  transition: color 0.15s;
}

.tm-link:hover {
  color: var(--accent, #d4c84a);
}

.icon-sm {
  width: 16px;
  height: 16px;
}

.tm-badge {
  position: absolute;
  bottom: 0.5rem;
  right: 0.5rem;
  font-size: 0.65rem;
  color: var(--text-muted, #666);
  opacity: 0.6;
}
</style>
