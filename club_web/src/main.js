import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

// --- 新增代码开始 ---
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// --- 新增代码结束 ---

const app = createApp(App)

app.use(router)
app.use(ElementPlus) // 使用 Element Plus

app.mount('#app')