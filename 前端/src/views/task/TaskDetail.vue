<template>
  <div class="page-container">
    <div class="mb-4">
      <el-button @click="router.back()"><el-icon><ArrowLeft /></el-icon>返回</el-button>
    </div>

    <el-card v-if="task" :loading="loading">
      <template #header>
        <div class="detail-header">
          <span style="font-weight: 600; font-size: 18px">{{ task.title }}</span>
          <el-tag :type="statusType(task.status)" size="large">{{ statusText(task.status) }}</el-tag>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="创建人" :span="1">{{ task.creatorName }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ task.assigneeName || '未指派' }}</el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag :type="priorityType(task.priority)" size="small">{{ priorityText(task.priority) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="积分奖励">
          <span v-if="task.pointsReward" style="color: #e6a23c">+{{ task.pointsReward }} 积分</span>
          <span v-else>无</span>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ task.createTime }}</el-descriptions-item>
        <el-descriptions-item label="截止时间">{{ task.deadline || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ task.completedTime || '-' }}</el-descriptions-item>
      </el-descriptions>

      <div class="detail-section">
        <h4>任务描述</h4>
        <p>{{ task.description || '暂无描述' }}</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { taskApi } from '../../api/task'
import { ArrowLeft } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const task = ref(null)
const loading = ref(true)

const priorityText = p => ['低', '中', '高', '紧急'][p] || '未知'
const priorityType = p => ['info', '', 'warning', 'danger'][p] || 'info'
const statusText = s => ['待处理', '进行中', '待审核', '已完成', '已取消'][s] || '未知'
const statusType = s => ['info', 'warning', 'danger', 'success', 'info'][s] || 'info'

onMounted(async () => {
  try {
    const res = await taskApi.getDetail(route.params.id)
    task.value = res.data
  } catch (e) { /* ignore */ }
  loading.value = false
})
</script>

<style scoped>
.mb-4 { margin-bottom: 16px; }
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.detail-section {
  margin-top: 24px;
}
.detail-section h4 {
  margin-bottom: 12px;
  color: #303133;
}
.detail-section p {
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
