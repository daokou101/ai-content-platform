import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

/**
 * Axios 请求封装
 *
 * Axios: 基于 Promise 的 HTTP 请求库，浏览器和 Node.js 通用
 * 拦截器（Interceptor）: 请求发出前和响应回来后执行的钩子函数
 *
 * 请求拦截器：自动给每个请求带上 Token
 * 响应拦截器：统一处理错误，比如 Token 过期时跳转到登录页
 */
const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器：自动附加 Token
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器：统一处理错误
request.interceptors.response.use(
  response => {
    const res = response.data
    // 业务状态码不是 200 的也算错误
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  error => {
    if (error.response) {
      switch (error.response.status) {
        case 401:
          ElMessage.error('登录已过期，请重新登录')
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          router.push('/login')
          break
        case 403:
          ElMessage.error('没有权限执行此操作')
          break
        case 429:
          ElMessage.warning('请求太频繁，请稍后再试')
          break
        default:
          ElMessage.error(error.response.data?.message || '服务器错误')
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default request
