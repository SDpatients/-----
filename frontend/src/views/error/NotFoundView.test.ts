/**
 * views/error/NotFoundView.vue 测试
 */
import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import NotFoundView from '@/views/error/NotFoundView.vue'

vi.mock('vue-router', () => ({
  useRouter: vi.fn(() => ({ push: vi.fn() })),
  useRoute: vi.fn(() => ({ path: '/' })),
}))

import { useRouter } from 'vue-router'

describe('NotFoundView', () => {
  it('should render 404 heading', () => {
    const wrapper = mount(NotFoundView)
    expect(wrapper.find('h1').text()).toBe('404')
  })

  it('should render description text', () => {
    const wrapper = mount(NotFoundView)
    expect(wrapper.text()).toContain('页面不存在或已被移动')
  })

  it('should have a button to go back', () => {
    const wrapper = mount(NotFoundView)
    expect(wrapper.find('button').exists()).toBe(true)
  })

  it('should navigate to dashboard on button click', async () => {
    const push = vi.fn()
    vi.mocked(useRouter).mockReturnValue({ push } as any)

    const wrapper = mount(NotFoundView)
    await wrapper.find('button').trigger('click')
    expect(push).toHaveBeenCalledWith('/purchasing/dashboard')
  })
})