import { http, HttpResponse } from 'msw'

const BASE = '/api'

const suppliers = [
  { id: 1, supplierCode: 'SUP001', supplierName: '华兴电子', supplierType: 1, rating: 4, status: 4, contactName: '张三', contactPhone: '13800001111', creditCode: '91440300MA5HX001A1', address: '深圳市', remark: '', blacklisted: false },
  { id: 2, supplierCode: 'SUP002', supplierName: '恒达科技', supplierType: 2, rating: 3, status: 4, contactName: '李四', contactPhone: '13800002222', creditCode: '91440300MA5HX002B2', address: '广州市', remark: '', blacklisted: false },
  { id: 3, supplierCode: 'SUP003', supplierName: '黑名单供应商A', supplierType: 1, rating: 2, status: 6, contactName: '王五', contactPhone: '13800003333', creditCode: '91440300MA5HX003C3', address: '东莞市', remark: '', blacklisted: true, blacklistReason: '质量多次不合格', blacklistStartTime: '2026-05-01T00:00:00', blacklistEndTime: null },
  { id: 4, supplierCode: 'SUP004', supplierName: '黑名单供应商B', supplierType: 2, rating: 1, status: 6, contactName: '赵六', contactPhone: '13800004444', creditCode: '91440300MA5HX004D4', address: '佛山市', remark: '', blacklisted: true, blacklistReason: '交期严重延误', blacklistStartTime: '2026-04-15T00:00:00', blacklistEndTime: '2026-07-15T00:00:00' },
]

const blacklists = [
  { id: 1, supplierId: 3, supplierName: '黑名单供应商A', creditCode: '91440300MA5HX003C3', reason: '质量多次不合格', startTime: '2026-05-01T00:00:00', endTime: null, status: 1, createTime: '2026-05-01T10:00:00' },
  { id: 2, supplierId: 4, supplierName: '黑名单供应商B', creditCode: '91440300MA5HX004D4', reason: '交期严重延误', startTime: '2026-04-15T00:00:00', endTime: '2026-07-15T00:00:00', status: 1, createTime: '2026-04-15T09:00:00' },
]

export const supplierHandlers = [
  http.get(`${BASE}/v1/suppliers`, ({ request }) => {
    const url = new URL(request.url)
    const keyword = url.searchParams.get('keyword')
    const status = url.searchParams.get('status')
    const includeBlacklisted = url.searchParams.get('includeBlacklisted')

    let filtered = [...suppliers]
    if (keyword) {
      filtered = filtered.filter(s => s.supplierName.includes(keyword) || s.supplierCode.includes(keyword) || (s.creditCode && s.creditCode.includes(keyword)))
    }
    if (status !== null && status !== '') {
      filtered = filtered.filter(s => s.status === Number(status))
    }
    if (includeBlacklisted !== 'true') {
      filtered = filtered.filter(s => !s.blacklisted)
    }

    return HttpResponse.json({
      code: 200,
      message: '操作成功',
      data: { records: filtered, total: filtered.length, pageNum: 1, pageSize: 10, pages: 1 },
    })
  }),

  http.get(`${BASE}/v1/suppliers/:id`, ({ params }) => {
    const s = suppliers.find(s => s.id === Number(params.id))
    if (!s) return HttpResponse.json({ code: 404, message: '未找到' }, { status: 404 })
    return HttpResponse.json({ code: 200, message: '操作成功', data: s })
  }),

  http.post(`${BASE}/v1/suppliers`, async () => {
    return HttpResponse.json({ code: 200, message: '操作成功', data: 5 })
  }),
]

export const blacklistHandlers = [
  http.get(`${BASE}/v1/supplier-blacklists`, ({ request }) => {
    const url = new URL(request.url)
    const statusParam = url.searchParams.get('status')
    const supplierName = url.searchParams.get('supplierName')

    let filtered = [...blacklists]
    if (statusParam !== null && statusParam !== '') {
      filtered = filtered.filter(b => b.status === Number(statusParam))
    }
    if (supplierName) {
      filtered = filtered.filter(b => b.supplierName.includes(supplierName))
    }

    return HttpResponse.json({
      code: 200,
      message: '操作成功',
      data: { records: filtered, total: filtered.length, pageNum: 1, pageSize: 10, pages: 1 },
    })
  }),

  http.post(`${BASE}/v1/supplier-blacklists`, async ({ request }) => {
    const body = await request.json() as Record<string, unknown>
    if (typeof body.startTime === 'string' && !body.startTime.includes('T')) {
      return HttpResponse.json(
        { code: 40001, message: '请求参数类型错误: Cannot deserialize value of type `java.time.LocalDateTime`' },
        { status: 400 },
      )
    }
    return HttpResponse.json({ code: 200, message: '操作成功', data: blacklists.length + 1 })
  }),

  http.put(`${BASE}/v1/supplier-blacklists/:id`, async () => {
    return HttpResponse.json({ code: 200, message: '操作成功', data: null })
  }),

  http.post(`${BASE}/v1/supplier-blacklists/:id/remove`, async () => {
    return HttpResponse.json({ code: 200, message: '操作成功', data: null })
  }),

  http.get(`${BASE}/v1/supplier-blacklists/check`, () => {
    return HttpResponse.json({ code: 200, message: '操作成功', data: false })
  }),
]
