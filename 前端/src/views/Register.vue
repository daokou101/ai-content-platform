<template>
  <div class="register-container">
    <div class="register-card">
      <h2>创建账号</h2>
      <p class="subtitle">注册加入智能任务协作平台</p>

      <el-form ref="formRef" :model="registerForm" :rules="rules" size="large">
        <el-form-item prop="username">
          <el-input v-model="registerForm.username" placeholder="用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input v-model="registerForm.nickname" placeholder="昵称（选填）" :prefix-icon="EditPen" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="密码（至少6位）" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" class="register-btn" @click="handleRegister">
            {{ loading ? '注册中...' : '注 册' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-footer">
        <span>已有账号？</span>
        <router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { User, Lock, EditPen } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  username: '',
  nickname: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.register(registerForm)
    ElMessage.success('注册成功！')
    router.push('/dashboard')
  } catch (e) {
    // 错误已由响应拦截器处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.register-card {
  width: 420px;
  padding: 40px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}
.register-card h2 {
  text-align: center;
  font-size: 24px;
  color: #303133;
}
.subtitle {
  text-align: center;
  color: #909399;
  font-size: 14px;
  margin: 8px 0 32px;
}
.register-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  border-radius: 8px;
}
.register-footer {
  text-align: center;
  font-size: 14px;
  color: #909399;
}
.register-footer a {
  color: #409eff;
  text-decoration: none;
  margin-left: 4px;
}
</style>
