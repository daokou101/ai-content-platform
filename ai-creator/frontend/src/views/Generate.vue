<template>
  <div class="generate-page">
    <div class="page-header">
      <h2 class="page-title">AI 内容生成</h2>
      <p class="page-desc">输入关键词，选择模板，AI 将为您生成优质内容</p>
    </div>

    <div class="generate-grid">
      <div class="form-section">
        <el-card shadow="never" class="form-card">
          <template #header>
            <span class="card-title">生成配置</span>
          </template>

          <el-form label-position="top" size="large">
            <el-form-item label="关键词">
              <el-input
                v-model="form.keywords"
                placeholder="请输入内容关键词，多个关键词用逗号分隔"
                type="textarea"
                :rows="2"
              />
            </el-form-item>

            <el-form-item label="选择模板类型">
              <div class="template-radio-group">
                <div
                  v-for="tpl in templates"
                  :key="tpl.type"
                  class="template-card"
                  :class="{ active: form.templateType === tpl.type }"
                  @click="form.templateType = tpl.type"
                >
                  <div class="template-icon">
                    <el-icon :size="24">
                      <component :is="getTemplateIcon(tpl.type)" />
                    </el-icon>
                  </div>
                  <div class="template-name">{{ tpl.name }}</div>
                </div>
              </div>
            </el-form-item>

            <el-form-item label="选择模型">
              <el-select v-model="form.modelName" placeholder="选择 AI 模型" style="width: 100%">
                <el-option
                  v-for="m in models"
                  :key="m.model"
                  :label="m.name"
                  :value="m.model"
                >
                  <span>{{ m.name }}</span>
                  <span class="model-tag">{{ m.model }}</span>
                </el-option>
              </el-select>
            </el-form-item>

            <el-form-item label="额外提示（可选）">
              <el-input
                v-model="form.additionalPrompt"
                placeholder="可以对 AI 提出额外的要求，如语气、风格、长度等"
                type="textarea"
                :rows="3"
              />
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="generating"
                :disabled="!form.keywords || !form.templateType"
                class="generate-btn"
                @click="startGeneration"
              >
                <el-icon><MagicStick /></el-icon>
                {{ generating ? '生成中...' : '开始生成' }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>

      <div class="result-section">
        <el-card shadow="never" class="result-card">
          <template #header>
            <div class="result-header">
              <span class="card-title">生成结果</span>
              <div class="result-actions">
                <el-button
                  size="small"
                  :disabled="!generatedContent"
                  @click="copyContent"
                >
                  <el-icon><CopyDocument /></el-icon>
                  复制
                </el-button>
                <el-button
                  type="primary"
                  size="small"
                  :disabled="!generatedContent || saving"
                  :loading="saving"
                  @click="handleSave"
                >
                  <el-icon><Download /></el-icon>
                  保存
                </el-button>
                <el-button
                  size="small"
                  :disabled="!generatedContent"
                  @click="clearResult"
                >
                  <el-icon><Delete /></el-icon>
                  清除
                </el-button>
              </div>
            </div>
          </template>

          <div class="result-body">
            <div v-if="!generatedContent && !generating" class="result-placeholder">
              <el-icon :size="64" color="#dcdfe6"><EditPen /></el-icon>
              <p>配置参数后点击"开始生成"</p>
              <p>生成的内容将在此处显示</p>
            </div>

            <div v-else class="result-content">
              <div v-if="generating" class="streaming-indicator">
                <span class="streaming-dot"></span>
                <span>AI 正在生成中...</span>
              </div>
              <div class="content-text" v-html="renderedContent"></div>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { EditPen, CopyDocument, Download, Delete } from '@element-plus/icons-vue'
import { getTemplates, getModels } from '@/api/ai'
import { saveContent } from '@/api/content'
import { useRouter } from 'vue-router'

const router = useRouter()

const templates = ref<any[]>([])
const models = ref<any[]>([])
const generating = ref(false)
const saving = ref(false)
const generatedContent = ref('')
const eventSource = ref<EventSource | null>(null)

const form = reactive({
  keywords: '',
  templateType: '',
  additionalPrompt: '',
  modelName: ''
})

const renderedContent = computed(() => {
  return generatedContent.value
    .replace(/\n/g, '<br>')
    .replace(/\s{2}/g, '&nbsp;&nbsp;')
})

const templateIcons: Record<string, string> = {
  'article': 'Document',
  'marketing': 'TrendCharts',
  'social': 'ChatLineSquare',
  'seo': 'Search',
  'summary': 'Notebook',
  'default': 'Edit'
}

function getTemplateIcon(type: string): string {
  return templateIcons[type] || templateIcons.default
}

onMounted(async () => {
  try {
    const res: any = await getTemplates()
    templates.value = res.data || res
    if (templates.value.length > 0) {
      form.templateType = templates.value[0].type
    }
  } catch {
    templates.value = [
      { type: 'article', name: '文章写作' },
      { type: 'marketing', name: '营销文案' },
      { type: 'social', name: '社交媒体' },
      { type: 'seo', name: 'SEO 优化' },
      { type: 'summary', name: '内容摘要' }
    ]
  }

  try {
    const res: any = await getModels()
    models.value = res.data || res
    if (models.value.length > 0) {
      form.modelName = models.value[0].model
    }
  } catch {
    models.value = [
      { name: 'DeepSeek', model: 'deepseek-chat' }
    ]
    form.modelName = 'deepseek-chat'
  }
}) as any

onUnmounted(() => {
  closeEventSource()
})

function closeEventSource() {
  if (eventSource.value) {
    eventSource.value.close()
    eventSource.value = null
  }
}

function startGeneration() {
  if (!form.keywords || !form.templateType) {
    ElMessage.warning('请填写关键词并选择模板类型')
    return
  }

  generatedContent.value = ''
  generating.value = true

  // Use EventSource for SSE streaming
  const params = new URLSearchParams()
  params.append('keywords', form.keywords)
  params.append('templateType', form.templateType)
  if (form.additionalPrompt) {
    params.append('additionalPrompt', form.additionalPrompt)
  }
  if (form.modelName) {
    params.append('model', form.modelName)
  }

  const token = localStorage.getItem('token')
  closeEventSource()

  // Create EventSource connection with token as query param
  const url = `/api/ai/generate/sse?${params.toString()}${token ? `&token=${token}` : ''}`
  eventSource.value = new EventSource(url)

  eventSource.value.addEventListener('message', (event: MessageEvent) => {
    try {
      const data = JSON.parse(event.data)
      if (data.content) {
        generatedContent.value += data.content
      } else if (data.text) {
        generatedContent.value += data.text
      } else if (data.data) {
        generatedContent.value += data.data
      }
    } catch {
      // If not JSON, treat as plain text chunk
      if (event.data && event.data !== '[DONE]') {
        generatedContent.value += event.data
      }
    }
  })

  eventSource.value.addEventListener('error', () => {
    // EventSource auto-reconnects; if we get a final message or error that ends generation
    if (generating.value) {
      generating.value = false
      if (!generatedContent.value) {
        ElMessage.error('生成失败，请重试')
      } else {
        ElMessage.success('生成完成')
      }
    }
    closeEventSource()
  })

  eventSource.value.addEventListener('done', () => {
    generating.value = false
    ElMessage.success('生成完成')
    closeEventSource()
  })

  eventSource.value.addEventListener('end', () => {
    generating.value = false
    ElMessage.success('生成完成')
    closeEventSource()
  })

  // Handle case where EventSource doesn't get proper close events
  // Timeout fallback
  setTimeout(() => {
    if (generating.value && generatedContent.value) {
      generating.value = false
      closeEventSource()
    }
  }, 120000)
}

async function handleSave() {
  if (!generatedContent.value) return

  saving.value = true
  try {
    const data = {
      title: form.keywords.split(',')[0].trim(),
      keywords: form.keywords,
      content: generatedContent.value,
      templateType: form.templateType
    }
    const res: any = await saveContent(data)
    ElMessage.success('保存成功')
    const id = res?.data?.id || res?.id
    if (id) {
      router.push(`/contents/${id}`)
    }
  } catch {
    // Error message handled by interceptor
  } finally {
    saving.value = false
  }
}

function copyContent() {
  navigator.clipboard.writeText(generatedContent.value).then(() => {
    ElMessage.success('已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

function clearResult() {
  generatedContent.value = ''
}
</script>

<style scoped>
.generate-page {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.model-tag {
  float: right;
  color: #909399;
  font-size: 12px;
  margin-left: 8px;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
}

.page-desc {
  font-size: 14px;
  color: #909399;
}

.generate-grid {
  display: grid;
  grid-template-columns: 380px 1fr;
  gap: 20px;
  align-items: start;
}

.form-card,
.result-card {
  border-radius: 12px;
  border: 1px solid #ebeef5;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.template-radio-group {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.template-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px 12px;
  border: 2px solid #ebeef5;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s;
}

.template-card:hover {
  border-color: #b3d8ff;
  background-color: #f5faff;
}

.template-card.active {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.template-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background-color: #f5f7fa;
  color: #409eff;
}

.template-card.active .template-icon {
  background-color: #409eff;
  color: #ffffff;
}

.template-name {
  font-size: 13px;
  font-weight: 500;
  color: #606266;
}

.template-card.active .template-name {
  color: #409eff;
}

.generate-btn {
  width: 100%;
  border-radius: 8px;
  font-size: 15px;
  letter-spacing: 1px;
}

/* Result section */
.result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.result-actions {
  display: flex;
  gap: 8px;
}

.result-body {
  min-height: 400px;
  max-height: 600px;
  overflow-y: auto;
}

.result-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 400px;
  color: #c0c4cc;
}

.result-placeholder p {
  margin-top: 12px;
  font-size: 14px;
}

.streaming-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background-color: #ecf5ff;
  border-radius: 6px;
  margin-bottom: 16px;
  font-size: 13px;
  color: #409eff;
}

.streaming-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #409eff;
  animation: pulse 1.2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 0.4; transform: scale(0.8); }
  50% { opacity: 1; transform: scale(1.2); }
}

.content-text {
  font-size: 14px;
  line-height: 1.8;
  color: #303133;
  white-space: pre-wrap;
  word-break: break-word;
}

@media (max-width: 900px) {
  .generate-grid {
    grid-template-columns: 1fr;
  }
}
</style>
