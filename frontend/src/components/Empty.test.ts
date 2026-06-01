/**
 * components/Empty.vue 测试
 */
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import Empty from '@/components/Empty.vue'

describe('Empty', () => {
  it('should render empty text', () => {
    const wrapper = mount(Empty)
    expect(wrapper.text()).toBe('empty')
  })
})