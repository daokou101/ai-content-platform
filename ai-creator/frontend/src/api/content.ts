import request from '@/utils/request'

export interface ContentData {
  id?: number
  title?: string
  summary?: string
  content?: string
  keywords?: string
  templateType?: string
  categoryId?: number
  categoryName?: string
  status?: string
  favorite?: boolean
  createdTime?: string
  updatedTime?: string
  createdBy?: string
}

export interface CategoryData {
  id?: number
  name?: string
}

export interface VersionData {
  id?: number
  version?: number
  content?: string
  createdTime?: string
  createdBy?: string
}

export function getContents(page: number, size: number, keyword?: string, templateType?: string) {
  const params: Record<string, any> = { page, size }
  if (keyword) params.keyword = keyword
  if (templateType) params.templateType = templateType
  return request.get('/contents', { params })
}

export function getContent(id: number) {
  return request.get(`/contents/${id}`)
}

export function saveContent(data: ContentData) {
  return request.post('/contents', data)
}

export function updateContent(id: number, data: ContentData) {
  return request.put(`/contents/${id}`, data)
}

export function deleteContent(id: number) {
  return request.delete(`/contents/${id}`)
}

export function getCategories() {
  return request.get('/contents/categories')
}

export function createCategory(name: string) {
  return request.post('/contents/categories', { name })
}

export function deleteCategory(id: number) {
  return request.delete(`/contents/categories/${id}`)
}

export function getVersions(id: number) {
  return request.get(`/contents/${id}/versions`)
}

export function rollback(id: number, version: number) {
  return request.post(`/contents/${id}/rollback/${version}`)
}

export function toggleFavorite(id: number) {
  return request.post(`/contents/${id}/favorite`)
}
