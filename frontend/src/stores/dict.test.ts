/**
 * stores/dict.ts 测试
 */
import { describe, it, expect } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useDictStore } from '@/stores/dict'

describe('dict store', () => {
  it('should provide statuses from constants', () => {
    setActivePinia(createPinia())
    const store = useDictStore()
    expect(store.statuses).toBeDefined()
    expect(store.statuses['0'].label).toBe('待审核')
    expect(store.statuses['active'].label).toBe('合作中')
  })

  it('should provide risk map from constants', () => {
    setActivePinia(createPinia())
    const store = useDictStore()
    expect(store.risks).toBeDefined()
    expect(store.risks.low.label).toBe('低风险')
    expect(store.risks.high.label).toBe('高风险')
  })
})