import type { ApiPage } from './request'

export const asArray = <T>(value: unknown): T[] => {
  if (Array.isArray(value)) return value as T[]
  if (value && typeof value === 'object') {
    const objectValue = value as Record<string, unknown>
    if (Array.isArray(objectValue.records)) return objectValue.records as T[]
    if (Array.isArray(objectValue.list)) return objectValue.list as T[]
    if (Array.isArray(objectValue.rows)) return objectValue.rows as T[]
    if (Array.isArray(objectValue.data)) return objectValue.data as T[]
  }
  return []
}

export const asPage = <T>(value: unknown, pageNum = 1, pageSize = 10): ApiPage<T> => {
  if (value && typeof value === 'object') {
    const objectValue = value as Record<string, unknown>
    const records = asArray<T>(objectValue.records ?? objectValue.list ?? objectValue.rows ?? objectValue.data)
    return {
      records,
      total: Number(objectValue.total ?? records.length),
      pageNum: Number(objectValue.pageNum ?? objectValue.current ?? pageNum),
      pageSize: Number(objectValue.pageSize ?? objectValue.size ?? pageSize),
      pages: Number(objectValue.pages ?? Math.ceil(records.length / pageSize)),
    }
  }
  const records = asArray<T>(value)
  return { records, total: records.length, pageNum, pageSize, pages: Math.ceil(records.length / pageSize) }
}

export const isHtmlResponse = (value: unknown) => typeof value === 'string' && value.trim().toLowerCase().startsWith('<!doctype html')
