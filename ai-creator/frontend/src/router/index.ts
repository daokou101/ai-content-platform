import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import MainLayout from '@/layout/MainLayout.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'generate',
        name: 'Generate',
        component: () => import('@/views/Generate.vue'),
        meta: { title: 'AI 生成' }
      },
      {
        path: 'contents',
        name: 'ContentList',
        component: () => import('@/views/ContentList.vue'),
        meta: { title: '内容管理' }
      },
      {
        path: 'contents/:id',
        name: 'ContentEdit',
        component: () => import('@/views/ContentEdit.vue'),
        meta: { title: '内容编辑' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Navigation guard
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    next()
  }
})

export default router
