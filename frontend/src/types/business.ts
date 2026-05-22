export type UserType = 'internal' | 'supplier'

export interface UserInfo {
  id: number
  username: string
  realName: string
  roleName: string
  department: string
  userType: UserType
  avatarColor: string
}

export interface PageQuery {
  pageNum: number
  pageSize: number
  keyword?: string
  status?: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
}

export interface Supplier {
  id: number
  code: string
  name: string
  category: string
  level: string
  status: string
  contact: string
  phone: string
  admissionStage: string
  performanceScore: number
  riskLevel: string
  address: string
}

export interface PurchaseOrder {
  id: number
  orderNo: string
  supplierName: string
  buyer: string
  amount: number
  deliveryDate: string
  status: string
  confirmStatus: string
  riskLevel: string
}

export interface AsnNotice {
  id: number
  asnNo: string
  orderNo: string
  supplierName: string
  shipDate: string
  eta: string
  status: string
  quantity: number
  warehouse: string
}

export interface QualityCase {
  id: number
  caseNo: string
  type: string
  supplierName: string
  severity: string
  status: string
  owner: string
  createdAt: string
  description: string
}

export interface Settlement {
  id: number
  statementNo: string
  supplierName: string
  period: string
  amount: number
  diffAmount: number
  status: string
  invoiceStatus: string
  paymentStatus: string
}

export interface TimelineItem {
  title: string
  content: string
  time: string
}
