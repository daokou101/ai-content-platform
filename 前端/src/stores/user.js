import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authApi } from '../api/auth'

/**
 * 用户状态管理
 *
 * Pinia: Vue 3 官方推荐的状态管理库，替代 Vuex
 * defineStore: 定义一个 Store（状态容器）
 *
 * 持久化: pinia-plugin-persistedstate 插件自动将状态保存到 localStorage
 * 刷新页面后登录状态不丢失
 */
export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  // 判断是否为管理员
  const isAdmin = () => {
    const role = userInfo.value?.role
    return role === 'SUPER_ADMIN' || role === 'ADMIN'
  }

  // 判断是否为超级管理员
  const isSuperAdmin = () => {
    return userInfo.value?.role === 'SUPER_ADMIN'
  }

  // 登录
  const login = async (loginData) => {
    const res = await authApi.login(loginData)
    token.value = res.data.token
    userInfo.value = res.data
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('userInfo', JSON.stringify(res.data))
    return res.data
  }

  // 注册
  const register = async (registerData) => {
    const res = await authApi.register(registerData)
    token.value = res.data.token
    userInfo.value = res.data
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('userInfo', JSON.stringify(res.data))
    return res.data
  }

  // 退出登录
  const logout = async () => {
    try {
      await authApi.logout()
    } catch (e) {
      // 即使退出接口失败，前端也要清除状态
    }
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return {
    token,
    userInfo,
    isAdmin,
    isSuperAdmin,
    login,
    register,
    logout
  }
})
