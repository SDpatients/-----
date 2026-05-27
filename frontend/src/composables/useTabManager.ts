import { reactive, ref } from 'vue'
import { asPage } from '@/utils/apiNormalize'

export interface TabState {
  records: any[]
  total: number
  loading: boolean
  query: Record<string, unknown>
}

export interface TabDef {
  name: string
  label: string
  defaultQuery: Record<string, unknown>
  fetcher: (params: Record<string, unknown>) => Promise<any>
}

export interface TabInstance {
  name: string
  label: string
  state: TabState
}

interface TabOptions {
  defaultTab?: string
}

/**
 * 多 Tab 页面状态管理器 —— 抽取 PurchasingQualityView / PurchasingFinanceView 的重复模式
 *
 * @example
 * const tabMgr = useTabManager([
 *   { name: 'ncr', label: 'NCR', defaultQuery: { pageNum: 1, pageSize: 10, keyword: '' }, fetcher: p => ncrApi.page(p) },
 *   { name: '8d', label: '8D', defaultQuery: { pageNum: 1, pageSize: 10, keyword: '' }, fetcher: p => eightDApi.page(p) },
 * ])
 */
export function useTabManager(
  tabs: TabDef[],
  options?: TabOptions,
) {
  const activeTab = ref(options?.defaultTab || tabs[0]?.name || '')

  const tabStates = new Map<string, TabState>()
  const typedTabs: TabInstance[] = tabs.map((tab) => {
    const state = reactive<TabState>({
      records: [],
      total: 0,
      loading: false,
      query: reactive({ ...tab.defaultQuery }),
    })
    tabStates.set(tab.name, state)
    return { name: tab.name, label: tab.label, state }
  })

  const getState = (name: string): TabState | undefined => tabStates.get(name)

  const loadTab = async (name: string) => {
    const state = tabStates.get(name)
    if (!state) return
    state.loading = true
    try {
      const spec = tabs.find((t) => t.name === name)
      if (!spec) return
      const result = await spec.fetcher(state.query)
      const page = asPage(result, (state.query as any).pageNum, (state.query as any).pageSize)
      state.records = page.records
      state.total = page.total
      if (state.total === 0) (state.query as any).pageNum = 1
    } finally {
      state.loading = false
    }
  }

  const handleTabChange = (name: string) => {
    const state = tabStates.get(name)
    if (state && state.records.length === 0) {
      loadTab(name)
    }
  }

  return {
    activeTab,
    tabs: typedTabs,
    getState,
    loadTab,
    handleTabChange,
  }
}