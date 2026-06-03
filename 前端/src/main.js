import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import './style.css'

const app = createApp(App)

// 注册所有 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// Pinia 状态管理 + 持久化插件（刷新页面状态不丢失）
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

app.use(ElementPlus)
app.use(router)
app.use(pinia)
app.mount('#app')
