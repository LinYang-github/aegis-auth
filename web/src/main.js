import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import './style.css'
import './styles/theme.css'
import App from './App.vue'

const app = createApp(App)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

app.use(ElementPlus, {
    locale: zhCn,
})
app.mount('#app')
