import { defineStore } from 'pinia'
import { riskMap, statusMap } from '@/constants/status'

export const useDictStore = defineStore('dict', {
  state: () => ({
    statuses: statusMap,
    risks: riskMap,
  }),
})
