import request from './request'

export const taskApi = {
  getList() {
    return request.get('/tasks')
  },
  getDetail(id) {
    return request.get(`/tasks/${id}`)
  },
  create(data) {
    return request.post('/tasks', data)
  },
  updateStatus(id, status) {
    return request.put(`/tasks/${id}/status`, { status })
  },
  getStats() {
    return request.get('/tasks/stats')
  }
}
