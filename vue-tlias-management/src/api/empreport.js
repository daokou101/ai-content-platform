// api/empreport.js
import request from '@/utils/request'

// 员工职位统计 - 修正接口路径
export const queryEmpCountByJobApi = () => request.get('/emps/stats/job')
