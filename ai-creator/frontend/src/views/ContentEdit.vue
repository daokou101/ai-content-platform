<template>
  <div class="content-edit-page">
    <div class="page-header">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回内容列表
      </el-button>
    </div>

    <div class="edit-grid" v-loading="loading">
      <!-- Main Content -->
      <div class="main-section">
        <el-card shadow="never" class="content-card">
          <template #header>
            <div class="card-header">
              <div class="header-tabs">
                <el-switch
                  v-model="isEditing"
                  active-text="编辑模式"
                  inactive-text="预览模式"
                  inline-prompt
                />
              </div>
              <div class="header-actions">
                <el-button type="primary" :loading="saving" @click="handleSave">
                  <el-icon><Check /></el-icon>
                  保存
                </el-button>
              </div>
            </div>
          </template>

          <!-- Title -->
          <div class="field-group">
            <div class="field-label">标题</div>
            <el-input
              v-if="isEditing"
              v-model="editForm.title"
              size="large"
              placeholder="请输入标题"
            />
            <h1 v-else class="display-title">{{ contentData.title || '无标题' }}</h1>
          </div>

          <!-- Summary -->
          <div class="field-group">
            <div class="field-label">摘要</div>
            <el-input
              v-if="isEditing"
              v-model="editForm.summary"
              type="textarea"
              :rows="2"
              placeholder="请输入摘要"
            />
            <p v-else class="display-summary">{{ contentData.summary || '暂无摘要' }}</p>
          </div>

          <!-- Keywords -->
          <div class="field-group">
            <div class="field-label">关键词</div>
            <el-input
              v-if="isEditing"
              v-model="editForm.keywords"
              placeholder="多个关键词用逗号分隔"
            />
            <div v-else class="keywords-display">
              <el-tag
                v-for="kw in keywordList"
                :key="kw"
                size="small"
                style="margin: 2px 4px 2px 0"
              >
                {{ kw }}
              </el-tag>
            </div>
          </div>

          <!-- Category -->
          <div class="field-group">
            <div class="field-label">分类</div>
            <el-select
              v-if="isEditing"
              v-model="editForm.categoryId"
              placeholder="选择分类"
              style="width: 240px"
            >
              <el-option label="无分类" :value="undefined" />
              <el-option
                v-for="cat in categories"
                :key="cat.id"
                :label="cat.name"
                :value="cat.id"
              />
            </el-select>
            <el-tag v-else type="info">
              {{ contentData.categoryName || '未分类' }}
            </el-tag>
          </div>

          <!-- Content Body -->
          <div class="field-group">
            <div class="field-label">内容</div>
            <el-input
              v-if="isEditing"
              v-model="editForm.content"
              type="textarea"
              :rows="16"
              placeholder="请输入内容"
            />
            <div v-else class="content-body">
              {{ contentData.content || '暂无内容' }}
            </div>
          </div>
        </el-card>
      </div>

      <!-- Sidebar -->
      <div class="side-section">
        <!-- Info Card -->
        <el-card shadow="never" class="info-card">
          <template #header>
            <span class="card-title">内容信息</span>
          </template>
          <div class="info-items">
            <div class="info-item">
              <span class="info-label">状态</span>
              <el-tag :type="contentData.status === 'published' ? 'success' : 'info'" size="small">
                {{ contentData.status === 'published' ? '已发布' : '草稿' }}
              </el-tag>
            </div>
            <div class="info-item">
              <span class="info-label">模板</span>
              <span class="info-value">{{ templateName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">创建时间</span>
              <span class="info-value">{{ formatTime(contentData.createdTime) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">更新时间</span>
              <span class="info-value">{{ formatTime(contentData.updatedTime) }}</span>
            </div>
          </div>
        </el-card>

        <!-- Version History -->
        <el-card shadow="never" class="version-card">
          <template #header>
            <span class="card-title">版本历史</span>
          </template>
          <div v-if="versions.length === 0" class="no-versions">
            暂无历史版本
          </div>
          <div v-else class="version-timeline">
            <el-timeline>
              <el-timeline-item
                v-for="ver in versions"
                :key="ver.id"
                :timestamp="formatTime(ver.createdTime)"
                placement="top"
              >
                <div class="version-item">
                  <span class="version-number">版本 {{ ver.version }}</span>
                  <el-button
                    v-if="ver.version !== currentVersion"
                    type="primary"
                    text
                    size="small"
                    @click="handleRollback(ver)"
                  >
                    回滚
                  </el-button>
                  <el-tag v-else size="small" type="success">当前版本</el-tag>
                </div>
              </el-timeline-item>
            </el-timeline>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Check } from '@element-plus/icons-vue'
import {
  getContent,
  updateContent,
  getCategories,
  getVersions,
  rollback
} from '@/api/content'
import { getTemplates } from '@/api/ai'

const route = useRoute()
const router = useRouter()

const contentId = computed(() => Number(route.params.id))
const loading = ref(false)
const saving = ref(false)
const isEditing = ref(false)

const contentData = ref<any>({})
const versions = ref<any[]>([])
const categories = ref<any[]>([])
const templates = ref<any[]>([])

const editForm = reactive({
  title: '',
  summary: '',
  keywords: '',
  content: '',
  categoryId: undefined as number | undefined,
  status: ''
})

const currentVersion = computed(() => {
  if (versions.value.length > 0) {
    return versions.value[versions.value.length - 1]?.version || 1
  }
  return 1
})

const keywordList = computed(() => {
  if (!contentData.value.keywords) return []
  return contentData.value.keywords.split(/[,，、\s]+/).filter(Boolean)
})

const templateName = computed(() => {
  const tpl = templates.value.find((t: any) => t.type === contentData.value.templateType)
  return tpl?.name || contentData.value.templateType || '未知'
})

onMounted(async () => {
  await Promise.all([fetchContent(), fetchCategories(), fetchTemplates()])
})

async function fetchContent() {
  loading.value = true
  try {
    const res: any = await getContent(contentId.value)
    const data = res.data || res
    contentData.value = data
    editForm.title = data.title || ''
    editForm.summary = data.summary || ''
    editForm.keywords = data.keywords || ''
    editForm.content = data.content || ''
    editForm.categoryId = data.categoryId || undefined
    editForm.status = data.status || 'draft'

    // Fetch versions after getting content
    await fetchVersions()
  } catch {
    ElMessage.error('获取内容失败')
    router.push('/contents')
  } finally {
    loading.value = false
  }
}

async function fetchVersions() {
  try {
    const res: any = await getVersions(contentId.value)
    versions.value = res.data || res || []
  } catch {
    versions.value = []
  }
}

async function fetchCategories() {
  try {
    const res: any = await getCategories()
    categories.value = res.data || res || []
  } catch {
    categories.value = []
  }
}

async function fetchTemplates() {
  try {
    const res: any = await getTemplates()
    templates.value = res.data || res
  } catch {
    templates.value = [
      { type: 'article', name: '文章写作' },
      { type: 'marketing', name: '营销文案' },
      { type: 'social', name: '社交媒体' },
      { type: 'seo', name: 'SEO 优化' },
      { type: 'summary', name: '内容摘要' }
    ]
  }
}

async function handleSave() {
  saving.value = true
  try {
    const data = {
      title: editForm.title,
      summary: editForm.summary,
      keywords: editForm.keywords,
      content: editForm.content,
      categoryId: editForm.categoryId
    }
    const res: any = await updateContent(contentId.value, data)
    ElMessage.success('保存成功')
    isEditing.value = false
    // Refresh content data
    await fetchContent()
  } catch {
    // Error handled by interceptor
  } finally {
    saving.value = false
  }
}

async function handleRollback(ver: any) {
  try {
    await ElMessageBox.confirm(
      `确定要回滚到版本 ${ver.version} 吗？当前未保存的更改将丢失。`,
      '确认回滚',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await rollback(contentId.value, ver.version)
    ElMessage.success(`已回滚到版本 ${ver.version}`)
    await fetchContent()
  } catch {
    // cancelled or error
  }
}

function goBack() {
  router.push('/contents')
}

function formatTime(time: string): string {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}
</script>

<style scoped>
.content-edit-page {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 16px;
}

.edit-grid {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 20px;
  align-items: start;
}

.main-section {
  min-width: 0;
}

.content-card {
  border-radius: 12px;
  border: 1px solid #ebeef5;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.field-group {
  margin-bottom: 20px;
}

.field-label {
  font-size: 13px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.display-title {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  line-height: 1.4;
}

.display-summary {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  background: #f5f7fa;
  padding: 12px 16px;
  border-radius: 8px;
}

.keywords-display {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.content-body {
  font-size: 14px;
  line-height: 1.8;
  color: #303133;
  white-space: pre-wrap;
  word-break: break-word;
  background: #fafafa;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  min-height: 200px;
}

/* Sidebar */
.side-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-card,
.version-card {
  border-radius: 12px;
  border: 1px solid #ebeef5;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.info-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.info-label {
  font-size: 13px;
  color: #909399;
}

.info-value {
  font-size: 13px;
  color: #303133;
  font-weight: 500;
}

.no-versions {
  text-align: center;
  padding: 20px;
  color: #909399;
  font-size: 13px;
}

.version-timeline {
  max-height: 400px;
  overflow-y: auto;
}

.version-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.version-number {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
}

@media (max-width: 900px) {
  .edit-grid {
    grid-template-columns: 1fr;
  }
}
</style>
