<template>
  <div class="page-container">
    <!-- 积分概览 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="8">
        <el-card shadow="hover" class="dashboard-card" style="border-top: 4px solid #e6a23c; text-align: center">
          <div class="stat-value" style="color: #e6a23c">{{ pointsBalance }}</div>
          <div class="stat-label">当前积分</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="dashboard-card" style="border-top: 4px solid #67c23a; text-align: center; cursor: pointer" @click="handleSignIn">
          <el-icon :size="32" color="#67c23a"><Select /></el-icon>
          <div class="stat-label" style="margin-top: 8px">{{ signedIn ? '今日已签到' : '每日签到 +10积分' }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="dashboard-card" style="border-top: 4px solid #409eff; text-align: center; cursor: pointer" @click="showRecharge = true">
          <el-icon :size="32" color="#409eff"><Coin /></el-icon>
          <div class="stat-label" style="margin-top: 8px">积分充值</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 权限升级 -->
    <el-card class="mb-4">
      <template #header><span style="font-weight: 600">权限等级</span></template>
      <el-steps :active="currentLevel" finish-status="success" align-center>
        <el-step title="普通用户" description="初始等级" />
        <el-step title="VIP用户" description="需500积分" />
        <el-step title="管理员" description="需2000积分" />
        <el-step title="超级管理员" description="最高权限" />
      </el-steps>
    </el-card>

    <!-- 积分明细 -->
    <el-card>
      <template #header><span style="font-weight: 600">积分明细</span></template>
      <el-table :data="logs" stripe v-if="logs.length">
        <el-table-column prop="createTime" label="时间" width="180" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.points > 0 ? 'success' : 'danger'" size="small">{{ typeMap[row.type] || row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="points" label="变动" width="100" align="center">
          <template #default="{ row }">
            <span :style="{ color: row.points > 0 ? '#67c23a' : '#f56c6c', fontWeight: 600 }">
              {{ row.points > 0 ? '+' : '' }}{{ row.points }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" />
      </el-table>
      <el-empty v-else description="暂无积分记录" />
    </el-card>

    <!-- 充值对话框 -->
    <el-dialog v-model="showRecharge" title="模拟充值" width="400px">
      <el-form>
        <el-form-item label="充值金额">
          <el-input-number v-model="rechargeAmount" :min="1" :max="1000" />
        </el-form-item>
        <el-form-item label="获得积分">
          <span style="color: #e6a23c; font-weight: 600; font-size: 18px">{{ rechargeAmount * 10 }} 积分</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRecharge = false">取消</el-button>
        <el-button type="warning" :loading="recharging" @click="handleRecharge">确认充值</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { pointsApi } from '../api/points'
import { ElMessage } from 'element-plus'
import { Select, Coin } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const pointsBalance = ref(0)
const logs = ref([])
const signedIn = ref(false)
const showRecharge = ref(false)
const rechargeAmount = ref(10)
const recharging = ref(false)

const currentLevel = computed(() => userStore.userInfo?.level || 0)

const typeMap = {
  'SIGN_IN': '每日签到',
  'TASK_DONE': '任务奖励',
  'RECHARGE': '充值',
  'UPGRADE': '升级消费',
  'ADMIN_ADJUST': '管理员调整'
}

onMounted(async () => {
  try {
    const [balanceRes, logsRes] = await Promise.all([
      pointsApi.getBalance(),
      pointsApi.getLogs()
    ])
    pointsBalance.value = balanceRes.data.points
    logs.value = logsRes.data
  } catch (e) { /* ignore */ }
})

const handleSignIn = async () => {
  if (signedIn.value) return
  try {
    const res = await pointsApi.signIn()
    ElMessage.success(res.data.message)
    pointsBalance.value = res.data.totalPoints
    signedIn.value = true
  } catch (e) { /* ignore */ }
}

const handleRecharge = async () => {
  recharging.value = true
  try {
    const res = await pointsApi.recharge(rechargeAmount.value)
    ElMessage.success(res.data.message)
    pointsBalance.value = res.data.totalPoints
    showRecharge.value = false
    // 刷新日志
    const logsRes = await pointsApi.getLogs()
    logs.value = logsRes.data
  } catch (e) { /* ignore */ }
  recharging.value = false
}
</script>

<style scoped>
.mb-4 { margin-bottom: 16px; }
</style>
