import { defineStore } from 'pinia'
import { riskMap, statusMap } from '@/constants/status'
import { dictApi, type DictItem } from '@/api/dict'

export const useDictStore = defineStore('dict', {
  state: () => ({
    statuses: statusMap,
    risks: riskMap,
    dictCache: {} as Record<string, DictItem[]>,
    dictLoading: {} as Record<string, boolean>,
  }),
  actions: {
    async loadDict(dictCode: string) {
      if (this.dictCache[dictCode] || this.dictLoading[dictCode]) return
      this.dictLoading[dictCode] = true
      try {
        this.dictCache[dictCode] = await dictApi.getItemsByCode(dictCode)
      } catch {
        this.dictCache[dictCode] = []
      } finally {
        this.dictLoading[dictCode] = false
      }
    },
    getItems(dictCode: string): DictItem[] {
      return this.dictCache[dictCode] || []
    },
    getLabel(dictCode: string, value: string | number | undefined): string {
      if (value === undefined || value === null || value === '') return ''
      const item = this.dictCache[dictCode]?.find(d => d.itemValue === String(value))
      return item?.itemLabel || String(value)
    },
  },
})
