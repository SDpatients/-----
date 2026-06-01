/**
 * components/business/DashboardMetricGrid.vue 测试
 */
import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import DashboardMetricGrid from '@/components/business/DashboardMetricGrid.vue'
import type { DashboardMetric } from '@/types/dashboard'

vi.mock('vue-router', () => ({
  useRouter: vi.fn(() => ({ push: vi.fn() })),
  useRoute: vi.fn(() => ({ path: '/' })),
}))

describe('DashboardMetricGrid', () => {
  const metrics: DashboardMetric[] = [
    { label: '订单数', value: 128, trend: '+12%', tone: 'blue' },
    { label: '供应商数', value: 45, trend: '+3', tone: 'green', path: '/suppliers' },
    { label: '待处理', value: 8, trend: '-2', tone: 'orange' },
    { label: '异常', value: 0, trend: '无', tone: 'red' },
  ]

  it('should render all MetricCards', () => {
    const wrapper = mount(DashboardMetricGrid, {
      props: { metrics },
    })
    const cards = wrapper.findAllComponents({ name: 'MetricCard' })
    expect(cards).toHaveLength(4)
  })

  it('should pass correct props to child cards', () => {
    const wrapper = mount(DashboardMetricGrid, {
      props: { metrics },
    })
    const labels = wrapper.findAll('.metric-label')
    const values = wrapper.findAll('.metric-value')
    expect(labels[0].text()).toBe('订单数')
    expect(values[1].text()).toBe('45')
  })

  it('should render correctly with empty metrics', () => {
    const wrapper = mount(DashboardMetricGrid, {
      props: { metrics: [] },
    })
    expect(wrapper.findAll('.metric-label')).toHaveLength(0)
  })
})