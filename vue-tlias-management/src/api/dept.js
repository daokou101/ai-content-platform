//部门管理模块的api封装 - 异步交互
import request from '@/utils/request'

//查询全部部门数据
export const queryAllDeptApi = () => request.get('/depts')

//新增部门
export const addDeptApi = (data) => request.post('/depts', data)

//根据部门id查询部门数据
export const queryDeptByIdApi = (id) => request.get(`/depts/${id}`)

//修改部门数据
export const updateDeptApi = (data) => request.put('/depts', data)

//删除部门数据
export const deleteDeptApi = (id) => request.delete(`/depts?id=${id}`)  