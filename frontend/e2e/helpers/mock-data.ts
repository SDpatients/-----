export const MOCK_INTERNAL_USER = {
  id: 1,
  username: 'admin',
  realName: '系统管理员',
  userType: 1,
  permissions: [
    'dashboard:view', 'supplier:view', 'order:view', 'order:change',
    'asn:view', 'asn:create', 'quality:view', 'settlement:view',
    'finance:view', 'rfq:view', 'quote:view', 'inventory:view',
    'integration:view', 'config:view', 'message:view',
  ],
}

export const MOCK_SUPPLIER_USER = {
  id: 2,
  username: 'supplier1',
  realName: '供应商用户',
  userType: 2,
  supplierId: 1,
  permissions: [
    'supplier:dashboard:view', 'supplier:order:view', 'supplier:delivery:view',
    'supplier:delivery:create', 'supplier:quality:view', 'supplier:settlement:view',
    'supplier:rfq:view', 'supplier:profile:view',
  ],
}

export const MOCK_TOKEN = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcwMDAwMDAwMCwiZXhwIjo5OTk5OTk5OTk5LCJ1c2VyVHlwZSI6ImludGVybmFsIiwidXNlcklkIjoxfQ.mocksignature'

export const MOCK_SUPPLIERS = [
  { id: 1, code: 'SUP10001', name: '华兴电子科技有限公司', category: '电子元件', status: 4, contact: '张三', phone: '13800001111', admissionStage: '已准入', address: '深圳市南山区科技园', creditCode: '91440300MA5EXAMPLE1', blacklisted: false },
  { id: 2, code: 'SUP10002', name: '鼎盛精密制造有限公司', category: '机械加工', status: 4, contact: '李四', phone: '13800002222', admissionStage: '已准入', address: '苏州市工业园区', creditCode: '91320500MA5EXAMPLE2', blacklisted: false },
  { id: 3, code: 'SUP10003', name: '鑫达新材料股份有限公司', category: '化工原料', status: 4, contact: '王五', phone: '13800003333', admissionStage: '已准入', address: '上海市浦东新区', creditCode: '91310115MA5EXAMPLE3', blacklisted: false },
  { id: 4, code: 'SUP10004', name: '恒信物流有限公司', category: '物流服务', status: 0, contact: '赵六', phone: '13800004444', admissionStage: '已注册', address: '广州市天河区', creditCode: '91440106MA5EXAMPLE4', blacklisted: false },
  { id: 5, code: 'SUP10005', name: '鸿远光电科技有限公司', category: '光学器件', status: 5, contact: '孙七', phone: '13800005555', admissionStage: '已驳回', address: '武汉市东湖高新区', creditCode: '91420100MA5EXAMPLE5', blacklisted: true, blacklistReason: '质量严重不达标', blacklistStartTime: '2025-01-15T00:00:00', blacklistEndTime: '' },
  { id: 6, code: 'SUP10006', name: '汇通包装材料有限公司', category: '包装材料', status: 4, contact: '周八', phone: '13800006666', admissionStage: '已准入', address: '杭州市余杭区', creditCode: '91330110MA5EXAMPLE6', blacklisted: false },
  { id: 7, code: 'SUP10007', name: '瑞安半导体有限公司', category: '半导体', status: 6, contact: '吴九', phone: '13800007777', admissionStage: '已停用', address: '成都市高新区', creditCode: '91510100MA5EXAMPLE7', blacklisted: true, blacklistReason: '多次交期延误', blacklistStartTime: '2025-03-01T00:00:00', blacklistEndTime: '2025-12-31T23:59:59' },
  { id: 8, code: 'SUP10008', name: '嘉和五金制品有限公司', category: '五金配件', status: 4, contact: '郑十', phone: '13800008888', admissionStage: '已准入', address: '东莞市松山湖', creditCode: '91441900MA5EXAMPLE8', blacklisted: false },
]

export const MOCK_BLACKLISTS = [
  { id: 1, supplierId: 5, supplierName: '鸿远光电科技有限公司', creditCode: '91440106MA5EXAMPLE5', reason: '质量严重不达标', startTime: '2025-01-15T00:00:00', endTime: '', status: 1, createTime: '2025-01-15T10:30:00' },
  { id: 2, supplierId: 7, supplierName: '瑞安半导体有限公司', creditCode: '91510100MA5EXAMPLE7', reason: '多次交期延误', startTime: '2025-03-01T00:00:00', endTime: '2025-12-31T23:59:59', status: 1, createTime: '2025-03-01T09:00:00' },
  { id: 3, supplierId: 99, supplierName: '已解除测试供应商', creditCode: '91330100MA5EXAMPLE9', reason: '临时拉黑测试', startTime: '2024-06-01T00:00:00', endTime: '2024-12-01T00:00:00', status: 0, createTime: '2024-06-01T08:00:00' },
]

export function wrapApiSuccess<T>(data: T) {
  return { code: 200, message: 'success', data, timestamp: new Date().toISOString() }
}

export function wrapPageResult<T>(records: T[], total: number, pageNum: number, pageSize: number) {
  return {
    code: 200,
    message: 'success',
    data: {
      records,
      total,
      pageNum,
      pageSize,
      pages: Math.ceil(total / pageSize),
    },
    timestamp: new Date().toISOString(),
  }
}

export function filterSuppliers(params: Record<string, unknown>) {
  let result = [...MOCK_SUPPLIERS]
  const keyword = (params.keyword as string || '').toLowerCase()
  const status = params.status as number | undefined
  const includeBlacklisted = params.includeBlacklisted as boolean

  if (!includeBlacklisted) {
    result = result.filter(s => !s.blacklisted)
  }
  if (keyword) {
    result = result.filter(s =>
      s.name.toLowerCase().includes(keyword) ||
      s.code.toLowerCase().includes(keyword) ||
      (s.creditCode && s.creditCode.toLowerCase().includes(keyword))
    )
  }
  if (status !== undefined && status !== null) {
    result = result.filter(s => s.status === status)
  }

  const pageNum = Number(params.pageNum || 1)
  const pageSize = Number(params.pageSize || 10)
  const start = (pageNum - 1) * pageSize
  const pageRecords = result.slice(start, start + pageSize)

  return wrapPageResult(pageRecords, result.length, pageNum, pageSize)
}

export function filterBlacklists(params: Record<string, unknown>) {
  let result = [...MOCK_BLACKLISTS]
  const supplierName = (params.supplierName as string || '').toLowerCase()
  const status = params.status as number | undefined

  if (supplierName) {
    result = result.filter(b => b.supplierName.toLowerCase().includes(supplierName))
  }
  if (status !== undefined && status !== null) {
    result = result.filter(b => b.status === status)
  }

  const pageNum = Number(params.pageNum || 1)
  const pageSize = Number(params.pageSize || 10)
  const start = (pageNum - 1) * pageSize
  const pageRecords = result.slice(start, start + pageSize)

  return wrapPageResult(pageRecords, result.length, pageNum, pageSize)
}
