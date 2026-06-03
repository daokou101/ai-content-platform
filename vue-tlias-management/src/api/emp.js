//员工管理模块的api封装 - 异步交互
import request from '@/utils/request'

//员工信息的分页条件查询
export const queryPageApi = (name, gender, begin, end, page, pageSize) => 
   request.get(`/emps?name=${name}&gender=${gender}&begin=${begin}&end=${end}&page=${page}&pageSize=${pageSize}`)

//添加员工信息
export const addEmpApi = (data) => request.post('/emps', data)
