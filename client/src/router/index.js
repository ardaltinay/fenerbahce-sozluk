import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
  },
  {
    path: '/baslik/:slug',
    name: 'TopicDetail',
    component: () => import('@/views/TopicDetail.vue'),
  },
  {
    path: '/biri/:username',
    name: 'AuthorProfile',
    component: () => import('@/views/AuthorProfile.vue'),
  },
  {
    path: '/giris',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
  },
  {
    path: '/kayit',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
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
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
