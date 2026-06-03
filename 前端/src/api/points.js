import request from './request'

export const pointsApi = {
  signIn() {
    return request.post('/points/sign-in')
  },
  recharge(amount) {
    return request.post('/points/recharge', { amount })
  },
  getLogs() {
    return request.get('/points/logs')
  },
  getBalance() {
    return request.get('/points/balance')
  }
}
