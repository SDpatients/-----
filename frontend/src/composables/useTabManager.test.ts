/**
 * composables/useTabManager.ts 测试
 */
import { describe, it, expect, vi } from 'vitest'
import { useTabManager } from '@/composables/useTabManager'

describe('useTabManager', () => {
  const tabs = [
    {
      name: 'tab1',
      label: 'Tab 1',
      defaultQuery: { pageNum: 1, pageSize: 10, keyword: '' },
      fetcher: vi.fn(),
    },
    {
      name: 'tab2',
      label: 'Tab 2',
      defaultQuery: { pageNum: 1, pageSize: 10 },
      fetcher: vi.fn(),
    },
  ]

  it('should initialize with first tab active', () => {
    const { activeTab, tabs: tabInstances } = useTabManager(tabs)
    expect(activeTab.value).toBe('tab1')
    expect(tabInstances).toHaveLength(2)
    expect(tabInstances[0].label).toBe('Tab 1')
  })

  it('should use defaultTab option', () => {
    const { activeTab } = useTabManager(tabs, { defaultTab: 'tab2' })
    expect(activeTab.value).toBe('tab2')
  })

  it('should initialize tab states with default queries', () => {
    const { getState } = useTabManager(tabs)
    const state1 = getState('tab1')
    expect(state1?.records).toEqual([])
    expect(state1?.total).toBe(0)
    expect(state1?.loading).toBe(false)
    expect(state1?.query.keyword).toBe('')
  })

  it('should return undefined for unknown tab', () => {
    const { getState } = useTabManager(tabs)
    expect(getState('nonexistent')).toBeUndefined()
  })

  describe('loadTab', () => {
    it('should set loading and call fetcher', async () => {
      const tabFetcher = vi.fn().mockResolvedValue({
        records: [{ id: 1 }, { id: 2 }],
        total: 2,
      })

      const tabDefs = [
        {
          name: 'test',
          label: 'Test',
          defaultQuery: { pageNum: 1, pageSize: 10 },
          fetcher: tabFetcher,
        },
      ]

      const { loadTab, getState } = useTabManager(tabDefs)
      await loadTab('test')

      expect(tabFetcher).toHaveBeenCalledWith({ pageNum: 1, pageSize: 10 })
      const state = getState('test')
      expect(state?.records).toHaveLength(2)
      expect(state?.total).toBe(2)
      expect(state?.loading).toBe(false)
    })

    it('should do nothing for unknown tab', async () => {
      const { loadTab } = useTabManager(tabs)
      await expect(loadTab('unknown')).resolves.toBeUndefined()
    })

    it('should handle fetcher errors gracefully', async () => {
      const errorFetcher = vi.fn().mockRejectedValue(new Error('API Error'))

      const errorTabs = [
        {
          name: 'errorTab',
          label: 'Error',
          defaultQuery: {},
          fetcher: errorFetcher,
        },
      ]

      const { loadTab, getState } = useTabManager(errorTabs)
      await expect(loadTab('errorTab')).rejects.toThrow('API Error')

      const state = getState('errorTab')
      expect(state?.loading).toBe(false)
    })
  })

  describe('handleTabChange', () => {
    it('should load tab only when empty', async () => {
      const fetcher = vi.fn().mockResolvedValue({
        records: [{ id: 1 }],
        total: 1,
      })

      const tabDefs = [
        {
          name: 'data',
          label: 'Data',
          defaultQuery: {},
          fetcher,
        },
      ]

      const { handleTabChange, getState } = useTabManager(tabDefs)

      // 首次切换 → 应该加载
      await handleTabChange('data')
      expect(fetcher).toHaveBeenCalledTimes(1)

      // 再次切换（已有数据）→ 不加载
      await handleTabChange('data')
      expect(fetcher).toHaveBeenCalledTimes(1)
    })
  })
})