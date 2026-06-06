/**
 * components/business/RiskWarningList.vue 测试
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createMemoryHistory, type Router } from 'vue-router'

vi.mock('@/stores/user', () => ({
  useUserStore: vi.fn(),
}))

import { useUserStore } from '@/stores/user'
import RiskWarningList from '@/components/business/RiskWarningList.vue'
import type { RiskWarning } from '@/types/dashboard'

const buildRouter = (): Router =>
  createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/purchasing/orders', component: { template: '<div/>' } },
      { path: '/purchasing/asn', component: { template: '<div/>' } },
      { path: '/supplier/orders', component: { template: '<div/>' } },
      { path: '/supplier/deliveries', component: { template: '<div/>' } },
      { path: '/messages', component: { template: '<div/>' } },
    ],
  })

const baseRisks: RiskWarning[] = [
  { id: 'order_pending', title: '未确认订单量', level: 'warning', module: 'order_pending', targetPath: '', count: 3 },
  { id: 'delivery_delay', title: '计划送货已逾期', level: 'danger', module: 'delivery_delay', targetPath: '', count: 1 },
]

describe('RiskWarningList', () => {
  let router: Router

  beforeEach(async () => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    router = buildRouter()
    await router.push('/')
    await router.isReady()
  })

  it('should render count, title and module for each risk', () => {
    vi.mocked(useUserStore).mockReturnValue({ user: { userType: 'internal' } } as any)
    const wrapper = mount(RiskWarningList, {
      props: { risks: baseRisks },
      global: { plugins: [router] },
    })

    expect(wrapper.text()).toContain('未确认订单量')
    expect(wrapper.text()).toContain('3 单')
    expect(wrapper.text()).toContain('计划送货已逾期')
    expect(wrapper.text()).toContain('1 单')
    expect(wrapper.text()).toContain('order_pending')
    expect(wrapper.text()).toContain('delivery_delay')
  })

  it('should show empty state when all counts are zero', () => {
    vi.mocked(useUserStore).mockReturnValue({ user: { userType: 'internal' } } as any)
    const wrapper = mount(RiskWarningList, {
      props: {
        risks: [
          { id: 'order_pending', title: 'A', level: 'warning', module: 'order_pending', targetPath: '', count: 0 },
          { id: 'delivery_delay', title: 'B', level: 'danger', module: 'delivery_delay', targetPath: '', count: 0 },
        ],
      },
      global: { plugins: [router] },
    })

    expect(wrapper.text()).toContain('当前无业务风险，状态良好')
    expect(wrapper.findAll('.risk-row').length).toBe(0)
  })

  it('should not show empty state if at least one count is non-zero', () => {
    vi.mocked(useUserStore).mockReturnValue({ user: { userType: 'internal' } } as any)
    const wrapper = mount(RiskWarningList, {
      props: { risks: baseRisks },
      global: { plugins: [router] },
    })

    expect(wrapper.find('.risk-empty').exists()).toBe(false)
    expect(wrapper.findAll('.risk-row').length).toBe(2)
  })

  it('should mark danger-level rows with risk-row--danger class', () => {
    vi.mocked(useUserStore).mockReturnValue({ user: { userType: 'internal' } } as any)
    const wrapper = mount(RiskWarningList, {
      props: { risks: baseRisks },
      global: { plugins: [router] },
    })

    const rows = wrapper.findAll('.risk-row')
    expect(rows[0].classes()).not.toContain('risk-row--danger')
    expect(rows[1].classes()).toContain('risk-row--danger')
  })

  it('should navigate to purchasing path for internal user on click', async () => {
    vi.mocked(useUserStore).mockReturnValue({ user: { userType: 'internal' } } as any)
    const pushSpy = vi.spyOn(router, 'push')
    const wrapper = mount(RiskWarningList, {
      props: { risks: baseRisks },
      global: { plugins: [router] },
    })

    await wrapper.findAll('.risk-row')[0].trigger('click')
    expect(pushSpy).toHaveBeenCalledWith('/purchasing/orders')

    await wrapper.findAll('.risk-row')[1].trigger('click')
    expect(pushSpy).toHaveBeenCalledWith('/purchasing/asn')
  })

  it('should navigate to supplier path for supplier user on click', async () => {
    vi.mocked(useUserStore).mockReturnValue({ user: { userType: 'supplier' } } as any)
    const pushSpy = vi.spyOn(router, 'push')
    const wrapper = mount(RiskWarningList, {
      props: { risks: baseRisks },
      global: { plugins: [router] },
    })

    await wrapper.findAll('.risk-row')[0].trigger('click')
    expect(pushSpy).toHaveBeenCalledWith('/supplier/orders')

    await wrapper.findAll('.risk-row')[1].trigger('click')
    expect(pushSpy).toHaveBeenCalledWith('/supplier/deliveries')
  })

  it('should fall back to targetPath when module is unmapped', async () => {
    vi.mocked(useUserStore).mockReturnValue({ user: { userType: 'internal' } } as any)
    const pushSpy = vi.spyOn(router, 'push')
    const wrapper = mount(RiskWarningList, {
      props: {
        risks: [
          { id: 'unknown_type', title: '未知', level: 'warning', module: 'unknown_type', targetPath: '/messages', count: 1 },
        ],
      },
      global: { plugins: [router] },
    })

    await wrapper.find('.risk-row').trigger('click')
    expect(pushSpy).toHaveBeenCalledWith('/messages')
  })

  it('should navigate to /messages when targetPath is empty and module is unmapped', async () => {
    vi.mocked(useUserStore).mockReturnValue({ user: { userType: 'internal' } } as any)
    const pushSpy = vi.spyOn(router, 'push')
    const wrapper = mount(RiskWarningList, {
      props: {
        risks: [
          { id: 'unknown_type', title: '未知', level: 'warning', module: 'unknown_type', targetPath: '', count: 1 },
        ],
      },
      global: { plugins: [router] },
    })

    await wrapper.find('.risk-row').trigger('click')
    expect(pushSpy).toHaveBeenCalledWith('/messages')
  })
})
