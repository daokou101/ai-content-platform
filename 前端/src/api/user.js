import request from './request'

export const userApi = {
  getList(params) {
    return request.get('/admin/users', { params })
  },
  getDetail(id) {
    return request.get(`/admin/users/${id}`)
  },
  update(id, data) {
    return request.put(`/admin/users/${id}`, data)
  },
  resetPassword(id, password) {
    return request.put(`/admin/users/${id}/reset-password`, { password })
  },
  delete(id) {
    return request.delete(`/admin/users/${id}`)
  }
}
