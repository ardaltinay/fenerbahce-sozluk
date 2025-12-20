<template>
  <div class="min-h-screen">
    <!-- Header -->
    <Header />

    <!-- Main Layout -->
    <div class="flex pt-16">
      <!-- Sidebar -->
      <Sidebar class="hidden lg:block" />

      <!-- Main Content -->
      <main class="flex-1 lg:ml-64 min-h-[calc(100vh-4rem)]">
        <div class="max-w-4xl mx-auto px-4 py-6">
          <!-- Entry Header -->
          <div class="flex items-center gap-2 mb-6">
            <router-link to="/" class="btn btn-ghost p-2">
              <ArrowLeft class="w-5 h-5" />
            </router-link>
            <h1 class="text-xl font-bold text-text-primary">
              entry #{{ entryId }}
            </h1>
          </div>

          <!-- Loading -->
          <div v-if="loading" class="card animate-pulse">
            <div class="h-4 bg-fb-navy-light rounded w-1/4 mb-4"></div>
            <div class="space-y-2">
              <div class="h-3 bg-fb-navy-light rounded w-full"></div>
              <div class="h-3 bg-fb-navy-light rounded w-5/6"></div>
              <div class="h-3 bg-fb-navy-light rounded w-4/6"></div>
            </div>
          </div>

          <!-- Entry -->
          <div v-else-if="entry" class="space-y-4">
            <EntryCard :entry="entry" />

            <!-- Topic Link -->
            <div class="card bg-fb-navy/30">
              <p class="text-sm text-text-muted mb-2">bu entry şu başlıkta yazıldı:</p>
              <router-link 
                :to="`/baslik/${entry.topicSlug}`"
                class="flex items-center justify-between p-3 rounded-lg hover:bg-fb-yellow/10 transition-colors"
              >
                <span class="text-fb-yellow font-medium">{{ entry.topicTitle }}</span>
                <ChevronRight class="w-5 h-5 text-text-muted" />
              </router-link>
            </div>
          </div>

          <!-- Not Found -->
          <div v-else class="card text-center py-12">
            <AlertCircle class="w-16 h-16 text-text-muted mx-auto mb-4" />
            <h2 class="text-xl font-bold text-text-primary mb-2">entry bulunamadı</h2>
            <p class="text-text-muted mb-4">aradığınız entry mevcut değil veya silinmiş olabilir.</p>
            <router-link to="/" class="btn btn-primary">
              ana sayfaya dön
            </router-link>
          </div>
        </div>
      </main>
    </div>

    <!-- Footer -->
    <Footer class="lg:ml-64" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowLeft, ChevronRight, AlertCircle } from 'lucide-vue-next'
import Header from '@/components/layout/Header.vue'
import Sidebar from '@/components/layout/Sidebar.vue'
import Footer from '@/components/layout/Footer.vue'
import EntryCard from '@/components/entry/EntryCard.vue'
import { useEntriesStore } from '@/stores/entries'

const route = useRoute()
const entriesStore = useEntriesStore()

const loading = ref(true)
const entryId = computed(() => route.params.id)
const entry = computed(() => entriesStore.currentEntry)

onMounted(() => {
  entriesStore.fetchEntryById(entryId.value)
  setTimeout(() => {
    loading.value = false
  }, 500)
})
</script>
