<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside">
      <div class="logo-section" :class="{ collapsed: isCollapse }">
        <el-icon v-if="isCollapse" :size="28" color="#409eff">
          <MagicStick />
        </el-icon>
        <span v-else class="logo-text">AI 创作平台</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        background-color="#001529"
        text-color="#ffffffa6"
        active-text-color="#ffffff"
        router
        class="side-menu"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <template #title>仪表盘</template>
        </el-menu-item>
        <el-menu-item index="/generate">
          <el-icon><MagicStick /></el-icon>
          <template #title>AI 生成</template>
        </el-menu-item>
        <el-menu-item index="/contents">
          <el-icon><Document /></el-icon>
          <template #title>内容管理</template>
        </el-menu-item>
      </el-menu>

      <div class="collapse-btn" @click="toggleCollapse">
        <el-icon>
          <Fold v-if="!isCollapse" />
          <Expand v-else />
        </el-icon>
      </div>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" icon="UserFilled" />
              <span class="username">{{ userDisplayName }}</span>
              <el-icon><CaretBottom /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人信息
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const isCollapse = ref(false)

const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/contents/')) return '/contents'
  return path
})

const userDisplayName = computed(() => {
  return authStore.userInfo?.username || authStore.userInfo?.nickname || '用户'
})

function toggleCollapse() {
  isCollapse.value = !isCollapse.value
}

function handleCommand(command: string) {
  if (command === 'logout') {
    authStore.logout()
  } else if (command === 'profile') {
    ElMessage.info('个人信息功能开发中')
  }
}
</script>


<style scoped>
.layout-container {
  height: 100vh;
}

.layout-aside {
  background-color: #001529;
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
  overflow: hidden;
}

.logo-section {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-section.collapsed {
  padding: 0;
}

.logo-text {
  color: #ffffff;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 1px;
  white-space: nowrap;
}

.side-menu {
  flex: 1;
  border-right: none;
  overflow-y: auto;
}

.side-menu .el-menu-item {
  margin: 4px 8px;
  border-radius: 8px;
  width: calc(100% - 16px);
}

.side-menu .el-menu-item.is-active {
  background-color: #409eff !important;
}

.collapse-btn {
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffffa6;
  cursor: pointer;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  transition: color 0.2s;
}

.collapse-btn:hover {
  color: #ffffff;
}

.layout-header {
  background: #ffffff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 6px;
  transition: background-color 0.2s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.username {
  font-size: 14px;
  color: #303133;
}

.layout-main {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
