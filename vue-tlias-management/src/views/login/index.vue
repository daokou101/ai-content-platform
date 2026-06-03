<script setup>
/**
 * 登录页面
 *
 * 【<script setup> 是什么？】
 * Vue 3 的语法糖，在 <script setup> 中写的代码会在组件初始化时执行一次。
 * 里面的变量和方法可以直接在模板中使用，不需要 return。
 *
 * 【ref 是什么？】
 * ref 是 Vue 3 的"响应式"API。用 ref 包裹的数据被称为"响应式数据"。
 * 响应式意味着：当数据变化时，页面会自动更新。
 * 比如 loginForm.value.username 变了，输入框中显示的内容也会自动变。
 *
 * 使用 ref 时，在 JS 中需要通过 .value 获取/设置值，
 * 但在模板（<template>）中不需要 .value，Vue 会自动解包。
 */

import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { loginApi } from '@/api/user'
import { useAuthStore } from '@/stores/auth'

// ===== 路由 =====
// useRouter() 是 Vue Router 提供的方法，返回路由实例
// 可以用 router.push() 跳转到其他页面
const router = useRouter()

// ===== 状态管理 =====
// useAuthStore() 获取认证状态管理
const authStore = useAuthStore()

// ===== 表单数据 =====
// 登录表单：包含用户名和密码
// ref({...}) 创建响应式对象
// loginForm.value.username 可以获取/设置用户名
let loginForm = ref({
  username: '',
  password: ''
})

/**
 * 登录按钮点击事件
 *
 * async 关键字：表示这个函数是"异步"的
 * await 关键字：等待异步操作完成（比如等待后端返回结果）
 *
 * 【执行流程】
 * 1. 点击登录按钮 → 执行 login() 函数
 * 2. 调用 loginApi() 发送 POST 请求到后端
 * 3. 等待后端验证用户名密码
 * 4. 验证成功 → 保存 token → 跳转到首页
 * 5. 验证失败 → 显示错误提示
 */
const login = async () => {
  // 1. 基本校验：检查用户名和密码是否为空
  if (!loginForm.value.username || !loginForm.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  // 2. 调用登录 API
  // try-catch：捕获异步操作中的异常
  try {
    // 发送 POST 请求到 /api/login，携带用户名和密码
    const result = await loginApi(loginForm.value.username, loginForm.value.password)

    // 3. 判断是否登录成功
    // result.code === 1 表示成功（参照后端 Result 类的定义）
    if (result.code === 1) {
      // 登录成功！

      // 保存 token 和用户名到状态管理（authStore）
      // 这样其他页面就知道用户已登录
      authStore.login(result.data.token, result.data.name)

      // 显示成功提示
      ElMessage.success('登录成功')

      // 跳转到首页（/index）
      // router.push('/index') 相当于在浏览器地址栏输入 http://localhost:5173/index
      router.push('/index')
    } else {
      // 登录失败（用户名或密码错误）
      ElMessage.error(result.msg || '登录失败')
    }
  } catch (error) {
    // 网络错误或服务器异常
    console.error('登录请求失败:', error)
    ElMessage.error('网络错误，请检查后端服务是否启动')
  }
}

/**
 * 重置按钮点击事件
 * 清空用户名和密码输入框
 */
const reset = () => {
  loginForm.value.username = ''
  loginForm.value.password = ''
}
</script>

<template>
  <div id="container">
    <div class="login-form">
      <!-- el-form：Element Plus 的表单组件 -->
      <el-form label-width="80px">
        <p class="title">Tlias智能学习辅助系统</p>

        <!-- 用户名输入框 -->
        <!-- v-model：双向绑定，输入框的内容变化会自动更新到 loginForm.username -->
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>

        <!-- 密码输入框 -->
        <!-- type="password" 表示密码框，输入的内容会显示为圆点 -->
        <el-form-item label="密码" prop="password">
          <el-input type="password" v-model="loginForm.password" placeholder="请输入密码"></el-input>
        </el-form-item>

        <!-- 按钮 -->
        <el-form-item>
          <!-- @click="login"：点击按钮时执行 login() 函数 -->
          <el-button class="button" type="primary" @click="login">登 录</el-button>
          <el-button class="button" type="info" @click="reset">重 置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
/* scoped：样式只在当前组件生效，不影响其他组件 */
#container {
  padding: 10%;
  height: 410px;
  background-image: url('../../assets/bg1.jpg');
  background-repeat: no-repeat;
  background-size: cover;
}

.login-form {
  max-width: 400px;
  padding: 30px;
  margin: 0 auto;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.5);
  background-color: white;
}

.title {
  font-size: 30px;
  font-family: '楷体';
  text-align: center;
  margin-bottom: 30px;
  font-weight: bold;
}

.button {
  margin-top: 30px;
  width: 120px;
}
</style>
