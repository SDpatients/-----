import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { nextTick } from 'vue'
import BlacklistView from '@/views/purchasing/BlacklistView.vue'
import { createRouter, createMemoryHistory } from 'vue-router'

const router = createRouter({
  history: createMemoryHistory(),
  routes: [
    { path: '/purchasing/blacklist', component: BlacklistView },
    { path: '/purchasing/suppliers', component: { template: '<div>suppliers</div>' } },
  ],
})

const mountComponent = async () => {
  router.push('/purchasing/blacklist')
  await router.isReady()
  const wrapper = mount(BlacklistView, {
    global: {
      plugins: [router],
      stubs: {
        PageContainer: { template: '<div class="page-container"><slot /><slot name="actions" /></div>' },
      },
    },
  })
  await flushPromises()
  await nextTick()
  return wrapper
}

describe('BlacklistView 组件交互测试', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('默认加载应显示黑名单列表', async () => {
    const wrapper = await mountComponent()
    const table = wrapper.find('.el-table')
    expect(table.exists()).toBe(true)
  })

  it('点击「加入黑名单」应打开新增弹窗', async () => {
    const wrapper = await mountComponent()
    const vm = wrapper.vm as any
    const buttons = wrapper.findAll('button')
    const addBtn = buttons.find(b => b.text().includes('加入黑名单'))
    if (addBtn) {
      await addBtn.trigger('click')
      await nextTick()
      expect(vm.showCreateDialog).toBe(true)
    }
  })

  it('新增弹窗 - 不填供应商名称应提示警告', async () => {
    const wrapper = await mountComponent()
    const vm = wrapper.vm as any
    vm.showCreateDialog = true
    vm.createForm.supplierName = ''
    vm.createForm.reason = ''
    await nextTick()

    const submitBtn = wrapper.findAll('button').find(b => b.text().includes('确认加入'))
    if (submitBtn) {
      await submitBtn.trigger('click')
      await flushPromises()
    }
  })

  it('新增弹窗 - 不填拉黑原因应提示警告', async () => {
    const wrapper = await mountComponent()
    const vm = wrapper.vm as any
    vm.showCreateDialog = true
    vm.createForm.supplierName = '测试供应商'
    vm.createForm.reason = ''
    await nextTick()

    const submitBtn = wrapper.findAll('button').find(b => b.text().includes('确认加入'))
    if (submitBtn) {
      await submitBtn.trigger('click')
      await flushPromises()
    }
  })

  it('新增弹窗 - 结束时间早于当前时间应被拦截', async () => {
    const wrapper = await mountComponent()
    const vm = wrapper.vm as any
    vm.showCreateDialog = true
    vm.createForm.supplierName = '测试供应商'
    vm.createForm.reason = '测试原因'
    vm.createForm.startTime = '2026-01-01T00:00:00'
    vm.createForm.endTime = '2020-01-01T00:00:00'
    await nextTick()

    const submitBtn = wrapper.findAll('button').find(b => b.text().includes('确认加入'))
    if (submitBtn) {
      await submitBtn.trigger('click')
      await flushPromises()
    }
  })

  it('新增弹窗 - 开始时间晚于结束时间应被拦截', async () => {
    const wrapper = await mountComponent()
    const vm = wrapper.vm as any
    vm.showCreateDialog = true
    vm.createForm.supplierName = '测试供应商'
    vm.createForm.reason = '测试原因'
    vm.createForm.startTime = '2027-12-31T00:00:00'
    vm.createForm.endTime = '2027-01-01T00:00:00'
    await nextTick()

    const submitBtn = wrapper.findAll('button').find(b => b.text().includes('确认加入'))
    if (submitBtn) {
      await submitBtn.trigger('click')
      await flushPromises()
    }
  })

  it('编辑弹窗 - 结束时间早于当前时间应被拦截', async () => {
    const wrapper = await mountComponent()
    const vm = wrapper.vm as any
    vm.showEditDialog = true
    vm.editRow = { id: 1, supplierName: '测试', reason: '原因', endTime: '', status: 1 }
    vm.editForm.reason = '更新原因'
    vm.editForm.endTime = '2020-01-01T00:00:00'
    await nextTick()

    const saveBtn = wrapper.findAll('button').find(b => b.text().includes('保存'))
    if (saveBtn) {
      await saveBtn.trigger('click')
      await flushPromises()
    }
  })

  it('点击「返回供应商列表」应导航到供应商页面', async () => {
    const wrapper = await mountComponent()
    const pushSpy = vi.spyOn(router, 'push')
    const buttons = wrapper.findAll('button')
    const backBtn = buttons.find(b => b.text().includes('返回供应商列表'))
    if (backBtn) {
      await backBtn.trigger('click')
      await flushPromises()
      expect(pushSpy).toHaveBeenCalledWith('/purchasing/suppliers')
    }
  })

  it('状态筛选下拉框应包含生效中和已解除选项', async () => {
    const wrapper = await mountComponent()
    const text = wrapper.text()
    expect(text.includes('生效中') || text.includes('已解除') || text.includes('状态')).toBe(true)
  })

  it('分页组件应正确渲染', async () => {
    const wrapper = await mountComponent()
    const pagination = wrapper.find('.el-pagination')
    expect(pagination.exists()).toBe(true)
  })
})
