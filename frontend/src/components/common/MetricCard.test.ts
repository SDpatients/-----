/**
 * components/common/MetricCard.vue 测试
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import MetricCard from '@/components/common/MetricCard.vue'

// Mock vue-router
vi.mock('vue-router', () => ({
  useRouter: vi.fn(() => ({ push: vi.fn() })),
  useRoute: vi.fn(() => ({ path: '/' })),
}))

import { useRouter } from 'vue-router'

describe('MetricCard', () => {
  const defaultProps = {
    label: '今日订单',
    value: 128,
    trend: '较昨日 +12%',
    tone: 'blue' as const,
  }

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should render label, value and trend', () => {
    const wrapper = mount(MetricCard, { props: defaultProps })
    expect(wrapper.find('.metric-label').text()).toBe('今日订单')
    expect(wrapper.find('.metric-value').text()).toBe('128')
    expect(wrapper.find('.metric-trend').text()).toBe('较昨日 +12%')
  })

  it('should apply tone class', () => {
    const wrapper = mount(MetricCard, { props: defaultProps })
    expect(wrapper.find('.metric-card--blue').exists()).toBe(true)
  })

  it('should apply green tone', () => {
    const wrapper = mount(MetricCard, {
      props: { ...defaultProps, tone: 'green' },
    })
    expect(wrapper.find('.metric-card--green').exists()).toBe(true)
  })

  it('should apply orange tone', () => {
    const wrapper = mount(MetricCard, {
      props: { ...defaultProps, tone: 'orange' },
    })
    expect(wrapper.find('.metric-card--orange').exists()).toBe(true)
  })

  it('should apply red tone', () => {
    const wrapper = mount(MetricCard, {
      props: { ...defaultProps, tone: 'red' },
    })
    expect(wrapper.find('.metric-card--red').exists()).toBe(true)
  })

  it('should be clickable when path is provided', () => {
    const wrapper = mount(MetricCard, {
      props: { ...defaultProps, path: '/orders' },
    })
    expect(wrapper.find('.metric-card--clickable').exists()).toBe(true)
  })

  it('should not be clickable when path is not provided', () => {
    const wrapper = mount(MetricCard, { props: defaultProps })
    expect(wrapper.find('.metric-card--clickable').exists()).toBe(false)
  })

  it('should navigate on click when path is set', async () => {
    const push = vi.fn()
    vi.mocked(useRouter).mockReturnValue({ push } as any)

    const wrapper = mount(MetricCard, {
      props: { ...defaultProps, path: '/orders' },
    })
    await wrapper.trigger('click')
    expect(push).toHaveBeenCalledWith('/orders')
  })

  it('should not navigate when path is not set', async () => {
    const push = vi.fn()
    vi.mocked(useRouter).mockReturnValue({ push } as any)

    const wrapper = mount(MetricCard, { props: defaultProps })
    await wrapper.trigger('click')
    expect(push).not.toHaveBeenCalled()
  })
})