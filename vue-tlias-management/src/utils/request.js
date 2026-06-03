/**
 * Axios 请求工具类 —— 封装了所有 HTTP 请求的通用逻辑
 *
 * 【axios 是什么？】
 * axios 是一个前端 HTTP 客户端库，用来发送 Ajax 请求（异步请求）。
 * 类似于后端的 RestTemplate，用于与后端 API 通信。
 *
 * 【为什么需要封装？】
 * 如果没有封装，每个页面都要写完整的请求路径（如 http://localhost:8080/api/depts），
 * 还要在每个请求中手动添加 token，非常麻烦。
 * 封装后，我们只需要：
 * 1. 配置基础路径（baseURL），每个请求只需写路径后缀
 * 2. 在拦截器中统一添加 token
 * 3. 在拦截器中统一处理错误
 */

import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建 axios 实例对象
// 这个实例已经配置了基础信息，之后所有请求都用这个实例
const request = axios.create({
  // 基础路径：所有请求的 URL 都以 /api 开头
  // Vite 在开发环境下会将 /api 开头的请求转发到 http://localhost:8080（见 vite.config.js）
  baseURL: '/api',
  // 请求超时时间（10分钟 = 600000 毫秒）
  timeout: 600000
})

// ========== 请求拦截器（Request Interceptor） ==========
// 在"发送请求之前"执行

// 【什么是拦截器？】
// 类似于后端的过滤器，可以在请求发送前或响应接收后做一些通用处理。
// 这里有两个拦截器：
// 1. 请求拦截器：在每个请求发送前，自动加上 token
// 2. 响应拦截器：在收到响应后，自动处理 401 未授权错误

request.interceptors.request.use(
  (config) => {
    // config：就是当前请求的配置对象
    // 我们可以修改它，比如添加请求头

    // 从 localStorage 中获取 token（如果用户登录过，就有值）
    const token = localStorage.getItem('token')
    if (token) {
      // 在请求头中添加 token
      // 后端的 LoginInterceptor 会从 "token" 这个请求头中获取令牌
      config.headers['token'] = token
    }

    // 返回修改后的配置，继续发送请求
    return config
  },
  (error) => {
    // 请求发送失败（很少发生）
    return Promise.reject(error)
  }
)

// ========== 响应拦截器（Response Interceptor） ==========
// 在"收到响应之后"执行

request.interceptors.response.use(
  // ---- 成功回调 ----
  (response) => {
    // response：后端返回的完整响应对象
    // response.data 就是后端返回的数据
    // 我们的后端接口返回的都是 Result 对象，所以直接返回 data
    return response.data
  },

  // ---- 失败回调 ----
  (error) => {
    // error：错误对象
    // 如果 HTTP 状态码是 401（未授权），说明 token 过期或无效
    if (error.response && error.response.status === 401) {
      // 清除本地存储的登录信息
      localStorage.removeItem('token')
      localStorage.removeItem('userName')

      // 提示用户重新登录
      ElMessage.error('登录已过期，请重新登录')

      // 跳转到登录页
      // router.push() 是编程式导航，相当于点击了某个链接跳转
      router.push('/login')
    } else {
      // 其他错误（网络错误、服务器错误等）
      ElMessage.error('请求失败：' + (error.message || '未知错误'))
    }

    // 返回一个失败的 Promise，让调用方也能捕获到错误
    return Promise.reject(error)
  }
)

// 导出 request 实例，其他文件通过 import request from '@/utils/request' 使用
export default request
