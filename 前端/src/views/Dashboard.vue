<template>
  <div class="page-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="6" v-for="stat in stats" :key="stat.label">
        <el-card shadow="hover" class="dashboard-card" :style="{ borderTop: `4px solid ${stat.color}` }">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表和排行榜 -->
    <el-row :gutter="20">
      <!-- 任务趋势图（管理员可见） -->
      <el-col :span="dashboardData?.totalUsers != null ? 16 : 24">
        <el-card shadow="hover">
          <template #header><span style="font-weight: 600">近7天任务趋势</span></template>
          <div ref="chartRef" style="height: 350px"></div>
        </el-card>
      </el-col>

      <!-- 积分排行榜 -->
      <el-col :span="dashboardData?.totalUsers != null ? 8 : 24">
        <el-card shadow="hover">
          <template #header><span style="font-weight: 600">🏆 积分排行榜</span></template>
          <div v-if="dashboardData?.pointsRanking?.length">
            <div v-for="(item, index) in dashboardData.pointsRanking.slice(0, 5)" :key="item.id" class="rank-item">
              <span class="rank-num" :class="'rank-' + (index + 1)">{{ index + 1 }}</span>
              <el-avatar :size="28" :src="item.avatar" />
              <span class="rank-name">{{ item.nickname }}</span>
              <span class="rank-points">{{ item.points }} 积分</span>
            </div>
          </div>
          <el-empty v-else description="暂无排行数据" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 管理员信息行 -->
    <el-row :gutter="20" class="mt-4" v-if="dashboardData?.totalUsers != null">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span style="font-weight: 600">系统概况</span></template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="总用户数">{{ dashboardData.totalUsers }}</el-descriptions-item>
            <el-descriptions-item label="在线用户数">{{ dashboardData.onlineUsers }}</el-descriptions-item>
            <el-descriptions-item label="总任务数">{{ dashboardData.totalTasks }}</el-descriptions-item>
            <el-descriptions-item label="任务完成率">{{ dashboardData.completionRate }}%</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import { useUserStore } from '../stores/user'
import { dashboardApi } from '../api/dashboard'
import * as echarts from 'echarts'

const userStore = useUserStore()
const chartRef = ref(null)
const dashboardData = ref(null)

const stats = computed(() => [
  { label: '总任务数', value: dashboardData.value?.totalTasks || 0, color: '#409eff' },
  { label: '进行中', value: dashboardData.value?.inProgressTasks || 0, color: '#e6a23c' },
  { label: '已完成', value: dashboardData.value?.completedTasks || 0, color: '#67c23a' },
  { label: '待处理', value: dashboardData.value?.pendingTasks || 0, color: '#909399' }
])

onMounted(async () => {
  try {
    const res = await dashboardApi.getDashboard()
    dashboardData.value = res.data
    nextTick(() => initChart())
  } catch (e) {
    // ignore
  }
})

const initChart = () => {
  if (!chartRef.value || !dashboardData.value?.taskTrend) return
  const chart = echarts.init(chartRef.value)
  const trend = dashboardData.value.taskTrend
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, bottom: 20, top: 20 },
    xAxis: {
      type: 'category',
      data: trend.map(t => t.date.slice(5))
    },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      data: trend.map(t => t.count),
      type: 'line',
      smooth: true,
      areaStyle: { opacity: 0.3 },
      lineStyle: { width: 3, color: '#409eff' },
      itemStyle: { color: '#409eff' }
    }]
  })
}
</script>

<style scoped>
.mb-4 { margin-bottom: 16px; }
.mt-4 { margin-top: 16px; }
.rank-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}
.rank-item:last-child { border-bottom: none; }
.rank-num {
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 600;
  background: #f0f0f0;
}
.rank-1 { background: #e6a23c; color: white; }
.rank-2 { background: #909399; color: white; }
.rank-3 { background: #cd7f32; color: white; }
.rank-name { flex: 1; font-size: 14px; }
.rank-points { font-size: 13px; color: #e6a23c; font-weight: 600; }
</style>
