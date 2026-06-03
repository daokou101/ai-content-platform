<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
      <div class="sidebar-header" @click="router.push('/dashboard')">
        <el-icon :size="28" color="#409eff"><Monitor /></el-icon>
        <span v-show="!isCollapse" class="sidebar-title">智能协作平台</span>
      </div>

      <el-menu
        :default-active="route.path"
        :collapse="isCollapse"
        :collapse-transition="false"
        background-color="#1d1e1f"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>工作台</span>
        </el-menu-item>

        <el-menu-item index="/tasks">
          <el-icon><List /></el-icon>
          <span>任务管理</span>
        </el-menu-item>

        <el-menu-item index="/points">
          <el-icon><Coin /></el-icon>
          <span>积分中心</span>
        </el-menu-item>

        <el-menu-item index="/notifications">
          <el-icon><Bell /></el-icon>
          <span>消息通知</span>
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notify-badge" />
        </el-menu-item>

        <!-- 管理员菜单 -->
        <el-divider v-if="userStore.isAdmin()" style="border-color: #333; margin: 8px 0" />
        <div v-if="userStore.isAdmin()" class="menu-section-label" v-show="!isCollapse">管理</div>

        <el-menu-item v-if="userStore.isAdmin()" index="/admin/users">
          <el-icon><UserFilled /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon :size="20" class="collapse-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <!-- 积分显示 -->
          <el-tag type="warning" effect="plain" class="points-tag">
            <el-icon style="margin-right: 4px"><Coin /></el-icon>
            {{ userStore.userInfo?.points || 0 }} 积分
          </el-tag>

          <!-- 通知图标 -->
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notify-icon">
            <el-icon :size="20" @click="router.push('/notifications')" style="cursor:pointer">
              <Bell />
            </el-icon>
          </el-badge>

          <!-- 用户下拉 -->
          <el-dropdown @command="handleCommand">
            <span class="user-dropdown">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar" />
              <span class="username">{{ userStore.userInfo?.nickname || '用户' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人信息
                </el-dropdown-item>
                <el-dropdown-item command="points">
                  <el-icon><Coin /></el-icon>积分中心
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 页面内容 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { notificationApi } from '../api/notification'
import { ElMessageBox } from 'element-plus'
import {
  Monitor, Odometer, List, Coin, Bell, UserFilled,
  Fold, Expand, ArrowDown, User, SwitchButton, EditPen
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)
const unreadCount = ref(0)
let timer = null

// 获取未读通知数（轮询）
const fetchUnreadCount = async () => {
  try {
    const res = await notificationApi.getUnreadCount()
    unreadCount.value = res.data.count
  } catch (e) {
    // ignore
  }
}

onMounted(() => {
  fetchUnreadCount()
  // 每30秒轮询一次未读通知数
  timer = setInterval(fetchUnreadCount, 30000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      break
    case 'points':
      router.push('/points')
      break
    case 'logout':
      await ElMessageBox.confirm('确定要退出登录吗？', '提示')
      await userStore.logout()
      router.push('/login')
      break
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}
.sidebar {
  background: #1d1e1f;
  overflow-y: auto;
  overflow-x: hidden;
  transition: width 0.3s;
}
.sidebar-header {
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  cursor: pointer;
  gap: 10px;
}
.sidebar-title {
  color: white;
  font-size: 16px;
  font-weight: 600;
  white-space: nowrap;
}
.menu-section-label {
  color: #606266;
  font-size: 12px;
  padding: 4px 20px;
  text-transform: uppercase;
  letter-spacing: 1px;
}
.header {
  background: white;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 20px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}
.collapse-btn {
  cursor: pointer;
  color: #606266;
}
.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.username {
  font-size: 14px;
  color: #303133;
}
.points-tag {
  display: flex;
  align-items: center;
}
.notify-icon {
  line-height: 1;
}
.main-content {
  background: #f5f7fa;
  overflow-y: auto;
}
</style>
