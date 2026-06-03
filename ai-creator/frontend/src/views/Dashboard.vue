<template>
  <div class="dashboard">
    <div class="welcome-card">
      <el-card shadow="never" class="welcome-inner">
        <div class="welcome-content">
          <div class="welcome-text">
            <h1>欢迎回来，{{ userDisplayName }}</h1>
            <p>今天想创作什么内容？使用 AI 助手快速生成高质量文章、营销文案等内容。</p>
            <div class="welcome-actions">
              <el-button type="primary" size="large" @click="$router.push('/generate')">
                <el-icon><MagicStick /></el-icon>
                新建生成
              </el-button>
              <el-button size="large" @click="$router.push('/contents')">
                <el-icon><Document /></el-icon>
                查看内容
              </el-button>
            </div>
          </div>
          <div class="welcome-illustration">
            <el-icon :size="120" color="#e6f1ff"><MagicStick /></el-icon>
          </div>
        </div>
      </el-card>
    </div>

    <div class="stats-grid">
      <el-card shadow="never" class="stat-card">
        <div class="stat-content">
          <div class="stat-info">
            <div class="stat-label">内容总数</div>
            <div class="stat-value">{{ stats.totalContents }}</div>
          </div>
          <div class="stat-icon" style="background: #e6f1ff; color: #409eff">
            <el-icon :size="24"><Document /></el-icon>
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="stat-card">
        <div class="stat-content">
          <div class="stat-info">
            <div class="stat-label">今日生成</div>
            <div class="stat-value">{{ stats.todayGenerations }}</div>
          </div>
          <div class="stat-icon" style="background: #f0f9eb; color: #67c23a">
            <el-icon :size="24"><MagicStick /></el-icon>
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="stat-card">
        <div class="stat-content">
          <div class="stat-info">
            <div class="stat-label">模板数量</div>
            <div class="stat-value">{{ stats.templates }}</div>
          </div>
          <div class="stat-icon" style="background: #fdf6ec; color: #e6a23c">
            <el-icon :size="24"><Collection /></el-icon>
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="stat-card">
        <div class="stat-content">
          <div class="stat-info">
            <div class="stat-label">收藏内容</div>
            <div class="stat-value">{{ stats.favorites }}</div>
          </div>
          <div class="stat-icon" style="background: #fef0f0; color: #f56c6c">
            <el-icon :size="24"><Star /></el-icon>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getDashboard } from '@/api/dashboard'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()

const userDisplayName = computed(() => {
  return authStore.userInfo?.username || authStore.userInfo?.nickname || '用户'
})

const stats = reactive({
  totalContents: 0,
  todayGenerations: 0,
  templates: 0,
  favorites: 0
})

onMounted(async () => {
  try {
    const data = await getDashboard()
    if (data) {
      stats.totalContents = data.totalContents || 0
      stats.todayGenerations = data.todayGenerations || 0
      stats.templates = data.templates || 0
      stats.favorites = data.favorites || 0
    }
  } catch (e) {
    // 接口调用失败时保持默认值 0，不阻塞页面渲染
    console.warn('获取仪表盘数据失败，使用默认值', e)
  }
})
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-card {
  margin-bottom: 24px;
}

.welcome-inner {
  border-radius: 12px;
  border: none;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.welcome-inner :deep(.el-card__body) {
  padding: 0;
}

.welcome-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 40px;
  color: #ffffff;
}

.welcome-text h1 {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 12px;
}

.welcome-text p {
  font-size: 15px;
  opacity: 0.85;
  margin-bottom: 24px;
  max-width: 480px;
  line-height: 1.6;
}

.welcome-actions {
  display: flex;
  gap: 12px;
}

.welcome-actions .el-button {
  border-radius: 8px;
}

.welcome-actions .el-button--default {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
  color: #ffffff;
}

.welcome-actions .el-button--default:hover {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 0.5);
}

.welcome-illustration {
  opacity: 0.3;
  flex-shrink: 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-card {
  border-radius: 12px;
  border: 1px solid #ebeef5;
}

.stat-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

@media (max-width: 900px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .welcome-content {
    flex-direction: column;
    text-align: center;
    padding: 24px;
  }
  .welcome-text p {
    max-width: 100%;
  }
  .welcome-actions {
    justify-content: center;
  }
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
