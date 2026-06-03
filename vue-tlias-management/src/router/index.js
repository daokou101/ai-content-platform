/**
 * 路由配置文件
 *
 * 【什么是路由？】
 * 路由是指"URL 地址"和"页面组件"之间的对应关系。
 * 比如 /login 对应登录页面，/dept 对应部门管理页面。
 * 当用户在浏览器地址栏输入 URL 时，路由会根据 URL 显示对应的页面。
 *
 * 【Vue Router 是什么？】
 * Vue 官方的路由管理器，用来实现"单页应用"（SPA）的页面导航。
 * 单页应用的特点是：页面切换时不刷新整个页面，只更新需要变化的部分。
 *
 * 【路由守卫（Router Guard）是什么？】
 * 路由守卫是 Vue Router 提供的一种机制，可以在"进入路由之前"执行一些逻辑。
 * 就像进小区需要刷卡一样，进入某些页面需要先检查是否登录。
 *
 * 【本文件定义了】
 * 1. 所有页面的路由规则（URL 对应哪个组件）
 * 2. 路由守卫规则（哪些页面需要登录才能访问）
 */

import { createRouter, createWebHistory } from 'vue-router'

// 导入页面组件（.vue 文件）
// 组件就是页面的"模板"，一个组件就是一个页面或页面的一部分
import Layout from '../views/layout/index.vue'  // 布局组件（含顶部导航和左侧菜单）
import Home from '../views/index/index.vue'      // 首页
import Clazz from '../views/clazz/index.vue'     // 班级管理
import Stu from '../views/stu/index.vue'         // 学员管理
import Dept from '../views/dept/index.vue'       // 部门管理
import Emp from '../views/emp/index.vue'         // 员工管理
import EmpReport from '../views/report/emp/index.vue'  // 员工信息统计
import StuReport from '../views/report/stu/index.vue'  // 学员信息统计
import Log from '../views/log/index.vue'         // 日志信息统计
import Login from '../views/login/index.vue'     // 登录页面

// ===== 定义路由表 =====
// routes 是一个数组，每个元素就是一个路由规则
const routes = [
  // ---- 需要登录才能访问的页面（嵌套在 Layout 中） ----
  {
    path: '/',           // 访问路径
    component: Layout,   // 对应的组件（Layout 包含顶部导航和左侧菜单）
    children: [          // 子路由（url 变化但 Layout 不变，只切换 Layout 中的 <router-view>）
      { path: '/', redirect: '/index' },          // 访问 / 自动跳转到 /index
      { path: '/index', component: Home },         // 首页
      { path: '/clazz', component: Clazz },        // 班级管理
      { path: '/stu', component: Stu },            // 学员管理
      { path: '/dept', component: Dept },          // 部门管理
      { path: '/emp', component: Emp },            // 员工管理
      { path: '/empReport', component: EmpReport },// 员工信息统计
      { path: '/stuReport', component: StuReport },// 学员信息统计
      { path: '/log', component: Log }             // 日志信息统计
    ]
  },
  // ---- 不需要登录就能访问的页面 ----
  { path: '/login', component: Login }  // 登录页
]

// ===== 创建路由实例 =====
const router = createRouter({
  // createWebHistory：使用 HTML5 的 History 模式，URL 中不会带 # 号
  history: createWebHistory(),
  routes, // 路由表
})

// ===== 路由守卫（导航守卫） =====
//
// 【什么是 beforeEach？】
// beforeEach 是一个"全局前置守卫"，在每次路由切换之前执行。
// 我们可以在这里检查用户是否登录，如果没登录就强制跳转到登录页。
//
// 【回调函数参数】
// - to：要跳转到的路由（目标路由）
// - from：从哪个路由跳转过来（来源路由）
// - next：放行函数，调用 next() 才能继续跳转
//   如果不调用 next()，页面就不会跳转
//
// 【流程】
// 1. 用户点击"部门管理"链接
// 2. 路由守卫执行
// 3. 检查是否已登录？
//    - 是 → next() 放行，显示部门管理页面
//    - 否 → next('/login') 强制跳转到登录页

router.beforeEach((to, from, next) => {
  // 从 localStorage 中获取 token
  // 如果 token 存在，说明已登录；不存在则未登录
  const token = localStorage.getItem('token')

  // 如果要访问的是登录页，直接放行（防止死循环）
  // 如果不加这个判断，未登录用户访问 /login 也会被重定向到 /login，造成死循环
  if (to.path === '/login') {
    next()  // 放行
    return
  }

  // 如果 token 不存在（未登录），强制跳转到登录页
  if (!token) {
    // next('/login') 会终止当前跳转，重新跳转到 /login
    next('/login')
    return
  }

  // token 存在（已登录），放行
  next()
})

// 导出路由实例，在 main.js 中使用 app.use(router) 注册
export default router
