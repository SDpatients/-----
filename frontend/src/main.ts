import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './style.css'
import App from './App.vue'
import router from './router'
import { request } from './utils/request'
import { setupMock } from './api/mockInterceptor'
import permissionDirective from './directives/permission'

// 当 VITE_USE_MOCK=true 时，为 request 实例注册 mock 拦截器
setupMock(request)

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.directive('permission', permissionDirective)

app.mount('#app')
