/**
 * components/business/StatusTag.vue 测试
 */
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import StatusTag from '@/components/business/StatusTag.vue'

describe('StatusTag', () => {
  it('should render status text for known status code', () => {
    const wrapper = mount(StatusTag, {
      props: { value: '4' },
    })
    expect(wrapper.text()).toBe('审核通过')
  })

  it('should render with prefix for order status', () => {
    const wrapper = mount(StatusTag, {
      props: { value: '2', prefix: '订单' },
    })
    expect(wrapper.text()).toBe('已确认')
  })

  it('should render risk level', () => {
    const wrapper = mount(StatusTag, {
      props: { value: 'high', kind: 'risk' },
    })
    expect(wrapper.text()).toBe('高风险')
  })

  it('should use default label for unknown status', () => {
    const wrapper = mount(StatusTag, {
      props: { value: 'unknown-status-xyz' },
    })
    expect(wrapper.text()).toBe('unknown-status-xyz')
  })

  it('should render dash for empty value', () => {
    const wrapper = mount(StatusTag)
    expect(wrapper.text()).toBe('-')
  })

  it('should render draft status', () => {
    const wrapper = mount(StatusTag, { props: { value: 'draft' } })
    expect(wrapper.text()).toBe('草稿')
  })

  it('should render pending status', () => {
    const wrapper = mount(StatusTag, { props: { value: 'pending' } })
    expect(wrapper.text()).toBe('待审核')
  })

  it('should render active status', () => {
    const wrapper = mount(StatusTag, { props: { value: 'active' } })
    expect(wrapper.text()).toBe('合作中')
  })

  it('should render NCR status', () => {
    const wrapper = mount(StatusTag, { props: { value: '3', prefix: 'NCR' } })
    expect(wrapper.text()).toBe('待验证')
  })

  it('should render 8D closed status', () => {
    const wrapper = mount(StatusTag, { props: { value: '4', prefix: '8D' } })
    expect(wrapper.text()).toBe('已关闭')
  })
})