<template>
  <div class="page-container">
    <div class="page-header">
      <h3>消息通知</h3>
      <el-button type="primary" plain size="small" @click="handleMarkAllRead" v-if="unreadCount > 0">
        全部标记为已读
      </el-button>
    </div>

    <el-card shadow="never">
      <el-empty v-if="!list.length" description="暂无通知" />
      <div v-else>
        <div v-for="item in list" :key="item.id"
             class="notify-item"
             :class="{ unread: !item.isRead }"
             @click="handleClick(item)">
          <div class="notify-left">
            <el-tag :type="typeTag(item.type)" size="small" effect="plain">{{ typeLabel(item.type) }}</el-tag>
          </div>
          <div class="notify-center">
            <div class="notify-title">{{ item.title }}</div>
            <div class="notify-content">{{ item.content }}</div>
          </div>
          <div class="notify-right">
            <span class="notify-time">{{ item.createTime }}</span>
            <el-icon v-if="!item.isRead" color="#409eff" style="margin-left: 8px"><CircleCheck /></el-icon>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { CircleCheck } from '@element-plus/icons-vue'
import { notificationApi } from '../api/notification'
import { useRouter } from 'vue-router'

const router = useRouter()
const list = ref([])
const unreadCount = ref(0)

const typeMap = {
  TASK_ASSIGN: { label: '任务分配', tag: 'warning' },
  TASK_REVIEW: { label: '任务审核', tag: 'primary' },
  SYSTEM: { label: '系统通知', tag: 'info' },
  UPGRADE: { label: '权限变更', tag: 'success' }
}

const typeLabel = type => typeMap[type]?.label || type
const typeTag = type => typeMap[type]?.tag || 'info'

onMounted(async () => {
  try {
    const [listRes, countRes] = await Promise.all([
      notificationApi.getList(),
      notificationApi.getUnreadCount()
    ])
    list.value = listRes.data
    unreadCount.value = countRes.data.count
  } catch (e) { /* ignore */ }
})

const handleClick = async (item) => {
  if (!item.isRead) {
    await notificationApi.markAsRead(item.id)
    item.isRead = 1
    unreadCount.value--
  }
}

const handleMarkAllRead = async () => {
  await notificationApi.markAllAsRead()
  list.value.forEach(n => { n.isRead = 1 })
  unreadCount.value = 0
  ElMessage.success('全部标记为已读')
}
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.notify-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;
}
.notify-item:hover { background: #f5f7fa; }
.notify-item.unread { background: #ecf5ff; }
.notify-item:first-child { padding-top: 0; }
.notify-left { flex-shrink: 0; }
.notify-center { flex: 1; min-width: 0; }
.notify-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}
.notify-content {
  color: #909399;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.notify-right {
  flex-shrink: 0;
  display: flex;
  align-items: center;
}
.notify-time {
  font-size: 12px;
  color: #c0c4cc;
  white-space: nowrap;
}
</style>
