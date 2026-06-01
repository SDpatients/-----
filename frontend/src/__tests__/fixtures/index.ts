/**
 * 测试数据工厂
 * 提供可复用的 mock 数据，支持 overrides 模式
 */

// ---------- 用户 ----------
export interface UserFixture {
  id: string
  username: string
  nickname: string
  role: string
  token: string
}

export function createUser(overrides?: Partial<UserFixture>): UserFixture {
  return {
    id: '1',
    username: 'admin',
    nickname: '管理员',
    role: 'PURCHASING',
    token: 'mock-token-xxxx',
    ...overrides,
  }
}

// ---------- 订单 ----------
export interface OrderFixture {
  id: string
  orderNo: string
  supplierName: string
  status: string
  createTime: string
  totalAmount: number
}

export function createOrder(overrides?: Partial<OrderFixture>): OrderFixture {
  return {
    id: '1001',
    orderNo: 'PO-2026-001',
    supplierName: '测试供应商',
    status: 'pending',
    createTime: '2026-05-27T00:00:00Z',
    totalAmount: 9999.99,
    ...overrides,
  }
}

export function createOrderList(count = 3): OrderFixture[] {
  return Array.from({ length: count }, (_, i) =>
    createOrder({
      id: String(1001 + i),
      orderNo: `PO-2026-${String(i + 1).padStart(3, '0')}`,
    }),
  )
}

// ---------- 分页响应 ----------
export function createPageResult<T>(records: T[], total?: number) {
  return {
    code: 200,
    message: 'success',
    data: {
      records,
      total: total ?? records.length,
      pageSize: 10,
      pageNum: 1,
      pages: Math.ceil((total ?? records.length) / 10),
    },
  }
}