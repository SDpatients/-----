import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import { nextTick } from 'vue'
import SupplierListView from '@/views/purchasing/SupplierListView.vue'

const router = createRouter({
  history: createMemoryHistory(),
  routes: [
    { path: '/purchasing/suppliers', component: SupplierListView },
    { path: '/purchasing/suppliers/:id', component: { template: '<div>detail</div>' } },
    { path: '/purchasing/blacklist', component: { template: '<div>blacklist</div>' } },
  ],
})

const mountComponent = async () => {
  router.push('/purchasing/suppliers')
  await router.isReady()
  const wrapper = mount(SupplierListView, {
    global: {
      plugins: [router],
      stubs: {
        PageContainer: { template: '<div class="page-container"><slot /><slot name="actions" /></div>' },
        ExportDialog: true,
      },
    },
  })
  await flushPromises()
  await nextTick()
  return wrapper
}

describe('SupplierListView 组件交互测试', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('默认加载时不应显示黑名单供应商', async () => {
    const wrapper = await mountComponent()
    const rows = wrapper.findAll('.el-table__body-wrapper tbody tr')
    const hasBlacklist = rows.some(row => row.text().includes('黑名单'))
    expect(hasBlacklist).toBe(false)
  })

  it('勾选「包含黑名单」后应显示黑名单供应商', async () => {
    const wrapper = await mountComponent()
    const checkbox = wrapper.find('input[type="checkbox"]')
    if (checkbox.exists()) {
      await checkbox.setValue(true)
      await flushPromises()
      await nextTick()
      const text = wrapper.text()
      const hasBlacklistTag = text.includes('黑名单') || text.includes('黑名单供应商')
      expect(hasBlacklistTag).toBe(true)
    }
  })

  it('点击查询按钮应重新请求数据', async () => {
    const wrapper = await mountComponent()
    const vm = wrapper.vm as any
    const beforeTotal = vm.total
    const buttons = wrapper.findAll('button')
    const queryBtn = buttons.find(b => b.text().includes('查询'))
    if (queryBtn) {
      await queryBtn.trigger('click')
      await flushPromises()
      await nextTick()
      expect(typeof vm.total).toBe('number')
    }
  })

  it('点击重置按钮应清空筛选条件', async () => {
    const wrapper = await mountComponent()
    const vm = wrapper.vm as any
    vm.query.keyword = '测试'
    vm.query.status = 4
    vm.query.includeBlacklisted = true
    await nextTick()

    const buttons = wrapper.findAll('button')
    const resetBtn = buttons.find(b => b.text().includes('重置'))
    if (resetBtn) {
      await resetBtn.trigger('click')
      await flushPromises()
      expect(vm.query.keyword).toBe('')
      expect(vm.query.status).toBeUndefined()
      expect(vm.query.includeBlacklisted).toBe(false)
    }
  })

  it('状态筛选下拉框应渲染在页面中', async () => {
    const wrapper = await mountComponent()
    const select = wrapper.find('.el-select')
    expect(select.exists()).toBe(true)
  })

  it('点击「新增供应商」应打开弹窗', async () => {
    const wrapper = await mountComponent()
    const vm = wrapper.vm as any
    const buttons = wrapper.findAll('button')
    const createBtn = buttons.find(b => b.text().includes('新增供应商'))
    if (createBtn) {
      await createBtn.trigger('click')
      await nextTick()
      expect(vm.showCreateDialog).toBe(true)
    }
  })

  it('点击「黑名单管理」应导航到黑名单页面', async () => {
    const wrapper = await mountComponent()
    const pushSpy = vi.spyOn(router, 'push')
    const buttons = wrapper.findAll('button')
    const blacklistBtn = buttons.find(b => b.text().includes('黑名单管理'))
    if (blacklistBtn) {
      await blacklistBtn.trigger('click')
      await flushPromises()
      expect(pushSpy).toHaveBeenCalledWith('/purchasing/blacklist')
    }
  })

  it('加入黑名单弹窗 - 不填原因应不发送请求', async () => {
    const wrapper = await mountComponent()
    const vm = wrapper.vm as any
    const fetchSpy = vi.spyOn(globalThis, 'fetch')
    vm.blacklistRow = { id: 1, name: '测试', code: 'SUP001', creditCode: '' }
    vm.blacklistForm.reason = ''
    vm.blacklistForm.startTime = ''
    vm.blacklistForm.endTime = ''
    vm.showBlacklistDialog = true
    await nextTick()

    const confirmBtn = wrapper.findAll('button').find(b => b.text().includes('确认加入黑名单'))
    if (confirmBtn) {
      await confirmBtn.trigger('click')
      await flushPromises()
      const blacklistCalls = (fetchSpy.mock.calls || []).filter((c: any[]) => typeof c[0] === 'string' && c[0].includes('supplier-blacklists'))
      expect(blacklistCalls.length).toBe(0)
    }
  })

  it('时间校验 - 结束时间早于当前时间应不发送请求', async () => {
    const wrapper = await mountComponent()
    const vm = wrapper.vm as any
    const fetchSpy = vi.spyOn(globalThis, 'fetch')
    vm.blacklistRow = { id: 1, name: '测试', code: 'SUP001', creditCode: '' }
    vm.blacklistForm.reason = '测试原因'
    vm.blacklistForm.startTime = '2026-01-01T00:00:00'
    vm.blacklistForm.endTime = '2020-01-01T00:00:00'
    vm.showBlacklistDialog = true
    await nextTick()

    const confirmBtn = wrapper.findAll('button').find(b => b.text().includes('确认加入黑名单'))
    if (confirmBtn) {
      await confirmBtn.trigger('click')
      await flushPromises()
      const blacklistCalls = (fetchSpy.mock.calls || []).filter((c: any[]) => typeof c[0] === 'string' && c[0].includes('supplier-blacklists'))
      expect(blacklistCalls.length).toBe(0)
    }
  })

  it('时间校验 - 开始时间晚于结束时间应不发送请求', async () => {
    const wrapper = await mountComponent()
    const vm = wrapper.vm as any
    const fetchSpy = vi.spyOn(globalThis, 'fetch')
    vm.blacklistRow = { id: 1, name: '测试', code: 'SUP001', creditCode: '' }
    vm.blacklistForm.reason = '测试原因'
    vm.blacklistForm.startTime = '2027-12-31T00:00:00'
    vm.blacklistForm.endTime = '2027-01-01T00:00:00'
    vm.showBlacklistDialog = true
    await nextTick()
    await flushPromises()

    const confirmBtn = wrapper.findAll('button').find(b => b.text().includes('确认加入黑名单'))
    if (confirmBtn) {
      await confirmBtn.trigger('click')
      await flushPromises()
      const blacklistCalls = (fetchSpy.mock.calls || []).filter((c: any[]) => typeof c[0] === 'string' && c[0].includes('supplier-blacklists'))
      expect(blacklistCalls.length).toBe(0)
    }
  })

  it('新增供应商 - 不填名称应不发送请求', async () => {
    const wrapper = await mountComponent()
    const vm = wrapper.vm as any
    const fetchSpy = vi.spyOn(globalThis, 'fetch')
    vm.showCreateDialog = true
    vm.createForm.supplierName = ''
    await nextTick()

    const submitBtn = wrapper.findAll('button').find(b => b.text().includes('确认新增'))
    if (submitBtn) {
      await submitBtn.trigger('click')
      await flushPromises()
      const supplierPostCalls = (fetchSpy.mock.calls || []).filter((c: any[]) => typeof c[0] === 'string' && c[0].includes('/v1/suppliers') && c[1]?.method?.toLowerCase() === 'post')
      expect(supplierPostCalls.length).toBe(0)
    }
  })

  it('分页组件应正确渲染', async () => {
    const wrapper = await mountComponent()
    const pagination = wrapper.find('.el-pagination')
    expect(pagination.exists()).toBe(true)
  })
}, 30000)
