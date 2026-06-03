/**
 * 用户认证状态管理（Pinia 状态管理库）
 *
 * 【什么是 Pinia？】
 * Pinia 是 Vue 3 官方推荐的状态管理工具，用于在多个组件之间共享数据。
 * 类似 Android 中的 SharedPreferences 或 iOS 中的 UserDefaults。
 *
 * 【这个 store 的作用】
 * 管理用户的登录状态，包括 token（令牌）和用户信息。
 * 整个应用都可以通过 useAuthStore() 来访问和修改这些数据。
 *
 * 【关键概念】
 * - state（状态）：就是数据，比如 token、userName
 * - action（动作）：修改状态的方法，比如 login()、logout()
 *
 * 【什么是 token？】
 * 用户登录成功后，后端返回的一个"身份令牌"。
 * 之后每次请求时都把 token 放在请求头中发给后端，
 * 后端就知道"你是谁"了。（类似酒店的房卡）
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

// 定义并导出 auth store
// useAuthStore 是一个"组合式函数"，在组件中用 useAuthStore() 调用
export const useAuthStore = defineStore('auth', () => {
  // ========== 状态（State） ==========

  // 用户令牌（从 localStorage 中读取，如果之前登录过就有值）
  // localStorage：浏览器的本地存储，页面关闭后数据还在
  // sessionStorage：页面关闭后数据就没了
  // 我们用 localStorage，这样用户关闭页面重新打开后还是登录状态
  const token = ref(localStorage.getItem('token') || '')

  // 用户名（从 localStorage 中读取）
  const userName = ref(localStorage.getItem('userName') || '')

  // ========== 计算属性（Getters） ==========

  // 判断是否已登录（token 不为空就是已登录）
  // computed 是"计算属性"，它的值会根据其他状态自动变化
  const isLoggedIn = computed(() => !!token.value)

  // ========== 操作方法（Actions） ==========

  /**
   * 登录成功后的处理
   *
   * 当用户登录成功后，后端返回了 token 和用户名，
   * 我们需要：
   * 1. 保存到 Pinia 状态中（这样其他组件能访问）
   * 2. 保存到 localStorage 中（这样刷新页面后数据还在）
   *
   * @param {string} newToken 后端返回的 JWT 令牌
   * @param {string} newName 后端返回的用户名
   */
  function login(newToken, newName) {
    // 保存到 Pinia 状态
    token.value = newToken
    userName.value = newName

    // 保存到 localStorage，这样刷新页面后还能记住登录状态
    localStorage.setItem('token', newToken)
    localStorage.setItem('userName', newName)
  }

  /**
   * 退出登录
   *
   * 清除所有登录信息（Pinia 和 localStorage 中的都要清除）
   * 然后页面会跳转到登录页（在路由守卫中处理）
   */
  function logout() {
    // 清除 Pinia 状态
    token.value = ''
    userName.value = ''

    // 清除 localStorage
    localStorage.removeItem('token')
    localStorage.removeItem('userName')
  }

  // 把状态和方法"暴露"出去，这样组件才能使用
  return { token, userName, isLoggedIn, login, logout }
})
