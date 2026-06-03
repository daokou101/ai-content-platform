import request from './request'

export const notificationApi = {
  getUnreadCount() {
    return request.get('/notifications/unread-count')
  },
  getList() {
    return request.get('/notifications')
  },
  markAsRead(id) {
    return request.put(`/notifications/${id}/read`)
  },
  markAllAsRead() {
    return request.put('/notifications/read-all')
  }
}
