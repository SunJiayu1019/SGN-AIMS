import { createApp } from 'vue'
import App from './App.vue'
import router from './router/index.js'

// Element Plus 组件库（整站美化所需的 UI 组件）
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
// Element Plus 图标
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 全局主题样式（设计令牌 + 原生控件美化）
import './styles/theme.css'

const app = createApp(App)

// 注册 Element Plus（中文语言包）
app.use(ElementPlus, { locale: zhCn })

// 全量注册图标组件
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 注册路由
app.use(router)

app.mount('#app')
