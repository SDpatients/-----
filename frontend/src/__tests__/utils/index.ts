import { mount, type MountingOptions } from '@vue/test-utils'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import type { Component } from 'vue'

/**
 * 挂载组件的工厂函数，自动注入 Pinia、Element Plus 等全局依赖
 * 使用方式：mountComponent(MyComponent, { props: { ... } })
 */
export function mountComponent<T extends Component>(
  component: T,
  options?: MountingOptions<T>,
) {
  const pinia = createPinia()

  return mount(component, {
    global: {
      plugins: [pinia, ElementPlus],
    },
    ...options,
  } as any)
}

/**
 * 等待指定毫秒数
 */
export function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

/**
 * 为 fetch / axios 超时模拟提供一个可控的延迟响应
 */
export function delayedResponse<T>(data: T, delay = 100): Promise<T> {
  return new Promise((resolve) => {
    setTimeout(() => resolve(data), delay)
  })
}