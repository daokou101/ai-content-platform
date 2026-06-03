<template>
  <div class="content-list-page">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">内容管理</h2>
        <p class="page-desc">管理和查看所有已生成的 AI 内容</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="$router.push('/generate')">
          <el-icon><MagicStick /></el-icon>
          新建生成
        </el-button>
        <el-button @click="showCategoryDialog = true">
          <el-icon><Collection /></el-icon>
          分类管理
        </el-button>
      </div>
    </div>

    <!-- Search Bar -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm" size="large">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索标题/关键词"
            clearable
            :prefix-icon="Search"
            style="width: 220px"
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="模板类型">
          <el-select
            v-model="searchForm.templateType"
            placeholder="全部类型"
            clearable
            style="width: 160px"
            @change="handleSearch"
          >
            <el-option
              v-for="tpl in templates"
              :key="tpl.type"
              :label="tpl.name"
              :value="tpl.type"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Content Table -->
    <el-card shadow="never" class="table-card">
      <el-table
        :data="contents"
        v-loading="loading"
        stripe
        style="width: 100%"
        @row-dblclick="handleRowClick"
      >
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="content-title" @click="$router.push(`/contents/${row.id}`)">
              {{ row.title || '无标题' }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="templateType" label="模板类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getTemplateTagType(row.templateType)" size="small">
              {{ getTemplateName(row.templateType) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'published' ? 'success' : 'info'" size="small">
              {{ row.status === 'published' ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="收藏" width="80" align="center">
          <template #default="{ row }">
            <el-button
              :type="row.favorite ? 'warning' : 'default'"
              :icon="Star"
              circle
              size="small"
              @click="handleToggleFavorite(row)"
            />
          </template>
        </el-table-column>

        <el-table-column prop="createdTime" label="创建时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatTime(row.createdTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="$router.push(`/contents/${row.id}`)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-popconfirm
              title="确定删除该内容吗？"
              confirm-button-text="确定"
              cancel-button-text="取消"
              @confirm="handleDelete(row)"
            >
              <template #reference>
                <el-button type="danger" size="small">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchContents"
          @current-change="fetchContents"
        />
      </div>
    </el-card>

    <!-- Category Management Dialog -->
    <el-dialog
      v-model="showCategoryDialog"
      title="分类管理"
      width="450px"
      :close-on-click-modal="false"
    >
      <div class="category-section">
        <div class="add-category">
          <el-input
            v-model="newCategoryName"
            placeholder="输入分类名称"
            size="large"
            @keyup.enter="handleAddCategory"
          >
            <template #append>
              <el-button @click="handleAddCategory">
                <el-icon><Plus /></el-icon>
                添加
              </el-button>
            </template>
          </el-input>
        </div>
        <div class="category-list">
          <div v-for="cat in categories" :key="cat.id" class="category-item">
            <el-icon><Folder /></el-icon>
            <span class="category-name">{{ cat.name }}</span>
            <el-button
              type="danger"
              :icon="Delete"
              circle
              size="small"
              text
              @click="handleDeleteCategory(cat)"
            />
          </div>
          <el-empty v-if="categories.length === 0" description="暂无分类" :image-size="60" />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Star, View, Delete, Plus, Folder } from '@element-plus/icons-vue'
import {
  getContents,
  deleteContent,
  toggleFavorite,
  getCategories,
  createCategory,
  deleteCategory
} from '@/api/content'
import { getTemplates } from '@/api/ai'
import { useRouter } from 'vue-router'

const router = useRouter()

const loading = ref(false)
const contents = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const templates = ref<any[]>([])
const categories = ref<any[]>([])
const showCategoryDialog = ref(false)
const newCategoryName = ref('')

const searchForm = reactive({
  keyword: '',
  templateType: ''
})

onMounted(() => {
  fetchContents()
  fetchTemplates()
  fetchCategories()
})

async function fetchContents() {
  loading.value = true
  try {
    const res: any = await getContents(
      currentPage.value,
      pageSize.value,
      searchForm.keyword || undefined,
      searchForm.templateType || undefined
    )
    const data = res.data || res
    contents.value = data.records || data.content || data.list || data || []
    total.value = data.total || (Array.isArray(data) ? data.length : 0)
  } catch {
    contents.value = []
    total.value = 0
  } finally {
    loading.value = false
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

async function fetchCategories() {
  try {
    const res: any = await getCategories()
    categories.value = res.data || res || []
  } catch {
    categories.value = []
  }
}

function handleSearch() {
  currentPage.value = 1
  fetchContents()
}

function resetSearch() {
  searchForm.keyword = ''
  searchForm.templateType = ''
  currentPage.value = 1
  fetchContents()
}

async function handleDelete(row: any) {
  try {
    await deleteContent(row.id)
    ElMessage.success('删除成功')
    fetchContents()
  } catch {
    // Error handled by interceptor
  }
}

async function handleToggleFavorite(row: any) {
  try {
    await toggleFavorite(row.id)
    row.favorite = !row.favorite
    ElMessage.success(row.favorite ? '已收藏' : '已取消收藏')
  } catch {
    // Error handled by interceptor
  }
}

function handleRowClick(row: any) {
  router.push(`/contents/${row.id}`)
}

function getTemplateName(type: string): string {
  const tpl = templates.value.find((t: any) => t.type === type)
  return tpl?.name || type || '未知'
}

function getTemplateTagType(type: string): string {
  const types: Record<string, string> = {
    article: 'primary',
    marketing: 'success',
    social: 'warning',
    seo: 'info',
    summary: ''
  }
  return types[type] || ''
}

function formatTime(time: string): string {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

async function handleAddCategory() {
  if (!newCategoryName.value.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }
  try {
    await createCategory(newCategoryName.value.trim())
    ElMessage.success('添加成功')
    newCategoryName.value = ''
    fetchCategories()
  } catch {
    // Error handled by interceptor
  }
}

async function handleDeleteCategory(cat: any) {
  try {
    await ElMessageBox.confirm(`确定删除分类「${cat.name}」吗？`, '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteCategory(cat.id)
    ElMessage.success('删除成功')
    fetchCategories()
  } catch {
    // cancelled or error
  }
}
</script>

<style scoped>
.content-list-page {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 20px;
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

.header-actions {
  display: flex;
  gap: 10px;
}

.search-card {
  margin-bottom: 20px;
  border-radius: 12px;
  border: 1px solid #ebeef5;
}

.table-card {
  border-radius: 12px;
  border: 1px solid #ebeef5;
}

.content-title {
  color: #409eff;
  cursor: pointer;
  font-weight: 500;
}

.content-title:hover {
  text-decoration: underline;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* Category management */
.category-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.add-category {
  margin-bottom: 8px;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 350px;
  overflow-y: auto;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fafafa;
}

.category-name {
  flex: 1;
  font-size: 14px;
  color: #303133;
}
</style>
