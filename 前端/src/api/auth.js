import request from './request'

/**
 * API 接口模块 - 认证相关
 */
export const authApi = {
  login(data) {
    return request.post('/auth/login', data)
  },
  register(data) {
    return request.post('/auth/register', data)
  },
  logout() {
    return request.post('/auth/logout')
  },
  getCurrentUser() {
    return request.get('/auth/me')
  }
}
