export type BusinessCategory = 'order' | 'quality' | 'finance' | 'system'

export interface NotificationMessage {
  id: number | string
  title: string
  content: string
  level: string
  read: boolean
  createdAt: string
  businessType: BusinessCategory
  businessId: number | string
  targetPath: string
}

export interface UnreadSummary {
  messageCount: number
  todoCount: number
}

export interface TodoTargetRoute {
  path: string
  query?: Record<string, string>
}

/** 将后端 businessType 映射到前端分类 */
export function mapBusinessCategory(raw: string): BusinessCategory {
  const t = raw?.toLowerCase() || ''
  if (t.includes('order') || t.includes('purchase')) return 'order'
  if (t.includes('quality') || t.includes('ncr') || t.includes('8d')) return 'quality'
  if (t.includes('finance') || t.includes('invoice') || t.includes('payment') || t.includes('settlement')) return 'finance'
  return 'system'
}

/** businessType → 单据路由映射 */
export function resolveTargetRoute(businessType: string, businessId: number | string): string {
  const t = businessType?.toLowerCase() || ''
  if (t.includes('order') || t.includes('purchase')) return `/purchasing/orders/${businessId}`
  if (t.includes('quality') || t.includes('ncr')) return `/purchasing/quality/${businessId}`
  if (t.includes('8d')) return `/purchasing/quality-ext?tab=8d&id=${businessId}`
  if (t.includes('settlement')) return `/purchasing/settlements/${businessId}`
  if (t.includes('invoice') || t.includes('payment') || t.includes('deduction')) return `/purchasing/finance`
  if (t.includes('rfq') || t.includes('quote')) return `/purchasing/rfq/${businessId}`
  if (t.includes('asn') || t.includes('delivery')) return `/purchasing/asn/${businessId}`
  if (t.includes('supplier')) return `/purchasing/suppliers/${businessId}`
  return `/messages?businessType=${businessType}&businessId=${businessId}`
}
