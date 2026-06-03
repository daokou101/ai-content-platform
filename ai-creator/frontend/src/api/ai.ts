import request from '@/utils/request'

export interface TemplateData {
  id?: number
  name?: string
  type?: string
  description?: string
}

export function getTemplates() {
  return request.get('/ai/templates')
}

export function getModels() {
  return request.get('/ai/models')
}

export function generate(keywords: string, templateType: string, additionalPrompt?: string, modelName?: string) {
  return request.post('/ai/generate', { keywords, templateType, additionalPrompt, modelName })
}
