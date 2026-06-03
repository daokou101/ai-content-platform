import { createRouter, createWebHistory } from 'vue-router'

/**
 * Vue Router 路由配置
 *
 * createRouter: 创建路由实例
 * createWebHistory: HTML5 History 模式，URL 中不带 # 号
 *   需要后端支持 fallback（所有路径返回 index.html）
 *
 * 路由守卫（beforeEach）: 在路由跳转前执行
 *   用来检查用户是否登录，根据角色跳转到不同页面
 */
const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/Login.vue'),
      meta: { title: '登录', noAuth: true }  // noAuth: 不需要登录
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('../views/Register.vue'),
      meta: { title: '注册', noAuth: true }
    },
    {
      path: '/',
      component: () => import('../views/Layout.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('../views/Dashboard.vue'),
          meta: { title: '工作台' }
        },
        {
          path: 'tasks',
          name: 'Tasks',
          component: () => import('../views/task/TaskList.vue'),
          meta: { title: '任务管理' }
        },
        {
          path: 'tasks/create',
          name: 'TaskCreate',
          component: () => import('../views/task/TaskForm.vue'),
          meta: { title: '创建任务' }
        },
        {
          path: 'tasks/:id',
          name: 'TaskDetail',
          component: () => import('../views/task/TaskDetail.vue'),
          meta: { title: '任务详情' }
        },
        {
          path: 'points',
          name: 'Points',
          component: () => import('../views/Points.vue'),
          meta: { title: '积分中心' }
        },
        {
          path: 'notifications',
          name: 'Notifications',
          component: () => import('../views/Notification.vue'),
          meta: { title: '消息通知' }
        },
        // 管理员页面
        {
          path: 'admin/users',
          name: 'UserManagement',
          component: () => import('../views/admin/UserManagement.vue'),
          meta: { title: '用户管理', role: ['ADMIN', 'SUPER_ADMIN'] }
        }
      ]
    }
  ]
})

// 路由守卫：判断登录状态和角色权限
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 智能任务协作平台` : '智能任务协作平台'

  const token = localStorage.getItem('token')
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')

  // 如果没登录且页面需要登录，跳转登录页
  if (!token && !to.meta.noAuth) {
    return next('/login')
  }

  // 如果已登录且访问登录/注册页，跳转首页
  if (token && to.meta.noAuth) {
    return next('/dashboard')
  }

  // 检查角色权限
  if (to.meta.role) {
    const role = userInfo.role
    const hasPermission = to.meta.role.includes(role)
    if (!hasPermission) {
      ElMessage.error('没有权限访问此页面')
      return next('/dashboard')
    }
  }

  next()
})

export default router
