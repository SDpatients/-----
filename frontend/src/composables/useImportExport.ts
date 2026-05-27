import { ref } from 'vue'
import { importExportApi } from '@/api/importExport'
import type { ImportValidateResult } from '@/types/importExport'

export const useImportExport = () => {
  const validating = ref(false)
  const validateResult = ref<ImportValidateResult>()

  const validate = async (importType: string, file: File) => {
    validating.value = true
    validateResult.value = await importExportApi.validateImport(importType, file)
    validating.value = false
  }

  return { validating, validateResult, validate }
}
