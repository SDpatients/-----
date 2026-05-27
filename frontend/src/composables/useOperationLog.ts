import { ref } from 'vue'
import { operationLogApi } from '@/api/operationLog'
import type { OperationLogItem, OperationLogQuery } from '@/types/operationLog'

export const useOperationLog = () => {
  const loading = ref(false)
  const logs = ref<OperationLogItem[]>([])

  const loadLogs = async (query: OperationLogQuery = {}) => {
    loading.value = true
    logs.value = await operationLogApi.list(query)
    loading.value = false
  }

  return { loading, logs, loadLogs }
}
