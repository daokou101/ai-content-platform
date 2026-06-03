<template>
  <div class="page-container">
    <div class="page-header">
      <h3>任务管理</h3>
      <el-button type="primary" @click="router.push('/tasks/create')">
        <el-icon><Plus /></el-icon>创建任务
      </el-button>
    </div>

    <!-- 统计 -->
    <el-row :gutter="16" class="mb-4">
      <el-col :span="4" v-for="s in taskStats" :key="s.label">
        <el-card shadow="hover" :body-style="{ padding: '16px' }">
          <div class="stat-value" :style="{ color: s.color }">{{ s.value }}</div>
          <div class="stat-label">{{ s.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 任务列表 -->
    <el-card shadow="never">
      <el-table :data="taskList" stripe v-loading="loading" empty-text="暂无任务">
        <el-table-column prop="title" label="任务标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="creatorName" label="创建人" width="120" />
        <el-table-column prop="assigneeName" label="负责人" width="120" />
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }">
            <el-tag :type="priorityType(row.priority)" size="small">{{ priorityText(row.priority) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="pointsReward" label="积分" width="80" align="center">
          <template #default="{ row }">
            <span v-if="row.pointsReward">+{{ row.pointsReward }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="router.push(`/tasks/${row.id}`)">详情</el-button>
            <el-dropdown v-if="row.status !== 3 && row.status !== 4" @command="cmd => updateStatus(row.id, cmd)">
              <el-button size="small" type="primary" plain>变更状态</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="1">设为进行中</el-dropdown-item>
                  <el-dropdown-item command="2">提交审核</el-dropdown-item>
                  <el-dropdown-item command="3">标记完成</el-dropdown-item>
                  <el-dropdown-item command="4">取消任务</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { taskApi } from '../../api/task'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()
const taskList = ref([])
const loading = ref(false)

const taskStats = ref([
  { label: '总任务', value: 0, color: '#409eff' },
  { label: '待处理', value: 0, color: '#909399' },
  { label: '进行中', value: 0, color: '#e6a23c' },
  { label: '待审核', value: 0, color: '#f56c6c' },
  { label: '已完成', value: 0, color: '#67c23a' }
])

const priorityText = p => ['低', '中', '高', '紧急'][p] || '未知'
const priorityType = p => ['info', '', 'warning', 'danger'][p] || 'info'
const statusText = s => ['待处理', '进行中', '待审核', '已完成', '已取消'][s] || '未知'
const statusType = s => ['info', 'warning', 'danger', 'success', 'info'][s] || 'info'

const fetchData = async () => {
  loading.value = true
  try {
    const [listRes, statsRes] = await Promise.all([
      taskApi.getList(),
      taskApi.getStats()
    ])
    taskList.value = listRes.data
    taskStats.value = [
      { label: '总任务', value: statsRes.data.total, color: '#409eff' },
      { label: '待处理', value: statsRes.data.pending, color: '#909399' },
      { label: '进行中', value: statsRes.data.inProgress, color: '#e6a23c' },
      { label: '待审核', value: statsRes.data.review, color: '#f56c6c' },
      { label: '已完成', value: statsRes.data.completed, color: '#67c23a' }
    ]
  } catch (e) { /* ignore */ }
  loading.value = false
}

const updateStatus = async (id, status) => {
  await taskApi.updateStatus(id, parseInt(status))
  ElMessage.success('状态更新成功')
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.mb-4 { margin-bottom: 16px; }
</style>
