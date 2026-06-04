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

// 修复 Chrome 警告：第三方组件（如 element-plus）注册了非 passive 的 wheel/touchmove
// 监听器，会阻塞滚动。这里在未显式声明 passive 选项时默认设为 true。
;(function patchPassiveEventListener() {
  if (typeof window === 'undefined') return
  const proto = EventTarget.prototype as any
  const originalAdd = proto.addEventListener
  if (originalAdd.__patched) return
  proto.addEventListener = function (type: string, listener: any, options?: any) {
    const isScrollBlocking = type === 'wheel' || type === 'touchstart' || type === 'touchmove'
    if (isScrollBlocking) {
      if (options === undefined) {
        options = { passive: true }
      } else if (typeof options === 'object' && options !== null && options.passive === undefined) {
        options = { ...options, passive: true }
      } else if (typeof options === 'boolean') {
        options = { capture: options, passive: true }
      }
    }
    return originalAdd.call(this, type, listener, options)
  }
  proto.addEventListener.__patched = true
})()

// 当 VITE_USE_MOCK=true 时，为 request 实例注册 mock 拦截器
setupMock(request)

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.directive('permission', permissionDirective)

app.mount('#app')
