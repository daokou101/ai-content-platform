import request from '@/utils/request'

/**
 * 仪表盘 API
 *
 * 调用后端 GET /api/dashboard 获取首页统计数据。
 * 后端接口定义：DashboardController.getDashboard()
 *
 * 返回数据格式（DashboardVO）：
 *   totalContents    - 当前用户的内容总数
 *   todayGenerations - 今日生成数量
 *   templates        - 模板总数
 *   favorites        - 收藏内容数
 *   myPoints         - 当前用户的积分
 *   myRank           - 当前用户在积分排行榜中的排名
 *   myRole           - 当前用户的角色编码
 *   pointsRanking    - 积分排行榜前 10 名列表
 *   totalUsers       - 系统总用户数（仅管理员可见）
 *   onlineUsers      - 当前在线用户数（仅管理员可见）
 */

export interface DashboardData {
  totalContents: number
  todayGenerations: number
  templates: number
  favorites: number
  myPoints: number
  myRank: number
  myRole: string
  pointsRanking: Array<{
    id: number
    nickname: string
    avatar: string
    points: number
    role: string
  }>
  totalUsers?: number
  onlineUsers?: number
}

/**
 * 获取仪表盘数据
 * GET /api/dashboard
 *
 * 前端调用时机：
 *   Dashboard.vue → onMounted() → getDashboard()
 *
 * @returns Promise<DashboardData> 仪表盘统计数据
 */
export function getDashboard() {
  return request.get<any, DashboardData>('/dashboard')
}
