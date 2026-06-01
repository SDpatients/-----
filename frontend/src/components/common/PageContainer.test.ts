/**
 * components/common/PageContainer.vue 测试
 */
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import PageContainer from '@/components/common/PageContainer.vue'

describe('PageContainer', () => {
  it('should render title', () => {
    const wrapper = mount(PageContainer, { props: { title: '订单管理' } })
    expect(wrapper.find('.page-title').text()).toBe('订单管理')
  })

  it('should render subtitle when provided', () => {
    const wrapper = mount(PageContainer, {
      props: { title: '首页', subtitle: '欢迎回来' },
    })
    expect(wrapper.find('.page-subtitle').text()).toBe('欢迎回来')
  })

  it('should not render subtitle when not provided', () => {
    const wrapper = mount(PageContainer, { props: { title: '设置' } })
    expect(wrapper.find('.page-subtitle').exists()).toBe(false)
  })

  it('should render default slot content', () => {
    const wrapper = mount(PageContainer, {
      props: { title: '页面' },
      slots: { default: '<div class="content">内容区域</div>' },
    })
    expect(wrapper.find('.content').text()).toBe('内容区域')
  })

  it('should render actions slot', () => {
    const wrapper = mount(PageContainer, {
      props: { title: '页面' },
      slots: { actions: '<button class="btn-export">导出</button>' },
    })
    expect(wrapper.find('.btn-export').text()).toBe('导出')
  })
})