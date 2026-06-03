<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
          <div class="login-logo">
            <el-icon :size="40" color="#409eff"><MagicStick /></el-icon>
          </div>
          <h2 class="login-title">AI 内容创作平台</h2>
          <p class="login-subtitle">{{ isLoginMode ? '欢迎回来，请登录您的账户' : '创建新账户，开始创作之旅' }}</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          size="large"
          class="login-form"
          @keyup.enter="handleSubmit"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              :loading="loading"
              class="submit-btn"
              @click="handleSubmit"
            >
              {{ isLoginMode ? '登 录' : '注 册' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          <span class="toggle-text">
            {{ isLoginMode ? '还没有账户？' : '已有账户？' }}
          </span>
          <el-button type="text" @click="toggleMode">
            {{ isLoginMode ? '立即注册' : '去登录' }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { User, Lock } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const isLoginMode = ref(true)

const form = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 个字符', trigger: 'blur' }
  ]
}

function toggleMode() {
  isLoginMode.value = !isLoginMode.value
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    if (isLoginMode.value) {
      await authStore.login(form.username, form.password)
    } else {
      await authStore.register(form.username, form.password)
      isLoginMode.value = true
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.login-page::before {
  content: '';
  position: absolute;
  width: 600px;
  height: 600px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.05);
  top: -200px;
  right: -200px;
}

.login-page::after {
  content: '';
  position: absolute;
  width: 400px;
  height: 400px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.05);
  bottom: -100px;
  left: -100px;
}

.login-container {
  width: 100%;
  max-width: 420px;
  padding: 20px;
  position: relative;
  z-index: 1;
}

.login-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-logo {
  margin-bottom: 16px;
}

.login-title {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 8px;
}

.login-subtitle {
  font-size: 14px;
  color: #909399;
}

.login-form {
  margin-bottom: 8px;
}

.login-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: #303133;
  padding-bottom: 4px;
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 2px;
  border-radius: 8px;
  margin-top: 8px;
}

.login-footer {
  text-align: center;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.toggle-text {
  font-size: 14px;
  color: #909399;
  margin-right: 4px;
}
</style>
