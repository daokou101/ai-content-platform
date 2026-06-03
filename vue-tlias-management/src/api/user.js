/**
 * 用户相关 API
 *
 * 【API 是什么？】
 * API（Application Programming Interface，应用程序接口）
 * 可以理解为"前后端约定好的沟通方式"。
 * 前端调后端的接口（URL），后端返回数据。
 *
 * 【export 关键字】
 * 导出函数，这样其他文件可以用 import 引入。
 */

import request from '@/utils/request'

/**
 * 用户登录
 *
 * 调用后端的 /login 接口（POST 请求）
 *
 * @param {string} username 用户名
 * @param {string} password 密码
 * @returns {Promise} 返回 Promise 对象，包含后端返回的数据
 *
 * 【async/await 是怎么工作的？】
 * 在组件中这样调用：
 * const result = await loginApi('admin', '123456')
 * 浏览器会等待后端返回结果后，才继续往下执行
 */
export const loginApi = (username, password) => {
  // request.post() 发送 POST 请求
  // 第二个参数是请求体（Body），会被转为 JSON 发送
  return request.post('/login', { username, password })
}
