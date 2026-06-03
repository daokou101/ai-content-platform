<template>
  <div class="page-container">
    <h3 class="mb-4">{{ isEdit ? '编辑任务' : '创建任务' }}</h3>
    <el-card>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="任务标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入任务标题" />
        </el-form-item>
        <el-form-item label="任务描述">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="任务详细描述" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="优先级">
              <el-select v-model="form.priority">
                <el-option :value="0" label="低" />
                <el-option :value="1" label="中" />
                <el-option :value="2" label="高" />
                <el-option :value="3" label="紧急" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="负责人">
              <el-select v-model="form.assigneeId" filterable placeholder="选择负责人" clearable>
                <el-option v-for="u in userList" :key="u.id" :label="u.nickname" :value="u.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="奖励积分">
              <el-input-number v-model="form.pointsReward" :min="0" :max="1000" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="截止时间">
          <el-date-picker v-model="form.deadline" type="datetime" placeholder="选择截止时间" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">提交</el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { taskApi } from '../../api/task'
import { userApi } from '../../api/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)
const userList = ref([])

const form = reactive({
  title: '',
  description: '',
  priority: 1,
  assigneeId: null,
  pointsReward: 0,
  deadline: null
})

const rules = {
  title: [{ required: true, message: '请输入任务标题', trigger: 'blur' }]
}

onMounted(async () => {
  try {
    const res = await userApi.getList({ pageNum: 1, pageSize: 100 })
    userList.value = res.data.records || []
  } catch (e) { /* ignore */ }
})

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await taskApi.create({
      ...form,
      deadline: form.deadline ? form.deadline.toISOString() : null
    })
    ElMessage.success('任务创建成功！')
    router.push('/tasks')
  } catch (e) { /* ignore */ }
  submitting.value = false
}
</script>

<style scoped>
.mb-4 { margin-bottom: 16px; }
</style>
