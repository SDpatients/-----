/**
 * views/error/ForbiddenView.vue 测试
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import ForbiddenView from '@/views/error/ForbiddenView.vue'

vi.mock('vue-router', () => ({
  useRouter: vi.fn(() => ({ push: vi.fn() })),
  useRoute: vi.fn(() => ({ path: '/' })),
}))

vi.mock('@/stores/user', () => ({
  useUserStore: vi.fn(),
}))

import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

describe('ForbiddenView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    vi.mocked(useRouter).mockReturnValue({ push: vi.fn() } as any)
  })

  it('should render 403 heading', () => {
    vi.mocked(useUserStore).mockReturnValue({
      user: null,
      token: '',
      permissions: [],
    } as any)

    const wrapper = mount(ForbiddenView)
    expect(wrapper.find('h1').text()).toBe('403')
  })

  it('should render no-permission message', () => {
    vi.mocked(useUserStore).mockReturnValue({
      user: null,
      token: '',
      permissions: [],
    } as any)

    const wrapper = mount(ForbiddenView)
    expect(wrapper.text()).toContain('暂无访问权限')
  })

  it('should navigate supplier to supplier dashboard', async () => {
    const push = vi.fn()
    vi.mocked(useRouter).mockReturnValue({ push } as any)
    vi.mocked(useUserStore).mockReturnValue({
      user: { userType: 'supplier' },
      token: 'xxx',
    } as any)

    const wrapper = mount(ForbiddenView)
    await wrapper.find('button').trigger('click')
    expect(push).toHaveBeenCalledWith('/supplier/dashboard')
  })

  it('should navigate internal user to purchasing dashboard', async () => {
    const push = vi.fn()
    vi.mocked(useRouter).mockReturnValue({ push } as any)
    vi.mocked(useUserStore).mockReturnValue({
      user: { userType: 'internal' },
      token: 'xxx',
    } as any)

    const wrapper = mount(ForbiddenView)
    await wrapper.find('button').trigger('click')
    expect(push).toHaveBeenCalledWith('/purchasing/dashboard')
  })

  it('should redirect to login when no token', async () => {
    const push = vi.fn()
    vi.mocked(useRouter).mockReturnValue({ push } as any)
    vi.mocked(useUserStore).mockReturnValue({
      user: null,
      token: '',
    } as any)

    const wrapper = mount(ForbiddenView)
    await wrapper.find('button').trigger('click')
    expect(push).toHaveBeenCalledWith('/login')
  })
})