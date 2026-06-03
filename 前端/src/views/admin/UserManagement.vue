<template>
  <div class="page-container">
    <div class="page-header">
      <h3>用户管理</h3>
      <el-input v-model="keyword" placeholder="搜索用户名/昵称/邮箱" style="width: 300px" clearable @clear="fetchUsers" @keyup.enter="fetchUsers">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
    </div>

    <el-card shadow="never">
      <el-table :data="userList" stripe v-loading="loading" empty-text="暂无用户">
        <el-table-column label="头像" width="60" align="center">
          <template #default="{ row }">
            <el-avatar :size="32" :src="row.avatar" />
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="roleType(row.role)" size="small">{{ roleLabel(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="level" label="权限等级" width="100" align="center">
          <template #default="{ row }">{{ levelLabel(row.level) }}</template>
        </el-table-column>
        <el-table-column prop="points" label="积分" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
              {{ row.status === 0 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" plain @click="openEdit(row)">编辑</el-button>
            <el-button size="small" @click="openResetPwd(row)">改密</el-button>
            <el-button size="small" type="danger" plain @click="handleDelete(row)" v-if="userStore.isSuperAdmin() && row.role !== 'SUPER_ADMIN'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @change="fetchUsers"
        />
      </div>
    </el-card>

    <!-- 编辑用户对话框 -->
    <el-dialog v-model="editVisible" title="编辑用户" width="500px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" />
        </el-form-item>
        <el-form-item label="权限等级">
          <el-select v-model="editForm.level">
            <el-option :value="0" label="普通用户 (Lv.0)" />
            <el-option :value="1" label="VIP用户 (Lv.1)" />
            <el-option :value="2" label="管理员 (Lv.2)" />
            <el-option :value="3" label="超级管理员 (Lv.3)" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="editForm.status" :active-value="0" :inactive-value="1" active-text="正常" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog v-model="pwdVisible" title="重置密码" width="400px">
      <el-form>
        <el-form-item label="新密码">
          <el-input v-model="newPassword" type="password" show-password placeholder="输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleResetPwd">确认重置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { userApi } from '../../api/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const userStore = useUserStore()
const userList = ref([])
const loading = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')

// 编辑对话框
const editVisible = ref(false)
const editForm = ref({})
const editUserId = ref(null)
const saving = ref(false)

// 密码对话框
const pwdVisible = ref(false)
const newPassword = ref('')
const resetUserId = ref(null)

const roleType = role => ({
  'SUPER_ADMIN': 'danger',
  'ADMIN': 'warning',
  'VIP_USER': 'success',
  'NORMAL_USER': 'info'
}[role] || 'info')

const roleLabel = role => ({
  'SUPER_ADMIN': '超级管理员',
  'ADMIN': '管理员',
  'VIP_USER': 'VIP用户',
  'NORMAL_USER': '普通用户'
}[role] || role)

const levelLabel = level => ['普通用户', 'VIP用户', '管理员', '超级管理员'][level] || '未知'

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await userApi.getList({ pageNum: pageNum.value, pageSize: pageSize.value, keyword: keyword.value })
    userList.value = res.data.records
    total.value = res.data.total
  } catch (e) { /* ignore */ }
  loading.value = false
}

const openEdit = (user) => {
  editUserId.value = user.id
  editForm.value = { ...user }
  editVisible.value = true
}

const handleSave = async () => {
  saving.value = true
  try {
    await userApi.update(editUserId.value, {
      nickname: editForm.value.nickname,
      email: editForm.value.email,
      level: editForm.value.level,
      status: editForm.value.status
    })
    ElMessage.success('更新成功')
    editVisible.value = false
    fetchUsers()
  } catch (e) { /* ignore */ }
  saving.value = false
}

const openResetPwd = (user) => {
  resetUserId.value = user.id
  newPassword.value = ''
  pwdVisible.value = true
}

const handleResetPwd = async () => {
  if (!newPassword.value || newPassword.value.length < 6) {
    ElMessage.warning('密码至少6位')
    return
  }
  saving.value = true
  try {
    await userApi.resetPassword(resetUserId.value, newPassword.value)
    ElMessage.success('密码重置成功')
    pwdVisible.value = false
  } catch (e) { /* ignore */ }
  saving.value = false
}

const handleDelete = async (user) => {
  await ElMessageBox.confirm(`确定要删除用户「${user.username}」吗？此操作不可恢复！`, '警告', { type: 'warning' })
  await userApi.delete(user.id)
  ElMessage.success('用户已删除')
  fetchUsers()
}

onMounted(fetchUsers)
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
