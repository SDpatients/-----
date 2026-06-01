import { afterAll, afterEach, beforeAll } from 'vitest'
import { config } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import { server } from './mocks/server'

// ---------- MSW Mock Server ----------
beforeAll(() => server.listen({ onUnhandledRequest: 'warn' }))
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

// ---------- Vue Test Utils 全局配置 ----------
// 全局注册 Element Plus，使 el-button / el-tag / el-badge 等组件可用
config.global.plugins = [ElementPlus]

// Stub Element Plus Icons（图标组件不需要真实渲染）
config.global.stubs = {
  'el-icon': { template: '<i class="el-icon-stub" />' },
  // element-plus 图标组件（lucide 等）
  'el-popover': true,
}