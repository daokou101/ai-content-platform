import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi } from '@/api/auth'
import { ElMessage } from 'element-plus'
import router from '@/router'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(null)
  const userInfo = ref<any>(null)

  const isLoggedIn = computed(() => !!token.value)

  function loadFromStorage() {
    const savedToken = localStorage.getItem('token')
    const savedUserInfo = localStorage.getItem('userInfo')
    if (savedToken) {
      token.value = savedToken
    }
    if (savedUserInfo) {
      try {
        userInfo.value = JSON.parse(savedUserInfo)
      } catch {
        userInfo.value = null
      }
    }
  }

  async function login(username: string, password: string) {
    try {
      const res: any = await loginApi(username, password)
      const data = res.data || res
      token.value = data.token || data
      // Try to extract user info from response
      if (data.userInfo || data.user) {
        userInfo.value = data.userInfo || data.user
      } else {
        userInfo.value = { username }
      }
      localStorage.setItem('token', token.value!)
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
      ElMessage.success('登录成功')
      router.push('/')
    } catch (error: any) {
      const msg = error?.response?.data?.message || error?.response?.data?.msg || '登录失败'
      ElMessage.error(msg)
      throw error
    }
  }

  async function register(username: string, password: string) {
    try {
      await registerApi(username, password)
      ElMessage.success('注册成功，请登录')
    } catch (error: any) {
      const msg = error?.response?.data?.message || error?.response?.data?.msg || '注册失败'
      ElMessage.error(msg)
      throw error
    }
  }

  function logout() {
    token.value = null
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/login')
  }

  return { token, userInfo, isLoggedIn, loadFromStorage, login, register, logout }
})
