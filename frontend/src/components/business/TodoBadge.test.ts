/**
 * components/business/TodoBadge.vue 测试
 */
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import TodoBadge from '@/components/business/TodoBadge.vue'

describe('TodoBadge', () => {
  it('should render badge with count', () => {
    const wrapper = mount(TodoBadge, { props: { count: 5 } })
    expect(wrapper.find('.el-badge').exists()).toBe(true)
  })

  it('should show value attribute when count > 0', () => {
    const wrapper = mount(TodoBadge, { props: { count: 3 } })
    const badge = wrapper.findComponent({ name: 'ElBadge' })
    expect(badge.props('value')).toBe(3)
  })

  it('should hide badge when count is 0', () => {
    const wrapper = mount(TodoBadge, { props: { count: 0 } })
    const badge = wrapper.findComponent({ name: 'ElBadge' })
    expect(badge.props('hidden')).toBe(true)
  })

  it('should show badge when count > 0', () => {
    const wrapper = mount(TodoBadge, { props: { count: 10 } })
    const badge = wrapper.findComponent({ name: 'ElBadge' })
    expect(badge.props('hidden')).toBe(false)
  })
})