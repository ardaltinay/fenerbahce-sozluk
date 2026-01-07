import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
  },
  {
    path: '/baslik/:id',
    name: 'TopicDetail',
    component: () => import('@/views/TopicDetail.vue'),
  },
  {
    path: '/biri/:username',
    name: 'AuthorProfile',
    component: () => import('@/views/AuthorProfile.vue'),
  },

  {
    path: '/entry/:id',
    name: 'EntryDetail',
    component: () => import('@/views/EntryDetail.vue'),
  },
  {
    path: '/arama',
    name: 'Search',
    component: () => import('@/views/Search.vue'),
  },
  {
    path: '/sifremi-unuttum',
    name: 'ForgotPassword',
    component: () => import('@/views/ForgotPassword.vue'),
  },
  {
    path: '/sifre-sifirla/:token',
    name: 'ResetPassword',
    component: () => import('@/views/ResetPassword.vue'),
  },
  {
    path: '/iletisim',
    name: 'Contact',
    component: () => import('@/views/Contact.vue'),
  },
  {
    path: '/istatistikler',
    name: 'Statistics',
    component: () => import('@/views/Statistics.vue'),
  },
  {
    path: '/mesajlar',
    name: 'Messages',
    component: () => import('@/views/MessagesView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/mesajlar/:username',
    name: 'MessageConversation',
    component: () => import('@/views/MessagesView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
