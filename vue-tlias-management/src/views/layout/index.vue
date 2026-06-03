<script setup>
/**
 * 布局组件（Layout）
 *
 * 【什么是布局组件？】
 * 这个组件就是页面的"外壳"，包含：
 * 1. 顶部的导航栏（显示系统名称、用户名、退出按钮）
 * 2. 左侧的菜单栏（导航链接）
 * 3. 中间的页面内容区（<router-view>）
 *
 * 【vue-router 的钩子函数说明】
 * - useRouter()：获取路由实例，用于跳转页面
 * - useRoute()：获取当前路由信息
 *
 * 【onMounted 是什么？】
 * onMounted 是 Vue 的"生命周期钩子函数"。
 * 当组件被"挂载"到页面上（也就是显示出来）时，会自动执行 onMounted 中的代码。
 * 类似在页面加载完成后自动执行的初始化操作。
 */

import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessageBox } from 'element-plus'

// 路由实例
const router = useRouter()
// 认证状态管理
const authStore = useAuthStore()

// 当前登录用户的用户名
// 从 authStore 中获取，如果未登录就显示"未登录"
const currentUser = ref('')

/**
 * onMounted：组件挂载完成后执行
 *
 * 这里我们读取 authStore 中的用户名并显示在顶部导航栏中
 */
onMounted(() => {
  currentUser.value = authStore.userName || '未登录'
})

/**
 * 退出登录
 *
 * 执行流程：
 * 1. 弹出确认框询问用户是否确认退出
 * 2. 用户点击"确定"
 * 3. 清除 authStore 中的登录信息
 * 4. 跳转到登录页
 */
const handleLogout = () => {
  // ElMessageBox.confirm：Element Plus 的确认对话框
  ElMessageBox.confirm(
    '确定要退出登录吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    // 用户点击了"确定"
    // 调用 authStore 的 logout() 方法清除登录信息
    // logout() 会清除 Pinia 和 localStorage 中的 token
    authStore.logout()

    // 跳转到登录页
    router.push('/login')
  }).catch(() => {
    // 用户点击了"取消"，什么都不做
  })
}

/**
 * 修改密码（目前只是一个占位功能，尚未实现）
 */
const handleChangePassword = () => {
  // 暂时只弹出提示
  ElMessageBox.alert('修改密码功能正在开发中...', '提示')
}
</script>

<template>
  <div class="common-layout">
    <el-container>
      <!-- ===== Header 顶部区域 ===== -->
      <el-header class="header">
        <!-- 系统名称 -->
        <span class="title">Tlias智能学习辅助系统</span>

        <!-- 右侧操作区域 -->
        <span class="right_tool">
          <!-- 显示当前登录用户 -->
          <span class="user-info">
            <el-icon><User /></el-icon>
            {{ currentUser }}
          </span>

          <!-- 修改密码链接 -->
          <a href="javascript:void(0)" @click="handleChangePassword">
            <el-icon><EditPen /></el-icon> 修改密码
          </a>
          <span class="divider">|</span>

          <!-- 退出登录链接 -->
          <!-- @click="handleLogout" 点击时执行退出登录 -->
          <a href="javascript:void(0)" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon> 退出登录
          </a>
        </span>
      </el-header>

      <el-container>
        <!-- ===== 左侧菜单栏 ===== -->
        <el-aside width="200px" class="aside">
          <!-- el-menu router：开启路由模式，点击菜单项会自动跳转到对应的路由路径 -->
          <el-menu router>
            <!-- 首页菜单 -->
            <el-menu-item index="/index">
              <el-icon><Promotion /></el-icon> 首页
            </el-menu-item>

            <!-- 班级学员管理（可展开的子菜单） -->
            <el-sub-menu index="/manage">
              <template #title>
                <el-icon><Menu /></el-icon> 班级学员管理
              </template>
              <el-menu-item index="/clazz">
                <el-icon><HomeFilled /></el-icon>班级管理
              </el-menu-item>
              <el-menu-item index="/stu">
                <el-icon><UserFilled /></el-icon>学员管理
              </el-menu-item>
            </el-sub-menu>

            <!-- 系统信息管理 -->
            <el-sub-menu index="/system">
              <template #title>
                <el-icon><Tools /></el-icon>系统信息管理
              </template>
              <el-menu-item index="/dept">
                <el-icon><HelpFilled /></el-icon>部门管理
              </el-menu-item>
              <el-menu-item index="/emp">
                <el-icon><Avatar /></el-icon>员工管理
              </el-menu-item>
            </el-sub-menu>

            <!-- 数据统计管理 -->
            <el-sub-menu index="/report">
              <template #title>
                <el-icon><Histogram /></el-icon>数据统计管理
              </template>
              <el-menu-item index="/empReport">
                <el-icon><InfoFilled /></el-icon>员工信息统计
              </el-menu-item>
              <el-menu-item index="/stuReport">
                <el-icon><Share /></el-icon>学员信息统计
              </el-menu-item>
              <el-menu-item index="/log">
                <el-icon><Document /></el-icon>日志信息统计
              </el-menu-item>
            </el-sub-menu>
          </el-menu>
        </el-aside>

        <!-- ===== 主内容区域 ===== -->
        <!-- <router-view> 是 Vue Router 提供的组件 -->
        <!-- 路由切换时，这里会显示对应的页面组件 -->
        <el-main>
          <router-view></router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<style scoped>
.header {
  background-image: linear-gradient(to right, #00547d, #007fa4, #00aaa0, #00d072, #a8eb12);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.title {
  color: white;
  font-size: 40px;
  font-family: 楷体;
  line-height: 60px;
  font-weight: bolder;
}

.right_tool {
  display: flex;
  align-items: center;
  gap: 10px;
  color: white;
}

.right_tool a {
  color: white;
  text-decoration: none;
  cursor: pointer;
}

.right_tool a:hover {
  text-decoration: underline;
}

.divider {
  color: white;
  margin: 0 5px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-right: 10px;
  font-size: 14px;
  background: rgba(255, 255, 255, 0.15);
  padding: 4px 12px;
  border-radius: 4px;
}

.aside {
  width: 220px;
  border-right: 1px solid #ccc;
  height: 730px;
}
</style>
